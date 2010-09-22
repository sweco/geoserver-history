/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.opengis.wfs.FeatureCollectionType;

import org.geoserver.kml.KMLReflector;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.ServiceException;
import org.geoserver.sld.GetStyles;
import org.geoserver.sld.GetStylesRequest;
import org.geoserver.wms.response.DescribeLayerTransformer;
import org.geoserver.wms.response.GetCapabilitiesTransformer;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.geotools.styling.StyledLayerDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.vividsolutions.jts.geom.Envelope;

public class DefaultWebMapService implements WebMapService,
        ApplicationContextAware, InitializingBean, DisposableBean {
    /**
     * default for 'format' parameter.
     */
    public static String FORMAT = "image/png";

    /**
     * default for 'styles' parameter.
     */
    public static List STYLES = Collections.EMPTY_LIST;

    /**
     * longest side for the preview
     */
    public static int MAX_SIDE = 512;

    /**
     * minimum height to have a decent looking OL preview
     */
    public static int MIN_OL_HEIGHT = 330;

    /**
     * default for 'srs' parameter.
     */
    public static String SRS = "EPSG:4326";

    /**
     * default for 'transparent' parameter.
     */
    public static Boolean TRANSPARENT = Boolean.TRUE;

    /**
     * default for 'transparent' parameter.
     */
    public static ExecutorService RENDERING_POOL;
    
    /**
     * default for 'bbox' paramter
     */
    public static ReferencedEnvelope BBOX = new ReferencedEnvelope(
            new Envelope(-180, 180, -90, 90), DefaultGeographicCRS.WGS84);

    /**
     * wms configuration
     */
    WMS wms;

    /**
     * Application context
     */
    ApplicationContext context;

    /**
     * Temporary field that handles the usage of the line width optimization code
     */
    private static Boolean OPTIMIZE_LINE_WIDTH = null;

    /**
     * Temporary field that handles the choice of renderer to be used
     */
    private static Boolean USE_SHAPEFILE_RENDERER = null;

    /**
     * Max number of rule filters to be used against the data source
     */
    public static Integer MAX_FILTER_RULES = null;

    public DefaultWebMapService(WMS wms) {
        this.wms = wms;
    }

    /**
     * @see WebMapService#getServiceInfo()
     */
    public WMSInfo getServiceInfo() {
        return wms.getServiceInfo();
    }

    /**
     * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;

        // first time initialization of line width optimization flag
        if (OPTIMIZE_LINE_WIDTH == null) {
            String enabled = GeoServerExtensions.getProperty("OPTIMIZE_LINE_WIDTH", context);
            // default to true, but allow switching off
            if (enabled == null)
                OPTIMIZE_LINE_WIDTH = false;
            else
                OPTIMIZE_LINE_WIDTH = Boolean.valueOf(enabled);
        }

        // initialization of the renderer choice flag
        if (USE_SHAPEFILE_RENDERER == null) {
            String enabled = GeoServerExtensions.getProperty("USE_SHAPEFILE_RENDERER", context);
            // default to true, but allow switching on
            if (enabled == null)
                USE_SHAPEFILE_RENDERER = false;
            else
                USE_SHAPEFILE_RENDERER = Boolean.valueOf(enabled);
        }

        // initialization of the renderer choice flag
        if (MAX_FILTER_RULES == null) {
            String rules = GeoServerExtensions.getProperty("MAX_FILTER_RULES", context);
            // default to true, but allow switching off
            if (rules == null)
                MAX_FILTER_RULES = 20;
            else
                MAX_FILTER_RULES = Integer.valueOf(rules);
        }
    }

    /**
     * Checks wheter the line width optimization is enabled, or not (defaults to true unless the
     * user sets the OPTIMIZE_LINE_WIDTH property to false)
     * 
     * @return
     */
    public static boolean isLineWidthOptimizationEnabled() {
        return OPTIMIZE_LINE_WIDTH;
    }

    /**
     * Checks wheter the shapefile renderer is enabled, or not (defaults to false unless the user
     * sets the USE_STREAMING_RENDERER property to true)
     * 
     * @return
     */
    public static boolean useShapefileRenderer() {
        return USE_SHAPEFILE_RENDERER;
    }

    /**
     * If true (default) use the sld rule filters to compose the query to the DB, otherwise don't
     * and get down only with the bbox and eventual definition filter)
     * 
     * @return
     */
    public static int getMaxFilterRules() {
        return MAX_FILTER_RULES;
    }

    /**
     * @see WebMapService#getCapabilities(GetCapabilitiesRequest)
     */
    public GetCapabilitiesTransformer getCapabilities(GetCapabilitiesRequest request) {
        GetCapabilities capabilities = (GetCapabilities) context.getBean("wmsGetCapabilities");

        return capabilities.run(request);
    }

    /**
     * @see WebMapService#capabilities(GetCapabilitiesRequest)
     */
    public GetCapabilitiesTransformer capabilities(GetCapabilitiesRequest request) {
        return getCapabilities(request);
    }

    /**
     * @see WebMapService#describeLayer(DescribeLayerRequest)
     */
    public DescribeLayerTransformer describeLayer(DescribeLayerRequest request) {
        DescribeLayer describeLayer = (DescribeLayer) context.getBean("wmsDescribeLayer");

        return describeLayer.run(request);
    }

    /**
     * @see WebMapService#getMap(GetMapRequest)
     */
    public Map getMap(GetMapRequest request) {
        GetMap getMap = (GetMap) context.getBean("wmsGetMap");

        return getMap.run(request);
    }

    /**
     * @see WebMapService#map(GetMapRequest)
     */
    public Map map(GetMapRequest request) {
        return getMap(request);
    }

    /**
     * @see WebMapService#getFeatureInfo(GetFeatureInfoRequest)
     */
    public FeatureCollectionType getFeatureInfo(final GetFeatureInfoRequest request) {
        GetFeatureInfo getFeatureInfo = (GetFeatureInfo) context.getBean("wmsGetFeatureInfo");

        return getFeatureInfo.run(request);
    }

    /**
     * @see WebMapService#getLegendGraphic(GetLegendGraphicRequest)
     */
    public LegendGraphic getLegendGraphic(GetLegendGraphicRequest request) {
        GetLegendGraphic getLegendGraphic = (GetLegendGraphic) context
                .getBean("wmsGetLegendGraphic");

        return getLegendGraphic.run(request);
    }

    public Map kml(GetMapRequest getMap) {
        try {
            return KMLReflector.doWms(getMap, this, wms);
            // return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see WebMapService#reflect(GetMapRequest)
     */
    public Map reflect(GetMapRequest request) {
        return getMapReflect(request);
    }

    /**
     * @see org.geoserver.wms.WebMapService#getStyles(org.geoserver.sld.GetStylesRequest)
     */
    public StyledLayerDescriptor getStyles(GetStylesRequest request) {

        GetStyles getStyles = (GetStyles) context.getBean("wmsGetStyles");

        return (StyledLayerDescriptor) getStyles.run(request);

    }

    /**
     * s
     * 
     * @see WebMapService#getMapReflect(GetMapRequest)
     */
    public Map getMapReflect(GetMapRequest request) {

        GetMapRequest getMap = autoSetMissingProperties(request);

        return getMap(getMap);
    }

    public static GetMapRequest autoSetMissingProperties(GetMapRequest getMap) {
        // set the defaults
        if (getMap.getFormat() == null) {
            getMap.setFormat(FORMAT);
        }

        if ((getMap.getStyles() == null) || getMap.getStyles().isEmpty()) {
            // set styles to be the defaults for the specified layers
            // TODO: should this be part of core WMS logic? is so lets throw
            // this
            // into the GetMapKvpRequestReader
            if ((getMap.getLayers() != null) && (getMap.getLayers().size() > 0)) {
                ArrayList styles = new ArrayList(getMap.getLayers().size());

                for (int i = 0; i < getMap.getLayers().size(); i++) {
                    styles.add(getMap.getLayers().get(i).getDefaultStyle());
                }

                getMap.setStyles(styles);
            } else {
                getMap.setStyles(STYLES);
            }
        }

        // auto-magic missing info configuration
        autoSetBoundsAndSize(getMap);

        return getMap;
    }

    /**
     * This method tries to automatically determine SRS, bounding box and output size based on the
     * layers provided by the user and any other parameters.
     * 
     * If bounds are not specified by the user, they are automatically se to the union of the bounds
     * of all layers.
     * 
     * The size of the output image defaults to 512 pixels, the height is automatically determined
     * based on the width to height ratio of the requested layers. This is also true if either
     * height or width are specified by the user. If both height and width are specified by the
     * user, the automatically determined bounding box will be adjusted to fit inside these bounds.
     * 
     * General idea 1) Figure out whether SRS has been specified, fall back to EPSG:4326 2)
     * Determine whether all requested layers use the same SRS, - if so, try to do bounding box
     * calculations in native coordinates 3) Aggregate the bounding boxes (in EPSG:4326 or native)
     * 4a) If bounding box has been specified, adjust height of image to match 4b) If bounding box
     * has not been specified, but height has, adjust bounding box
     */
    public static void autoSetBoundsAndSize(GetMapRequest getMap) {
        // Get the layers
        List<MapLayerInfo> layers = getMap.getLayers();

        /** 1) Check what SRS has been requested */
        String reqSRS = getMap.getSRS();

        // if none, try to determine which SRS to use
        // and keep track of whether we can use native all the way
        boolean useNativeBounds = true;
        if (reqSRS == null) {
            reqSRS = guessCommonSRS(layers);
            forceSRS(getMap, reqSRS);
        }

        /** 2) Compare requested SRS */
        for (int i = 0; useNativeBounds && i < layers.size(); i++) {
            if (layers.get(i) != null) {
                String layerSRS = layers.get(i).getSRS();
                useNativeBounds = reqSRS.equalsIgnoreCase(layerSRS)
                        && layers.get(i).getResource().getNativeBoundingBox() != null;
            } else {
                useNativeBounds = false;
            }
        }

        CoordinateReferenceSystem reqCRS;
        try {
            reqCRS = CRS.decode(reqSRS);
        } catch (Exception e) {
            throw new ServiceException(e);
        }

        // Ready to determine the bounds based on the layers, if not specified
        Envelope aggregateBbox = getMap.getBbox();
        boolean specifiedBbox = true;

        // If bbox is not specified by request
        if (aggregateBbox == null) {
            specifiedBbox = false;

            // Get the bounding box from the layers
            for (int i = 0; i < layers.size(); i++) {
                MapLayerInfo layerInfo = layers.get(i);
                ReferencedEnvelope curbbox;
                try {
                    curbbox = layerInfo.getLatLongBoundingBox();
                    if (useNativeBounds) {
                        ReferencedEnvelope nativeBbox = layerInfo.getBoundingBox();
                        if (nativeBbox == null) {
                            try {
                                CoordinateReferenceSystem nativeCrs = layerInfo
                                        .getCoordinateReferenceSystem();
                                nativeBbox = curbbox.transform(nativeCrs, true);
                            } catch (Exception e) {
                                throw new ServiceException(
                                        "Best effort native bbox computation failed", e);
                            }
                        }
                        curbbox = nativeBbox;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (aggregateBbox != null) {
                    aggregateBbox.expandToInclude(curbbox);
                } else {
                    aggregateBbox = curbbox;
                }
            }

            ReferencedEnvelope ref = null;
            // Reproject back to requested SRS if we have to
            if (!useNativeBounds && !reqSRS.equalsIgnoreCase(SRS)) {
                try {
                    ref = new ReferencedEnvelope(aggregateBbox, CRS.decode("EPSG:4326"));
                    aggregateBbox = ref.transform(reqCRS, true);
                } catch (ProjectionException pe) {
                    ref.expandBy(-1 * ref.getWidth() / 50, -1 * ref.getHeight() / 50);
                    try {
                        aggregateBbox = ref.transform(reqCRS, true);
                    } catch (FactoryException e) {
                        e.printStackTrace();
                    } catch (TransformException e) {
                        e.printStackTrace();
                    }
                    // And again...
                } catch (NoSuchAuthorityCodeException e) {
                    e.printStackTrace();
                } catch (TransformException e) {
                    e.printStackTrace();
                } catch (FactoryException e) {
                    e.printStackTrace();
                }
            }
        }

        // Just in case
        if (aggregateBbox == null) {
            forceSRS(getMap, DefaultWebMapService.SRS);
            aggregateBbox = DefaultWebMapService.BBOX;
        }

        // Start the processing of adjust either the bounding box
        // or the pixel height / width

        double bbheight = aggregateBbox.getHeight();
        double bbwidth = aggregateBbox.getWidth();
        double bbratio = bbwidth / bbheight;

        double mheight = getMap.getHeight();
        double mwidth = getMap.getWidth();

        if (mheight > 0.5 && mwidth > 0.5 && specifiedBbox) {
            // This person really doesnt want our help,
            // we'll warp it any way they like it...
        } else {
            if (mheight > 0.5 && mwidth > 0.5) {
                // Fully specified, need to adjust bbox
                double mratio = mwidth / mheight;
                // Adjust bounds to be less than ideal to meet spec
                if (bbratio > mratio) {
                    // Too wide, need to increase height of bb
                    double diff = ((bbwidth / mratio) - bbheight) / 2;
                    aggregateBbox.expandBy(0, diff);
                } else {
                    // Too tall, need to increase width of bb
                    double diff = ((bbheight * mratio) - bbwidth) / 2;
                    aggregateBbox.expandBy(diff, 0);
                }

                adjustBounds(reqSRS, aggregateBbox);

            } else if (mheight > 0.5) {
                mwidth = bbratio * mheight;
            } else {
                if (mwidth > 0.5) {
                    mheight = (mwidth / bbratio >= 1) ? mwidth / bbratio : 1;
                } else {
                    if (bbratio > 1) {
                        mwidth = MAX_SIDE;
                        mheight = (mwidth / bbratio >= 1) ? mwidth / bbratio : 1;
                    } else {
                        mheight = MAX_SIDE;
                        mwidth = (mheight * bbratio >= 1) ? mheight * bbratio : 1;
                    }

                    // make sure OL output height is sufficient to show the OL scale bar fully
                    if (mheight < MIN_OL_HEIGHT
                            && ("application/openlayers".equalsIgnoreCase(getMap.getFormat()) || "openlayers"
                                    .equalsIgnoreCase(getMap.getFormat()))) {
                        mheight = MIN_OL_HEIGHT;
                        mwidth = (mheight * bbratio >= 1) ? mheight * bbratio : 1;
                    }

                }

            }

            // Actually set the bounding box and size of image
            getMap.setBbox(aggregateBbox);
            getMap.setWidth((int) mwidth);
            getMap.setHeight((int) mheight);
        }
    }

    private static String guessCommonSRS(List<MapLayerInfo> layers) {
        String SRS = null;
        for (MapLayerInfo layer : layers) {
            String layerSRS = layer.getSRS();
            if (SRS == null) {
                SRS = layerSRS.toUpperCase();
            } else if (!SRS.equals(layerSRS)) {
                // layers with mixed native SRS, let's just use the default
                return DefaultWebMapService.SRS;
            }
        }
        if (SRS == null) {
            return DefaultWebMapService.SRS;
        }
        return SRS;
    }

    private static void forceSRS(GetMapRequest getMap, String srs) {
        getMap.setSRS(srs);

        try {
            getMap.setCrs(CRS.decode(srs));
        } catch (NoSuchAuthorityCodeException e) {
            e.printStackTrace();
        } catch (FactoryException e) {
            e.printStackTrace();
        }
    }

    /**
     * This adjusts the bounds by zooming out 2%, but also ensuring that the maximum bounds do not
     * exceed the world bounding box
     * 
     * This only applies if the SRS is EPSG:4326 or EPSG:900913
     * 
     * @param reqSRS
     *            the SRS
     * @param bbox
     *            the current bounding box
     * @return the adjusted bounding box
     */
    private static Envelope adjustBounds(String reqSRS, Envelope bbox) {
        if (reqSRS.equalsIgnoreCase("EPSG:4326")) {
            bbox.expandBy(bbox.getWidth() / 100, bbox.getHeight() / 100);
            Envelope maxEnv = new Envelope(-180.0, -90.0, 180.0, 90.0);
            return bbox.intersection(maxEnv);

        } else if (reqSRS.equalsIgnoreCase("EPSG:900913")) {
            bbox.expandBy(bbox.getWidth() / 100, bbox.getHeight() / 100);
            Envelope maxEnv = new Envelope(-20037508.33, -20037508.33, 20037508.33, 20037508.33);
            return bbox.intersection(maxEnv);
        }
        return bbox;
    }
    
    /**
     * Returns a app wide cached rendering pool that can be used for parallelized rendering
     * @return
     */
    public static ExecutorService getRenderingPool() {
        return RENDERING_POOL;
    }

    public void destroy() throws Exception {
        if(RENDERING_POOL != null) {
            RENDERING_POOL.shutdown();
            RENDERING_POOL.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    public void afterPropertiesSet() throws Exception {
        RENDERING_POOL = Executors.newCachedThreadPool();
    }
}
