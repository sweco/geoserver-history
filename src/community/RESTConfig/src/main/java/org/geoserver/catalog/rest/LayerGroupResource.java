package org.geoserver.catalog.rest;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogBuilder;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.rest.RestletException;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public class LayerGroupResource extends AbstractCatalogResource {

    public LayerGroupResource(Context context, Request request,
            Response response, Catalog catalog) {
        super(context, request, response, LayerGroupInfo.class, catalog);
    }

    @Override
    protected Object handleObjectGet() throws Exception {
        String lg = getAttribute( "layergroup" );
        
        if ( lg == null ) {
            LOGGER.fine( "GET all layer groups");
            //return all layers
            return catalog.getLayerGroups();
        }
        
        LOGGER.fine( "GET layer group " + lg );
        return catalog.getLayerGroupByName( lg ); 
    }

    @Override
    public boolean allowPost() {
        return getAttribute( "layergroup") == null;
    }
    
    @Override
    protected String handleObjectPost(Object object) throws Exception {
        LayerGroupInfo lg = (LayerGroupInfo) object;
        LOGGER.info( "POST layer group " + lg.getName() );
        
        if ( lg.getLayers().isEmpty() ) {
            throw new RestletException( "layer group must not be empty", Status.CLIENT_ERROR_BAD_REQUEST );
        }
       
        if ( lg.getBounds() == null ) {
            LOGGER.fine( "Auto calculating layer group bounds");
            new CatalogBuilder( catalog ).calculateLayerGroupBounds(lg);
        }
        
        
        catalog.add( lg );
        //saveCatalog();
        saveConfiguration();
        return lg.getName();
    }

    @Override
    public boolean allowPut() {
        return getAttribute( "layergroup") != null;
    }
    
    @Override
    protected void handleObjectPut(Object object) throws Exception {
        String layergroup = getAttribute("layergroup");
        LOGGER.info( "PUT layer group " + layergroup );
        
        LayerGroupInfo lg = (LayerGroupInfo) object;
        LayerGroupInfo original = catalog.getLayerGroupByName( layergroup );
       
        //ensure not a name change
        if ( lg.getName() != null && !lg.getName().equals( original.getName() ) ) {
            throw new RestletException( "Can't change name of a layer group", Status.CLIENT_ERROR_FORBIDDEN );
        }
        
        new CatalogBuilder( catalog ).updateLayerGroup( original, lg );
        catalog.save( original );
        saveConfiguration();
        //saveCatalog();
    }

    @Override
    public boolean allowDelete() {
        return getAttribute( "layergroup" ) != null;
    }
    
    @Override
    protected void handleObjectDelete() throws Exception {
        String layergroup = getAttribute( "layergroup" );
        LOGGER.info( "DELETE layer group " + layergroup );
        
        LayerGroupInfo lg = catalog.getLayerGroupByName( layergroup );
        catalog.remove( lg );
        saveConfiguration();
        //saveCatalog();
    }
}
