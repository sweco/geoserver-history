package org.vfny.geoserver.wms.responses.map.kml;

/**
 * Copy of the exception class defined in the SVG producer.
 * @TODO move up the package hiarachy and use single version from KML and SVG
 */
public class AbortedException extends Exception {
    public AbortedException(String msg) {
        super(msg);
    }
}
