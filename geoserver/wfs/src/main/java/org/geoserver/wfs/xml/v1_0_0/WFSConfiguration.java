/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.xml.v1_0_0;

import net.opengis.ows.OwsFactory;
import net.opengis.wfs.WfsFactory;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.geoserver.wfs.xml.FeatureTypeSchemaBuilder;
import org.geoserver.wfs.xml.WFSHandlerFactory;
import org.geoserver.wfs.xml.gml3.AbstractGeometryTypeBinding;
import org.geotools.filter.v1_0.OGCConfiguration;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.gml2.bindings.GML;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.OptionalComponentParameter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.defaults.SetterInjectionComponentAdapter;
import org.vfny.geoserver.global.Data;


/**
 * Parser configuration for wfs 1.0.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class WFSConfiguration extends Configuration {
    Data catalog;
    FeatureTypeSchemaBuilder schemaBuilder;

    public WFSConfiguration(Data catalog, FeatureTypeSchemaBuilder schemaBuilder) {
        super();

        this.catalog = catalog;
        this.schemaBuilder = schemaBuilder;

        addDependency(new OGCConfiguration());
        addDependency(new GMLConfiguration());
    }

    public String getNamespaceURI() {
        return WFS.NAMESPACE;
    }

    public String getSchemaFileURL() {
        return getSchemaLocationResolver()
                   .resolveSchemaLocation(null, WFS.NAMESPACE, "WFS-transaction.xsd");
    }

    public BindingConfiguration getBindingConfiguration() {
        return new WFSBindingConfiguration();
    }

    public XSDSchemaLocationResolver getSchemaLocationResolver() {
        return new WFSSchemaLocationResolver();
    }

    public void configureContext(MutablePicoContainer context) {
        super.configureContext(context);

        context.registerComponentInstance(OwsFactory.eINSTANCE);
        context.registerComponentInstance(WfsFactory.eINSTANCE);
        context.registerComponentInstance(new WFSHandlerFactory(catalog, schemaBuilder));
        context.registerComponentInstance(catalog);
    }

    protected void configureBindings(MutablePicoContainer bindings) {
        super.configureBindings(bindings);

        //override the GMLAbstractFeatureTypeBinding
        bindings.registerComponentImplementation(GML.AbstractFeatureType,
            GMLAbstractFeatureTypeBinding.class);
        
        //use setter injection for AbstractGeometryType bindign to allow an 
        // optional crs to be set in teh binding context for parsing, this crs
        // is set by the binding of a parent element.
        // note: it is important that this component adapter is non-caching so 
        // that the setter property gets updated properly every time
        bindings.registerComponent(
            new SetterInjectionComponentAdapter( 
                GML.AbstractGeometryType, AbstractGeometryTypeBinding.class, 
                new Parameter[]{ new OptionalComponentParameter(CoordinateReferenceSystem.class)} 
            )
        );
        
    }
}
