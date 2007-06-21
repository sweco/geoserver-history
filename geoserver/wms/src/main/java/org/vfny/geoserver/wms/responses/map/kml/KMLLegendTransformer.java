/* Copyright (c) 2001, 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses.map.kml;

import org.geotools.map.MapLayer;
import org.geotools.xml.transform.TransformerBase;
import org.geotools.xml.transform.Translator;
import org.vfny.geoserver.wms.WMSMapContext;
import org.xml.sax.ContentHandler;


/**
 * Encodes the legend for a map layer as a kml ScreenOverlay.
 *
 * @author Simone Giannecchini - GeoSolutions
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class KMLLegendTransformer extends KMLTransformerBase {
    WMSMapContext mapContext;

    public KMLLegendTransformer(WMSMapContext mapContext) {
        this.mapContext = mapContext;
        setNamespaceDeclarationEnabled(false);
    }

    public Translator createTranslator(ContentHandler handler) {
        return new KMLLegendTranslator(handler);
    }

    class KMLLegendTranslator extends KMLTranslatorSupport {
        public KMLLegendTranslator(ContentHandler contentHandler) {
            super(contentHandler);
        }

        /**
         * Encodes a KML ScreenOverlay wihch depicts the legend of a map.
         */
        public void encode(Object o) throws IllegalArgumentException {
            MapLayer mapLayer = (MapLayer) o;

            if (isStandAlone()) {
            	start( "kml" );
            }
            
            start("ScreenOverlay");
            element("name", "Legend");

            element("overlayXY", null,
                KMLUtils.attributes(
                    new String[] { "x", "0", "y", "0", "xunits", "pixels", "yunits", "pixels" }));
            element("screenXY", null,
                KMLUtils.attributes(
                    new String[] { "x", "10", "y", "20", "xunits", "pixels", "yunits", "pixels" }));

            start("Icon");

            // reference the image as a remote wms call
            element("href", KMLUtils.getLegendGraphicUrl(mapContext, mapLayer, null));

            end("Icon");

            end("ScreenOverlay");
            
            if (isStandAlone()) {
            	end( "kml" );
            }
        }
    }
}
