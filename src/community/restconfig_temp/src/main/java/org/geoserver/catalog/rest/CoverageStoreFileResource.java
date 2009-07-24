package org.geoserver.catalog.rest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogBuilder;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.rest.RestletException;
import org.geoserver.rest.format.DataFormat;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.opengis.coverage.grid.Format;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public class CoverageStoreFileResource extends StoreFileResource {

    Format coverageFormat;
    
    public CoverageStoreFileResource(Request request, Response response,
            Format coverageFormat, Catalog catalog) {
        super(request, response, catalog);
        this.coverageFormat = coverageFormat;
    }
    
    @Override
    public void handlePut() {
        final Request request = getRequest();
        final Response response = getResponse();
        
        final String workspace = (String)request.getAttributes().get("workspace");
        final String coveragestore = (String)request.getAttributes().get("coveragestore");
        final String format = (String)request.getAttributes().get("format");
        final String method = ((String) request.getResourceRef().getLastSegment()).toLowerCase();
        
        File directory = null;
        boolean isExternal = true;
        
        // Prepare the directory only in case this is not an external upload
        if (method != null && (method.startsWith("file.") || method.startsWith("url."))){ 
            isExternal = false;
            try {
                 directory = catalog.getResourceLoader().createDirectory( "data/" + coveragestore );
            } 
            catch (IOException e) {
                throw new RestletException( e.getMessage(), Status.SERVER_ERROR_INTERNAL, e );
            }
        }
        final File uploadedFile = handleFileUpload(coveragestore, format, directory);
        
        // /////////////////////////////////////////////////////////////////////
        //
        // Add overviews to the Coverage
        //
        // /////////////////////////////////////////////////////////////////////
        final Form form = request.getResourceRef().getQueryAsForm();
        if ("yes".equalsIgnoreCase(form.getFirstValue("overviews")) ) {
            /* TODO: Add overviews here */;
        }
            
        //create a builder to help build catalog objects
        CatalogBuilder builder = new CatalogBuilder(catalog);
        builder.setWorkspace( catalog.getWorkspaceByName( workspace ) );
        
        //create the coverage store
        CoverageStoreInfo info = catalog.getCoverageStoreByName(workspace, coveragestore);
        boolean add = false;
        if ( info == null ) {
          //create a new coverage store
            LOGGER.info("Auto-configuring coverage store: " + coveragestore);
            
            info = builder.buildCoverageStore(coveragestore);
            add = true;
        }
        else {
            //use the existing
            LOGGER.info("Using existing coverage store: " + coveragestore);
        }
        
        info.setType(coverageFormat.getName());
        if (!isExternal)
            info.setURL("file:data/" + coveragestore + "/" + uploadedFile.getName() );
        else
            try {
                info.setURL( uploadedFile.toURL().toExternalForm());
            } catch (MalformedURLException e) {
                throw new RestletException( "Error auto-configuring coverage", Status.SERVER_ERROR_INTERNAL, e );
            }
       
        
        //add or update the datastore info
        if ( add ) {
            catalog.add( info );
        }
        else {
            catalog.save( info );
        }
        
        builder.setStore(info);
        
        //check configure parameter, if set to none to not try to configure coverage
        String configure = form.getFirstValue( "configure" );
        if ( "none".equalsIgnoreCase( configure ) ) {
            getResponse().setStatus( Status.SUCCESS_CREATED );
            return;
        }
        
        try {
            AbstractGridCoverage2DReader reader = 
                (AbstractGridCoverage2DReader) ((AbstractGridFormat) coverageFormat).getReader(uploadedFile.toURL());
            if ( reader == null ) {
                throw new RestletException( "Could not acquire reader for coverage.", Status.SERVER_ERROR_INTERNAL );
            }
            
            CoverageInfo cinfo = builder.buildCoverage( reader );
            
            //check if the name of the coverage was specified
            String coverageName = form.getFirstValue("coverageName");
            if(coverageName == null) {
                coverageName = uploadedFile.getName();
                coverageName = coverageName.substring(0, coverageName.lastIndexOf('.'));
            } else {
                cinfo.setName( coverageName );
            }
            
            CoverageInfo existing = catalog.getCoverageByCoverageStore(info, coverageName);
            if (existing != null) {
                //update the existing
                builder.updateCoverage(existing,cinfo);
                catalog.save( existing );
                cinfo = existing;
            }
            
            //do some post configuration, if srs is not known or unset, transform to 4326
            if ("UNKNOWN".equals(cinfo.getSRS())) {
                //CoordinateReferenceSystem sourceCRS = cinfo.getBoundingBox().getCoordinateReferenceSystem();
                //CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326", true);
                //ReferencedEnvelope re = cinfo.getBoundingBox().transform(targetCRS, true);
                cinfo.setSRS( "EPSG:4326" );
                //cinfo.setCRS( targetCRS );
                //cinfo.setBoundingBox( re );
            }

            //add/save
            String layerName = cinfo.getName();
            if (existing == null) {
                catalog.add( cinfo );
                
                final LayerInfo layerInfo=builder.buildLayer(cinfo);
                if (form.getFirst("style") != null){
                    final String layerStyle =  form.getFirstValue("style");
                    StyleInfo style = catalog.getStyleByName(layerStyle);
                    layerInfo.setDefaultStyle(style);
                    layerInfo.getStyles().add(style);
                }
                if (form.getFirst("wmspath") != null){
                    layerInfo.setPath(form.getFirstValue("wmspath"));
                }
              
                layerName = layerInfo.getName();
                catalog.add(layerInfo);
            }
            else {
                catalog.save( cinfo );
                
                //TODO: update the layers pointing at this coverage
            }
            
            AbstractCatalogResource.saveCatalog( catalog );
            DataFormat df = new CoverageStoreResource(getContext(),request,response,catalog)
            .createXMLFormat(request, response);
            response.setEntity(df.toRepresentation(info));
            getResponse().setStatus( Status.SUCCESS_CREATED );
        }
        catch( Exception e ) {
            throw new RestletException( "Error auto-configuring coverage", Status.SERVER_ERROR_INTERNAL, e );
        }
    }

    protected File findPrimaryFile(File directory, String format) {
        for ( File f : directory.listFiles() ) {
            if ( ((AbstractGridFormat)coverageFormat).accepts(f) ) {
                return f;
            }
        }
        
        return null;
    }
}