/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.ows.xml.v1_0;

import net.opengis.ows.AcceptFormatsType;
import net.opengis.ows.OwsFactory;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import javax.xml.namespace.QName;


/**
 * Binding object for the type http://www.opengis.net/ows:AcceptFormatsType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="AcceptFormatsType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Prioritized sequence of zero or more GetCapabilities operation response formats desired by client, with preferred formats listed first. Each response format shall be identified by its MIME type. See AcceptFormats parameter use subclause for more information. &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence&gt;
 *          &lt;element maxOccurs="unbounded" minOccurs="0" name="OutputFormat" type="ows:MimeType"/&gt;
 *      &lt;/sequence&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class AcceptFormatsTypeBinding extends AbstractComplexBinding {
    OwsFactory owsfactory;

    public AcceptFormatsTypeBinding(OwsFactory owsfactory) {
        this.owsfactory = owsfactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return OWS.ACCEPTFORMATSTYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return AcceptFormatsType.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        AcceptFormatsType acceptFormats = owsfactory
            .createAcceptFormatsType();
        acceptFormats.getOutputFormat()
                     .addAll(node.getChildValues("OutputFormat"));

        return acceptFormats;
    }
}
