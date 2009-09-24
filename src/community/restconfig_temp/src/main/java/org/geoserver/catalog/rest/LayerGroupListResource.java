package org.geoserver.catalog.rest;

import java.util.List;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.LayerGroupInfo;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class LayerGroupListResource extends AbstractCatalogListResource {

    protected LayerGroupListResource(Context context, Request request,
            Response response, Catalog catalog) {
        super(context, request, response, LayerGroupInfo.class, catalog);
    }

    @Override
    protected List handleListGet() throws Exception {
        LOGGER.fine( "GET all layer groups");
        return catalog.getLayerGroups();
    }

}