/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.xml.filter.v1_1;

import org.geoserver.wfs.WFSException;
import org.geotools.filter.v1_0.OGCPropertyNameTypeBinding;
import org.geotools.gml3.GML;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.PropertyName;
import org.vfny.geoserver.global.Data;
import org.xml.sax.helpers.NamespaceSupport;


/**
 * A binding for ogc:PropertyName which does a special case check for an empty
 * property name.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class PropertyNameTypeBinding extends OGCPropertyNameTypeBinding {
    /** the geoserver catalog */
    Data catalog;

    /** parser namespace mappings */
    NamespaceSupport namespaceSupport;

    public PropertyNameTypeBinding(FilterFactory filterFactory, NamespaceSupport namespaceSupport,
        Data catalog) {
        super(filterFactory);
        this.namespaceSupport = namespaceSupport;
        this.catalog = catalog;
    }

    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        PropertyName propertyName = (PropertyName) super.parse(instance, node, value);

        //JD: temporary hack, this should be carried out at evaluation time
        String name = propertyName.getPropertyName();

        if (name != null && name.matches("\\w+:\\w+")) {
            //namespace qualified name, ensure the prefix is valid
            String prefix = name.substring(0, name.indexOf(':'));
            String namespaceURI = namespaceSupport.getURI(prefix);

            //only accept if its an application schema namespace, or gml
            if (!GML.NAMESPACE.equals(namespaceURI)
                    && (catalog.getNameSpaceFromURI(namespaceURI) == null)) {
                throw new WFSException("Illegal attribute namespace: " + namespaceURI);
            }
        }

        return propertyName;
    }
}
