/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.responses.wfs;

import org.vfny.geoserver.global.Service;
import org.vfny.geoserver.responses.CapabilitiesResponse;
import org.vfny.geoserver.responses.CapabilitiesResponseHandler;
import org.vfny.geoserver.responses.ResponseHandler;
import org.xml.sax.ContentHandler;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Rold�n
 * @version $Id: WFSCapabilitiesResponse.java,v 1.7 2004/09/05 17:18:31 cholmesny Exp $
 */
public class WFSCapabilitiesResponse extends CapabilitiesResponse {
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Service getGlobalService() {
        return request.getWFS();
    }

    /**
     * DOCUMENT ME!
     *
     * @param contentHandler DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected ResponseHandler getResponseHandler(ContentHandler contentHandler) {
        CapabilitiesResponseHandler cr = new WfsCapabilitiesResponseHandler(contentHandler,
                request);
        cr.setPrettyPrint(request.getWFS().getGeoServer().isVerbose());

        return cr;
    }

    /* (non-Javadoc)
     * @see org.vfny.geoserver.responses.Response#abort()
     */
    public void abort(Service s) {
        // nothing to undo
    }
}
