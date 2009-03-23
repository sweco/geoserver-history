/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.responses;

import org.vfny.geoserver.wms.WMSMapContext;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.File;
import java.util.Map;

/**
 * The Decoration class describes a set of overlays to be used to enhance a WMS response.
 * It maintains a collection of Decoration objects and the configuration associated with each, and
 * delegates the actual rendering operations to the decorations.
 *
 * @author David Winslow <dwinslow@opengeo.org> 
 */
public interface Decoration {
    /**
     * Load in configuration parameters from a map.  All subsequent paint operations should use the 
     * provided parameters.  Implementations do not need to respect multiple calls to this method.
     * @param options a Map<String,String> containing the configuration parameters
     */
    public void loadOptions(Map<String,String> options);

    public Dimension findOptimalSize(WMSMapContext mapContext);

    /**
     * Render the contents of this decoration onto the provided graphics object within the 
     * specified bounds.  The WMSMapContext object can be used to provide additional info about the 
     * map for context-sensitive decorations.
     *
     * @param g2d the Graphics2D object onto which the decoration should be drawn
     * @param paintArea the bounds within the graphics object where the decoration should be drawn
     * @param context the mapContext for the image being rendered
     * @throws InvalidStateException if loadOptions() has not been called yet
     */
    public void paint(Graphics2D g2d, Rectangle paintArea, WMSMapContext context) throws Exception;
}
