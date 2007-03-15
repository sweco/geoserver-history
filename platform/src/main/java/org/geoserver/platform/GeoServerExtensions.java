/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.platform;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Utility class uses to process GeoServer extension points.
 * <p>
 * An instance of this class needs to be registered in spring context as follows.
 * <code>
 *         <pre>
 *         &lt;bean id="geoserverExtensions" class="org.geoserver.GeoServerExtensions"/&gt;
 *         </pre>
 * </code>
 * It must be a singleton, and must not be loaded lazily. Futhermore, this
 * bean must be loaded before any beans that use it.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class GeoServerExtensions implements ApplicationContextAware {
    /**
     * A static application context
     */
    static ApplicationContext context;

    /**
     * Sets the web application context to be used for looking up extensions.
     * <p>
     * This method is called by the spring container, and should never be called
     * by client code. If client needs to supply a particular context, methods
     * which take a context are available.
     * </p>
     * <p>
     * This is the context that is used for methods which dont supply their
     * own context.
     * </p>
     */
    public void setApplicationContext(ApplicationContext context)
        throws BeansException {
        GeoServerExtensions.context = context;
    }

    /**
     * Loads all extensions implementing or extending <code>extensionPoint</code>.
     *
     * @param extensionPoint The class or interface of the extensions.
     * @param context The context in which to perform the lookup.
     *
     * @return A collection of the extensions, or an empty collection.
     */
    public static final List extensions(Class extensionPoint, ApplicationContext context) {
        return new ArrayList(context.getBeansOfType(extensionPoint).values());
    }

    /**
     * Loads all extensions implementing or extending <code>extensionPoint</code>.
     * <p>
     * This method uses the "default" application context to perform the lookup.
     * See {@link #setApplicationContext(ApplicationContext)}.
     * </p>
     * @param extensionPoint The class or interface of the extensions.
     *
     * @return A collection of the extensions, or an empty collection.
     */
    public static final List extensions(Class extensionPoint) {
        return extensions(extensionPoint, context);
    }
}
