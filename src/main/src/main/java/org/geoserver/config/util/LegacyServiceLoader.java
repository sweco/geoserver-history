package org.geoserver.config.util;

import java.util.List;
import java.util.Map;

import org.geoserver.catalog.MetadataLinkInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.ServiceLoader;

/**
 * Base class for service loaders loading from the legacy service.xml file.
 * <p>
 * 
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public abstract class LegacyServiceLoader implements ServiceLoader {

    /**
     * reader pointing to services.xml
     */
    LegacyServicesReader reader;
    
    /**
     * Sets the legacy services.xml reader.
     * <p>
     * This method is called by the GeoServer startup, it should not be called
     * by client code.
     * </p>
     */
    public void setReader( LegacyServicesReader reader ) {
        this.reader = reader;
    }
    
    /**
     * Loads the service.
     * <p>
     * This method calls through to {@link #load(LegacyServicesReader, GeoServer)} 
     * </p>
     */
    public final ServiceInfo load(GeoServer gs) throws Exception {
        return load( reader, gs );
    }
    
    /**
     * Creates the service configuration object.
     * <p>
     * Subclasses implementing this method can use the {@link #readCommon(ServiceInfo, Map, GeoServer)} 
     * method to read those attributes common to all services.
     * </p>
     * 
     * @param reader The services.xml reader.
     * 
     */
    abstract public ServiceInfo load( LegacyServicesReader reader, GeoServer geoServer ) throws Exception;
    
    /**
     * Reads all the common attributes from the service info class.
     * <p>
     * This method is intended to be called by subclasses after creating an 
     * instance of ServiceInfo. Example:
     * <pre>
     *   // read properties
     *   Map<String,Object> props = reader.wfs();
     *   
     *   // create config object
     *   WFSInfo wfs = new WFSInfoImpl();
     *   
     *   //load common properties
     *   load( wfs, reader );
     * 
     *   //load wfs specific properties
     *   wfs.setServiceLevel( map.get( "serviceLevel") );
     *   ...
     * </pre>
     * </p>
     */
    protected void readCommon( ServiceInfo service, Map<String,Object> properties, GeoServer gs) 
        throws Exception {
     
        service.setEnabled( (Boolean) properties.get( "enabled") );
        service.setName( (String) properties.get( "name") );
        service.setTitle( (String) properties.get( "title") );
        service.setAbstract( (String) properties.get( "abstract") );
        
        Map metadataLink = (Map) properties.get("metadataLink");
        if ( metadataLink != null ) {
            MetadataLinkInfo ml = gs.getCatalog().getFactory().createMetadataLink();
            ml.setAbout( (String) metadataLink.get( "about" ) );
            ml.setMetadataType( (String) metadataLink.get( "metadataType" ) );
            ml.setType( (String) metadataLink.get( "type" ) );
            service.setMetadataLink( ml );
        }
        
        List keywords = (List) properties.get( "keywords" );
        if ( keywords != null ) {
            service.getKeywords().addAll( keywords );
        }
        
        service.setOnlineResource( (String) properties.get( "onlineResource" ) );
        service.setFees( (String) properties.get( "fees" ) );
        service.setAccessConstraints( (String) properties.get( "accessConstraints" ) );
        service.setCiteCompliant((Boolean)properties.get( "citeConformanceHacks"));
        service.setMaintainer((String)properties.get( "maintainer" ) );
        service.setSchemaBaseURL((String)properties.get("SchemaBaseUrl"));
    }
    
    public void save(ServiceInfo service, GeoServer gs) throws Exception {
        //do nothing, saving implemented elsewhere
    }
    
}