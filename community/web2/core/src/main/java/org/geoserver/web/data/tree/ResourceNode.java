/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
/**
 * 
 */
package org.geoserver.web.data.tree;

import java.util.Collections;
import java.util.List;

import org.geoserver.catalog.ResourceInfo;

class ResourceNode extends AbstractCatalogNode {

    private Class resourceType;

    private String storeName;

    public ResourceNode(String storeName, String name,
            AbstractCatalogNode parent, Class clazz) {
        super(name, parent);
        this.storeName = storeName;
        this.resourceType = clazz;
    }

    protected List buildChildNodes() {
        return Collections.EMPTY_LIST;
    }

    @Override
    protected ResourceInfo getModel() {
        return getCatalog().getResourceByName(name, resourceType);
    }

    @Override
    protected String getNodeLabel() {
        return getModel().getName();
    }

    protected Class getResourceType() {
        return resourceType;
    }

    public String getStoreName() {
        return storeName;
    }

}