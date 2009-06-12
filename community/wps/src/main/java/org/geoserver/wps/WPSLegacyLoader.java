package org.geoserver.wps;

import org.geoserver.config.GeoServer;
import org.geoserver.config.util.LegacyServiceLoader;
import org.geoserver.config.util.LegacyServicesReader;
import org.geotools.util.Version;

public class WPSLegacyLoader extends LegacyServiceLoader<WPSInfo> {

    public Class<WPSInfo> getServiceClass() {
        return WPSInfo.class;
    }
    
    public WPSInfo load(LegacyServicesReader reader, GeoServer geoServer)
            throws Exception {
        
        WPSInfoImpl wps = new WPSInfoImpl();
        wps.setId("wps");
        wps.setGeoServer(geoServer);
        wps.getVersions().add( new Version( "1.0.0") );
        
        return wps;
    }

}

