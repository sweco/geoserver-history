/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wms.response;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.geoserver.wms.WMSMapContext;

/**
 * The MetatiledMapDecorationLayout class customizes the {MapDecorationLayout} to handle the special
 * case of metatiles; that is, maps that are rendered with the intent of being divided up into
 * smaller image segments.  Basically, it divided the map up into a grid and repeats the layout and
 * rendering process for each section of the grid.
 *
 * @author David Winslow <dwinslow@opengeo.org> 
 */
public class MetatiledMapDecorationLayout extends MapDecorationLayout {
    private int tileSize = 3;

    public MetatiledMapDecorationLayout() { super(); }

    public MetatiledMapDecorationLayout(int tileSize) {
        this.tileSize = tileSize;
    }

    /**
     * Paint all the Blocks in this layout.
     *
     * @param g2d the Graphics2D context in which the Blocks will be rendered
     * @param paintArea the drawable area
     * @param mapContext the WMSMapContext for the current map request
     *
     * @see {Block#paint}
     */
    public void paint(Graphics2D g2d, Rectangle paintArea, WMSMapContext mapContext) { 
        int width = paintArea.width / tileSize;
        int height = paintArea.height / tileSize;

        for (int i = 0; i < tileSize; i++) {
            for (int j = 0; j < tileSize; j++) {
                int x = paintArea.x + (paintArea.width * i / tileSize);
                int y = paintArea.y + (paintArea.height * j / tileSize);
                Rectangle tileArea = new Rectangle(x, y, width, height);
                super.paint(g2d, tileArea, mapContext);
            }
        }
    }
}