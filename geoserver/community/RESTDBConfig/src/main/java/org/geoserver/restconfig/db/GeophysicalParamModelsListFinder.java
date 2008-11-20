package org.geoserver.restconfig.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.GeophysicParamInfo;
import org.geoserver.catalog.ModelInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.rest.AutoXMLFormat;
import org.geoserver.rest.DataFormat;
import org.geoserver.rest.FreemarkerFormat;
import org.geoserver.rest.JSONFormat;
import org.geoserver.rest.MapResource;
import org.restlet.Finder;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

/**
 * Restlet for GeophysicalParamModelsListFinder resources
 *
 * @author Alessio Fabiani <alessio.fabiani@geo-solutions.it> , GeoSolutions S.a.S.
 */
public class GeophysicalParamModelsListFinder extends Finder {

    private GeoServer geoServer;
    private Catalog rawCatalog;

    public void setGeoServer(GeoServer geoServer){
        this.geoServer = geoServer;
    }

    public GeoServer getGeoServer(){
        return this.geoServer;
    }

    public void setCatalog(Catalog catalog){
        this.rawCatalog = catalog;
    }

    public Catalog getCatalog(){
        return this.rawCatalog;
    }

    public Resource findTarget(Request request, Response response){
        Resource r = new GeophysicParamModelsList();
        r.init(getContext(), request, response);
        return r;
    }

    protected class GeophysicParamModelsList extends MapResource{
        private Map myPostFormats;

        public GeophysicParamModelsList(){
            super();
            myPostFormats = new HashMap();
            myPostFormats.put(MediaType.TEXT_XML, new AutoXMLFormat());
            myPostFormats.put(MediaType.APPLICATION_JSON, new JSONFormat());
            myPostFormats.put(null, myPostFormats.get(MediaType.TEXT_XML));
        }

        public Map getSupportedFormats() {
            Map m = new HashMap();
            m.put("html",
                    new FreemarkerFormat(
                        "HTMLTemplates/geophysicparammodels.ftl",
                        getClass(),
                        MediaType.TEXT_HTML)
                 );
            m.put("json", new JSONFormat());
            m.put("xml", new AutoXMLFormat("GeophysicParamModels"));
            m.put(null, m.get("html"));
            return m;
        }

        public Map getMap() {
            Map m = new HashMap();
            List l = new ArrayList();
            Map geophysicParamModels = getVirtualGeophysicParamModelsMap(getRequest(), getCatalog());
            
            l.addAll(geophysicParamModels.keySet());
            Collections.sort(l);
            m.put("GeophysicParamModels", l);
            
            return m;
        }

        public boolean allowPost(){
            return true;
        }

        // TODO: POST support for folders/ url
        public void handlePost(){
            MediaType type = getRequest().getEntity().getMediaType();
            LOG.info("GeophysicParamModel posted, mediatype is:" + type);
            DataFormat format = (DataFormat)myPostFormats.get(type);
            LOG.info("Using post format: " + format);
            Map m = (Map)format.readRepresentation(getRequest().getEntity());
            LOG.info("Read data as: " + m);
        }
    }

    public static Map getVirtualGeophysicParamModelsMap(Request request, Catalog catalog){
        Map geophysicParamModels = new HashMap();
        Map attributes = request.getAttributes();
        String variableName = null;

        GeophysicParamInfo variable = null;
        if (attributes.containsKey("parameter")) {
            variableName = (String) attributes.get("parameter");
            
            variable = catalog.getGeophysicParamByName(variableName);
            
            if (variable != null && variable.getModels() != null) {
                for (ModelInfo m : catalog.getModels(variable)) {
                    geophysicParamModels.put(m.getName(), m);
                }
            }
        }
        
        return geophysicParamModels;
    }
}