/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import net.opengis.wfs.AllSomeType;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.GetFeatureWithLockType;
import net.opengis.wfs.LockFeatureResponseType;
import net.opengis.wfs.LockFeatureType;
import net.opengis.wfs.LockType;
import net.opengis.wfs.WfsFactory;

import org.eclipse.emf.ecore.EObject;
import org.geoserver.data.GeoServerCatalog;
import org.geoserver.data.feature.AttributeTypeInfo;
import org.geoserver.data.feature.FeatureTypeInfo;
import org.geoserver.ows.ServiceException;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.crs.ForceCoordinateSystemFeatureResults;
import org.geotools.data.crs.ReprojectFeatureResults;
import org.geotools.feature.Feature;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.opengis.filter.expression.PropertyName;

/**
 * Web Feature Service GetFeature operation.
 * <p>
 * </p>
 * 
 * @author Rob Hranac, TOPP
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 * 
 * @version $Id$
 */
public class GetFeature {
	
    /** Standard logging instance for class */
    private static final Logger LOGGER = Logger.getLogger(
            "org.vfny.geoserver.requests");

    /** The catalog */
    protected GeoServerCatalog catalog;
    
    /** The wfs configuration */
    protected WFS wfs;
    
    /** filter factory */
    protected FilterFactory filterFactory;
   
    /**
     * Creates the GetFeature operation.
     * 
     */
    public GetFeature( WFS wfs, GeoServerCatalog catalog) {
		this.wfs = wfs;
		this.catalog = catalog;
    }
    
    /**
     * @return The reference to the GeoServer catalog.
     */
    public GeoServerCatalog getCatalog() {
		return catalog;
	}
    
    /**
     * @return The reference to the WFS configuration.
     */
    public WFS getWFS() {
		return wfs;
    }
    
    /**
     * Sets the filter factory to use to create filters.
     * 
     * @param filterFactory
     */
    public void setFilterFactory(FilterFactory filterFactory) {
		this.filterFactory = filterFactory;
	}
    
    /**
     * Initializes a getFeature operation by reading the outputFormat and 
     * looking up the feature producer.
     * 
     * @return A GetFeatureResults object.
     */
    protected GetFeatureResults init( GetFeatureType request ) throws WFSException {
    	
    	List queries = request.getQuery();
    	
    	if ( queries.isEmpty() ) {
    		throw new WFSException( "No query specified" );
		}
		
		if ( EMFUtils.isUnset( queries, "typeName") ) {
			String msg = "No feature types specified";
			throw new WFSException( msg );
		}
		
		GetFeatureResults results = new GetFeatureResults();
	    for ( Iterator q = queries.iterator(); q.hasNext(); ) {
	    	EObject query = (EObject) q.next();
    		results.addQuery( query( query ) );
	    }
	 
	    return results;
    }
    
    protected Query query( EObject q ) {
		Query query = new Query();
    	List typeName = (List) EMFUtils.get( q, "typeName" );	
		
        query.setTypeName( (QName) typeName.get( 0 ) );
        
        List propertyNames = (List) EMFUtils.get( q, "propertyName" );
        for (int i = 0; i < propertyNames.size(); i++) {
    		PropertyName propertyName = (PropertyName) propertyNames.get( i );
            query.addPropertyName( propertyName.getPropertyName() );
        }
        
        Filter filter = (Filter) EMFUtils.get( q, "filter" );
        if ( filter != null) {
            query.addFilter( filter );
        }

        return query;	
    }
    
    /**
     * Performs a getFeatures, or getFeaturesWithLock (using gt2 locking ).
     * 
     * <p>
     * The idea is to grab the FeatureResulsts during execute, and use them
     * during writeTo.
     * </p>
     *
     * @param request
     *
     * @throws ServiceException
     * @throws WfsException DOCUMENT ME!
     *
     * @task TODO: split this up a bit more?  Also get the proper namespace
     *       declrations and schema locations.  Right now we're back up to
     *       where we were with 1.0., as we can return two FeatureTypes in the
     *       same namespace.  CITE didn't check for two in different
     *       namespaces, and gml builder just couldn't deal.  Now we should be
     *       able to, we just need to get the reporting right, use the
     *       AllSameType function as  Describe does.
     */
    public GetFeatureResults getFeature( GetFeatureType request ) throws WFSException {
    	GetFeatureResults results = init( request );
        
		run( request, results );
		
		return results;
    }
    
    public GetFeatureResults getFeatureWithLock( GetFeatureWithLockType request ) throws WFSException {
		GetFeatureResults results = init( request );
		results.setLocking( true );
		
		try {
			run( request, results );	
		}
		catch( Exception e ) {
			//free locks
			if ( results.getLockId() != null ) {
				LockFeature lockFeature = new LockFeature( wfs, catalog );
				lockFeature.release( results.getLockId() );
			}
		
			throw new WFSException( e );
		}
		
		return results;
    }
    
    protected void run( GetFeatureType request, GetFeatureResults results ) throws WFSException {
    	
        // Optimization Idea
        //
        // We should be able to reduce this to a two pass opperations.
        //
        // Pass #1 execute
        // - Attempt to Locks Fids during the first pass
        // - Also collect Bounds information during the first pass
        //
        // Pass #2 writeTo
        // - Using the Bounds to describe our FeatureCollections
        // - Iterate through FeatureResults producing GML
        //
        // And allways remember to release locks if we are failing:
        // - if we fail to aquire all the locks we will need to fail and
        //   itterate through the the FeatureSources to release the locks
        //
        FeatureTypeInfo meta = null;
        
        Query query;
        
        if ( request.getMaxFeatures() == null) {
        	request.setMaxFeatures( BigInteger.valueOf( Integer.MAX_VALUE ) );
        }
        int maxFeatures = request.getMaxFeatures().intValue();
        
        Set lockedFids = new HashSet();
        Set lockFailedFids = new HashSet();

        FeatureSource source;
        Feature feature;
        String fid;
        FilterFactory filterFactory = FilterFactoryFinder.createFilterFactory();
        FidFilter fidFilter;
        int numberLocked;

        try {
            for (Iterator it = results.getQueries().iterator(); it.hasNext() && (maxFeatures > 0);) {
                query = (Query) it.next();
                
                meta = featureTypeInfo( query.getTypeName() );
                source = meta.featureSource();

                List attrs = meta.getAttributes();
                List propNames = query.getPropertyNames(); // REAL LIST: be careful here :)
                List attributeNames = meta.attributeNames();

                for (Iterator iter = propNames.iterator(); iter.hasNext();) {
                    String propName = (String) iter.next();

                    if (!attributeNames.contains(propName)) {
                        String mesg = "Requested property: " + propName
                            + " is " + "not available for "
                            + query.getTypeName() + ".  "
                            + "The possible propertyName values are: "
                            + attributeNames;
                        throw new WFSException( mesg );
                    }
                }

                if (propNames.size() != 0) {
                    Iterator ii = attrs.iterator();
                    List tmp = new LinkedList();

                    while (ii.hasNext()) {
                        AttributeTypeInfo ati = (AttributeTypeInfo) ii.next();

                        //String attName = (String) ii.next();
                        LOGGER.finer("checking to see if " + propNames
                            + " contains" + ati);

                        if (((ati.getMinOccurs() > 0)
                                && (ati.getMaxOccurs() != 0))
                                || propNames.contains(ati.getName())) {
                            tmp.add(ati.getName());
                        }
                    }

                    query.setPropertyNames(tmp);
                }

                // This doesn't seem to be working?
                // Run through features and record FeatureIDs
                // Lock FeatureIDs as required
                //}
                LOGGER.fine("Query is " + query + "\n To gt2: "
                    + query.toDataQuery(maxFeatures));

                //DJB: note if maxFeatures gets to 0 the while loop above takes care of this! (this is a subtle situation)
                
                org.geotools.data.Query gtQuery = query.toDataQuery( maxFeatures );
                FeatureResults features = source.getFeatures( gtQuery );
                if (it.hasNext()) //DJB: dont calculate feature count if you dont have to. The MaxFeatureReader will take care of the last iteration
                	maxFeatures -= features.getCount();

                //JD: reproject if neccessary
                if ( gtQuery.getCoordinateSystemReproject() != null ) {
                	try {
						features = new ReprojectFeatureResults( features, gtQuery.getCoordinateSystemReproject() );
					} 
                	catch ( Exception e ) {
                		String msg = "Unable to reproject features";
                		throw new WFSException( msg, e );
					} 
                }
                
                //JD: override crs if neccessary
                if ( gtQuery.getCoordinateSystem() != null ) {
                	try {
						features = new ForceCoordinateSystemFeatureResults( features, gtQuery.getCoordinateSystem() );
					} 
            		catch (Exception e) {
						String msg = "Unable to set coordinate system";
						throw new WFSException( msg, e );
					}
                }
                //GR: I don't know if the featuresults should be added here for later
                //encoding if it was a lock request. may be after ensuring the lock
                //succeed?
                results.addFeatures(meta, features);
                if ( request instanceof GetFeatureWithLockType ) {
                	GetFeatureWithLockType withLockRequest = (GetFeatureWithLockType) request;
                	
                	LockType lock = WfsFactory.eINSTANCE.createLockType();
                	lock.setFilter( query.getFilter() );
                	lock.setHandle( query.getHandle() );
                	lock.setTypeName( query.getTypeName() );
                	
                	LockFeatureType lockRequest = WfsFactory.eINSTANCE.createLockFeatureType();
                	lockRequest.setExpiry( withLockRequest.getExpiry() );
                	lockRequest.setHandle( withLockRequest.getHandle() );
                	lockRequest.setLockAction( AllSomeType.ALL_LITERAL );
                	lockRequest.getLock().add( lock );
                	
                	LockFeature lockFeature = new LockFeature( wfs, catalog );
                	lockFeature.setFilterFactory( filterFactory );
                	
                	LockFeatureResponseType response = lockFeature.lockFeature( lockRequest );
                	results.setLockId( response.getLockId() );
                }
                
                if ( results.isLocking() ) {
                	

                }
            }
            
        } 
        catch (IOException e) {
    		throw new WFSException( "problem with FeatureResults", e, request.getHandle() );
        } 
       
    }
    
    FeatureTypeInfo featureTypeInfo( QName name ) throws WFSException, IOException {
    	
    	FeatureTypeInfo meta = 
    		catalog.featureType( name.getPrefix(), name.getLocalPart() );
    	
		if ( meta == null ) {
    		String msg = "Could not locate " + name + " in catalog.";
    		throw new WFSException( msg );
        }
    		
        return meta;
    }
    
}
