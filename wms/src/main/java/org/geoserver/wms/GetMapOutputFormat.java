/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wms;

import java.io.IOException;
import java.util.Set;

import org.geoserver.ows.Response;
import org.geoserver.platform.ServiceException;
import org.geoserver.wms.response.Map;

/**
 * Produces and maps for an specific output format.
 * <p>
 * A GetMapOutputFormat implementation is meant to produce one and only one type of content, whose
 * normative MIME-Type is advertised through the {@link #getContentType()} method. Yet, the
 * {@link #getOutputFormatNames()} method is meant to advertise the map format in the capabilities
 * document and may or may not match the MIME-Type.
 * </p>
 * <p>
 * To incorporate a new producer specialized in a given output format, there must be a
 * {@code GetMapOutputFormat} registered in the Spring context that can provide instances of that
 * concrete implementation, as well as a {@link Response} Spring bean that can encode the produced
 * {@link Map}. Hence, it's counterpart {@code Response} implementation
 * {@link Response#canHandle(org.geoserver.platform.Operation) canHandle(Operation)} method must be
 * implemented in a consistent way with the output format's {@link #getMimeType()} and
 * {@link #getOutputFormatNames()}.
 * </p>
 * 
 * <p>
 * The methods defined in this interface respect the general parse request/produce response/get mime
 * type/write content workflow, so they should raise an exception if are called in the wrong order
 * (which is produceMap -> getContentType -> writeTo)
 * </p>
 * 
 * @author Gabriel Roldan
 * @author Simone Giannecchini, GeoSolutions
 * @version $Id$
 */
public interface GetMapOutputFormat {
    /**
     * Asks this map producer to create a map image for the passed {@linkPlain WMSMapContext}, which
     * contains enough information for doing such a process.
     * 
     * @param mapContext
     * 
     * 
     * @throws ServiceException
     *             something goes wrong
     */
    public Map produceMap(WMSMapContext mapContext) throws ServiceException, IOException;

    /**
     * Returns the list of content type aliases for this output format, that are the ones to be used
     * as Format elements in the GetCapabilities document.
     * <p>
     * Aliases are used to easy the task of writing a GetMap request, (for example, to type
     * &outputformat=svg instead of the full &outputformat=image/svg+xml)
     * </p>
     * 
     * @return the aliases a map of the content type this map producer creates content type can be
     *         asked by through a GetMap request.
     */
    public Set<String> getOutputFormatNames();

    /**
     * Returns
     * 
     * @return
     */
    public String getMimeType();
}