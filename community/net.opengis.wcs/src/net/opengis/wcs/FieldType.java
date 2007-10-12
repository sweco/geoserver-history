/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.wcs;

import net.opengis.wcs.ows.DescriptionType;
import net.opengis.wcs.ows.InterpolationMethodsType;
import net.opengis.wcs.ows.UnNamedDomainType;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Field Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Description of an individual field in a coverage range record. 
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wcs.FieldType#getIdentifier <em>Identifier</em>}</li>
 *   <li>{@link net.opengis.wcs.FieldType#getDefinition <em>Definition</em>}</li>
 *   <li>{@link net.opengis.wcs.FieldType#getNullValue <em>Null Value</em>}</li>
 *   <li>{@link net.opengis.wcs.FieldType#getInterpolationMethods <em>Interpolation Methods</em>}</li>
 *   <li>{@link net.opengis.wcs.FieldType#getAxis <em>Axis</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wcs.wcsPackage#getFieldType()
 * @model extendedMetaData="name='FieldType' kind='elementOnly'"
 * @generated
 */
public interface FieldType extends DescriptionType {
    /**
     * Returns the value of the '<em><b>Identifier</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * Identifier of this Field. These field identifiers shall be unique in one CoverageDescription. 
     * <!-- end-model-doc -->
     * @return the value of the '<em>Identifier</em>' attribute.
     * @see #setIdentifier(String)
     * @see net.opengis.wcs.wcsPackage#getFieldType_Identifier()
     * @model dataType="net.opengis.wcs.IdentifierType" required="true"
     *        extendedMetaData="kind='element' name='Identifier' namespace='##targetNamespace'"
     * @generated
     */
    String getIdentifier();

    /**
     * Sets the value of the '{@link net.opengis.wcs.FieldType#getIdentifier <em>Identifier</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Identifier</em>' attribute.
     * @see #getIdentifier()
     * @generated
     */
    void setIdentifier(String value);

    /**
     * Returns the value of the '<em><b>Definition</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * Further definition of this field, including meaning, units, etc. In this Definition, the AllowedValues should be used to encode the extent of possible values for this field, excluding the Null Value. If the range is not known, AnyValue should be used. 
     * <!-- end-model-doc -->
     * @return the value of the '<em>Definition</em>' containment reference.
     * @see #setDefinition(UnNamedDomainType)
     * @see net.opengis.wcs.wcsPackage#getFieldType_Definition()
     * @model containment="true" required="true"
     *        extendedMetaData="kind='element' name='Definition' namespace='##targetNamespace'"
     * @generated
     */
    UnNamedDomainType getDefinition();

    /**
     * Sets the value of the '{@link net.opengis.wcs.FieldType#getDefinition <em>Definition</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Definition</em>' containment reference.
     * @see #getDefinition()
     * @generated
     */
    void setDefinition(UnNamedDomainType value);

    /**
     * Returns the value of the '<em><b>Null Value</b></em>' attribute list.
     * The list contents are of type {@link java.lang.Object}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * Unordered list of the values used when valid Field values are not available for whatever reason. The coverage encoding itself may specify a fixed value for null (e.g. 99999 or N/A), but often the choice is up to the provider and must be communicated to the client outside the coverage itself. Each null value shall be encoded as a string. The optional codeSpace attribute can reference a definition of the reason why this value is null. 
     * <!-- end-model-doc -->
     * @return the value of the '<em>Null Value</em>' attribute list.
     * @see net.opengis.wcs.wcsPackage#getFieldType_NullValue()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.AnySimpleType"
     *        extendedMetaData="kind='element' name='NullValue' namespace='##targetNamespace'"
     * @generated
     */
    EList getNullValue();

    /**
     * Returns the value of the '<em><b>Interpolation Methods</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * Spatial interpolation method(s) that server can apply to this field. One of these interpolation methods shall be used when a GetCoverage operation request requires resampling. When the only interpolation method listed is none, clients may only retrieve coverages from this coverage in its native CRS at its native resolution. 
     * <!-- end-model-doc -->
     * @return the value of the '<em>Interpolation Methods</em>' containment reference.
     * @see #setInterpolationMethods(InterpolationMethodsType)
     * @see net.opengis.wcs.wcsPackage#getFieldType_InterpolationMethods()
     * @model containment="true" required="true"
     *        extendedMetaData="kind='element' name='InterpolationMethods' namespace='http://www.opengis.net/wcs/1.1/ows'"
     * @generated
     */
    InterpolationMethodsType getInterpolationMethods();

    /**
     * Sets the value of the '{@link net.opengis.wcs.FieldType#getInterpolationMethods <em>Interpolation Methods</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Interpolation Methods</em>' containment reference.
     * @see #getInterpolationMethods()
     * @generated
     */
    void setInterpolationMethods(InterpolationMethodsType value);

    /**
     * Returns the value of the '<em><b>Axis</b></em>' containment reference list.
     * The list contents are of type {@link net.opengis.wcs.AxisType}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * Unordered list of the axes in a vector field for which there are Field values. This list shall be included when this Field has a vector of values. Notice that the axes can be listed here in any order; however, the axis order listed here shall be used in the KVP encoding of a GetCoverage operation request (TBR). 
     * <!-- end-model-doc -->
     * @return the value of the '<em>Axis</em>' containment reference list.
     * @see net.opengis.wcs.wcsPackage#getFieldType_Axis()
     * @model type="net.opengis.wcs.AxisType" containment="true"
     *        extendedMetaData="kind='element' name='Axis' namespace='##targetNamespace'"
     * @generated
     */
    EList getAxis();

} // FieldType
