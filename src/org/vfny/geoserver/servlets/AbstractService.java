/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.servlets;

import org.vfny.geoserver.ExceptionHandler;
import org.vfny.geoserver.ServiceException;
import org.vfny.geoserver.global.GeoServer;
import org.vfny.geoserver.global.Service;
import org.vfny.geoserver.requests.Request;
import org.vfny.geoserver.requests.readers.KvpRequestReader;
import org.vfny.geoserver.requests.readers.XmlRequestReader;
import org.vfny.geoserver.responses.Response;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Represents a service that all others extend from.  Subclasses should provide
 * response and exception handlers as appropriate.
 * 
 * <p>
 * It is <b>really</b> important to adhere to the following workflow:
 * 
 * <ol>
 * <li>
 * get a Request reader
 * </li>
 * <li>
 * ask the Request Reader for the Request object
 * </li>
 * <li>
 * Provide the resulting Request with the ServletRequest that generated it
 * </li>
 * <li>
 * get the appropiate ResponseHandler
 * </li>
 * <li>
 * ask it to execute the Request
 * </li>
 * <li>
 * set the response content type
 * </li>
 * <li>
 * write to the http response's output stream
 * </li>
 * <li>
 * pending - call Response cleanup
 * </li>
 * </ol>
 * </p>
 * 
 * <p>
 * If anything goes wrong a ServiceException can be thrown and will be written
 * to the output stream instead.
 * </p>
 * 
 * <p>
 * This is because we have to be sure that no exception have been produced
 * before setting the response's content type, so we can set the exception
 * specific content type; and that Response.getContentType is called AFTER
 * Response.execute, since the MIME type can depend on any request parameter
 * or another kind of desission making during the execute process. (i.e.
 * FORMAT in WMS GetMap)
 * </p>
 * 
 * <p>
 * TODO: We need to call Response.abort() if anything goes wrong to allow the
 * Response a chance to cleanup after itself.
 * </p>
 *
 * @author Gabriel Rold?n
 * @author Chris Holmes
 * @author Jody Garnett, Refractions Research
 * @version $Id: AbstractService.java,v 1.23 2004/09/08 17:34:38 cholmesny Exp $
 */
public abstract class AbstractService extends HttpServlet {
    /** Class logger */
    protected static Logger LOGGER = Logger.getLogger(
            "org.vfny.geoserver.servlets");

    /** DOCUMENT ME! */

    //protected static final GeoServer config = GeoServer.getInstance();

    /** Specifies mime type */

    //protected static final String MIME_TYPE = config.getMimeType();
    private static Map context;

    /**
     * GR: if SPEED, FILE and BUFFER are static instances, so their methods
     * should be synchronized, ending in a not multiuser server, so I made
     * safetyMode dynamically instantiated in init() and the strategy choosed
     * at server config level in web.xml. If I'm wrong, just tell me. If this
     * is correct, may be it will be better to allow for user customized
     * ServiceStrategy implementations to be parametrized by a servlet context
     * param JG: You are exactly right! My-Bad, I was just trying to
     * understand what chris was talking about.
     */
    public static final Map serviceStrategys = new HashMap();

    static {
        serviceStrategys.put("SPEED", SpeedStrategy.class);
        serviceStrategys.put("FILE", FileStrategy.class);
        serviceStrategys.put("BUFFER", BufferStrategy.class);
    }

    /** Controls the Safty Mode used when using execute/writeTo. */
    private static Class safetyMode;

    /** DOCUMENT ME!  */
    protected HttpServletRequest curRequest;

    /**
     * loads the "serviceStrategy" servlet context parameter and checks it if
     * reffers to a valid ServiceStrategy (by now, one of SPEED, FILE or
     * BUFFER); if no, just sets the strategy to BUFFER as default
     *
     * @param config the servlet environment
     *
     * @throws ServletException if the configured strategy class is not a
     *         derivate of ServiceStrategy or it is thrown by the parent class
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LOGGER.info("Looking for configured service responses' strategy");

        ServletContext servContext = config.getServletContext();
        String stgyKey = servContext.getInitParameter("serviceStratagy");
        Class stgyClass = BufferStrategy.class;

        if (stgyKey == null) {
            LOGGER.info("No service strategy configured, defaulting to BUFFER");
        } else {
            LOGGER.info("Looking for configured service strategy " + stgyKey);

            Class configurefStgyClass = (Class) serviceStrategys.get(stgyKey);

            if (configurefStgyClass == null) {
                LOGGER.info("No service strategy named " + stgyKey
                    + "found, defaulting to BUFFER. Please check your config");
            } else {
                stgyClass = configurefStgyClass;
            }
        }

        LOGGER.fine("verifying configured strategy");

        if (!(ServiceStrategy.class.isAssignableFrom(stgyClass))) {
            throw new ServletException("the configured service strategy "
                + stgyClass + " is not a ServiceStrategy derivate");
        }

        LOGGER.info("Using service strategy " + stgyClass);
        AbstractService.safetyMode = stgyClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @param req DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected abstract boolean isServiceEnabled(HttpServletRequest req);

    /**
     * DOCUMENT ME!
     *
     * @param request DOCUMENT ME!
     * @param response DOCUMENT ME!
     *
     * @throws ServletException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        // implements the main request/response logic
        this.curRequest = request;

        Request serviceRequest = null;

        if (!isServiceEnabled(request)) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);

            return;
        }

        try {
            String qString = request.getQueryString();
            LOGGER.fine("reading request: " + qString);

            //Map requestParams = KvpRequestReader.parseKvpSet(qString);
            Map requestParams = new HashMap();
            String paramName;
            String paramValue;

            for (Enumeration pnames = request.getParameterNames();
                    pnames.hasMoreElements();) {
                paramName = (String) pnames.nextElement();
                paramValue = request.getParameter(paramName);
                requestParams.put(paramName.toUpperCase(), paramValue);
            }

            KvpRequestReader requestReader = getKvpReader(requestParams);

            serviceRequest = requestReader.getRequest(request);
            LOGGER.finer("serviceRequest provided with HttpServletRequest: "
                + request);

            //serviceRequest.setHttpServletRequest(request);
        } catch (ServiceException se) {
            sendError(response, se);

            return;
        } catch (Throwable e) {
            sendError(response, e);

            return;
        }

        doService(request, response, serviceRequest);
    }

    /**
     * Performs the post method.  Simply passes itself on to the three argument
     * doPost method, with null for the reader, because the
     * request.getReader() will not have been used if this servlet is called
     * directly.
     *
     * @param request DOCUMENT ME!
     * @param response DOCUMENT ME!
     *
     * @throws ServletException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        doPost(request, response, null);
    }

    /**
     * Performs the post method.  Gets the appropriate xml reader and
     * determines the request from that, and then passes the request on to
     * doService.
     *
     * @param request The request made.
     * @param response The response to be returned.
     * @param requestXml A reader of the xml to be read.  This is only used by
     *        the dispatcher, everyone else should just pass in null.  This is
     *        needed because afaik HttpServletRequest.getReader() can not be
     *        used twice.  So in a dispatched case we write it to a temp file,
     *        which we can then read in twice.
     *
     * @throws ServletException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public void doPost(HttpServletRequest request,
        HttpServletResponse response, Reader requestXml)
        throws ServletException, IOException {
        this.curRequest = request;

        Request serviceRequest = null;

        //TODO: This isn't a proper ogc service response.
        if (!isServiceEnabled(request)) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);

            return;
        }

        // implements the main request/response logic
        try {
            XmlRequestReader requestReader = getXmlRequestReader();
            Reader xml = (requestXml != null) ? requestXml : request.getReader();
            serviceRequest = requestReader.read(xml, request);
            serviceRequest.setHttpServletRequest(request);
        } catch (ServiceException se) {
            sendError(response, se);

            return;
        } catch (Throwable e) {
            sendError(response, e);

            return;
        }

        doService(request, response, serviceRequest);
    }

    /**
     * Peforms service according to ServiceStrategy.
     * 
     * <p>
     * This method has very strict requirements, please see the class
     * description for the specifics.
     * </p>
     * 
     * <p>
     * It has a lot of try/catch blocks, but they are fairly necessary to
     * handle things correctly and to avoid as many ugly servlet responses, so
     * that everything is wrapped correctly.
     * </p>
     *
     * @param request The httpServlet of the request.
     * @param response The response to be returned.
     * @param serviceRequest The OGC request to service.
     *
     * @throws ServletException if the strategy can't be instantiated
     */
    protected void doService(HttpServletRequest request,
        HttpServletResponse response, Request serviceRequest)
        throws ServletException {
        LOGGER.info("handling request: " + serviceRequest);

        if (!isServiceEnabled(request)) {
            try {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } catch (IOException e) {
                // do nothing
            }

            return;
        }

        ServiceStrategy strategy = null;
        Response serviceResponse = null;

        try {
            strategy = getServiceStrategy();
            LOGGER.fine("strategy is: " + strategy);
            serviceResponse = getResponseHandler();
        } catch (Throwable t) {
            sendError(response, t);

            return;
        }

        Service s = null;

        if ("WFS".equals(serviceRequest.getService())) {
            s = serviceRequest.getWFS();
        } else {
            s = serviceRequest.getWMS();
        }

        try {
            // execute request
            LOGGER.finer("executing request");
            serviceResponse.execute(serviceRequest);
            LOGGER.finer("execution succeed");
        } catch (ServiceException serviceException) {
            LOGGER.warning("service exception while executing request: "
                + serviceRequest + "\ncause: " + serviceException.getMessage());
            serviceResponse.abort(s);
            sendError(response, serviceException);

            return;
        } catch (Throwable t) {
            //we can safelly send errors here, since we have not touched response yet
            serviceResponse.abort(s);
            sendError(response, t);

            return;
        }

        OutputStream strategyOuput = null;

        //obtain the strategy output stream
        try {
            LOGGER.finest("getting strategy output");
            strategyOuput = strategy.getDestination(response);
            LOGGER.finer("strategy output is: "
                + strategyOuput.getClass().getName());

            String mimeType = serviceResponse.getContentType(s.getGeoServer());
            LOGGER.fine("mime type is: " + mimeType);
            response.setContentType(mimeType);

            String encoding = serviceResponse.getContentEncoding();

            if (encoding != null) {
                LOGGER.fine("content encoding is: " + encoding);
                response.setHeader("content-encoding", encoding);
            }
        } catch (SocketException socketException) {
            LOGGER.fine(
                "it seems that the user has closed the request stream: "
                + socketException.getMessage());

            // It seems the user has closed the request stream
            // Apparently this is a "cancel" and will quietly go away
            //
            // I will still give strategy and serviceResponse
            // a chance to clean up
            //
            serviceResponse.abort(s);
            strategy.abort();

            return;
        } catch (IOException ex) {
            serviceResponse.abort(s);
            strategy.abort();
            sendError(response, ex);

            return;
        }

        try {
            // gather response
            serviceResponse.writeTo(strategyOuput);
            strategyOuput.flush();
            strategy.flush();
        } catch (java.net.SocketException sockEx) { // user cancel
            serviceResponse.abort(s);
            strategy.abort();

            return;
        } catch (IOException ioException) { // strategyOutput error
            serviceResponse.abort(s);
            strategy.abort();
            sendError(response, ioException);

            return;
        } catch (ServiceException writeToFailure) { // writeTo Failure
            serviceResponse.abort(s);
            strategy.abort();
            sendError(response, writeToFailure);

            return;
        } catch (Throwable help) { // This is an unexpected error(!)
            serviceResponse.abort(s);
            strategy.abort();
            sendError(response, help);

            return;
        }

        // Finish Response
        // I have moved closing the output stream out here, it was being
        // done by a few of the ServiceStrategy
        //
        // By this time serviceResponse has finished successfully
        // and strategy is also finished
        //
        try {
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (SocketException sockEx) { // user cancel
            LOGGER.warning("Could not send completed response to user:"
                + sockEx);

            return;
        } catch (IOException ioException) {
            // This is bad, the user did not get the completed response
            LOGGER.warning("Could not send completed response to user:"
                + ioException);

            return;
        }

        LOGGER.info("Service handled");
    }

    /**
     * Gets the response class that should handle the request of this service.
     * All subclasses must implement.
     *
     * @return The response that the request read by this servlet should be
     *         passed to.
     */
    protected abstract Response getResponseHandler();

    /**
     * Gets a reader that will figure out the correct Key Vaule Pairs for this
     * service.
     *
     * @param params A map of the kvp pairs.
     *
     * @return An initialized KVP reader to decode the request.
     */
    protected abstract KvpRequestReader getKvpReader(Map params);

    /**
     * Gets a reader that will handle a posted xml request for this servlet.
     *
     * @return An XmlRequestReader appropriate to this service.
     */
    protected abstract XmlRequestReader getXmlRequestReader();

    /**
     * Gets the exception handler for this service.
     *
     * @return The correct ExceptionHandler
     */
    protected abstract ExceptionHandler getExceptionHandler();

    /**
     * Instantiates a given strategy class and throws the proper exceptions.
     * Used as a helper class to create service strategies, since the
     * hierarchy for inner classes and whatnot is a bit funky.
     *
     * @param strategyClass the strategy to instantiate.
     *
     * @return The Service Strategy insantiated.
     *
     * @throws ServiceException for any instantiation problems.
     *
     * @see #getServiceStrategy
     */
    protected AbstractService.ServiceStrategy getServiceStrategy(
        Class strategyClass) throws ServiceException {
        ServiceStrategy strategy = null;

        try {
            strategy = (ServiceStrategy) strategyClass.newInstance();
        } catch (InstantiationException ex) {
            throw new ServiceException(strategy
                + " is not a valid ServiceStrategy", ex);
        } catch (IllegalAccessException ex) {
            throw new ServiceException(strategy
                + " is not a valid ServiceStrategy", ex);
        }

        return strategy;
    }

    /**
     * Gets the strategy for outputting the response.  This method gets the
     * strategy from the serviceStrategy param in the web.xml file.  This is
     * sort of odd behavior, as all other such parameters are set in the
     * services and catalog xml files, and this param may move there.  But as
     * it is much  more of a programmer configuration than a user
     * configuration there is  no rush to move it.
     * 
     * <p>
     * Subclasses may choose to override this method in order to get a strategy
     * more suited to their response.  Currently only Transaction will do
     * this, since the commit is only called after writeTo, and it often
     * messes up, so we want to be able to see the error message (SPEED writes
     * the output directly, so errors in writeTo do not show up.)
     * </p>
     * 
     * <p>
     * Most subclasses should not override, this method will most always return
     * the SPEED  strategy, since it is the fastest response and should work
     * fine if everything is well tested.  FILE and BUFFER should be used when
     * there  are errors in writeTo methods of child classes, set by the
     * programmer in the web.xml file.
     * </p>
     *
     * @return The service strategy found in the web.xml serviceStrategy
     *         parameter.   The code that finds this is in the init method
     *
     * @throws ServiceException If the service strategy set in #init() is not
     *         valid.
     *
     * @see #init() for the code that sets the serviceStrategy.
     */
    protected AbstractService.ServiceStrategy getServiceStrategy()
        throws ServiceException {
        ServletContext servContext = getServletContext();
        GeoServer geoServer = (GeoServer) servContext.getAttribute(GeoServer.WEB_CONTAINER_KEY);

        //If verbose exceptions is on then lets make sure they actually get the
        //exception by using the file strategy.
        if (geoServer.isVerboseExceptions()) {
            return getServiceStrategy(FileStrategy.class);
        } else {
            return getServiceStrategy(safetyMode);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected String getMimeType() {
        ServletContext servContext = getServletContext();

        try {
            return ((GeoServer) servContext.getAttribute("GeoServer"))
            .getMimeType();
        } catch (NullPointerException e) {
            return "text/xml; charset="
            + Charset.forName("UTF-8").displayName();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param response DOCUMENT ME!
     * @param content DOCUMENT ME!
     */
    protected void send(HttpServletResponse response, CharSequence content) {
        send(response, content, getMimeType());
    }

    /**
     * DOCUMENT ME!
     *
     * @param response DOCUMENT ME!
     * @param content DOCUMENT ME!
     * @param mimeType DOCUMENT ME!
     */
    protected void send(HttpServletResponse response, CharSequence content,
        String mimeType) {
        try {
            response.setContentType(mimeType);
            response.getWriter().write(content.toString());
        } catch (IOException ex) { //stream closed by client, do nothing
            LOGGER.fine(ex.getMessage());
        }
    }

    /**
     * Send error produced during getService opperation.
     * 
     * <p>
     * Some errors know how to write themselves out WfsTransactionException for
     * instance. It looks like this might be is handled by
     * getExceptionHandler().newServiceException( t, pre, null ). I still
     * would not mind seeing a check for ServiceConfig Exception here.
     * </p>
     * 
     * <p>
     * This code says that it deals with UNCAUGHT EXCEPTIONS, so I think it
     * would be wise to explicitly catch ServiceExceptions.
     * </p>
     *
     * @param response DOCUMENT ME!
     * @param t DOCUMENT ME!
     */
    protected void sendError(HttpServletResponse response, Throwable t) {
        if (t instanceof ServiceException) {
            sendError(response, (ServiceException) t);

            return;
        }

        LOGGER.info("Had an undefined error: " + t.getMessage());

        //TODO: put the stack trace in the logger.
        //t.printStackTrace();
        //String pre = "UNCAUGHT EXCEPTION";
        ExceptionHandler exHandler = getExceptionHandler();
        ServiceException se = exHandler.newServiceException(t);

        sendError(response, se);

        //GeoServer geoServer = (GeoServer) this.getServletConfig()
        //                                      .getServletContext().getAttribute(GeoServer.WEB_CONTAINER_KEY);
        //send(response, se.getXmlResponse(geoServer.isVerboseExceptions()));
    }

    /**
     * Send a serviceException produced during getService opperation.
     *
     * @param response DOCUMENT ME!
     * @param se DOCUMENT ME!
     */
    protected void sendError(HttpServletResponse response, ServiceException se) {
        GeoServer geoServer = (GeoServer) this.getServletConfig()
                                              .getServletContext().getAttribute(GeoServer.WEB_CONTAINER_KEY);

        String mimeType = se.getMimeType(geoServer);

        send(response,
            se.getXmlResponse(geoServer.isVerboseExceptions(), curRequest),
            mimeType);
    }

    /**
     * DOCUMENT ME!
     *
     * @param response DOCUMENT ME!
     * @param result DOCUMENT ME!
     */
    protected void send(HttpServletResponse response, Response result) {
        OutputStream responseOut = null;

        try {
            responseOut = response.getOutputStream();
        } catch (IOException ex) { //stream closed, do nothing.
            LOGGER.info("apparently client has closed stream: "
                + ex.getMessage());
        }

        OutputStream out = new BufferedOutputStream(responseOut);
        ServletContext servContext = getServletContext();
        response.setContentType(result.getContentType(
                (GeoServer) servContext.getAttribute("GeoServer")));

        try {
            result.writeTo(out);
            out.flush();
            responseOut.flush();
        } catch (IOException ioe) {
            //user just closed the socket stream, do nothing
            LOGGER.fine("connection closed by user: " + ioe.getMessage());
        } catch (ServiceException ex) {
            sendError(response, ex);
        }
    }

    /**
     * Checks if the client requests supports gzipped responses by quering it's
     * 'accept-encoding' header.
     *
     * @param request the request to query the HTTP header from
     *
     * @return true if 'gzip' if one of the supported content encodings of
     *         <code>request</code>, false otherwise.
     */
    protected boolean requestSupportsGzip(HttpServletRequest request) {
        boolean supportsGzip = false;
        String header = request.getHeader("accept-encoding");

        if ((header != null) && (header.indexOf("gzip") > -1)) {
            supportsGzip = true;
        }

        if (LOGGER.isLoggable(Level.CONFIG)) {
            LOGGER.config("user-agent=" + request.getHeader("user-agent"));
            LOGGER.config("accept=" + request.getHeader("accept"));
            LOGGER.config("accept-encoding="
                + request.getHeader("accept-encoding"));
        }

        return supportsGzip;
    }

    /**
     * Interface used for ServiceMode strategy objects.
     * 
     * <p>
     * While this interface resembles the Enum idiom in that only three
     * instances are available SPEED, BUFFER and FILE, we are using this class
     * to plug-in the implementation for our doService request in the manner
     * of the strategy pattern.
     * </p>
     *
     * @author Jody Garnett, Refractions Research
     */
    static public interface ServiceStrategy {
        /**
         * Get a OutputStream we can use to add content.
         * 
         * <p>
         * JG - Can we replace this with a Writer?
         * </p>
         *
         * @param response
         *
         * @return
         *
         * @throws IOException
         */
        public OutputStream getDestination(HttpServletResponse response)
            throws IOException;

        /**
         * Complete opperation in the positive.
         * 
         * <p>
         * Gives service a chance to finish with destination, and clean up any
         * resources.
         * </p>
         *
         * @throws IOException DOCUMENT ME!
         */
        public void flush() throws IOException;

        /**
         * Complete opperation in the negative.
         * 
         * <p>
         * Gives ServiceConfig a chance to clean up resources
         * </p>
         */
        public void abort();
    }
}


/**
 * Fast and Dangeroud service strategy.
 * 
 * <p>
 * Will fail when a ServiceException is encountered on writeTo, and will not
 * tell the user about it!
 * </p>
 * 
 * <p>
 * This is the worst case scenario, you are trading speed for danger by using
 * this ServiceStrategy.
 * </p>
 *
 * @author jgarnett
 */
class SpeedStrategy implements AbstractService.ServiceStrategy {
    /** DOCUMENT ME!  */
    private OutputStream out = null;

    /**
     * Works against the real output stream provided by the response.
     * 
     * <p>
     * This is dangerous of course, but fast and exciting.
     * </p>
     *
     * @param response Response provided by doService
     *
     * @return An OutputStream that works against, the response output stream.
     *
     * @throws IOException If response output stream could not be aquired
     */
    public OutputStream getDestination(HttpServletResponse response)
        throws IOException {
        out = response.getOutputStream();
        out = new BufferedOutputStream(out);

        return out;
    }

    /**
     * Completes writing to Response.getOutputStream.
     *
     * @throws IOException If Response.getOutputStream not available.
     */
    public void flush() throws IOException {
        if (out != null) {
            out.flush();
        }
    }

    /* (non-Javadoc)
     * @see org.vfny.geoserver.servlets.AbstractService.ServiceStrategy#abort()
     */
    public void abort() {
        // out.close();
    }
}


/**
 * A safe Service strategy that buffers output until writeTo completes.
 * 
 * <p>
 * This strategy wastes memory, for saftey. It represents a middle ground
 * between SpeedStrategy and FileStrategy
 * </p>
 *
 * @author jgarnett To change the template for this generated type comment go
 *         to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 *         Comments
 */
class BufferStrategy implements AbstractService.ServiceStrategy {
    /** DOCUMENT ME!  */
    ByteArrayOutputStream buffer = null;

    /** DOCUMENT ME!  */
    private HttpServletResponse response;

    /**
     * Provides a ByteArrayOutputStream for writeTo.
     *
     * @param response Response being processed.
     *
     * @return A ByteArrayOutputStream for writeTo opperation.
     *
     * @throws IOException DOCUMENT ME!
     */
    public OutputStream getDestination(HttpServletResponse response)
        throws IOException {
        this.response = response;
        buffer = new ByteArrayOutputStream(1024 * 1024);

        return buffer;
    }

    /**
     * Copies Buffer to Response output output stream.
     *
     * @throws IOException If the response outputt stream is unavailable.
     */
    public void flush() throws IOException {
        if ((buffer == null) || (response == null)) {
            return; // should we throw an Exception here
        }

        OutputStream out = response.getOutputStream();
        BufferedOutputStream buffOut = new BufferedOutputStream(out, 1024 * 1024);
        buffer.writeTo(buffOut);
        buffOut.flush();
    }

    /**
     * Clears the buffer with out writing anything out to response.
     *
     * @see org.vfny.geoserver.servlets.AbstractService.ServiceStrategy#abort()
     */
    public void abort() {
        if (buffer == null) {
            return;
        }
    }
}


/**
 * A safe ServiceConfig strategy that uses a temporary file until writeTo
 * completes.
 *
 * @author $author$
 * @version $Revision: 1.23 $
 */
class FileStrategy implements AbstractService.ServiceStrategy {
    /** Buffer size used to copy safe to response.getOutputStream() */
    private static int BUFF_SIZE = 4096;

    /** Temporary file number */
    static int sequence = 0;

    /** Class logger */
    protected static Logger LOGGER = Logger.getLogger(
            "org.vfny.geoserver.servlets");

    /** Response being targeted */
    private HttpServletResponse response;

    /** OutputStream provided to writeTo method */
    private OutputStream safe;

    /** Temporary file used by safe */
    private File temp;

    /**
     * Provides a outputs stream on a temporary file.
     * 
     * <p>
     * I have changed this to use a BufferedWriter to agree with SpeedStrategy.
     * </p>
     *
     * @param response Response being handled
     *
     * @return Outputstream for a temporary file
     *
     * @throws IOException If temporary file could not be created.
     */
    public OutputStream getDestination(HttpServletResponse response)
        throws IOException {
        // REVISIT: Should do more than sequence here
        // (In case we are running two GeoServers at once)
        // - Could we use response.getHandle() in the filename?
        // - ProcessID is traditional, I don't know how to find that in Java
        this.response = response;
        sequence++;
        temp = File.createTempFile("wfs" + sequence, "tmp");

        safe = new BufferedOutputStream(new FileOutputStream(temp));

        return safe;
    }

    /**
     * Closes safe output stream, copies resulting file to response.
     *
     * @throws IOException If temporay file or response is unavailable
     * @throws IllegalStateException if flush is called before getDestination
     */
    public void flush() throws IOException {
        if ((temp == null) || (response == null) || (safe == null)
                || !temp.exists()) {
            LOGGER.fine("temp is " + temp + ", response is " + response
                + " safe is " + safe + ", temp exists " + temp.exists());
            throw new IllegalStateException(
                "flush should only be called after getDestination");
        }

        InputStream copy = null;

        try {
            safe.flush();
            safe.close();
            safe = null;

            // service succeeded in producing a response!
            // copy result to the real output stream
            copy = new BufferedInputStream(new FileInputStream(temp));

            OutputStream out = response.getOutputStream();
            out = new BufferedOutputStream(out, 1024 * 1024);

            byte[] buffer = new byte[BUFF_SIZE];
            int b;

            while ((b = copy.read(buffer, 0, BUFF_SIZE)) > 0) {
                out.write(buffer, 0, b);
            }

            // Speed Writer closes output Stream
            // I would prefer to leave that up to doService...
            out.flush();

            // out.close();
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            if (copy != null) {
                try {
                    copy.close();
                } catch (Exception ex) {
                }
            }

            copy = null;

            if ((temp != null) && temp.exists()) {
                temp.delete();
            }

            temp = null;
            response = null;
            safe = null;
        }
    }

    /**
     * Clean up after writeTo fails.
     *
     * @see org.vfny.geoserver.servlets.AbstractService.ServiceStrategy#abort()
     */
    public void abort() {
        if (safe != null) {
            try {
                safe.close();
            } catch (IOException ioException) {
            }

            safe = null;
        }

        if ((temp != null) && temp.exists()) {
            temp.delete();
        }

        temp = null;
        response = null;
    }
}
