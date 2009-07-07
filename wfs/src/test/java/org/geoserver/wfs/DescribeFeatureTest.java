package org.geoserver.wfs;

import junit.framework.Test;

import org.geoserver.data.test.MockData;
import org.w3c.dom.Document;

public class DescribeFeatureTest extends WFSTestSupport {
    
    /**
     * This is a READ ONLY TEST so we can use one time setup
     */
    public static Test suite() {
        return new OneTimeTestSetup(new DescribeFeatureTest());
    }
    
    @Override
    protected void populateDataDirectory(MockData dataDirectory) throws Exception {
        super.populateDataDirectory(dataDirectory);
        dataDirectory.disableDataStore(MockData.CITE_PREFIX);
    }

    public void testGet() throws Exception {
        Document doc = getAsDOM("wfs?service=WFS&request=DescribeFeatureType&version=1.0.0");
        assertEquals("xsd:schema", doc.getDocumentElement().getNodeName());
    }

    public void testPost() throws Exception {
        String xml = "<wfs:DescribeFeatureType " + "service=\"WFS\" "
                + "version=\"1.0.0\" "
                + "xmlns:wfs=\"http://www.opengis.net/wfs\" />";

        Document doc = postAsDOM("wfs", xml);
        assertEquals("xsd:schema", doc.getDocumentElement().getNodeName());
    }

    public void testPostDummyFeature() throws Exception {

        String xml = "<wfs:DescribeFeatureType " + "service=\"WFS\" "
                + "version=\"1.0.0\" "
                + "xmlns:wfs=\"http://www.opengis.net/wfs\" >"
                + " <wfs:TypeName>cgf:DummyFeature</wfs:TypeName>"
                + "</wfs:DescribeFeatureType>";

        Document doc = postAsDOM("wfs", xml);
        assertEquals("ServiceExceptionReport", doc.getDocumentElement()
                .getNodeName());

    }
    
    public void testWithoutExplicitMapping() throws Exception {
        String xml = "<DescribeFeatureType xmlns='http://www.opengis.net/wfs'"+
           " xmlns:gml='http://www.opengis.net/gml'"+
           " xmlns:ogc='http://www.opengis.net/ogc' version='1.0.0' service='WFS'>"+
           " <TypeName>cdf:Locks</TypeName>"+ 
           " </DescribeFeatureType>";

        Document doc = postAsDOM("wfs", xml);
        assertEquals("xsd:schema", doc.getDocumentElement().getNodeName());
        
        assertEquals( 1, doc.getElementsByTagName("xsd:complexType").getLength());
    }
}