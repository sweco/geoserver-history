/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.kvp;

import net.opengis.ows.v1_0_0.AcceptVersionsType;
import net.opengis.ows.v1_0_0.OWSFactory;
import net.opengis.wfs.GetCapabilitiesType;
import java.util.Map;


public class GetCapabilitiesKvpRequestReader extends WFSKvpRequestReader {
    public GetCapabilitiesKvpRequestReader() {
        super(GetCapabilitiesType.class);
    }

    public Object read(Object request, Map kvp) throws Exception {
        request = super.read(request, kvp);

        //set the version attribute on the request
        if (kvp.containsKey("version")) {
            AcceptVersionsType acceptVersions = OWSFactory.eINSTANCE
                .createAcceptVersionsType();
            acceptVersions.getVersion().add(kvp.get("version"));

            GetCapabilitiesType getCapabilities = (GetCapabilitiesType) request;
            getCapabilities.setAcceptVersions(acceptVersions);
        }

        return request;
    }
}
