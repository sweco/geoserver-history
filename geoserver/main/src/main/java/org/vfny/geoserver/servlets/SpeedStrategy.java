/* Copyright (c) 2001, 2003 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.servlets;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;


/**
 * Fast and Dangeroud service strategy.<p>Will fail when a ServiceException
 * is encountered on writeTo, and will not tell the user about it!</p>
 *  <p>This is the worst case scenario, you are trading speed for danger by
 * using this ServiceStrategy.</p>
 *
 * @author jgarnett
 */
public class SpeedStrategy implements ServiceStrategy {
    /** DOCUMENT ME! */
    private OutputStream out = null;

    public String getId() {
        return "SPEED";
    }

    /**
     * Works against the real output stream provided by the response.<p>This
     * is dangerous of course, but fast and exciting.</p>
     *
     * @param response Response provided by doService
     *
     * @return An OutputStream that works against, the response output stream.
     *
     * @throws IOException If response output stream could not be aquired
     */
    public OutputStream getDestination(HttpServletResponse response)
        throws IOException {
        out = response.getOutputStream();
        out = new BufferedOutputStream(out);

        return out;
    }

    /**
     * Completes writing to Response.getOutputStream.
     *
     * @throws IOException If Response.getOutputStream not available.
     */
    public void flush() throws IOException {
        if (out != null) {
            out.flush();
        }
    }

    /* (non-Javadoc)
     * @see org.vfny.geoserver.servlets.AbstractService.ServiceStrategy#abort()
     */
    public void abort() {
        // out.close();
    }

    public Object clone() throws CloneNotSupportedException {
        return new SpeedStrategy();
    }
}
