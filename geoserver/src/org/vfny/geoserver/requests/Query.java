/* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root 
 * application directory.
 */
package org.vfny.geoserver.requests;

import java.util.*;
import java.util.logging.Logger;
import org.geotools.filter.Filter;
import org.vfny.geoserver.config.FeatureTypeBean;

/**
 * Provides an internal, generic representation of a query component to a 
 * Feature request.  Note that Feature requests can contain multiple 
 * query components and that the 'version' inside the query component is 
 * different than the 'version' of the GetFeature request.
 *
 * @author Rob Hranac, TOPP
 * @version $version$
 */
public class Query {

    /** Standard logging instance for the class */
    private static final Logger LOGGER = 
        Logger.getLogger("org.vfny.geoserver.requests");
    
    /** The user-specified name for the query. */
    protected String handle = new String();
    
    // UNIMPLEMENTED - YOU CAN SET THIS BUT IT DOES NOTHING
    // NOTE THAT THIS IS FOR 'EVOLVING FEATURES' OR WHATEVER
    /** The version of the feature to request - current implementation 
        ignores entirely. */
    protected String version = new String();
    
    /** The feature type name requested. */
    protected String featureTypeName = new String();
    
    /** The property names requested */
    protected List propertyNames = new ArrayList();
    
    /** The filter for the query */
    protected Filter filter = null;
    
    /** Stores datbase configuration meta data for the query  */
    protected FeatureTypeBean featureType = null;
        

    /** Empty constructor. */ 
    public Query() {}
    
    
    /**
     * This method sets the configuration data for the query, based on the
     * feature type name.
     *
     */ 
    public void setDatastoreConfiguration() {
        this.featureType = new FeatureTypeBean(featureTypeName);
    }

    
    /** Returns the configuration data for the query datastore. */ 
    public FeatureTypeBean getDatastoreConfiguration() { return featureType; }
        
    /** Gets the requested property names as a vector. */ 
    public List getPropertyNames() { return propertyNames; }
    
    /**
     * Sets the feature type name requested by the query.
     * @param featureTypeName The feature type name of the query.
     */ 
    public void setFeatureTypeName(String featureTypeName) {
        this.featureTypeName = featureTypeName;
    }
    
    
    /** Gets the feature type name for this query. */ 
    public String getFeatureTypeName() { return this.featureTypeName; }
    
    /** Adds a requested property name to the query. */ 
    public void addPropertyName(String propertyName) { 
        propertyNames.add(propertyName);
    }

    /** Sets the user-defined 'handle' for the query. */ 
    public void setHandle (String handle) { this.handle = handle; }
    
    /** Gets the user-defined 'handle' for the query. */ 
    public String getHandle() { return this.handle; }
    
    
    /** Sets the 'version' of features to retrieve. */ 
    public void setVersion (String version) { this.version = version; }
    
    /** Gets the 'version' of features to retrieve. */ 
    public String getVersion() { return this.version; }
        
    /** Sets the filter for the query. */ 
    public void addFilter (Filter filter) { this.filter = filter; }    
    
    /** Passes the Post method to the Get method, with no modifications. */ 
    public Filter getFilter() { return this.filter; }
    
    
    /** Passes the Post method to the Get method, with no modifications. */ 
    public int getDatastoreType() { return 1; }
    
    /** Returns the bounding box for this request. */ 
    public FeatureTypeBean getMetadata() { return this.featureType; }
    

    /*************************************************************************
     * OVERRIDES OF toString AND equals METHODS.                             *
     *************************************************************************/
    public String toString() {
        StringBuffer returnString = new StringBuffer("\n  Query");
        returnString.append(" [" + handle + "]");
        returnString.append("\n   feature type: " + featureTypeName);
        if(filter != null) {
            returnString.append("\n   filter: " + filter.toString());
        }
        returnString.append("\n   properties: ");
        
        Iterator iterator = propertyNames.listIterator();
        while( iterator.hasNext()) {
            returnString.append( iterator.next().toString());
            if(iterator.hasNext()) {
                returnString.append(", ");
            }
        }
        return returnString.toString();
    }
    
    public boolean equals(Query query) {

        boolean isEqual = true;

        // check basic attributes for equality
        isEqual = query.getHandle().equals(handle) ?
            true && isEqual : false;
        LOGGER.finest("checked handle: " + isEqual);
        isEqual = query.getFeatureTypeName().equals(featureTypeName) ?
            true && isEqual : false;
        LOGGER.finest("checked feature type: " + isEqual + 
                      "; internal: " + query.getFeatureTypeName() +
                      "; external: " + featureTypeName);
        isEqual = query.getVersion().equals(version) ?
            true && isEqual : false;
        LOGGER.finest("checked version: " + isEqual);

        // check filter for equality, handling null case
        if( query.getFilter() != null) {
            LOGGER.finest("checking filter: " + filter);
            isEqual = query.getFilter().equals(filter) ?
                true && isEqual : false;
            LOGGER.finest("checked filter: " + isEqual);
        } else {
            isEqual = (filter == null) ?
                true && isEqual : false;
        }

        // check property names for equality, handling case where property
        //  lists are of different lengths
        LOGGER.finest("checking properties, internal: " + propertyNames.size()+
                      "; external: " + query.getPropertyNames().size());
        if( propertyNames.size() == query.getPropertyNames().size()) {
            Iterator i = propertyNames.listIterator();
            Iterator e = query.getPropertyNames().listIterator();
            while( i.hasNext()) {
                isEqual = i.next().equals(e.next()) ?
                    true && isEqual : false;
            }
        } else {
            isEqual = false;
        }
        return isEqual;
    }
    
}
