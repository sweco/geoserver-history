/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.wms.servlets;

import org.vfny.geoserver.Request;
import org.vfny.geoserver.Response;
import org.vfny.geoserver.ServiceException;
import org.vfny.geoserver.global.WMS;
import org.vfny.geoserver.util.requests.readers.KvpRequestReader;
import org.vfny.geoserver.util.requests.readers.XmlRequestReader;
import org.vfny.geoserver.wms.requests.GetMapKvpReader;
import org.vfny.geoserver.wms.requests.GetMapXmlReader;
import org.vfny.geoserver.wms.responses.GetMapResponse;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * WMS service wich returns request and response handlers to manage a GetMap
 * request
 *
 * @author Gabriel Rold?n
 * @version $Id$
 */
public class GetMap extends WMService {
    /**
     * Part of HTTP content type header.
     */
    public static final String URLENCODED = "application/x-www-form-urlencoded";

    /**
     * Creates a new GetMap object.
     *
     */
    public GetMap(WMS wms) {
        super("GetMap", wms);
    }

    protected GetMap(String id, WMS wms) {
        super(id, wms);
    }

    // TODO: check is this override adds any value compared to the superclass one,
    // remove otherwise
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        //If the post is of mime-type application/x-www-form-urlencoded
        //Then the get system can handle it. For all other requests the
        //post code must handle it.
        if (isURLEncoded(request)) {
            doGet(request, response);

            return;
        }

        //DJB: added post support
        Request serviceRequest = null;

        //        this.curRequest = request;
        if (!isServiceEnabled(request)) {
            sendDisabledServiceError(response);

            return;
        }

        //we need to construct an approriate serviceRequest from the GetMap XML POST.
        try {
            GetMapXmlReader xmlPostReader = new GetMapXmlReader(this);

            Reader xml = request.getReader();
            serviceRequest = xmlPostReader.read(xml, request);
        } catch (ServiceException se) {
            sendError(request, response, se);

            return;
        } catch (Throwable e) {
            sendError(request, response, e);

            return;
        }

        doService(request, response, serviceRequest);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Response getResponseHandler() {
        return new GetMapResponse(getWMS(), getApplicationContext());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws java.lang.UnsupportedOperationException DOCUMENT ME!
     */
    protected XmlRequestReader getXmlRequestReader() {
        return new GetMapXmlReader(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param params DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected KvpRequestReader getKvpReader(Map params) {
        Map layers = this.getWMS().getBaseMapLayers();
        Map styles = this.getWMS().getBaseMapStyles();

        GetMapKvpReader kvp = new GetMapKvpReader(params, this);

        // filter layers and styles if the user specified "layers=basemap"
        // This must happen after the kvp reader has been initially called
        if ((layers != null) && !layers.equals("")) {
            kvp.filterBaseMap(layers, styles);
        }

        return kvp;
    }

    /**
     * A method that decides if a request is a multipart request.
     * <p>
     * <a href="http://www.w3.org/TR/REC-html40/interact/forms.html#form-content-type">w3.org content type</a>
     * </p>
     *
     * @param req the servlet request
     * @return if this is multipart or not
     */
    public boolean isURLEncoded(HttpServletRequest req) {
        //Get the content type from the request
        String contentType = req.getContentType();

        //If there is no content type, then it is not multipart
        if (contentType == null) {
            return false;
        }

        //If it starts with multipart/ then it is multipart
        return contentType.toLowerCase().startsWith(URLENCODED);
    }
}
