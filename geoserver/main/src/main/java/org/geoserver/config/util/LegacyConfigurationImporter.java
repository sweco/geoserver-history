package org.geoserver.config.util;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.config.ContactInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerFactory;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.ServiceLoader;
import org.geoserver.jai.JAIInfo;
import org.geoserver.platform.GeoServerExtensions;
import org.geotools.util.logging.Logging;

/**
 * Imports configuration from a legacy "services.xml" file into a geoserver
 * configuration instance.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * 
 */
public class LegacyConfigurationImporter extends LegacyImporterSupport {

    /** logger */
    static Logger LOGGER = Logging.getLogger( "org.geoserver.confg" );

    /**
     * configuration
     */
    GeoServer geoServer;

    /**
     * Creates the importer.
     * 
     * @param geoServer
     *                The configuration to import into.
     */
    public LegacyConfigurationImporter(GeoServer geoServer) {
        this.geoServer = geoServer;
    }

    /**
     * No argument constructor.
     * <p>
     * Calling code should use {@link #setConfiguration(GeoServer)} when using
     * this constructor.
     * </p>
     * 
     */
    public LegacyConfigurationImporter() {

    }

    /**
     * Sets teh configuration to import into.
     */
    public void setConfiguration(GeoServer geoServer) {
        this.geoServer = geoServer;
    }

    /**
     * The configuration being imported into.
     */
    public GeoServer getConfiguration() {
        return geoServer;
    }

    /**
     * Imports configuration from a geoserver data directory into the
     * configuration.
     * 
     * @param dir
     *                The root of the data directory.
     * 
     */
    public void imprt(File dir) throws Exception {

        LegacyServicesReader reader = reader( dir );
        
        //TODO: this routine needs to be safer about accessing paramerters, 
        // wrapping in null checks
        
        GeoServerFactory factory = geoServer.getFactory();

        //
        //global
        //
        GeoServerInfo info = geoServer.getGlobal();
        if ( info == null ) {
            info = factory.createGlobal();
            geoServer.setGlobal( info );
        }
            
        Map<String,Object> global = reader.global(); 

        info.setMaxFeatures( get( global, "maxFeatures", Integer.class ) );
        info.setVerbose( get( global, "verbose", Boolean.class ) );
        info.setVerboseExceptions( get( global, "verboseExceptions", Boolean.class ) );
        info.setNumDecimals( get( global, "numDecimals", Integer.class ) );
        info.setCharset( (String) global.get( "charSet" ) );
        info.setUpdateSequence( get( global, "updateSequence", Integer.class ) );
        info.setOnlineResource( get( global, "onlineResource", String.class ) );
        info.setProxyBaseUrl( get( global, "ProxyBaseUrl", String.class ) );
        
        //contact
        Map<String,Object> contact = reader.contact();
        ContactInfo contactInfo = factory.createContact();
       
        contactInfo.setContactPerson( (String) contact.get( "ContactPerson") );
        contactInfo.setContactOrganization( (String) contact.get( "ContactOrganization") );
        contactInfo.setContactVoice( (String) contact.get( "ContactVoiceTelephone" ) );
        contactInfo.setContactFacsimile( (String) contact.get( "ContactFacsimileTelephone" ) );
        contactInfo.setContactPosition( (String) contact.get( "ContactPosition" ) );
        contactInfo.setContactEmail( (String) contact.get( "ContactElectronicMailAddress" ) );
        
        contactInfo.setAddress( (String) contact.get( "Address") );
        contactInfo.setAddressType( (String) contact.get( "AddressType") );
        contactInfo.setAddressCity( (String) contact.get( "City") );
        contactInfo.setAddressCountry( (String) contact.get( "Country") );
        contactInfo.setAddressState( (String) contact.get( "StateOrProvince") );
        contactInfo.setAddressPostalCode( (String) contact.get( "PostCode") );
        info.setContactInfo( contactInfo );
        
        //jai
        JAIInfo jai = new JAIInfo();
        jai.setMemoryCapacity( (Double) value( global.get( "JaiMemoryCapacity"), 0.5 ) );
        jai.setMemoryThreshold( (Double) value( global.get( "JaiMemoryThreshold"), 0.75) );
        jai.setTileThreads( (Integer) value( global.get( "JaiTileThreads"), 7 ) );
        jai.setTilePriority( (Integer) value( global.get( "JaiTilePriority"), 5 ) );
        jai.setImageIOCache( (Boolean) value( global.get( "ImageIOCache" ), false) );
        jai.setJPEGAcceleration( (Boolean) value( global.get( "JaiJPEGNative" ), true) );
        jai.setPNGAcceleration( (Boolean) value( global.get( "JaiPNGNative" ), true)  );
        jai.setRecycling( (Boolean) value( global.get( "JaiRecycling" ), true)  );
        
        info.getMetadata().put( JAIInfo.KEY, jai );
        
        geoServer.setGlobal( info );
        
        // read services
        for ( ServiceLoader sl : GeoServerExtensions.extensions( ServiceLoader.class ) ) {
            try {
                //special case for legacy stuff
                if ( sl instanceof LegacyServiceLoader ) {
                    ((LegacyServiceLoader)sl).setReader(reader);
                }
                
                ServiceInfo service = sl.load( geoServer );
                if ( service != null ) {
                    LOGGER.info( "Loading service '" + service.getId()  + "'");
                    geoServer.add( service );
                }
            }
            catch( Exception e ) {
                String msg = "Error occured loading service: " + sl.getServiceId();
                LOGGER.warning( msg );
                LOGGER.log( Level.INFO, "", e );
            }
        }
    }
}
