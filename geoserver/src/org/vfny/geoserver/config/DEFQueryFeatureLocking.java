/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.config;

import com.vividsolutions.jts.geom.*;
import org.geotools.data.*;
import org.geotools.data.postgis.*;
import org.geotools.factory.*;
import org.geotools.feature.*;
import org.geotools.filter.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;


/**
 * Support FeatureSource decorator for FeatureTypeConfig that takes care of
 * mapping the FeatureTypeConfig's FeatureSource with the schema and
 * definition query configured for it.
 *
 * @author Gabriel Rold�n
 * @version $Id: DEFQueryFeatureLocking.java,v 1.4 2004/01/06 01:39:20 cholmesny Exp $
 */
public class DEFQueryFeatureLocking implements FeatureLocking {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(
            "org.vfny.geoserver.config");
    private FeatureSource source;
    private FeatureType schema;
    private Filter definitionQuery;
    private List mandatoryAtts;

    /**
     * Creates a new DEFQueryFeatureLocking object.
     *
     * @param source DOCUMENT ME!
     * @param schema DOCUMENT ME!
     * @param definitionQuery DOCUMENT ME!
     * @param mandatoryAtts DOCUMENT ME!
     */
    public DEFQueryFeatureLocking(FeatureSource source, FeatureType schema,
        Filter definitionQuery, List mandatoryAtts) {
        this.source = source;
        this.schema = schema;
        this.definitionQuery = definitionQuery;
        this.mandatoryAtts = mandatoryAtts;
    }

    /**
     * takes a query and adapts it to match de definitionQuery filter
     * configured for a feature type
     *
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    private Query makeDefinitionQuery(Query query) throws IOException {
        if (query == Query.ALL) {
            return query;
        }

        try {
            String handle = query.getHandle();
            int maxFeatures = query.getMaxFeatures();
            String typeName = query.getTypeName();
            String[] propNames = extractAttributes(query);
            Filter filter = query.getFilter();
            filter = makeDefinitionFilter(filter);

            DefaultQuery constrainedQuery = null;
            constrainedQuery = new DefaultQuery(typeName, filter, maxFeatures,
                    propNames, handle);

            return constrainedQuery;
        } catch (DataSourceException dse) {
            throw dse;
        } catch (Exception ex) {
            throw new DataSourceException(
                "can't restrict the query to match the definition criteria: "
                + ex.getMessage(), ex);
        }
    }

    /**
     * creates a list of FeatureType's attribute names based on the attributes
     * requested by <code>query</code> and making sure they not contain any
     * non exposed attribute, and that they contain the mandatory attributes.
     * 
     * <p>
     * Exposed attributes are those configured in the "attributes" element of
     * the FeatureType's configuration
     * </p>
     *
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private String[] extractAttributes(Query query) {
        String[] propNames = null;

        if (query.retrieveAllProperties()) {
            propNames = new String[schema.getAttributeCount()];

            for (int i = 0; i < schema.getAttributeCount(); i++) {
                propNames[i] = schema.getAttributeType(i).getName();
            }
        } else {
            String[] queriedAtts = query.getPropertyNames();
            int queriedAttCount = queriedAtts.length;
            List allowedAtts = new LinkedList();

            if (mandatoryAtts != null) {
                for (Iterator i = mandatoryAtts.iterator(); i.hasNext();) {
                    String attName = (String) i.next();

                    if (!allowedAtts.contains(attName)) {
                        allowedAtts.add(attName);
                    }
                }
            }

            for (int i = 0; i < queriedAttCount; i++) {
                if (schema.getAttributeType(queriedAtts[i]) != null) {
                    if (!allowedAtts.contains(queriedAtts[i])) {
                        allowedAtts.add(queriedAtts[i]);
                    }
                } else {
                    //REVISIT: I think an exception should be thrown here, 
                    //asking for a property that is not present normally
                    //causes an exception to be thrown.
                    LOGGER.info("queried a not allowed property: "
                        + queriedAtts[i] + ". Ommitting it from query");
                }
            }

            propNames = (String[]) allowedAtts.toArray(new String[allowedAtts
                    .size()]);
        }

        return propNames;
    }

    /**
     * If a definition query has been configured for the FeatureType, makes and
     * return a new Filter that contains both the query's filter and the
     * layer's definition one, by logic AND'ing them.
     *
     * @param filter DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private Filter makeDefinitionFilter(Filter filter)
        throws DataSourceException {
        Filter newFilter = filter;

        try {
            if (definitionQuery != Filter.NONE) {
                FilterFactory ff = FilterFactory.createFilterFactory();
                newFilter = ff.createLogicFilter(AbstractFilter.LOGIC_AND);
                ((LogicFilter) newFilter).addFilter(definitionQuery);
                ((LogicFilter) newFilter).addFilter(filter);
            }
        } catch (Exception ex) {
            throw new DataSourceException("Can't create the definition filter",
                ex);
        }

        return newFilter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param lock DOCUMENT ME!
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public void setFeatureLock(FeatureLock lock) {
        if (source instanceof FeatureLocking) {
            ((FeatureLocking) source).setFeatureLock(lock);
        } else {
            throw new UnsupportedOperationException(
                "FeatureType does not supports locking");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public int lockFeatures(Query query) throws IOException {
        if (source instanceof FeatureLocking) {
            return ((FeatureLocking) source).lockFeatures(query);
        } else {
            throw new DataSourceException(
                "FeatureType does not supports locking");
        }
    }

    public int lockFeature(Feature feature) throws IOException {
        if (source instanceof PostgisFeatureLocking) {
            return ((PostgisFeatureLocking) source).lockFeature(feature);
        } else {
            throw new DataSourceException(
                "FeatureType does not supports locking");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public int lockFeatures(Filter filter) throws IOException {
        filter = makeDefinitionFilter(filter);

        if (source instanceof FeatureLocking) {
            return ((FeatureLocking) source).lockFeatures(filter);
        } else {
            throw new DataSourceException(
                "FeatureType does not supports locking");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public int lockFeatures() throws IOException {
        if (source instanceof FeatureLocking) {
            return ((FeatureLocking) source).lockFeatures();
        } else {
            throw new DataSourceException(
                "FeatureType does not supports locking");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public void unLockFeatures() throws IOException {
        if (source instanceof FeatureLocking) {
            ((FeatureLocking) source).lockFeatures();
        } else {
            throw new DataSourceException(
                "FeatureType does not supports locking");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public void unLockFeatures(Filter filter) throws IOException {
        filter = makeDefinitionFilter(filter);

        if (source instanceof FeatureLocking) {
            ((FeatureLocking) source).unLockFeatures(filter);
        } else {
            throw new DataSourceException(
                "FeatureType does not supports locking");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public void unLockFeatures(Query query) throws IOException {
        query = makeDefinitionQuery(query);

        if (source instanceof FeatureLocking) {
            ((FeatureLocking) source).lockFeatures(query);
        } else {
            throw new DataSourceException(
                "FeatureType does not supports locking");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param reader DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public Set addFeatures(FeatureReader reader) throws IOException {
        if (source instanceof FeatureStore) {
            return ((FeatureStore) source).addFeatures(reader);
        } else {
            throw new DataSourceException(
                "FeatureType does not supports writing");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public void removeFeatures(Filter filter) throws IOException {
        filter = makeDefinitionFilter(filter);

        if (source instanceof FeatureStore) {
            ((FeatureStore) source).removeFeatures(filter);
        } else {
            throw new DataSourceException(
                "FeatureType does not supports writing");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @param filter DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     *
     * @task REVISIT: should we check that non exposed attributes are requiered
     *       in <code>type</code>?
     */
    public void modifyFeatures(AttributeType[] type, Object[] value,
        Filter filter) throws IOException {
        filter = makeDefinitionFilter(filter);

        if (source instanceof FeatureStore) {
            ((FeatureStore) source).modifyFeatures(type, value, filter);
        } else {
            throw new DataSourceException(
                "FeatureType does not supports writing");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @param filter DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public void modifyFeatures(AttributeType type, Object value, Filter filter)
        throws IOException {
        filter = makeDefinitionFilter(filter);

        if (source instanceof FeatureStore) {
            ((FeatureStore) source).modifyFeatures(type, value, filter);
        } else {
            throw new DataSourceException(
                "FeatureType does not supports writing");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param reader DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public void setFeatures(FeatureReader reader) throws IOException {
        if (source instanceof FeatureStore) {
            ((FeatureStore) source).setFeatures(reader);
        } else {
            throw new DataSourceException(
                "FeatureType does not supports write operations");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param transaction DOCUMENT ME!
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public void setTransaction(Transaction transaction) {
        if (source instanceof FeatureStore) {
            ((FeatureStore) source).setTransaction(transaction);
        } else {
            throw new UnsupportedOperationException(
                "FeatureType does not supports write operations");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public Transaction getTransaction() {
        if (source instanceof FeatureStore) {
            return ((FeatureStore) source).getTransaction();
        } else {
            throw new UnsupportedOperationException(
                "FeatureType does not supports write operations");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public DataStore getDataStore() {
        return source.getDataStore();
    }

    /**
     * DOCUMENT ME!
     *
     * @param listener DOCUMENT ME!
     */
    public void addFeatureListener(FeatureListener listener) {
        source.addFeatureListener(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param listener DOCUMENT ME!
     */
    public void removeFeatureListener(FeatureListener listener) {
        source.removeFeatureListener(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public FeatureResults getFeatures(Query query) throws IOException {
        query = makeDefinitionQuery(query);

        return source.getFeatures(query);
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public FeatureResults getFeatures(Filter filter) throws IOException {
        filter = makeDefinitionFilter(filter);

        return source.getFeatures(filter);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public FeatureResults getFeatures() throws IOException {
        if (definitionQuery == Filter.NONE) {
            return source.getFeatures();
        } else {
            return source.getFeatures(definitionQuery);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public FeatureType getSchema() {
        return schema;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public Envelope getBounds() throws IOException {
        if (definitionQuery == Filter.NONE) {
            return source.getBounds();
        } else {
            Query query = new DefaultQuery(definitionQuery);

            return source.getBounds(query);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public Envelope getBounds(Query query) throws IOException {
        try {
            query = makeDefinitionQuery(query);
        } catch (IOException ex) {
            return null;
        }

        return source.getBounds(query);
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getCount(Query query) {
        try {
            query = makeDefinitionQuery(query);
        } catch (IOException ex) {
            return -1;
        }

        return source.getCount(query);
    }
}
