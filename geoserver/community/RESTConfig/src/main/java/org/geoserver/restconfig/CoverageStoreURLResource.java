/* Copyright (c) 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.restconfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.opengis.coverage.grid.Format;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.vfny.geoserver.config.CoverageConfig;
import org.vfny.geoserver.config.CoverageStoreConfig;
import org.vfny.geoserver.config.DataConfig;
import org.vfny.geoserver.global.ConfigurationException;
import org.vfny.geoserver.global.Data;
import org.vfny.geoserver.global.GeoserverDataDirectory;
import org.vfny.geoserver.global.dto.DataDTO;
import org.vfny.geoserver.global.xml.XMLConfigWriter;
import org.vfny.geoserver.util.CoverageStoreUtils;

/**
 * This class extends Resource to handle the GET and PUT requests to manage the
 * upload of the GeoTIFF files.
 * 
 * @author $Author: Tobia Di Pisa (ilcovo@gmail.com) $ (last modification)
 * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last modification)
 */

public class CoverageStoreURLResource extends Resource {
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	
    private DataConfig myDataConfig;

    private Data myData;

    // //////
    // A map from .xxx file extensions
    // //////

    private Map myFormats;

    Logger log = Logger.getLogger(CoverageStoreURLResource.class);

    public CoverageStoreURLResource() {
        myFormats = new HashMap();
        myFormats.put("tiff", "GeoTIFF");
    }

    public void setDataConfig(DataConfig dc) {
        myDataConfig = dc;
    }

    public DataConfig getDataConfig() {
        return myDataConfig;
    }

    public void setData(Data d) {
        myData = d;
    }

    public Data getData() {
        return myData;
    }

    public boolean allowGet() {
        return true;
    }

    /**
     * This function handles the GET request to verify if the CoverageStore or
     * the Coverage are available.
     */

    public void handleGet() {
        String coverageStore = (String) getRequest().getAttributes().get("coveragestore");
        String coverageName = (String) getRequest().getAttributes().get("coverage");
        String qualified = coverageStore + ":" + coverageName;

        CoverageStoreConfig csc = myDataConfig.getDataFormat(coverageStore);
        CoverageConfig cc = (CoverageConfig) myDataConfig.getCoverages().get(qualified);

        if (csc == null || cc == null) {
            getResponse().setEntity(new StringRepresentation("Giving up because coveragestore " + coverageStore + "or" + coverageName + "does not exist.", MediaType.TEXT_PLAIN));
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return;
        }

        getResponse().setEntity(new StringRepresentation("Handling GET on a CoverageStoreFileResource", MediaType.TEXT_PLAIN));
        getResponse().setStatus(Status.SUCCESS_OK);
    }

    public boolean allowPut() {
        return true;
    }

    /**
     * This function handles the PUT requests managing the construction of the
     * coverage store and the coverage if necessary.
     */

    public void handlePut() {
        String coverageStore = (String) getRequest().getAttributes().get("coveragestore");
        String coverageName = (String) getRequest().getAttributes().get("coverage");
        String extension = (String) getRequest().getAttributes().get("type");

        String formatId = (String) myFormats.get(extension);
        Format format;
        try {
            format = CoverageStoreUtils.acquireFormat(formatId, null);
        } catch (IOException e) {
            getResponse().setEntity(new StringRepresentation("Error while storing uploaded file: " + e, MediaType.TEXT_PLAIN));
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return;
        }

        if (format == null) {
            getResponse().setEntity(new StringRepresentation("Unrecognized extension: " + extension, MediaType.TEXT_PLAIN));
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }

        File uploadedFile = null;
        try {
            uploadedFile = handleUpload(coverageName, extension, getRequest());
        } catch (Exception e) {
            getResponse().setEntity(new StringRepresentation("Error while storing uploaded file: " + e, MediaType.TEXT_PLAIN));
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return;
        }

        // //////
        // Configuration for the coverage store
        // //////

        CoverageStoreConfig csc = myDataConfig.getDataFormat(coverageStore);

        if (csc == null) {
            csc = new CoverageStoreConfig(coverageStore, format);
            csc.setEnabled(true);
            csc.setNameSpaceId(myDataConfig.getDefaultNameSpace().getPrefix());
            try {
                csc.setUrl(uploadedFile.toURL().toExternalForm());
            } catch (MalformedURLException e) {
                getResponse().setEntity(new StringRepresentation("Error while storing uploaded file: " + e, MediaType.TEXT_PLAIN));
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                return;
            }

            // //////
            // TODO: something better exists, I hope
            // //////

            csc.setAbstract("Autoconfigured by RESTConfig");

            myDataConfig.addDataFormat(csc);

        }

        save();

        // //////
        // Configuration for the coverage
        // //////

        String qualified = coverageStore + ":" + coverageName;

        CoverageConfig cc = (CoverageConfig) myDataConfig.getCoverages().get(qualified);

        if (cc == null) {
            AbstractGridCoverage2DReader reader = (AbstractGridCoverage2DReader) ((AbstractGridFormat) format).getReader(uploadedFile);

            if (reader == null) {
                getResponse().setEntity(new StringRepresentation("Error while storing uploaded file: Invalid GeoTIFF file!", MediaType.TEXT_PLAIN));
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                return;
            }

            try {
                cc = new CoverageConfig(csc.getId(), format, reader, myDataConfig);
            } catch (Exception e) {
                getResponse().setEntity(new StringRepresentation("Failure while saving configuration: " + e, MediaType.TEXT_PLAIN));
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                return;
            }

        }

        myDataConfig.removeCoverage(qualified);
        myDataConfig.addCoverage(qualified, cc);

        save();

        getResponse().setEntity(new StringRepresentation("Handling PUT on a CoverageStoreFileResource", MediaType.TEXT_PLAIN));
        getResponse().setStatus(Status.SUCCESS_OK);
    }

    /**
     * This function gets the stream of the request to copy it into a file.
     * 
     * @param coverageName
     * @param extension
     * @param request
     * @return File
     * @throws IOException
     * @throws ConfigurationException
     * @throws URISyntaxException 
     */

    private File handleUpload(String coverageName, String extension, Request request) throws IOException, ConfigurationException, URISyntaxException {

        // //////
        // TODO: don't manage the temp file manually, java can take care of it
        // //////

        File dir = GeoserverDataDirectory.findCreateConfigDir("data");
        File tempFile = new File(dir, coverageName + "." + extension + ".tmp");
        File newFile  = new File(dir, coverageName + "." + extension);

        InputStreamReader  inReq  = new InputStreamReader(request.getEntity().getStream());
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        int len;

        while ((len = inReq.read(buffer)) >= 0) {
            char[] read = new char[len];
            System.arraycopy(buffer, 0, read, 0, len);
            sb.append(read);
        }

        inReq.close();
        
        URL fileURL = new URL(sb.toString());

        BufferedInputStream stream = new BufferedInputStream(fileURL.openStream());

        FileOutputStream out = new FileOutputStream(tempFile);
        byte[] binBuffer = new byte[DEFAULT_BUFFER_SIZE];
        int count = 0;
        int n = 0;
        while (-1 != (n = stream.read(binBuffer))) {
        	out.write(binBuffer, 0, n);
            count += n;
        }
        out.flush();
        out.close();
        
        tempFile.renameTo(newFile);

        return newFile;
    }

    /**
     * This function manages the save of the configuration for the CoverageStore
     * and the Coverage
     */

    private void save() {
        try {
            saveConfiguration();
        } catch (Exception e) {
            getResponse().setEntity(new StringRepresentation("Failure while saving configuration: " + e, MediaType.TEXT_PLAIN));
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return;
        }
    }

    private void saveConfiguration() throws ConfigurationException {
        getData().load(getDataConfig().toDTO());
        XMLConfigWriter.store((DataDTO) getData().toDTO(), GeoserverDataDirectory.getGeoserverDataDirectory());
    }

}
