/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.kml;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import junit.framework.Test;

import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.geoserver.data.test.MockData;
import org.geoserver.ows.kvp.FormatOptionsKvpParser;
import org.geoserver.wms.WMSTestSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Some functional tests for kml reflector
 * 
 * @author David Winslow (OpenGeo)
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class KMLReflectorTest extends WMSTestSupport {

    /**
     * This is a READ ONLY TEST so we can use one time setup
     */
    public static Test suite() {
        return new OneTimeTestSetup(new KMLReflectorTest());
    }

    /**
     * Verify that NetworkLink's generated by the reflector do not include a BBOX parameter,
     * since that would override the BBOX provided by Google Earth.  
     * @see http://jira.codehaus.org/browse/GEOS-2185
     */
    public void testNoBBOXInHREF() throws Exception {
        final String layerName = MockData.BASIC_POLYGONS.getPrefix() + ":" +
            MockData.BASIC_POLYGONS.getLocalPart();
        final XpathEngine xpath = XMLUnit.newXpathEngine();
        String requestURL = "wms/kml?mode=refresh&layers=" + layerName;
        Document dom = getAsDOM(requestURL);
         print(dom);
        assertXpathEvaluatesTo("1", "count(kml/Folder)", dom);
        assertXpathEvaluatesTo("1", "count(kml/Folder/NetworkLink)", dom);
        assertXpathEvaluatesTo("1", "count(kml/Folder/LookAt)", dom);

        assertXpathEvaluatesTo(layerName, "kml/Folder/NetworkLink[1]/name", dom);
        assertXpathEvaluatesTo("1", "kml/Folder/NetworkLink[1]/open", dom);
        assertXpathEvaluatesTo("1", "kml/Folder/NetworkLink[1]/visibility", dom);
        
        assertXpathEvaluatesTo("onStop", "kml/Folder/NetworkLink[1]/Url/viewRefreshMode", dom);
        assertXpathEvaluatesTo("1", "kml/Folder/NetworkLink[1]/Url/viewRefreshTime", dom);
        Map<String, String> expectedKVP = toKvp("http://localhost:80/geoserver/wms?format_options=KMPLACEMARK%3Afalse%3BKMATTR%3Atrue%3BKMSCORE%3A40%3BSUPEROVERLAY%3Afalse%3B&service=wms&srs=EPSG%3A4326&width=1024&styles=BasicPolygons&height=1024&transparent=false&request=GetMap&layers=cite%3ABasicPolygons&format=application%2Fvnd.google-earth.kmz&version=1.1.1");
        Map<String, String> resultedKVP = 
           toKvp(xpath.evaluate("kml/Folder/NetworkLink[1]/Url/href", dom));

        assertMapsEqual(expectedKVP, resultedKVP);

        String href = xpath.evaluate("kml/Folder/NetworkLink/Link/href", dom);
        Pattern badPattern = Pattern.compile("&bbox=", Pattern.CASE_INSENSITIVE);
        assertFalse(badPattern.matcher(href).matches());
    }

    /**
     * Do some spot checks on the KML generated when an overlay hierarchy is requested.
     */
    public void testSuperOverlayReflection() throws Exception {
        final String layerName = MockData.BASIC_POLYGONS.getPrefix() + ":"
               + MockData.BASIC_POLYGONS.getLocalPart();

        final String requestUrl = "wms/kml?layers=" + layerName + "&styles=&mode=superoverlay";
        Document dom = getAsDOM(requestUrl);
        // print(dom);
        assertEquals("kml", dom.getDocumentElement().getLocalName());
        assertXpathExists("kml/Folder/NetworkLink/Link/href", dom);
        assertXpathExists("kml/Folder/LookAt/longitude", dom);
    }
    
    public void testWmsRepeatedLayerWithNonStandardStyleAndCqlFiler() throws Exception {
        final String layerName = MockData.BASIC_POLYGONS.getPrefix() + ":"
                + MockData.BASIC_POLYGONS.getLocalPart();

        String requestUrl = "wms/kml?mode=refresh&layers=" + layerName + "," + layerName
                + "&styles=Default,Default&cql_filter=att1<10;att1>1000";
        Document dom = getAsDOM(requestUrl);

        assertEquals("kml", dom.getDocumentElement().getLocalName());

        NodeList folders = dom.getDocumentElement().getElementsByTagName("Folder");
        assertEquals(1, folders.getLength());
        Element folder = (Element) folders.item(0);

        NodeList netLinks = folder.getElementsByTagName("NetworkLink");
        assertEquals(2, netLinks.getLength());

        assertXpathEvaluatesTo(layerName, "kml/Folder/NetworkLink[1]/name", dom);
        assertXpathEvaluatesTo(layerName, "kml/Folder/NetworkLink[2]/name", dom);

        XPath xpath = XPathFactory.newInstance().newXPath();

        String url1 = xpath.compile("/kml/Folder/NetworkLink[1]/Url/href").evaluate(dom);
        String url2 = xpath.compile("/kml/Folder/NetworkLink[2]/Url/href").evaluate(dom);

        assertNotNull(url1);
        assertNotNull(url2);

        Map<String, String> kvp1 = toKvp(url1);
        Map<String, String> kvp2 = toKvp(url2);

        assertEquals(layerName, kvp1.get("LAYERS"));
        assertEquals(layerName, kvp2.get("LAYERS"));

        assertEquals("Default", kvp1.get("STYLES"));
        assertEquals("Default", kvp2.get("STYLES"));

        assertEquals("att1<10", kvp1.get("CQL_FILTER"));
        assertEquals("att1>1000", kvp2.get("CQL_FILTER"));
    }

    /**
     * Creates a key/value pair map from the cgi parameters in the provided url
     * 
     * @param url
     *            an url where all the cgi parameter values are url encoded
     * @return a map with the key value pairs from the url with all the
     *         parameter names in upper case
     */
    static Map<String, String> toKvp(String url) {
        if (url.indexOf('?') > 0) {
            url = url.substring(url.indexOf('?') + 1);
        }
        Map<String, String> kvpMap = new HashMap<String, String>();

        String[] tuples = url.split("&");
        for (String tuple : tuples) {
            String[] kvp = tuple.split("=");
            String key = kvp[0].toUpperCase();
            String value = kvp.length > 1 ? kvp[1] : null;
            if (value != null) {
                try {
                    value = URLDecoder.decode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            kvpMap.put(key, value);
        }

        return kvpMap;
    }

    static void assertMapsEqual(Map<String, String> expected, Map<String, String> actual) 
        throws Exception {
        for (Map.Entry<String, String> entry : expected.entrySet()){
            if (entry.getKey().equalsIgnoreCase("format_options")){
                FormatOptionsKvpParser parser = new FormatOptionsKvpParser();
                Map expectedFormatOptions = (Map) parser.parse(entry.getValue());
                Map actualFormatOptions = (Map) parser.parse(actual.get(entry.getKey()));

                for (Object o : expectedFormatOptions.entrySet()){
                    Map.Entry formatOption = (Map.Entry) o;
                    assertEquals(
                            formatOption.getValue(), 
                            actualFormatOptions.get(formatOption.getKey())
                        );
                }

                for (Object key : actualFormatOptions.keySet()){
                    assertTrue("found unexpected key " + key + " in format options", expectedFormatOptions.containsKey(key));
                }

                // special treatment for the format options
            } else {
                assertEquals(entry.getValue(), actual.get(entry.getKey()));
            }
        }

        for (String key : actual.keySet()){
            assertTrue(expected.containsKey(key));
        }
    }
}
