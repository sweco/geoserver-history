/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.xml.v1_1_0;

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;


public class WFSSchemaLocationResolver implements XSDSchemaLocationResolver {
    public String resolveSchemaLocation(XSDSchema xsdSchema, String namespaceURI,
        String schemaLocationURI) {
        if (schemaLocationURI == null) {
            return null;
        }

        //if no namespace given, assume default for the current schema
        if (((namespaceURI == null) || "".equals(namespaceURI)) && (xsdSchema != null)) {
            namespaceURI = xsdSchema.getTargetNamespace();
        }

        if ("http://www.opengis.net/wfs".equals(namespaceURI) && (schemaLocationURI != null)) {
            if (schemaLocationURI.endsWith("wfs.xsd")) {
                return getClass().getResource("wfs.xsd").toString();
            }
        }

        return null;
    }
}
