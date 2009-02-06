package org.geoserver.catalog.rest;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.rest.RestletException;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;

public class CoverageFinder extends AbstractCatalogFinder {

    public CoverageFinder(Catalog catalog) {
        super(catalog);
    }
    
    @Override
    public Resource findTarget(Request request, Response response) {
        String ws = (String) request.getAttributes().get( "workspace" );
        String cs = (String) request.getAttributes().get( "coveragestore" );
        String c = (String) request.getAttributes().get( "coverage");
        
        //ensure referenced resources exist
        if ( ws != null && catalog.getWorkspaceByName( ws ) == null ) {
            throw new RestletException( "No such workspace: " + ws, Status.CLIENT_ERROR_NOT_FOUND );
        }
        if ( cs != null && catalog.getCoverageStoreByName(ws, cs) == null ) {
            throw new RestletException( "No such coveragestore: " + ws + "," + cs, Status.CLIENT_ERROR_NOT_FOUND );
        }
        if ( c != null ) {
            if ( cs != null &&
                    catalog.getCoverageByCoverageStore(catalog.getCoverageStoreByName(ws, cs), c) == null) {
                throw new RestletException( "No such coverage: "+ws+","+cs+","+c, Status.CLIENT_ERROR_NOT_FOUND );
            }
            else {
                //look up by workspace/namespace
                NamespaceInfo ns = catalog.getNamespaceByPrefix( ws );
                if ( ns == null || catalog.getCoverageByName( ns, c ) == null ) {
                    throw new RestletException( "No such coverage: "+ws+","+c, Status.CLIENT_ERROR_NOT_FOUND );
                }
            
            }
        }
            
        return new CoverageResource(null,request,response,catalog);
    }

}
