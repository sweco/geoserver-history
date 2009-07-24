/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.wps.kvp;

import java.util.Map;
import net.opengis.wps.GetCapabilitiesType;

/**
 * GetCapabilities KVP request reader
 *
 * @author Lucas Reed, Refractions Research Inc
 */
public class GetCapabilitiesKvpRequestReader extends WPSKvpRequestReader {
    public GetCapabilitiesKvpRequestReader() {
        super(GetCapabilitiesType.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object read(Object request, Map kvp, Map rawKvp) throws Exception {
        request = super.read(request, kvp, rawKvp);

        // Version arbitration could be done at this point

        return request;
    }
}