/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wms.xml;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.geoserver.ows.XmlRequestReader;
import org.geoserver.ows.xml.v1_0.OWS;
import org.geoserver.wms.request.GetCapabilitiesRequest;
import org.vfny.geoserver.wms.WmsException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Reads a WMS GetCapabilities request from an XML stream
 * 
 * @author Gabriel Roldan
 * @version $Id$
 */
public class CapabilitiesXmlReader extends XmlRequestReader {

    /**
     * Creates the new reader.
     * 
     * @param wms
     *            The WMS service config.
     */
    public CapabilitiesXmlReader() {
        super(OWS.GETCAPABILITIES, null, "WMS");
    }

    /**
     * @param request
     * @see org.geoserver.ows.XmlRequestReader#read(java.lang.Object, java.io.Reader, java.util.Map)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object read(Object request, Reader reader, Map kvp) throws Exception {
        // instantiante parsers and content handlers
        GetCapabilitiesRequest req = new GetCapabilitiesRequest();
        CapabilitiesHandler currentRequest = new CapabilitiesHandler(req);

        // read in XML file and parse to content handler
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            ParserAdapter adapter = new ParserAdapter(parser.getParser());
            adapter.setContentHandler(currentRequest);
            adapter.parse(new InputSource(reader));
        } catch (SAXException e) {
            throw new WmsException(e, "XML capabilities request parsing error", getClass()
                    .getName());
        } catch (IOException e) {
            throw new WmsException(e, "XML capabilities request input error", getClass().getName());
        } catch (ParserConfigurationException e) {
            throw new WmsException(e, "Some sort of issue creating parser", getClass().getName());
        }

        return req;
    }

    /**
     * A SAX content handler that acquires a GetCapabilities request from an incoming XML stream.
     * 
     * @author Rob Hranac, TOPP
     * @version $Id$
     */
    private static class CapabilitiesHandler extends XMLFilterImpl implements ContentHandler {
        /** Class logger */
        private static Logger LOGGER = org.geotools.util.logging.Logging
                .getLogger("org.geoserver.wms.xml.CapabilitiesHandler");

        /** Internal Capabilities request for construction. */
        private GetCapabilitiesRequest request = null;

        /**
         * Creates a new CapabilitiesHandler
         * 
         * @param service
         *            this is the AbstractService Handling the Request
         * @param req
         */
        public CapabilitiesHandler(GetCapabilitiesRequest request) {
            this.request = request;
        }

        /* ***********************************************************************
         * Standard SAX content handler methods *
         * **********************************************************************
         */

        /**
         * Notes the start of the element and sets version and service tags, as required.
         * 
         * @param namespaceURI
         *            URI for namespace appended to element.
         * @param localName
         *            Local name of element.
         * @param rawName
         *            Raw name of element.
         * @param atts
         *            Element attributes.
         * 
         * @throws SAXException
         *             For any standard SAX errors.
         */
        public void startElement(String namespaceURI, String localName, String rawName,
                Attributes atts) throws SAXException {
            if (localName.equals("GetCapabilities")) {
                LOGGER.finer("found capabilities start.");

                for (int i = 0, n = atts.getLength(); i < n; i++) {
                    if (atts.getLocalName(i).equals("version")) {
                        request.setVersion(atts.getValue(i));
                    } else if (atts.getLocalName(i).equals("service")) {
                        //ok WMS is implicit
                    } else if (atts.getLocalName(i).equals("updateSequence")) {
                        request.setUpdateSequence(atts.getValue(i));
                    }
                }
            }
        }
    }

}