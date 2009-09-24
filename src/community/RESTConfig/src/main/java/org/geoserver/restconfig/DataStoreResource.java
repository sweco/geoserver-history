/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.restconfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataAccessFactory.Param;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.StringRepresentation;
import org.vfny.geoserver.config.DataConfig;
import org.vfny.geoserver.config.DataStoreConfig;
import org.vfny.geoserver.global.ConfigurationException;
import org.vfny.geoserver.global.Data;
import org.vfny.geoserver.global.GeoserverDataDirectory;
import org.vfny.geoserver.global.dto.DataDTO;
import org.vfny.geoserver.global.xml.XMLConfigWriter;
import org.vfny.geoserver.util.DataStoreUtils;


import org.geoserver.config.GeoServer;
import org.geoserver.rest.MapResource;
import org.geoserver.rest.format.DataFormat;
import org.geoserver.rest.format.FreemarkerFormat;
import org.geoserver.rest.format.MapJSONFormat;
import org.geoserver.rest.format.MapXMLFormat;

/**
 * Restlet for DataStore resources
 *
 * @author Arne Kepp <ak@openplans.org> , The Open Planning Project
 */
public class DataStoreResource extends MapResource {
    private DataConfig myDC;
    private Data myData;

    public DataStoreResource(Context context,Request request, Response response){
        super(context,request,response);
    }

    public DataStoreResource(Data d, DataConfig dc,Context context,Request request, Response response){
        super(context,request,response);
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
   

    private DataStoreConfig findMyDataStore() {
        Map attributes = getRequest().getAttributes();

        if (attributes.containsKey("folder")) {
            String dsid = (String) attributes.get("folder");

            return myDC.getDataStore(dsid);
        }

        return null;
    }

    @Override
    protected List<DataFormat> createSupportedFormats(Request request,
            Response response) {
        List l = new ArrayList();
        l.add(new FreemarkerFormat("HTMLTemplates/datastore.ftl", getClass(), MediaType.TEXT_HTML));
        l.add(new MapJSONFormat());
        l.add(new MapXMLFormat("datastore"));

        return l;
    }

    public Map getMap() {
        Map m = getMap(findMyDataStore());
        m.put( "page", getPageInfo());
        return m;
    }

    public static Map getMap(DataStoreConfig myDSC){
        Map map = new HashMap();
        map.put("Enabled", Boolean.toString(myDSC.isEnabled()));
        map.put("Namespace", myDSC.getNameSpaceId());
        map.put("Description", (myDSC.getAbstract() == null ? "" : myDSC.getAbstract()));
        map.put("DataStoreType", myDSC.getFactory().getDisplayName());

        DataStoreFactorySpi factory = myDSC.getFactory();

        List storeSpecificParameters = new ArrayList();
        Param[] parameters = factory.getParametersInfo();
        for (int i = 0; i < parameters.length; i++){
            Param p = parameters[i];
            if (!("namespace".equals(p.key))){
                Object value = myDSC.getConnectionParams().get(p.key);
                Map entry = new HashMap();
                if (value == null) {
                    entry.put("value", "");
                } else if (value instanceof String){
                    entry.put("value", value);
                } else {
                    entry.put("value", p.text(value));
                }
                String key = p.key.substring(p.key.lastIndexOf(':') + 1);
                entry.put("name", key);
                entry.put("type", p.type.getName());
                entry.put("required", Boolean.toString(p.required));
                storeSpecificParameters.add(entry);
            }
        }

        map.put("Params", storeSpecificParameters);
        return map;
    }

    public boolean allowPut(){
        return true;
    }
    
    @Override
    protected void putMap(Map m) throws Exception {
        try{
    	String dataStoreName = (String)getRequest().getAttributes().get("datastore");
    	DataStoreConfig myDSC = findMyDataStore();
        
    	if (myDSC == null){
//            DataStoreFactorySpi factory = 
//                findDataStoreFactory((String)details.get("DataStoreType"));
    	    myDSC = new DataStoreConfig(
    	    		dataStoreName,
    	    		(String)m.get("DataStoreType")
    	            );
    	}
    	
    	myDSC.setEnabled(Boolean.valueOf((String)m.get("Enabled")));
    	myDSC.setNameSpaceId((String)m.get("Namespace"));
    	myDSC.setAbstract((String)m.get("Description"));
    	
        Map params = buildParamMap(((List)m.get("Params")),
        		myDSC.getFactory().getParametersInfo());
        
        myDSC.setConnectionParams(params);

        if (myDC.getDataStores().containsKey(dataStoreName)){
        	myDC.removeDataStore(dataStoreName);
        }
        
        myDC.addDataStore(myDSC);
        myData.load(myDC.toDTO());
        
        saveConfiguration();
        getResponse().setEntity(new StringRepresentation("Succeeded writing configuration!", MediaType.TEXT_PLAIN));
        } catch (Exception e){
            getResponse().setEntity(new StringRepresentation("Couldn't write config due to " + e, MediaType.TEXT_PLAIN));
        }
    }
    
    private Map simplifyParameterMap(List parameters){
    	Map simple = new HashMap();
    	
        Iterator it = parameters.iterator();
        while (it.hasNext()){
        	Map m = (Map)it.next();
        	simple.put(m.get("name"), m.get("value"));
        }
    	
    	return simple;
    }
    
    private Map buildParamMap(List paramValues, Param[] paramSpecs){
    	Map simplified = simplifyParameterMap(paramValues);
    	Map returnable = new HashMap();
        for (Iterator i = simplified.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();

            Param param = DataStoreUtils.find(paramSpecs, key);

            if (param == null) {
            	// TODO: actually report the error.
            	return null;
            }

            Object value = null;

            try {
                value = param.lookUp(simplified);
            } catch (IOException erp) {
            }

            if ((value != null) && !"".equals(value)) {
                returnable.put(key, value);
            }
        }
        
        return returnable;
    }
    
    private void saveConfiguration() throws ConfigurationException{
        synchronized (GeoServer.CONFIGURATION_LOCK) {
            getData().load(getDataConfig().toDTO());
            XMLConfigWriter.store((DataDTO)getData().toDTO(),
            		GeoserverDataDirectory.getGeoserverDataDirectory()
            		);
        }
    }

    public boolean allowDelete(){
        return true;
    }

    public void handleDelete(){
        String name = (String)getRequest().getAttributes().get("datastore");
        if (myDC.getDataStores().containsKey(name)){
            myDC.removeDataStore(name);
            getResponse().setStatus(Status.SUCCESS_OK);
            getResponse().setEntity(new StringRepresentation(name + " found and deleted!",
                        MediaType.TEXT_PLAIN)
            );
            
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            getResponse().setEntity(new StringRepresentation(name + " not found!",
                        MediaType.TEXT_PLAIN
                        )
            );
        }
    }
}