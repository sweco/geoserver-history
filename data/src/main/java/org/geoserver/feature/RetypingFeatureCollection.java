/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.feature;

import org.geotools.data.FeatureReader;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.collection.DelegateFeatureIterator;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * FeatureCollection with "casts" features from on feature type to another.
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class RetypingFeatureCollection extends DecoratingFeatureCollection {
    FeatureType target;

    public RetypingFeatureCollection(FeatureCollection delegate, FeatureType target) {
        super(delegate);
        this.target = target;
    }

    public FeatureType getSchema() {
        return target;
    }

    public Iterator iterator() {
        return new RetypingIterator(delegate.iterator(), target);
    }

    public void close(Iterator iterator) {
        RetypingIterator retyping = (RetypingIterator) iterator;
        delegate.close(retyping.delegate);
    }

    public FeatureIterator features() {
        return new DelegateFeatureIterator(this, iterator());
    }

    public void close(FeatureIterator iterator) {
        DelegateFeatureIterator delegate = (DelegateFeatureIterator) iterator;
        delegate.close();
    }

    static Feature retype(Feature source, FeatureType target)
        throws IllegalAttributeException {
        Object[] attributes = new Object[target.getAttributeCount()];

        for (int i = 0; i < target.getAttributeCount(); i++) {
            AttributeType attributeType = target.getAttributeType(i);
            Object value = null;

            if (source.getFeatureType().getAttributeType(attributeType.getName()) != null) {
                value = source.getAttribute(attributeType.getName());
            }

            attributes[i] = value;
        }

        return target.create(attributes);
    }

    public static class RetypingIterator implements Iterator {
        FeatureType target;
        Iterator delegate;

        public RetypingIterator(Iterator delegate, FeatureType target) {
            this.delegate = delegate;
            this.target = target;
        }

        public boolean hasNext() {
            return delegate.hasNext();
        }

        public Object next() {
            try {
                return RetypingFeatureCollection.retype((Feature) delegate.next(), target);
            } catch (IllegalAttributeException e) {
                throw new RuntimeException(e);
            }
        }

        public void remove() {
            delegate.remove();
        }
    }

    public static class RetypingFeatureReader implements FeatureReader {
        FeatureReader delegate;
        FeatureType target;

        public RetypingFeatureReader(FeatureReader delegate, FeatureType target) {
            this.delegate = delegate;
            this.target = target;
        }

        public void close() throws IOException {
            delegate.close();
            delegate = null;
            target = null;
        }

        public FeatureType getFeatureType() {
            return target;
        }

        public boolean hasNext() throws IOException {
            return delegate.hasNext();
        }

        public Feature next() throws IOException, IllegalAttributeException, NoSuchElementException {
            return RetypingFeatureCollection.retype(delegate.next(), target);
        }
    }
}
