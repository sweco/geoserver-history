package org.geoserver.wfs.xml.v1_0_0;


import org.geotools.xml.*;
import org.opengis.filter.Filter;

import net.opengis.wfs.PropertyType;
import net.opengis.wfs.UpdateElementType;
import net.opengis.wfs.WfsFactory;		

import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/wfs:UpdateElementType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="UpdateElementType"&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;xsd:element maxOccurs="unbounded" ref="wfs:Property"/&gt;
 *          &lt;xsd:element maxOccurs="1" minOccurs="0" ref="ogc:Filter"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                    The Filter element is used to constrain the scope
 *                    of the update operation to those features identified
 *                    by the filter.  Feature instances can be specified
 *                    explicitly and individually using the identifier of
 *                    each feature instance OR a set of features to be
 *                    operated on can be identified by specifying spatial
 *                    and non-spatial constraints in the filter.
 *                    If no filter is specified, then the update operation 
 *                    applies to all feature instances.
 *                 &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *      &lt;/xsd:sequence&gt;
 *      &lt;xsd:attribute name="handle" type="xsd:string" use="optional"/&gt;
 *      &lt;xsd:attribute name="typeName" type="xsd:QName" use="required"/&gt;
 *  &lt;/xsd:complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class UpdateElementTypeBinding extends AbstractComplexBinding {

	WfsFactory wfsfactory;		
	public UpdateElementTypeBinding( WfsFactory wfsfactory ) {
		this.wfsfactory = wfsfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return WFS.UPDATEELEMENTTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return UpdateElementType.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		UpdateElementType updateElement = wfsfactory.createUpdateElementType();
		
		//&lt;xsd:element maxOccurs="unbounded" ref="wfs:Property"/&gt;
		updateElement.getProperty().addAll( node.getChildValues( PropertyType.class ) );
		
		//&lt;xsd:element maxOccurs="1" minOccurs="0" ref="ogc:Filter"&gt;
		updateElement.setFilter( (Filter) node.getChildValue( Filter.class ) );
		
		//&lt;xsd:attribute name="handle" type="xsd:string" use="optional"/&gt;
		if ( node.hasAttribute( "handle" ) )
			updateElement.setHandle( (String) node.getAttributeValue( "handle" ) );
		
		//&lt;xsd:attribute name="typeName" type="xsd:QName" use="required"/&gt;
		updateElement.setTypeName( (QName) node.getAttributeValue( "typeName" ) ); 
		 
		return updateElement;
	}

}