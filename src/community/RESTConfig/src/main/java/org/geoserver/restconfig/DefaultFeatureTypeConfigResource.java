/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.restconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.geoserver.rest.AutoXMLFormat;
import org.geoserver.rest.FreemarkerFormat;
import org.geoserver.rest.JSONFormat;
import org.geoserver.rest.MapResource;
import org.geoserver.rest.RestletException;
import org.geotools.data.DataStore;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.vfny.geoserver.config.DataConfig;
import org.vfny.geoserver.config.DataStoreConfig;
import org.vfny.geoserver.config.FeatureTypeConfig;
import org.vfny.geoserver.global.Data;
import org.vfny.geoserver.util.DataStoreUtils;

/**
 * Restlet resource that returns the default configuration for a resource (with autogenerated 
 * metadata and correct BBox/projection settings if possible)
 *
 * @author David Winslow <dwinslow@opengeo.org> , OpenGeo 
 */
public class DefaultFeatureTypeConfigResource extends MapResource {
    private DataConfig myDC;
    private DataStoreConfig myDSC = null;
    private FeatureTypeConfig myFTC = null;
    private Data myData;
    private static Logger LOGGER = 
        org.geotools.util.logging.Logging.getLogger("org.geoserver.community.RESTConfig");

    public DefaultFeatureTypeConfigResource(Data d, DataConfig dc){
        super();
        setData(d);
        setDataConfig(dc);
    }

    public void setDataConfig(DataConfig dc){
        myDC = dc;
    }

    public DataConfig getDataConfig(){
        return myDC;
    }
    
    public void setData(Data d){
        myData = d;
    }
    
    public Data getData(){
        return myData;
    }

    public Map getSupportedFormats() {
        Map m = new HashMap();

        m.put(
                "html",
                new FreemarkerFormat("HTMLTemplates/featuretype.ftl",
                    getClass(),
                    MediaType.TEXT_HTML
                    )
             );
        m.put("json", new JSONFormat());
        m.put("xml", new AutoXMLFormat("FeatureTypes"));
        m.put(null, m.get("html"));

        return m;
    }

    public Map getMap(){
        String dataStoreName = null;
        try {
            dataStoreName = (String) getRequest().getAttributes().get("layer");

            DataStoreConfig dataStoreConfig = getDataConfig().getDataStore(dataStoreName);

            DataStore dataStore = 
                DataStoreUtils.acquireDataStore(
                        dataStoreConfig.getConnectionParams(),
                        (ServletContext) null
                        );

            Map m = new HashMap();
            List featureTypes = new ArrayList();
            
            Iterator it = myDC.getFeaturesTypes().values().iterator();
            while (it.hasNext()){
                FeatureTypeConfig ftc = (FeatureTypeConfig)it.next();
                if (ftc.getDataStoreId().equals(dataStoreName)){
                        featureTypes.add(ftc.getName());
                }
            }
            
            m.put("FeatureTypes", featureTypes);
            
            return m; 
            
//            FeatureTypeConfig featureTypeConfig = 
//                DataStoreFileResource.autoConfigure(dataStore, dataStoreName, typeName);
//            if (featureTypeConfig == null) 
//                throw new RestletException(
//                        "FTC was null! Bad bad bad!",
//                        Status.SERVER_ERROR_INTERNAL
//                        );
//
//            return RESTUtils.getMap(featureTypeConfig);
        } catch (Exception e) {
            throw new RestletException(
                    "Failure while getting defaults for " + dataStoreName,
                    Status.SERVER_ERROR_INTERNAL,
                    e
                    ); 
        }
    }

    public boolean allowGet() {
        return true;
    }
    
    public boolean allowPut() {
        return false;
    }

    public boolean allowDelete() {
        return false;
    }

    public void handleDelete() {
    }
}
