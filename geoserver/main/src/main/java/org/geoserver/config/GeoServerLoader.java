package org.geoserver.config;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.Wrapper;
import org.geoserver.catalog.util.LegacyCatalogImporter;
import org.geoserver.catalog.util.WrapperUtils;
import org.geoserver.config.util.LegacyConfigurationImporter;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geotools.util.logging.Logging;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.vfny.geoserver.global.GeoserverDataDirectory;

/**
 * Base class for initializing based on GeoServer configuration.
 * <p>
 * This class should  be subclassed in cases where some sort of system 
 * initialization must be carried out, and aspects of the initialization depend 
 * on GeoServer configuration.
 * </p>
 * <p>
 * Instances of this class should be registered in a spring context. Example:
 * <pre>
 *  &lt;bean id="geoServerLoader" class="GeoServerLoader"/>
 * </pre>
 * </p>
 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public final class GeoServerLoader implements BeanPostProcessor, DisposableBean, 
    ApplicationContextAware {

    static Logger LOGGER = Logging.getLogger( "org.geoserver" );
    
    GeoServerResourceLoader resourceLoader;
    GeoServer geoserver;
    
    public GeoServerLoader( GeoServerResourceLoader resourceLoader ) {
        this.resourceLoader = resourceLoader;
    }
    
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        GeoserverDataDirectory.init((WebApplicationContext)applicationContext);
    }
    
    public final Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

    public final Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        if ( bean instanceof GeoServer ) {
            geoserver = (GeoServer) bean;
            initialize();
        }
        
        return bean;
    }
    
    protected void initialize() {
        //load catalog
        LegacyCatalogImporter catalogImporter = new LegacyCatalogImporter();
        catalogImporter.setResourceLoader(resourceLoader);
        Catalog catalog = geoserver.getCatalog();
        if(catalog instanceof Wrapper && ((Wrapper) catalog).isWrapperFor(Catalog.class)) {
            catalog = WrapperUtils.deepUnwrap((Wrapper<Catalog>) catalog);
        }
        catalogImporter.setCatalog(catalog);
        
        try {
            catalogImporter.imprt( resourceLoader.getBaseDirectory() );
        }
        catch(Exception e) {
            throw new RuntimeException( e );
        }
        
        //load configuration
        LegacyConfigurationImporter importer = new LegacyConfigurationImporter();
        importer.setConfiguration(geoserver);
        
        try {
            importer.imprt( resourceLoader.getBaseDirectory() );
        } 
        catch (Exception e) {
            throw new RuntimeException( e );
        }
        
        //load initializer extensions
        List<GeoServerInitializer> initializers = GeoServerExtensions.extensions( GeoServerInitializer.class );
        for ( GeoServerInitializer initer : initializers ) {
            try {
                initer.initialize( geoserver );
            }
            catch( Throwable t ) {
                //TODO: log this
                t.printStackTrace();
            }
        }
    }
    
    public void reload() throws Exception {
        destroy();
        initialize();
    }
    
    public void destroy() throws Exception {
        //TODO: persist catalog
        //TODO: persist global

        //persist services
        Collection services = geoserver.getServices();
        List<ServiceLoader> loaders = GeoServerExtensions.extensions( ServiceLoader.class );
        
        for ( Iterator s = services.iterator(); s.hasNext(); ) {
            ServiceInfo service = (ServiceInfo) s.next();
            for ( ServiceLoader loader : loaders ) {
                if ( loader.getServiceId().equals(service.getId()) ) {
                    try {
                        loader.save( service, geoserver );
                        break;
                    }
                    catch( Throwable t ) {
                        LOGGER.warning( "Error persisting service: " + service.getId() );
                        LOGGER.log( Level.INFO, "", t );
                    }
                }
            }
        }
        
        //dispose
        geoserver.dispose();
    }
}
