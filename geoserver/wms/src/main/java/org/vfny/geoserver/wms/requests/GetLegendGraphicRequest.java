/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.requests;

import org.geotools.feature.FeatureType;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.vfny.geoserver.wms.servlets.WMService;


/**
 * Holds the parsed parameters for a GetLegendGraphic WMS request.<p>The
 * GET parameters of the GetLegendGraphic operation are defined as follows
 * (from SLD 1.0 spec, ch.12):<br><pre><table>
 *  <tr><td><b>Parameter</b></td><td><b>Required</b></td><td><b>Description</b></td></tr>
 *  <tr><td>VERSION </td><td>Required </td><td>Version as required by OGC interfaces.</td></tr>
 *  <tr><td>REQUEST </td><td>Required </td><td>Value must be  GetLegendRequest . </td></tr>
 *  <tr><td>LAYER </td><td>Required </td><td>Layer for which to produce legend graphic. </td></tr>
 *  <tr><td>STYLE </td><td>Optional </td><td>Style of layer for which to produce legend graphic. If not present, the default style is selected. The style may be any valid style available for a layer, including non-SLD internally-defined styles.</td></tr>
 *  <tr><td>FEATURETYPE </td><td>Optional </td><td>Feature type for which to produce the legend graphic. This is not needed if the layer has only a single feature type. </td></tr>
 *  <tr><td>RULE </td><td>Optional </td><td>Rule of style to produce legend graphic for, if applicable. In the case that a style has multiple rules but no specific rule is selected, then the map server is obligated to produce a graphic that is representative of all of the rules of the style.</td></tr>
 *  <tr><td>SCALE </td><td>Optional </td><td>In the case that a RULE is not specified for a style, this parameter may assist the server in selecting a more appropriate representative graphic by eliminating internal rules that are outof- scope. This value is a standardized scale denominator, defined in Section 10.2</td></tr>
 *  <tr><td>SLD </td><td>Optional </td><td>This parameter specifies a reference to an external SLD document. It works in the same way as the SLD= parameter of the WMS GetMap operation. </td></tr>
 *  <tr><td>SLD_BODY </td><td>Optional </td><td>This parameter allows an SLD document to be included directly in an HTTP-GET request. It works in the same way as the SLD_BODY= parameter of the WMS GetMap operation.</td></tr>
 *  <tr><td>FORMAT </td><td>Required </td><td>This gives the MIME type of the file format in which to return the legend graphic. Allowed values are the same as for the FORMAT= parameter of the WMS GetMap request. </td></tr>
 *  <tr><td>WIDTH </td><td>Optional </td><td>This gives a hint for the width of the returned graphic in pixels. Vector-graphics can use this value as a hint for the level of detail to include. </td></tr>
 *  <tr><td>HEIGHT </td><td>Optional </td><td>This gives a hint for the height of the returned graphic in pixels. </td></tr>
 *  <tr><td>EXCEPTIONS </td><td>Optional </td><td>This gives the MIME type of the format in which to return exceptions. Allowed values are the same as for the EXCEPTIONS= parameter of the WMS GetMap request.</td></tr>
 *  </table> </pre></p>
 *  <p>The GetLegendGraphic operation itself is optional for an SLD-enabled
 * WMS. It provides a general mechanism for acquiring legend symbols, beyond
 * the LegendURL reference of WMS Capabilities. Servers supporting the
 * GetLegendGraphic call might code LegendURL references as GetLegendGraphic
 * for interface consistency. Vendorspecific parameters may be added to
 * GetLegendGraphic requests and all of the usual OGC-interface options and
 * rules apply. No XML-POST method for GetLegendGraphic is presently defined.</p>
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public class GetLegendGraphicRequest extends WMSRequest {
    /** DOCUMENT ME! */
    public static final String SLD_VERSION = "1.0.0";

    /**
     * default legend graphic width, in pixels, to apply if no WIDTH
     * parameter was passed
     */
    public static final int DEFAULT_WIDTH = 20;

    /**
     * default legend graphic height, in pixels, to apply if no WIDTH
     * parameter was passed
     */
    public static final int DEFAULT_HEIGHT = 20;

    /**
     * The default image format in which to produce a legend graphic.
     * Not really used when performing user requests, since FORMAT is a
     * mandatory parameter, but by now serves as a default for expressing
     * LegendURL layer attribute in GetCapabilities.
     */
    public static final String DEFAULT_FORMAT = "image/png";

    /** The featuretype of the requested LAYER */
    private FeatureType layer;

    /**
     * The Style object for styling the legend graphic, or layer's
     * default if not provided. This style can be aquired by evaluating the
     * STYLE parameter, which provides one of the  layer's named styles, the
     * SLD parameter, which provides a URL for an external SLD document, or
     * the SLD_BODY parameter, which provides the SLD body in the request
     * body.
     */
    private Style style;

    /**
     * should hold FEATURETYPE parameter value, though not used by now,
     * since GeoServer WMS still does not supports nested layers and layers
     * has only a single feature type. This should change in the future.
     */
    private String featureType;

    /** holds RULE parameter value, or <code>null</code> if not provided */
    private Rule rule;

    /**
     * holds the standarized scale denominator passed as the SCALE
     * parameter value, or <code>-1.0</code> if not provided
     */
    private double scale = -1d;

    /**
     * the mime type of the file format in which to return the legend
     * graphic, as requested by the FORMAT request parameter value.
     */
    private String format;

    /**
     * the width in pixels of the returned graphic, or
     * <code>DEFAULT_WIDTH</code> if not provided
     */
    private int width = DEFAULT_WIDTH;

    /**
     * the height in pixels of the returned graphic, or
     * <code>DEFAULT_HEIGHT</code> if not provided
     */
    private int height = DEFAULT_HEIGHT;

    /** mime type of the format in which to return exceptions information. */
    private String exceptionsFormat = GetMapRequest.SE_XML;

    /**
             * Creates a new GetLegendGraphicRequest object.
             * @param service The service that will handle the request
             */
    public GetLegendGraphicRequest(WMService service) {
        super("GetLegendGraphic", service);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getExceptionsFormat() {
        return exceptionsFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @param exceptionsFormat DOCUMENT ME!
     */
    public void setExceptionsFormat(String exceptionsFormat) {
        this.exceptionsFormat = exceptionsFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getFeatureType() {
        return featureType;
    }

    /**
     * DOCUMENT ME!
     *
     * @param featureType DOCUMENT ME!
     */
    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getFormat() {
        return format;
    }

    /**
     * DOCUMENT ME!
     *
     * @param format DOCUMENT ME!
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getHeight() {
        return height;
    }

    /**
     * DOCUMENT ME!
     *
     * @param height DOCUMENT ME!
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public FeatureType getLayer() {
        return layer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param layer DOCUMENT ME!
     */
    public void setLayer(FeatureType layer) {
        this.layer = layer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * DOCUMENT ME!
     *
     * @param rule DOCUMENT ME!
     */
    public void setRule(Rule rule) {
        this.rule = rule;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getScale() {
        return scale;
    }

    /**
     * DOCUMENT ME!
     *
     * @param scale DOCUMENT ME!
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Style getStyle() {
        return style;
    }

    /**
     * DOCUMENT ME!
     *
     * @param style DOCUMENT ME!
     */
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getWidth() {
        return width;
    }

    /**
     * DOCUMENT ME!
     *
     * @param width DOCUMENT ME!
     */
    public void setWidth(int width) {
        this.width = width;
    }
}
