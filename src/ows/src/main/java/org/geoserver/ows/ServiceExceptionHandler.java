/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.ows;

import org.geoserver.ows.util.ResponseUtils;
import org.geoserver.platform.Service;
import org.geoserver.platform.ServiceException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Handles an exception thrown by a service.
 * <p>
 * A service exception handler must declare the services in which it is capable
 * of handling exceptions for, see {@link #getServices()}.
 * </p>
 * <p>
 * Instances must be declared in a spring context as follows:
 * <pre>
 *         <code>
 *  &lt;bean id="myServiceExcepionHandler" class="com.xyz.MyServiceExceptionHandler"&gt;
 *     &lt;constructor-arg ref="myService"/&gt;
 *  &lt;/bean&gt;
 * </code>
 * </pre>
 *
 * Where <code>myService</code> is the id of another bean somewhere in the
 * context.
 *
 * </p>
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public abstract class ServiceExceptionHandler {
    /**
     * Logger
     */
    protected static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.ows");

    /**
     * The services this handler handles exceptions for.
     */
    List /*<Service>*/ services;

    /**
     * Constructs the handler with the list of {@link Service}'s that it
     * handles exceptions for.
     *
     * @param services A list of {@link Service}.
     */
    public ServiceExceptionHandler(List services) {
        this.services = services;
    }

    /**
     * Constructs the handler for a single {@link Service} that it handles
     * exceptions for.
     *
     * @param service The service to handle exceptions for.
     */
    public ServiceExceptionHandler(Service service) {
        this.services = Collections.singletonList(service);
    }

    /**
     * @return The services this handler handles exceptions for.
     */
    public List getServices() {
        return services;
    }

    /**
     * Handles the service exception.
     *
     * @param exception The service exception.
     * @param request The informations collected by the dispatcher about the request
     */
    public abstract void handleServiceException(ServiceException exception, Request request);
    
    /**
     * Dumps an exception message along all its causes messages (since more often
     * than not the real cause, such as "unknown property xxx" is a few levels down)
     * @param e
     * @param s
     * @param xmlEscape 
     */
    protected void dumpExceptionMessages(ServiceException e, StringBuffer s, boolean xmlEscape) {
        Throwable ex = e;
        do {
            Throwable cause = ex.getCause();
            final String message = ex.getMessage();
            String lastMessage = message;
            if(!"".equals(message)) {
                if(xmlEscape)
                    s.append(ResponseUtils.encodeXML(message));
                else
                    s.append(message);
                if(ex instanceof ServiceException) {
                    for ( Iterator t = ((ServiceException) ex).getExceptionText().iterator(); t.hasNext(); ) {
                        s.append("\n");
                        String msg = (String) t.next();
                        if(!lastMessage.equals(msg)) {
                            if(xmlEscape)
                                s.append(ResponseUtils.encodeXML(msg));
                            else
                                s.append( t.next() );
                            lastMessage = msg;
                        }
                        
                    }
                }
                if(cause != null)
                    s.append("\n");
            }
            
            // avoid infinite loop if someone did the very stupid thing of setting
            // the cause as the exception itself (I only found this situation once, but...)
            if(ex == cause || cause == null)
                break;
            else
                ex = cause;
        } while(true);
    }
}