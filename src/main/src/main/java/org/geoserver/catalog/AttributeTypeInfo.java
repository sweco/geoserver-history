/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.catalog;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.opengis.feature.type.AttributeDescriptor;

/**
 * An attribute exposed by a {@link FeatureTypeInfo}.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public interface AttributeTypeInfo extends Serializable {

    /**
     * Name of the attribute.
     */
    String getName();
    
    /**
     * Sets name of the attribute.
     */
    void setName( String name );
    
    /**
     * Minimum number of occurrences of the attribute.
     */
    int getMinOccurs();
    
    /**
     * Sets minimum number of occurrences of the attribute.
     */
    void setMinOccurs( int minOccurs );
    
    /**
     * Maximum number of occurrences of the attribute.
     */
    int getMaxOccurs();
    
    /**
     * Sets maximum number of occurrences of the attribute.
     */
    void setMaxOccurs( int maxOccurs );
    
    /**
     * Flag indicating if null is an acceptable value for the attribute.
     */
    boolean isNillable();
    
    /**
     * Sets flag indicating if null is an acceptable value for the attribute.
     */
    void setNillable( boolean nillable );
    
    /**
     * The feature type this attribute is part of.
     */
    FeatureTypeInfo getFeatureType();
    
    /**
     * Sets the feature type this attribute is part of.
     */
    void setFeatureType( FeatureTypeInfo featureType );
    
    /**
     * A persistent map of metadata.
     * <p>
     * Data in this map is intended to be persisted. Common case of use is to
     * have services associate various bits of data with a particular attribute.
     * An example might be its associated xml or gml type.
     * </p>
     * 
     */
    Map<String,Serializable> getMetadata();
    
    
    /**
     * The underlying attribute descriptor.
     * <p>
     * Note that this value is not persisted with other attributes, and could 
     * be <code>null</code>. 
     * </p>
     */
    AttributeDescriptor getAttribute() throws IOException;
    
    /**
     * Sets the underlying attribute descriptor.
     */
    void setAttribute( AttributeDescriptor attribute );
    
}