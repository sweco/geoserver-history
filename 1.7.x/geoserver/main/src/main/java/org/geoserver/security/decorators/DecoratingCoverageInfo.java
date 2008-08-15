/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.decorators;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CoverageDimensionInfo;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.MetadataLinkInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.impl.AbstractDecorator;
import org.geotools.factory.Hints;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

/**
 * Delegates all methods to the provided delegate. Suclasses will override
 * methods in order to perform their decoration work
 * 
 * @author Andrea Aime - TOPP
 * 
 * @param <T>
 * @param <F>
 */
public class DecoratingCoverageInfo extends AbstractDecorator<CoverageInfo>
        implements CoverageInfo {

    public DecoratingCoverageInfo(CoverageInfo delegate) {
        super(delegate);
    }

    public String getAbstract() {
        return delegate.getAbstract();
    }

    public <T> T getAdapter(Class<T> adapterClass, Map<?, ?> hints) {
        return delegate.getAdapter(adapterClass, hints);
    }

    public List<String> getAlias() {
        return delegate.getAlias();
    }

    public ReferencedEnvelope getBoundingBox() throws Exception {
        return delegate.getBoundingBox();
    }

    public Catalog getCatalog() {
        return delegate.getCatalog();
    }

    public CoordinateReferenceSystem getCRS() throws Exception {
        return delegate.getCRS();
    }

    public String getDefaultInterpolationMethod() {
        return delegate.getDefaultInterpolationMethod();
    }

    public String getDescription() {
        return delegate.getDescription();
    }

    public List<CoverageDimensionInfo> getDimensions() {
        return delegate.getDimensions();
    }

    public GridGeometry getGrid() {
        return delegate.getGrid();
    }

    public GridCoverage getGridCoverage(ProgressListener listener, Hints hints)
            throws IOException {
        return delegate.getGridCoverage(listener, hints);
    }

    public GridCoverage getGridCoverage(ProgressListener listener,
            ReferencedEnvelope envelope, Hints hints) throws IOException {
        return delegate.getGridCoverage(listener, envelope, hints);
    }

    public GridCoverageReader getGridCoverageReader(ProgressListener listener,
            Hints hints) throws IOException {
        return delegate.getGridCoverageReader(listener, hints);
    }

    public String getId() {
        return delegate.getId();
    }

    public List<String> getInterpolationMethods() {
        return delegate.getInterpolationMethods();
    }

    public List<String> getKeywords() {
        return delegate.getKeywords();
    }

    public ReferencedEnvelope getLatLonBoundingBox() {
        return delegate.getLatLonBoundingBox();
    }

    public Map<String, Serializable> getMetadata() {
        return delegate.getMetadata();
    }

    public List<MetadataLinkInfo> getMetadataLinks() {
        return delegate.getMetadataLinks();
    }

    public String getName() {
        return delegate.getName();
    }

    public NamespaceInfo getNamespace() {
        return delegate.getNamespace();
    }

    public ReferencedEnvelope getNativeBoundingBox() {
        return delegate.getNativeBoundingBox();
    }

    public CoordinateReferenceSystem getNativeCRS() {
        return delegate.getNativeCRS();
    }

    public String getNativeFormat() {
        return delegate.getNativeFormat();
    }

    public String getNativeName() {
        return delegate.getNativeName();
    }

    public Map<String, Serializable> getParameters() {
        return delegate.getParameters();
    }

    public String getPrefixedName() {
        return delegate.getPrefixedName();
    }

    public ProjectionPolicy getProjectionPolicy() {
        return delegate.getProjectionPolicy();
    }

    public List<String> getRequestSRS() {
        return delegate.getRequestSRS();
    }

    public List<String> getResponseSRS() {
        return delegate.getResponseSRS();
    }

    public String getSRS() {
        return delegate.getSRS();
    }

    public CoverageStoreInfo getStore() {
        return delegate.getStore();
    }

    public List<String> getSupportedFormats() {
        return delegate.getSupportedFormats();
    }

    public String getTitle() {
        return delegate.getTitle();
    }

    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    public void setAbstract(String _abstract) {
        delegate.setAbstract(_abstract);
    }

    public void setDefaultInterpolationMethod(String defaultInterpolationMethod) {
        delegate.setDefaultInterpolationMethod(defaultInterpolationMethod);
    }

    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    public void setEnabled(boolean enabled) {
        delegate.setEnabled(enabled);
    }

    public void setGrid(GridGeometry grid) {
        delegate.setGrid(grid);
    }

    public void setLatLonBoundingBox(ReferencedEnvelope box) {
        delegate.setLatLonBoundingBox(box);
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public void setNamespace(NamespaceInfo namespace) {
        delegate.setNamespace(namespace);
    }

    public void setNativeBoundingBox(ReferencedEnvelope box) {
        delegate.setNativeBoundingBox(box);
    }

    public void setNativeCRS(CoordinateReferenceSystem nativeCRS) {
        delegate.setNativeCRS(nativeCRS);
    }

    public void setNativeFormat(String nativeFormat) {
        delegate.setNativeFormat(nativeFormat);
    }

    public void setNativeName(String nativeName) {
        delegate.setNativeName(nativeName);
    }

    public void setProjectionPolicy(ProjectionPolicy policy) {
        delegate.setProjectionPolicy(policy);
    }

    public void setSRS(String srs) {
        delegate.setSRS(srs);
    }

    public void setStore(StoreInfo store) {
        delegate.setStore(store);
    }

    public void setTitle(String title) {
        delegate.setTitle(title);
    }
    
}
