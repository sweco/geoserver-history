package org.geoserver.wms;

import java.io.Serializable;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogFactory;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.Wrapper;
import org.geoserver.config.GeoServer;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.util.LegacyServiceLoader;
import org.geoserver.config.util.LegacyServicesReader;
import org.geoserver.wms.WatermarkInfo.Position;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class WMSLoader extends LegacyServiceLoader {

    static Logger LOGGER = Logging.getLogger( "org.geoserver.wms" );
    
    public String getServiceId() {
        return "wms";
    }
    
    public ServiceInfo load(LegacyServicesReader reader, GeoServer geoServer)
            throws Exception {
        WMSInfoImpl wms = new WMSInfoImpl();
        wms.setId( "wms" );
        
        Map<String,Object> props = reader.wms();
        readCommon( wms, props, geoServer );
        
        WatermarkInfo wm = new WatermarkInfoImpl();
        wm.setEnabled( (Boolean) props.get( "globalWatermarking" ) );
        wm.setURL( (String) props.get("globalWatermarkingURL" ) );
        wm.setTransparency( (Integer) props.get("globalWatermarkingTransparency") );
        wm.setPosition( Position.get( (Integer) props.get( "globalWatermarkingPosition" ) ) );
        wms.setWatermark( wm );
            
        wms.setInterpolation( (String) props.get( "allowInterpolation" ) );
        wms.getMetadata().put( "svgRenderer", (Serializable) props.get( "svgRenderer") );
        wms.getMetadata().put( "svgAntiAlias",(Serializable) props.get( "svgAntiAlias") );
        
        // CRS list for getCapabilities 
        String crsListStr = (String) props.get("capabilitiesCrsList");
        if(crsListStr != null) {
            String[] crsArray = crsListStr.split(", ");
            for(int i=0; i< crsArray.length; i++) {
                // Check that this CRS exists
                try {
                    CoordinateReferenceSystem tmp = CRS.decode(crsArray[i]);
                    wms.getSRS().add(crsArray[i]);
                } catch(NoSuchAuthorityCodeException nsae) {
                    LOGGER.warning("Unknown CRS " + crsArray[i] + " in getCapabilities CRS list");
                }
            }
        }
        
        // max GetFeatureInfo search radius
        wms.setMaxBuffer((Integer) props.get("maxBuffer"));
        
        // max memory usage
        wms.setMaxRequestMemory((Integer) props.get("maxRequestMemory"));
        
        // the max rendering time
        wms.setMaxRenderingTime((Integer) props.get("maxRenderingTime"));
        
        // the max number of rendering errors
        wms.setMaxRenderingErrors((Integer) props.get("maxRenderingErrors"));
        
        // base maps
        Catalog catalog = geoServer.getCatalog();
        // ... we need access to the actual catalog, not a filtered out view of the
        // layers accessible to the current user
        if(catalog instanceof Wrapper)
            catalog = ((Wrapper) catalog).unwrap(Catalog.class);
        CatalogFactory factory = catalog.getFactory();
        
        List<Map> baseMaps = (List<Map>) props.get( "BaseMapGroups");
        if ( baseMaps != null ) {
         O:  for ( Map baseMap : baseMaps ) {
                LayerGroupInfo bm = factory.createLayerGroup();
                bm.setName( (String) baseMap.get( "baseMapTitle" ) );
                
                //process base map layers
                List<String> layerNames = (List) baseMap.get( "baseMapLayers");
                for ( String layerName : layerNames ) {
                    ResourceInfo resource = null;
                    if ( layerName.contains( ":" ) ) {
                        String[] qname = layerName.split( ":" );
                        resource = catalog.getResourceByName( qname[0],qname[1], ResourceInfo.class );
                    }
                    else {
                        resource = catalog.getResourceByName( layerName, ResourceInfo.class );
                    }
                   
                    if ( resource == null ) {
                        LOGGER.warning("Ignoring layer group '" + bm.getName() + 
                            "', resource '"+ layerName + "' does not exist" );
                        continue O;
                    }
                
                    List<LayerInfo> layers = catalog.getLayers(resource);
                    if ( layers.isEmpty() ) {
                        LOGGER.warning( "Ignoring layer group '" + bm.getName() + 
                            "', no layer found for resource '" + layerName + "'");
                        continue O;
                    }
                    
                    bm.getLayers().add( layers.get( 0 ) );
                }
                
                //process base map styles
                List<String> styleNames = (List) baseMap.get( "baseMapStyles" );
                if ( styleNames.isEmpty() ) {
                    //use defaults
                    for ( LayerInfo l : bm.getLayers() ) {
                        bm.getStyles().add( l.getDefaultStyle() );
                    }
                }
                else {
                    for ( int i = 0; i < styleNames.size(); i++ ) {
                        String styleName = styleNames.get( i );
                        styleName = styleName.trim();
                        
                        StyleInfo style = null;
                        if ( "".equals( styleName ) ) {
                            style = bm.getLayers().get(i).getDefaultStyle();
                        }
                        else {
                            style = catalog.getStyleByName( styleName );    
                        }
                        
                        if ( style == null ) {
                            LOGGER.warning( "Ignoring layer group '" + bm.getName() + 
                                    "', style '" + styleName + "' does not exist.");
                            continue O;
                        }
                        bm.getStyles().add( style );
                    }    
                }
                bm.getMetadata().put( "rawStyleList", (String)baseMap.get("rawBaseMapStyles"));
                
                //base map enveloper
                ReferencedEnvelope e = (ReferencedEnvelope) baseMap.get( "baseMapEnvelope");
                if ( e == null ) {
                    e = new ReferencedEnvelope();
                    e.setToNull();
                }
                bm.setBounds( e );
                
                LOGGER.info( "Processed layer group '" + bm.getName() + "'" );
                catalog.add( bm );
            }
        }
        
        return wms;
    }

}
