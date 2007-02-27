/* Copyright (c) 2001, 2003 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.ows.xml.v1_0;

import net.opengis.ows.OwsFactory;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import javax.xml.namespace.QName;


/**
 * Binding object for the type http://www.opengis.net/ows:OnlineResourceType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="OnlineResourceType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Reference to on-line resource from which data can be obtained. &lt;/documentation&gt;
 *          &lt;documentation&gt;For OWS use in the service metadata document, the CI_OnlineResource class was XML encoded as the attributeGroup "xlink:simpleLink", as used in GML. &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;attributeGroup ref="xlink:simpleLink"/&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class OnlineResourceTypeBinding extends AbstractComplexBinding {
    OwsFactory owsfactory;

    public OnlineResourceTypeBinding(OwsFactory owsfactory) {
        this.owsfactory = owsfactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return OWS.ONLINERESOURCETYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return null;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        //TODO: implement
        return null;
    }
}
