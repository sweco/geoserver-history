/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wms.map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.ows.LocalLayer;
import org.geoserver.ows.LocalWorkspace;
import org.geoserver.platform.ServiceException;
import org.geoserver.wms.GetMapOutputFormat;
import org.geoserver.wms.MapLayerInfo;
import org.geoserver.wms.WMS;
import org.geoserver.wms.WMSMapContext;
import org.geoserver.wms.request.GetMapRequest;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapLayer;
import org.geotools.map.WMSMapLayer;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.vfny.geoserver.wms.WmsException;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class OpenLayersMapOutputFormat implements GetMapOutputFormat {
    /** A logger for this class. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.vfny.geoserver.responses.wms.map.openlayers");

    /**
     * The mime type for the response header
     */
    private static final String MIME_TYPE = "text/html";

    /**
     * The formats accepted in a GetMap request for this producer and stated in getcaps
     */
    private static final Set<String> OUTPUT_FORMATS = new HashSet<String>(Arrays.asList(
            "application/openlayers", "openlayers"));

    /**
     * Set of parameters that we can ignore, since they are not part of the OpenLayers WMS request
     */
    private static final Set<String> ignoredParameters;

    static {
        ignoredParameters = new HashSet<String>();
        ignoredParameters.add("REQUEST");
        ignoredParameters.add("TILED");
        ignoredParameters.add("BBOX");
        ignoredParameters.add("SERVICE");
        ignoredParameters.add("VERSION");
        ignoredParameters.add("FORMAT");
        ignoredParameters.add("WIDTH");
        ignoredParameters.add("HEIGHT");
        ignoredParameters.add("SRS");
    }

    /**
     * static freemaker configuration
     */
    private static Configuration cfg;

    static {
        cfg = new Configuration();
        cfg.setClassForTemplateLoading(OpenLayersMapOutputFormat.class, "");
        BeansWrapper bw = new BeansWrapper();
        bw.setExposureLevel(BeansWrapper.EXPOSE_PROPERTIES_ONLY);
        cfg.setObjectWrapper(bw);
    }

    /**
     * wms configuration
     */
    private WMS wms;

    public OpenLayersMapOutputFormat(WMS wms) {
        this.wms = wms;
    }

    /**
     * @see org.geoserver.wms.GetMapOutputFormat#getOutputFormatNames()
     */
    public Set<String> getOutputFormatNames() {
        return OUTPUT_FORMATS;
    }

    /**
     * @see org.geoserver.wms.GetMapOutputFormat#getMimeType()
     */
    public String getMimeType() {
        return MIME_TYPE;
    }

    /**
     * @see org.geoserver.wms.GetMapOutputFormat#produceMap(org.geoserver.wms.WMSMapContext)
     */
    public org.geoserver.wms.response.Map produceMap(WMSMapContext mapContext)
            throws ServiceException, IOException {
        try {
            // create the template
            Template template = cfg.getTemplate("OpenLayersMapTemplate.ftl");
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("context", mapContext);
            map.put("pureCoverage", hasOnlyCoverages(mapContext));
            map.put("styles", styleNames(mapContext));
            map.put("request", mapContext.getRequest());
            map.put("maxResolution", new Double(getMaxResolution(mapContext.getAreaOfInterest())));

            String baseUrl = mapContext.getRequest().getBaseUrl();
            map.put("baseUrl", canonicUrl(baseUrl));

            // TODO: replace service path with call to buildURL since it does this
            // same dance
            String servicePath = "wms";
            if (LocalLayer.get() != null) {
                servicePath = LocalLayer.get().getName() + "/" + servicePath;
            }
            if (LocalWorkspace.get() != null) {
                servicePath = LocalWorkspace.get().getName() + "/" + servicePath;
            }
            map.put("servicePath", servicePath);

            map.put("parameters", getLayerParameter(mapContext.getRequest().getRawKvp()));
            map.put("units", getOLUnits(mapContext.getRequest()));

            if (mapContext.getLayerCount() == 1) {
                map.put("layerName", mapContext.getLayer(0).getTitle());
            } else {
                map.put("layerName", "Geoserver layers");
            }

            template.setOutputEncoding("UTF-8");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            template.process(map, new OutputStreamWriter(out, Charset.forName("UTF-8")));
            byte[] mapContents = out.toByteArray();

            RawMap result = new RawMap(mapContext, mapContents, MIME_TYPE);
            return result;
        } catch (TemplateException e) {
            throw new WmsException(e);
        }
    }

    /**
     * Guesses if the map context is made only of coverage layers by looking at the wrapping feature
     * type. Ugly, if you come up with better means of doing so, fix it.
     * 
     * @param mapContext
     * @return
     */
    private boolean hasOnlyCoverages(WMSMapContext mapContext) {
        for (MapLayer layer : mapContext.getLayers()) {
            FeatureType schema = layer.getFeatureSource().getSchema();
            boolean grid = schema.getName().getLocalPart().equals("GridCoverage")
                    && schema.getDescriptor("geom") != null && schema.getDescriptor("grid") != null
                    && !(layer instanceof WMSMapLayer);
            if (!grid)
                return false;
        }
        return true;
    }

    private List<String> styleNames(WMSMapContext mapContext) {
        if (mapContext.getLayerCount() != 1 || mapContext.getRequest() == null)
            return Collections.emptyList();

        MapLayerInfo info = mapContext.getRequest().getLayers().get(0);
        return info.getOtherStyleNames();
    }

    /**
     * OL does support only a limited number of unit types, we have to try and return one of those,
     * otherwise the scale won't be shown. From the OL guide: possible values are "degrees" (or
     * "dd"), "m", "ft", "km", "mi", "inches".
     * 
     * @param request
     * @return
     */
    private String getOLUnits(GetMapRequest request) {
        CoordinateReferenceSystem crs = request.getCrs();
        // first rough approximation, meters for projected CRS, degrees for the
        // others
        String result = crs instanceof ProjectedCRS ? "m" : "degrees";
        try {
            String unit = crs.getCoordinateSystem().getAxis(0).getUnit().toString();
            // use the unicode escape sequence for the degree sign so its not
            // screwed up by different local encodings
            final String degreeSign = "\u00B0";
            if (degreeSign.equals(unit) || "degrees".equals(unit) || "dd".equals(unit))
                result = "degrees";
            else if ("m".equals(unit) || "meters".equals(unit))
                result = "m";
            else if ("km".equals(unit) || "kilometers".equals(unit))
                result = "mi";
            else if ("in".equals(unit) || "inches".equals(unit))
                result = "inches";
            else if ("ft".equals(unit) || "feets".equals(unit))
                result = "ft";
            else if ("mi".equals(unit) || "miles".equals(unit))
                result = "mi";
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error trying to determine unit of measure", e);
        }
        return result;
    }

    /**
     * Returns a list of maps with the name and value of each parameter that we have to forward to
     * OpenLayers. Forwarded parameters are all the provided ones, besides a short set contained in
     * {@link #ignoredParameters}.
     * 
     * 
     * 
     * @param rawKvp
     * @return
     */
    private List<Map<String, String>> getLayerParameter(Map<String, String> rawKvp) {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>(rawKvp.size());

        for (Map.Entry<String, String> en : rawKvp.entrySet()) {
            String paramName = en.getKey();

            if (ignoredParameters.contains(paramName.toUpperCase())) {
                continue;
            }

            // this won't work for multi-valued parameters, but we have none so
            // far (they are common just in HTML forms...)
            Map<String, String> map = new HashMap<String, String>();
            map.put("name", paramName);
            map.put("value", en.getValue());
            result.add(map);
        }

        return result;
    }

    /**
     * Makes sure the url does not end with "/", otherwise we would have URL lik
     * "http://localhost:8080/geoserver//wms?LAYERS=..." and Jetty 6.1 won't digest them...
     * 
     * @param baseUrl
     * @return
     */
    private String canonicUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        } else {
            return baseUrl;
        }
    }

    private double getMaxResolution(ReferencedEnvelope areaOfInterest) {
        double w = areaOfInterest.getWidth();
        double h = areaOfInterest.getHeight();

        return ((w > h) ? w : h) / 256;
    }

}