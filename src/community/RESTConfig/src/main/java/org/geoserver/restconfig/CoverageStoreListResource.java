/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.restconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.MediaType;
import org.vfny.geoserver.config.DataConfig;

import org.geoserver.rest.MapResource;
import org.geoserver.rest.format.DataFormat;
import org.geoserver.rest.format.FreemarkerFormat;
import org.geoserver.rest.format.MapJSONFormat;
import org.geoserver.rest.format.MapXMLFormat;

public class CoverageStoreListResource extends MapResource {
    private DataConfig myDataConfig;

    public CoverageStoreListResource(Context context,Request request, Response response){
        super(context,request,response);
    }

    public CoverageStoreListResource(Context context, Request request, Response response,
        DataConfig config) {
        super(context, request, response);
        myDataConfig = config;
    }

    public void setDataConfig(DataConfig dc){
        myDataConfig = dc;
    }

    public DataConfig getDataConfig(){
        return myDataConfig;
    }

    @Override
    public Map getMap() {
        Map m = new HashMap();
        m.put("coveragestores", myDataConfig.getDataFormatIds());

        return m;
    }

    @Override
    protected List<DataFormat> createSupportedFormats(Request request,
            Response response) {
        List l = new ArrayList();
        l.add(new FreemarkerFormat("HTMLTemplates/coveragestores.ftl", getClass(), MediaType.TEXT_HTML));
        l.add(new MapJSONFormat());
        l.add(new MapXMLFormat("coveragestores"));
        
        return l;
    }
}
