/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.decorators;

import static org.geoserver.security.decorators.SecurityUtils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geoserver.security.Response;
import org.geoserver.security.SecureCatalogImpl;
import org.geoserver.security.WrapperPolicy;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureLocking;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureLocking;
import org.geotools.data.simple.SimpleFeatureStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

/**
 * Simple version of SecuredFeatureStore
 * 
 * @author Andrea Aime - GeoSolutions
 */
public class SecuredSimpleFeatureLocking extends
        SecuredFeatureLocking<SimpleFeatureType, SimpleFeature> implements SimpleFeatureLocking {

    protected SecuredSimpleFeatureLocking(FeatureLocking delegate, WrapperPolicy policy) {
        super(delegate, policy);
    }

    @Override
    public SimpleFeatureCollection getFeatures() throws IOException {
        return DataUtilities.simple(super.getFeatures());
    }

    @Override
    public SimpleFeatureCollection getFeatures(Filter filter) throws IOException {
        return DataUtilities.simple(super.getFeatures(filter));
    }

    @Override
    public SimpleFeatureCollection getFeatures(Query query) throws IOException {
        return DataUtilities.simple(super.getFeatures(query));
    }

    public void modifyFeatures(String name, Object attributeValue, Filter filter)
            throws IOException {
        modifyFeatures(new String[] { name }, new Object[] { attributeValue }, filter);

    }

    public void modifyFeatures(String[] names, Object[] values, Filter filter) throws IOException {
        // are we limiting anything?
        Query writeQuery = getWriteQuery(policy);
        if (writeQuery == Query.ALL) {
            ((SimpleFeatureStore) storeDelegate).modifyFeatures(names, values, filter);
        } else if (writeQuery.getFilter() == Filter.EXCLUDE
                || writeQuery.getPropertyNames() == Query.NO_NAMES) {
            throw unsupportedOperation();
        }

        // get the mixed filter
        final Query local = new Query(null, filter);
        Query mixed = DataUtilities.mixQueries(local, writeQuery, null);

        if (writeQuery.getPropertyNames() == Query.ALL_NAMES) {
            // it was just a matter of filtering.
            ((SimpleFeatureStore) storeDelegate).modifyFeatures(names, values, mixed.getFilter());
        } else {
            // get the writable attribute set
            Set<String> queryNames = new HashSet<String>(Arrays.asList(writeQuery
                    .getPropertyNames()));

            // check the update fields
            for (int i = 0; i < names.length; i++) {
                if (!queryNames.contains(names[i])) {
                    String typeName = getSchema().getName().getLocalPart();
                    if (policy.getResponse() == org.geoserver.security.Response.CHALLENGE) {
                        throw SecureCatalogImpl.unauthorizedAccess(typeName);
                    } else {
                        throw new UnsupportedOperationException(
                                "Trying to write on the write protected attribute " + names[i]);
                    }
                }
            }

            ((SimpleFeatureStore) storeDelegate).modifyFeatures(names, values, mixed.getFilter());
        }

    }

}
