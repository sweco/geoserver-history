package org.geoserver.wfs.xml.v1_0_0;


import java.util.Map;

import org.geotools.xml.*;
import org.geotools.xs.bindings.XSQNameBinding;
import org.xml.sax.helpers.NamespaceSupport;

import net.opengis.wfs.PropertyType;
import net.opengis.wfs.WfsFactory;		

import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/wfs:PropertyType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="PropertyType"&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;xsd:element name="Name" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                    The Name element contains the name of a feature property
 *                    to be updated.
 *                 &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="Value"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                    The Value element contains the replacement value for the
 *                    named property.
 *                 &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *      &lt;/xsd:sequence&gt;
 *  &lt;/xsd:complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class PropertyTypeBinding extends AbstractComplexBinding {

	/**
	 * The wfs factory
	 */
	WfsFactory wfsfactory;
	/**
	 * Namespace support
	 */
	NamespaceSupport namespaceSupport;
	
	public PropertyTypeBinding( WfsFactory wfsfactory, NamespaceSupport namespaceSupport ) {
		this.wfsfactory = wfsfactory;
		this.namespaceSupport = namespaceSupport;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return WFS.PROPERTYTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return PropertyType.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		PropertyType property = wfsfactory.createPropertyType();
		
		//&lt;xsd:element name="Name" type="xsd:string"&gt;
		String name = (String) node.getChildValue( "Name" );
		
		//turn into qname
		QName qName = (QName) new XSQNameBinding( namespaceSupport ).parse( null, name );
		property.setName( qName );
		
		
		//&lt;xsd:element minOccurs="0" name="Value"&gt;
		if ( node.hasChild( "Value" ) ) {
			Map map = (Map) node.getChildValue( "Value");
			if ( !map.isEmpty() ) {
				//first check for some test
				if ( map.containsKey( null ) ) {
					property.setValue( map.get( null ) );
				}
				else {
					//perhaps some other value
					property.setValue( map.values().iterator().next() );
				}	
			}
			
			
		}
		
		return property;
	}

}