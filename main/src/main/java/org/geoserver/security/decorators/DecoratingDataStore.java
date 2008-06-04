
package org.geoserver.security.decorators;

import java.io.IOException;
import java.util.List;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.ServiceInfo;
import org.geotools.data.Transaction;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

/**
 * Delegates every method to the wrapped feature source. Subclasses will
 * override selected methods to perform their "decoration" job
 * 
 * @author Andrea Aime - TOPP TODO: Move this class to gt2
 */
public abstract class DecoratingDataStore implements DataStore {
    DataStore delegate;
    
    public DecoratingDataStore(DataStore delegate) {
        this.delegate = delegate;
    }

    public void createSchema(SimpleFeatureType featureType) throws IOException {
        delegate.createSchema(featureType);
    }

    public void dispose() {
        delegate.dispose();
    }

    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(Query query,
            Transaction transaction) throws IOException {
        return delegate.getFeatureReader(query, transaction);
    }

    public FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource(Name typeName)
            throws IOException {
        return delegate.getFeatureSource(typeName);
    }

    public FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource(String typeName)
            throws IOException {
        return delegate.getFeatureSource(typeName);
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(String typeName,
            Filter filter, Transaction transaction) throws IOException {
        return delegate.getFeatureWriter(typeName, filter, transaction);
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(String typeName,
            Transaction transaction) throws IOException {
        return delegate.getFeatureWriter(typeName, transaction);
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriterAppend(String typeName,
            Transaction transaction) throws IOException {
        return delegate.getFeatureWriterAppend(typeName, transaction);
    }

    public ServiceInfo getInfo() {
        return delegate.getInfo();
    }

    public LockingManager getLockingManager() {
        return delegate.getLockingManager();
    }

    public List<Name> getNames() throws IOException {
        return delegate.getNames();
    }

    public SimpleFeatureType getSchema(Name name) throws IOException {
        return delegate.getSchema(name);
    }

    public SimpleFeatureType getSchema(String typeName) throws IOException {
        return delegate.getSchema(typeName);
    }

    public String[] getTypeNames() throws IOException {
        return delegate.getTypeNames();
    }

    public FeatureSource<SimpleFeatureType, SimpleFeature> getView(Query query) throws IOException,
            SchemaException {
        return delegate.getView(query);
    }

    public void updateSchema(Name typeName, SimpleFeatureType featureType) throws IOException {
        delegate.updateSchema(typeName, featureType);
    }

    public void updateSchema(String typeName, SimpleFeatureType featureType) throws IOException {
        delegate.updateSchema(typeName, featureType);
    }

}
