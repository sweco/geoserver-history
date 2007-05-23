package org.vfny.geoserver.wms.responses.map.kml;

import java.util.Map;

import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.xml.transform.TransformerBase;
import org.geotools.xml.transform.Translator;
import org.vfny.geoserver.wms.WMSMapContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Encodes the legend for a map layer as a kml ScreenOverlay.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class KMLLegendTransformer extends TransformerBase {

    WMSMapContext mapContext;
    
    public KMLLegendTransformer( WMSMapContext mapContext ) {
        this.mapContext = mapContext;
        setNamespaceDeclarationEnabled(false);
    }
    
    public Translator createTranslator(ContentHandler handler) {
        return new KMLLegendTranslator( handler );
    }

    class KMLLegendTranslator extends TranslatorSupport {

        public KMLLegendTranslator(ContentHandler contentHandler) {
            super(contentHandler, null, null);
        }
        
        /**
         * Encodes a KML ScreenOverlay wihch depicts the legend of a map.
         */
        public void encode(Object o) throws IllegalArgumentException {
            MapLayer mapLayer = (MapLayer) o;
            
            start( "ScreenOverlay" );
            element( "name", "Legend" );
            
            element( "overlayXY", null, KMLUtils.attributes( new String[] { "x","0","y","0","xunits","pixels","yunits","pixels"} ) );
            element( "screenXY", null, KMLUtils.attributes( new String[] { "x","10","y","20","xunits","pixels","yunits","pixels"} ) );
         
            start( "Icon" );
            
            //reference the image as a remote wms call
            Map getLegendGraphic = KMLUtils.createGetLegendGraphicRequest(mapContext, mapLayer );
            element( "href", KMLUtils.encode( mapContext, getLegendGraphic ) );   
            
            end( "Icon" );
            
            end( "ScreenOverlay" );
        }
    }
}
