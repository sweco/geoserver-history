/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wms.web.data;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.web.data.style.StyleDetachableModel;
import org.geoserver.web.wicket.GeoServerDataProvider;

/**
 * A {@link GeoServerDataProvider} provider for styles
 */
@SuppressWarnings("serial")
public class StyleProvider extends GeoServerDataProvider<StyleInfo> {

    public static Property<StyleInfo> NAME = 
        new BeanProperty<StyleInfo>( "name", "name" );

    static List PROPERTIES = Arrays.asList( NAME);
    
    @Override
    protected List<StyleInfo> getItems() {
        return getCatalog().getStyles();
    }

    @Override
    protected List<Property<StyleInfo>> getProperties() {
        return PROPERTIES;
    }

    public IModel model(Object object) {
        return new StyleDetachableModel( (StyleInfo) object );
    }

}