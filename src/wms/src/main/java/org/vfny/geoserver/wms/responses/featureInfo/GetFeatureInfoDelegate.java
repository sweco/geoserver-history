/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses.featureInfo;

import java.util.List;

import org.geoserver.platform.ServiceException;
import org.geoserver.wms.MapLayerInfo;
import org.opengis.filter.Filter;
import org.vfny.geoserver.Request;
import org.vfny.geoserver.Response;
import org.vfny.geoserver.wms.WmsException;
import org.vfny.geoserver.wms.requests.GetFeatureInfoRequest;


/**
 * Base class for GetFeatureInfo delegates responsible of creating
 * GetFeatureInfo responses in different formats.
 *
 * <p>
 * Subclasses should implement one or more output formats, wich will be
 * returned in a list of mime type strings in
 * <code>getSupportedFormats</code>. For example, a subclass can be created to
 * write one of the following output formats:
 *
 * <ul>
 * <li>
 * text/plain
 * </li>
 * <li>
 * text/html
 * </li>
 * </ul>
 * </p>
 *
 * <p>
 * This abstract class takes care of executing the request in the sense of
 * taking the GetFeatureInfo request parameters such as query_layers, bbox, x,
 * y, etc., create the gt2 query objects for each featuretype and executing
 * it. This process leads to a set of FeatureResults objects and its metadata,
 * wich will be given to the <code>execute(FeatureTypeInfo[] ,
 * FeatureResults[])</code> method, that a subclass should implement as a
 * matter of setting up any resource/state it needs to later encoding.
 * </p>
 *
 * <p>
 * So, it should be enough to a subclass to implement the following methods in
 * order to produce the requested output format:
 *
 * <ul>
 * <li>
 * execute(FeatureTypeInfo[], FeatureResults[], int, int)
 * </li>
 * <li>
 * canProduce(String mapFormat)
 * </li>
 * <li>
 * getSupportedFormats()
 * </li>
 * <li>
 * writeTo(OutputStream)
 * </li>
 * </ul>
 * </p>
 *
 * @author Gabriel Roldan, Axios Engineering
 * @author Chris Holmes
 * @version $Id$
 */
public abstract class GetFeatureInfoDelegate implements Response {
    /** DOCUMENT ME!  */
    private GetFeatureInfoRequest request;

    /**
     * Creates a new GetMapDelegate object.
     */
    public GetFeatureInfoDelegate() {
    }

    /**
     * Executes a Request, which must be a GetMapRequest.  Any other will cause
     * a class cast exception.
     *
     * @param request A valid GetMapRequest.
     *
     * @throws ServiceException If the request can not be executed.
     */
    public void execute(Request request) throws ServiceException {
        execute((GetFeatureInfoRequest) request);
    }

    /**
     * Executes a GetFeatureInfo request.  Builds the proper objects from the
     * request names.
     *
     * @param request A valid GetMapRequest.
     *
     * @throws WmsException If anything goes wrong.
     */
    protected void execute(GetFeatureInfoRequest request)
        throws WmsException {
        this.request = request;

        //use the layer of the QUERY_LAYERS parameter, not the LAYERS one
        MapLayerInfo[] layers = request.getQueryLayers();

        List filterList = request.getGetMapRequest().getFilter();
        Filter[] filters;

        if (filterList != null && filterList.size() > 0) {
            filters = (Filter[]) filterList.toArray(new Filter[filterList.size()]);
        } else {
            filters = new Filter[layers.length];
        }

        int x = request.getXPixel();
        int y = request.getYPixel();
        execute(layers, filters, x, y);
    }

    /**
     * Execute method for concrete children to implement.  Each param is an
     * array in the order things should be processed.
     *
     * @param requestedLayers Array of config information of the FeatureTypes
     *        to be processed.
     * @param queries Matching array of layer definition filters
     * @param x the X coordinate in pixels where the identification must be
     *        done relative to the image dimensions
     * @param y the Y coordinate in pixels where the identification must be
     *        done relative to the image dimensions
     *
     * @throws WmsException For any problems executing.
     */
    protected abstract void execute(MapLayerInfo[] requestedLayers, Filter[] filters, int x,
        int y) throws WmsException;

    
    /**
     * Gets the map request.  Used by delegate children to find out more
     * information about the request.
     *
     * @return The request to be processed.
     */
    protected GetFeatureInfoRequest getRequest() {
        return this.request;
    }

    /**
     * Evaluates if this GetFeatureInfo producer can generate the map format
     * specified by <code>mapFormat</code>, where <code>mapFormat</code> is
     * the MIME type of the requested response.
     *
     * @param mapFormat the MIME type of the output map format requiered
     *
     * @return true if class can produce a map in the passed format
     */
    public boolean canProduce(String mapFormat) {
        return getSupportedFormats().contains(mapFormat);
    }

    /**
     * Gets A list of the formats this delegate supports.
     *
     * @return A list of strings of the formats supported.
     */
    public abstract List getSupportedFormats();
}
