package org.geoserver.jai;

import java.util.List;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.RecyclingTileFactory;

import org.geoserver.config.ConfigurationListener;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.GeoServerInitializer;
import org.geoserver.config.ServiceInfo;

import com.sun.media.jai.util.SunTileCache;

/**
 * Initializes JAI functionality from configuration.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * 
 * TODO: we should figure out if we want JAI to be core to the model or a plugin
 * ... right now it is both
 *
 */
public class JAIInitializer implements GeoServerInitializer {

    public void initialize(GeoServer geoServer) throws Exception {
        initJAI( geoServer.getGlobal() );
        
        geoServer.addListener( new ConfigurationListener() {

            public void handleGlobalChange(GeoServerInfo global,
                    List<String> propertyNames, List<Object> oldValues,
                    List<Object> newValues) {
                
                initJAI( global );
            }

            public void handleServiceChange(ServiceInfo service,
                    List<String> propertyNames, List<Object> oldValues,
                    List<Object> newValues) {
            }
            
        });
    }

    void initJAI(GeoServerInfo global) {
        
        JAIInfo jai = (JAIInfo) global.getMetadata().get( JAIInfo.KEY );
        
        JAI jaiDef = JAI.getDefaultInstance();
        jai.setJAI( jaiDef );
        
        // setting JAI wide hints
        jaiDef.setRenderingHint(JAI.KEY_CACHED_TILE_RECYCLING_ENABLED, jai.getRecycling());
        
        // tile factory and recycler
        final RecyclingTileFactory recyclingFactory = new RecyclingTileFactory();
        jaiDef.setRenderingHint(JAI.KEY_TILE_FACTORY, recyclingFactory);
        jaiDef.setRenderingHint(JAI.KEY_TILE_RECYCLER, recyclingFactory);
        
        // Setting up Cache Capacity
        SunTileCache jaiCache = (SunTileCache) jaiDef.getTileCache();
        jai.setTileCache( jaiCache );
        
        long jaiMemory = (long) (jai.getMemoryCapacity() * Runtime.getRuntime().maxMemory());
        jaiCache.setMemoryCapacity(jaiMemory);
        
        // Setting up Cahce Threshold
        jaiCache.setMemoryThreshold((float) jai.getMemoryThreshold());
        
        jaiDef.getTileScheduler().setParallelism(jai.getTileThreads());
        jaiDef.getTileScheduler().setPrefetchParallelism(jai.getTileThreads());
        jaiDef.getTileScheduler().setPriority(jai.getTilePriority());
        jaiDef.getTileScheduler().setPrefetchPriority(jai.getTilePriority());
        
        // ImageIO Caching
        ImageIO.setUseCache(jai.getImageIOCache());
    }
}
