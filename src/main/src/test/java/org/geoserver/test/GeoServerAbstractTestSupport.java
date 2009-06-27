package org.geoserver.test;

import static org.custommonkey.xmlunit.XMLAssert.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import junit.framework.TestCase;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import org.custommonkey.xmlunit.XMLUnit;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.config.GeoServerLoader;
import org.geoserver.data.test.TestData;
import org.geoserver.logging.LoggingUtils;
import org.geoserver.ows.util.ResponseUtils;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geotools.data.FeatureSource;
import org.geotools.factory.Hints;
import org.geotools.util.logging.Log4JLoggerFactory;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.vfny.geoserver.global.GeoserverDataDirectory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;
import com.mockrunner.mock.web.MockServletConfig;
import com.mockrunner.mock.web.MockServletContext;
import com.mockrunner.mock.web.MockServletOutputStream;
import com.mockrunner.mock.web.MockServletInputStream;

/**
 * Base test class for GeoServer unit tests.
 * <p>
 * This test case provides a spring application context which loads the
 * application contexts from all modules on the classpath.
 * </p>
 * <p>
 * Subclasses should provide a data directory location, that will be inserted
 * in the mock servlet context for GeoServer to pick up
 * </p>
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 * @author Andrea Aime, The Open Planning Project
 */
public abstract class GeoServerAbstractTestSupport extends OneTimeSetupTest {
    /**
     * Common logger for test cases
     */
    protected static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.test"); 
    
    /**
     * Application context
     */
    protected static GeoServerTestApplicationContext applicationContext;

    protected static TestData testData;
    
    /**
     * Returns a test data instance
     * 
     * @return
     */
    protected abstract TestData buildTestData() throws Exception;
    
    public TestData getTestData() {
        return testData;
    }

    /**
     * Override runTest so that the test will be skipped if the TestData is not
     * available
     */
    protected void runTest() throws Throwable {
        if (getTestData().isTestDataAvailable()) {
            super.runTest();
        } else {
            LOGGER.warning("Skipping " + getClass() + "." + getName()
                    + " since test data is not available");
        }
    }

    /**
     * If subclasses override they *must* call super.setUp() first.
     */
    @Override
    protected void oneTimeSetUp() throws Exception {
        if (System.getProperty("org.geotools.referencing.forceXY") == null) {
            System.setProperty("org.geotools.referencing.forceXY", "true");
        }

        Hints.putSystemDefault(Hints.FORCE_AXIS_ORDER_HONORING, "http");

        // setup quiet logging (we need to to this here because Data
        // is loaded before GoeServer has a chance to setup logging for good)
        try {
            Logging.ALL.setLoggerFactory(Log4JLoggerFactory.getInstance());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not configure log4j logging redirection", e);
        }
        System.setProperty(LoggingUtils.RELINQUISH_LOG4J_CONTROL, "true");
        LoggingUtils.configureGeoServerLogging(getClass().getResourceAsStream(getLogConfiguration()), false, true, null);

        //HACK: once we port tests to the new data directory, remove this
        GeoServerLoader.setLegacy( useLegacyDataDirectory() );
        
        // set up test data and, if succeeds, create a mock servlet context and start up the spring configuration
        testData = buildTestData();
        testData.setUp();
        if (testData.isTestDataAvailable()) {
            MockServletContext servletContext = new MockServletContext();
            servletContext.setInitParameter("GEOSERVER_DATA_DIR", testData.getDataDirectoryRoot()
                    .getPath());
            servletContext.setInitParameter("serviceStrategy", "PARTIAL-BUFFER2");

            applicationContext = new GeoServerTestApplicationContext(getSpringContextLocations(),
                    servletContext);
            applicationContext.refresh();

            // set the parameter after a refresh because it appears a refresh
            // wipes
            // out all parameters
            servletContext.setAttribute(
                    WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                    applicationContext);
        }
    }
    
    /**
     * Flag which controls the mock data directory setup.
     * <p>
     * If true is returned, the legacy structure is presevered on sstartup, and no 
     * conversion to the new data directory structure happens.
     * </p>
     * 
     */
    protected boolean useLegacyDataDirectory() {
        return true;
    }
    
    /**
     * Returns the spring context locations to be used in order to build the GeoServer Spring
     * context. Subclasses might want to provide extra locations in order to test extension points.
     * @return
     */
    protected String[] getSpringContextLocations() {
        return new String[] {
                "classpath*:/applicationContext.xml",
                "classpath*:/applicationSecurityContext.xml"
            };
    }

    /**
     * Returns the logging configuration path. The default value is "/TEST_LOGGING.properties", which
     * is a pretty quiet configuration. Should you need more verbose logging override this method
     * in subclasses and choose a different configuration, for example "/DEFAULT_LOGGING.properties".
     * @return
     */
    protected String getLogConfiguration() {
        return "/TEST_LOGGING.properties";
    }

    /**
     * Returns a default services.xml file with WMS, WFS and WCS enabled. Subclasses may
     * need to override this in order to test extra services or specific configurations
     * @return
     */
    protected URL getServicesFile() {
        return GeoServerAbstractTestSupport.class.getResource("services.xml");
    }

    /**
     * Subclasses may override this method to force memory cleaning before the 
     * test data dir is cleaned up. This is necessary on windows if coverages are used in the
     * test, since readers might still be around in the heap as garbage without having
     * been disposed of
     * @return
     */
    protected boolean isMemoryCleanRequired() {
        return false;
    }

    /**
     * If subclasses overide they *must* call super.tearDown() first.
     */
    @Override
    protected void oneTimeTearDown() throws Exception {
        if(getTestData().isTestDataAvailable()) {
            try {
                // kill the context
                applicationContext.destroy();
                
                // kill static caches
                new GeoServerExtensions().setApplicationContext(null);
        
                // some tests do need a kick on the GC to fully clean up
                if(isMemoryCleanRequired()) {
                    System.gc(); 
                    System.runFinalization();
                }
                
                if(getTestData() != null) {
                    // this cleans up the data directory static loader, if we don't the next test
                    // will keep on running on the current data dir
                    GeoserverDataDirectory.destroy();
                    getTestData().tearDown();
                }
            } finally {
                applicationContext = null;
                testData = null;
            }
        }
    }
     
    /**
     * Reloads the catalog and configuration from disk.
     * <p>
     * This method can be used by subclasses from a test method after they have
     * changed the configuration on disk.
     * </p>
     */
    protected void reloadCatalogAndConfiguration() throws Exception {
        GeoServerLoader loader = GeoServerExtensions.bean( GeoServerLoader.class , applicationContext );
        loader.reload();
    }
    
    /**
     * Accessor for global catalog instance from the test application context.
     */
    protected Catalog getCatalog() {
        return (Catalog) applicationContext.getBean("catalog");
    }

    /**
     * Accessor for global geoserver instance from the test application context.
     */
    protected GeoServer getGeoServer() {
        return (GeoServer) applicationContext.getBean("geoServer");
    }
    
    /**
     * Accessor for global resource loader instance from the test application context.
     */
    protected GeoServerResourceLoader getResourceLoader() {
        return (GeoServerResourceLoader) applicationContext.getBean( "resourceLoader" );
    }
    
    protected GeoServerDataDirectory getDataDirectory() {
        return new GeoServerDataDirectory(getResourceLoader());
    }

    /**
     * Loads a feature source from the catalog.
     *
     * @param typeName The qualified type name of the feature source.
     */
    @SuppressWarnings("unchecked")
    protected FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource(QName typeName)
            throws IOException {
        // TODO: expand test support to DataAccess FeatureSource
        FeatureTypeInfo ft = getFeatureTypeInfo(typeName);
        return (FeatureSource<SimpleFeatureType, SimpleFeature>) ft.getFeatureSource(null, null);
    }

    /**
     * Get the FeatureTypeInfo for a featuretype to allow configuration tweaks for tests.
     *
     * @param typename the QName for the type
     */
    protected FeatureTypeInfo getFeatureTypeInfo(QName typename){
        return getCatalog().getFeatureTypeByName( typename.getNamespaceURI(), typename.getLocalPart() ); 
    }

    /**
     * Get the FeatureTypeInfo for a featuretype by the layername that would be used in a request.
     *
     * @param typename the layer name for the type
     */
    protected FeatureTypeInfo getFeatureTypeInfo(String typename){
        return getFeatureTypeInfo(resolveLayerName(typename));
    }

    /**
     * Get the QName for a layer specified by the layername that would be used in a request.
     * @param typename the layer name for the type
     */
    protected QName resolveLayerName(String typename){
        int i = typename.indexOf(":");
        String prefix = typename.substring(0, i);
        String name = typename.substring(i + 1);
        NamespaceInfo ns = getCatalog().getNamespaceByPrefix(prefix);
        QName qname = new QName(ns.getURI(), name, ns.getPrefix());
        return qname;
    }

    /**
     * @deprecated use {@link #getLayerId(QName)}.
     */
    public final String layerId(QName layerName) {
        return getLayerId( layerName );
    }
    
    /**
     * Given a qualified layer name returns a string in the form "prefix:localPart" if prefix
     * is available, "localPart" if prefix is null
     * @param layerName
     * @return
     */
    public String getLayerId(QName layerName) {
        if(layerName.getPrefix() != null)
            return layerName.getPrefix() + ":" + layerName.getLocalPart();
        else
            return layerName.getLocalPart();
    }

    /**
     * Convenience method for subclasses to create mock http servlet requests.
     * <p>
     * Examples of using this method are:
     * <pre>
     * <code>
     *   createRequest( "wfs?request=GetCapabilities" );  //get
     *   createRequest( "wfs" ); //post
     * </code>
     * </pre>
     * </p>
     * @param path The path for the request and optional the query string.
     * @return
     */
    protected MockHttpServletRequest createRequest(String path) {
        MockHttpServletRequest request = new GeoServerMockHttpServletRequest();

        request.setScheme("http");
        request.setServerName("localhost");
        request.setContextPath("/geoserver");
        request.setRequestURI(ResponseUtils.stripQueryString(ResponseUtils.appendPath(
                    "/geoserver/", path)));
        request.setRequestURL(ResponseUtils.appendPath("http://localhost/geoserver", path ) );
        request.setQueryString(ResponseUtils.getQueryString(path));
        request.setRemoteAddr("127.0.0.1");
        request.setServletPath(ResponseUtils.makePathAbsolute( ResponseUtils.stripRemainingPath(path)) );
        request.setPathInfo(ResponseUtils.makePathAbsolute( ResponseUtils.stripBeginningPath( path))); 
        
        kvp(request, path);

        MockHttpSession session = new MockHttpSession();
        session.setupServletContext(new MockServletContext());
        request.setSession(session);

        request.setUserPrincipal(null);

        return request;
    }

    /**
     * Convenience method for subclasses to create mock http servlet requests.
     * <p>
     * Examples of using this method are:
     * <pre>
     * <code>
     *   Map kvp = new HashMap();
     *   kvp.put( "service", "wfs" );
     *   kvp.put( "request", "GetCapabilities" );
     *   
     *   createRequest( "wfs", kvp );
     * </code>
     * </pre>
     * </p>
     * @param path The path for the request, minus any query string parameters.
     * @param kvp The key value pairs to be put in teh query string. 
     * 
     */
    protected MockHttpServletRequest createRequest( String path, Map kvp ) {
        StringBuffer q = new StringBuffer();
        for ( Iterator e = kvp.entrySet().iterator(); e.hasNext(); ) {
            Map.Entry entry = (Map.Entry) e.next();
            q.append( entry.getKey() ).append("=").append( entry.getValue() );
            q.append( "&" );
        }
        q.setLength(q.length()-1);
        
        return createRequest( ResponseUtils.appendQueryString(path, q.toString() ) );
    }
    
    /**
     * Executes an ows request using the GET method.
     *
     * @param path The porition of the request after hte context, 
     *      example: 'wms?request=GetMap&version=1.1.1&..."
     * 
     * @return An input stream which is the result of the request.
     * 
     * @throws Exception
     */
    protected InputStream get( String path ) throws Exception {
        MockHttpServletResponse response = getAsServletResponse(path);
        return new ByteArrayInputStream( response.getOutputStreamContent().getBytes() );
    }
    
    /**
     * Executes an ows request using the GET method.
     *
     * @param path The porition of the request after hte context, 
     *      example: 'wms?request=GetMap&version=1.1.1&..."
     * 
     * @return the mock servlet response
     * 
     * @throws Exception
     */
    protected MockHttpServletResponse getAsServletResponse( String path ) throws Exception {
        MockHttpServletRequest request = createRequest( path ); 
        request.setMethod( "GET" );
        request.setBodyContent(new byte[]{});
        
        return dispatch( request );
    }
        
    /**
     * Executes an ows request using the POST method with key value pairs 
     * form encoded. 
     *
     * @param path The porition of the request after hte context, 
     *      example: 'wms?request=GetMap&version=1.1.1&..."
     * 
     * @return An input stream which is the result of the request.
     * 
     * @throws Exception
     */
    protected InputStream post( String path ) throws Exception {
        MockHttpServletRequest request = createRequest( path ); 
        request.setMethod( "POST" );
        request.setContentType( "application/x-www-form-urlencoded" );
        request.setBodyContent(new byte[]{});
        
        MockHttpServletResponse response = dispatch( request );
        return new ByteArrayInputStream( response.getOutputStreamContent().getBytes() );
    }

    /**
     * Executes a request with an empty body using the PUT method.
     *
     * @param path the portion of the request after the context, for example:
     *      "api/datastores.xml"
     *
     * @throws Exception
     */
    protected InputStream put(String path) throws Exception{
        return put(path, "");
    }

    /**
     * Executes a request with a default mimetype using the PUT method.
     *
     * @param path the portion of the request after the context, for example:
     *      "api/datastores.xml"
     * @param body the content to send as the body of the request
     *
     * @throws Exception
     */
    protected InputStream put(String path, String body) throws Exception{
        return put(path, body, "text/plain");
    }

    /**
     * Executes a request using the PUT method.
     *
     * @param path the portion of the request after the context, for example:
     *      "api/datastores.xml"
     * @param body the content to send as the body of the request
     * @param contentType the mime-type to set for the request being sent
     *
     * @throws Exception
     */
    protected InputStream put(String path, String body, String contentType) throws Exception {
        return put( path, body.getBytes(), contentType );
    }

    /**
     * Executes a request using the PUT method.
     *
     * @param path the portion of the request after the context, for example:
     *      "api/datastores.xml"
     * @param body the content to send as the body of the request
     * @param contentType the mime-type to set for the request being sent
     *
     * @throws Exception
     */
    protected InputStream put(String path, byte[] body, String contentType) throws Exception {
        MockHttpServletResponse response = putAsServletResponse(path, body, contentType);
        return new ByteArrayInputStream(response.getOutputStreamContent().getBytes());
    }
    
    protected MockHttpServletResponse putAsServletResponse(String path) throws Exception {
        return putAsServletResponse(path, new byte[]{}, "text/plain");
    }
    
    protected MockHttpServletResponse putAsServletResponse(String path, String body, String contentType ) 
    throws Exception {
        return putAsServletResponse(path, body != null ? body.getBytes() : (byte[]) null, contentType);
    }
    
    protected MockHttpServletResponse putAsServletResponse(String path, byte[] body, String contentType ) 
        throws Exception {
        
        MockHttpServletRequest request = createRequest(path);
        request.setMethod("PUT");
        request.setContentType(contentType);
        request.setBodyContent(body);
        request.setHeader( "Content-type", contentType );

        return dispatch(request);
    }
    
    /**
     * Executes an ows request using the POST method.
     * <p>
     * 
     * </p>
     * @param path The porition of the request after the context ( no query string ), 
     *      example: 'wms'. 
     * 
     * @return An input stream which is the result of the request.
     * 
     * @throws Exception
     */
    protected InputStream post( String path , String xml ) throws Exception {
        MockHttpServletResponse response = postAsServletResponse(path, xml);
        return new ByteArrayInputStream(response.getOutputStreamContent().getBytes());
    }

    /**
     * Executes an ows request using the POST method, with xml as body content. 
     * 
     * 
     * @param path
     *            The porition of the request after the context ( no query
     *            string ), example: 'wms'.
     * @param xml The body content.
     * 
     * @return the servlet response
     * 
     * @throws Exception
     */
    protected MockHttpServletResponse postAsServletResponse(String path, String xml)
            throws Exception {
        
        return postAsServletResponse(path, xml, "application/xml");
    }

    /**
     * Extracts the true binary stream out of the response. The usual way (going
     * thru {@link MockHttpServletResponse#getOutputStreamContent()}) mangles
     * bytes if the content is not made of chars.
     * 
     * @param response
     * @return
     */
    protected ByteArrayInputStream getBinaryInputStream(MockHttpServletResponse response) {
        try {
            MockServletOutputStream os = (MockServletOutputStream) response.getOutputStream();
            final Field field = os.getClass().getDeclaredField("buffer");
            field.setAccessible(true);
            ByteArrayOutputStream bos = (ByteArrayOutputStream) field.get(os);
            return new ByteArrayInputStream(bos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Whoops, did you change the MockRunner version? "
                    + "If so, you might want to change this method too");
        }
    }
            
            
    /**
     * Executes an ows request using the POST method.
     * 
     * @param path
     *            The porition of the request after the context ( no query
     *            string ), example: 'wms'.
     * 
     * @param body
     *            the body of the request
     * @param contentType
     *            the mimetype to set for the request
     * 
     * @return An input stream which is the result of the request.
     * 
     * @throws Exception
     */
    protected InputStream post(String path, String body, String contentType) throws Exception{
        MockHttpServletResponse response = postAsServletResponse(path, body, contentType);
        return new ByteArrayInputStream(response.getOutputStreamContent().getBytes());
    }
    
    protected MockHttpServletResponse postAsServletResponse(String path, String body, String contentType) throws Exception {
        MockHttpServletRequest request = createRequest(path);
        request.setMethod("POST");
        request.setContentType(contentType);
        request.setBodyContent(body);
        request.setHeader("Content-type",  contentType );

        return dispatch(request);
    }
    
    /**
     * Execultes a request using the DELETE method.
     * 
     * @param path The path of the request.
     * 
     * @return The http status code.
     */
    protected MockHttpServletResponse deleteAsServletResponse(String path) throws Exception {
        MockHttpServletRequest request = createRequest(path);
        request.setMethod("DELETE");
        
        return dispatch(request);
    }
    
    /**
     * Executes an ows request using the GET method and returns the result as an 
     * xml document.
     * 
     * @param path The portion of the request after the context, 
     *      example: 'wms?request=GetMap&version=1.1.1&..."
     * @param the list of validation errors encountered during document parsing (validation
     *        will be activated only if this list is non null)
     * 
     * @return A result of the request parsed into a dom.
     * 
     * @throws Exception
     */
    protected Document getAsDOM(final String path)
            throws Exception {
        return getAsDOM(path, true);
    }
    
    /**
     * Executes a request using the GET method and parses the result as a json object.
     * 
     * @param path The path to request.
     *  
     * @return The result parsed as json.
     */
    protected JSON getAsJSON(final String path) throws Exception {
        BufferedReader in = new BufferedReader( new InputStreamReader ( get( path ) ) );
        StringBuffer json = new StringBuffer();
        String line = null;
        while( ( line = in.readLine() ) != null ) {
            json.append( line );
        }
        in.close();
        
        return JSONSerializer.toJSON( json.toString() );
    }

    /**
     * Executes an ows request using the GET method and returns the result as an xml document.
     * 
     * @param path
     *                The portion of the request after the context, example:
     *                'wms?request=GetMap&version=1.1.1&..."
     * @param skipDTD
     *                if true, will avoid loading and validating against the response document
     *                schema or DTD
     * 
     * @return A result of the request parsed into a dom.
     * 
     * @throws Exception
     */
    protected Document getAsDOM(final String path, final boolean skipDTD)
    throws Exception {
        return dom(get(path), skipDTD);
    }
    
    /**
     * Executes an ows request using the POST method with key value pairs 
     * form encoded, returning the result as a dom.
     *
     * @param path The porition of the request after hte context, 
     *      example: 'wms?request=GetMap&version=1.1.1&..."
     * @param the list of validation errors encountered during document parsing (validation
     *        will be activated only if this list is non null)     
     * 
     * @return An input stream which is the result of the request.
     * 
     * @throws Exception
     */
    protected Document postAsDOM( String path ) throws Exception {
        return postAsDOM(path, (List<Exception>) null);
    }
    
    /**
     * Executes an ows request using the POST method with key value pairs 
     * form encoded, returning the result as a dom.
     *
     * @param path The porition of the request after hte context, 
     *      example: 'wms?request=GetMap&version=1.1.1&..."
     * 
     * @return An input stream which is the result of the request.
     * 
     * @throws Exception
     */
    protected Document postAsDOM( String path, List<Exception> validationErrors ) throws Exception {
        return dom( post( path ));
    }
    
    /**
     * Executes an ows request using the POST method and returns the result as an
     * xml document.
     * <p>
     * 
     * </p>
     * @param path The porition of the request after the context ( no query string ), 
     *      example: 'wms'. 
     * 
     * @return An input stream which is the result of the request.
     * 
     * @throws Exception
     */
    protected Document postAsDOM( String path, String xml ) throws Exception {
            return postAsDOM(path, xml, null);
    }
    
    /**
     * Executes an ows request using the POST method and returns the result as an
     * xml document.
     * <p>
     * 
     * </p>
     * @param path The porition of the request after the context ( no query string ), 
     *      example: 'wms'. 
     * 
     * @return An input stream which is the result of the request.
     * 
     * @throws Exception
     */
    protected Document postAsDOM( String path, String xml, List<Exception> validationErrors ) throws Exception {
            return dom( post( path, xml ));
    }
    
    protected String getAsString(String path) throws Exception {
        return string(get(path));
    }
    
    /**
     * Parses a stream into a dom.
     */
    protected Document dom(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        return dom(is, true);
    }
    
    /**
     * Parses a stream into a dom.
     * @param input
     * @param skipDTD If true, will skip loading and validating against the associated DTD
     */
    protected Document dom(InputStream input, boolean skipDTD) throws ParserConfigurationException, SAXException, IOException {
        if(skipDTD) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
            factory.setNamespaceAware( true );
            factory.setValidating( false );
           
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new EmptyResolver());
            Document dom = builder.parse( input );
    
            return dom;
        } else {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(input);
        }
    }
    
    /**
     * Resolves everything to an empty xml document, useful for skipping errors due to missing
     * dtds and the like
     * @author Andrea Aime - TOPP
     */
    static class EmptyResolver implements org.xml.sax.EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId)
                throws org.xml.sax.SAXException, IOException {
            StringReader reader = new StringReader(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            InputSource source = new InputSource(reader);
            source.setPublicId(publicId);
            source.setSystemId(systemId);

            return source;
        }
    }
            
    protected void checkValidationErorrs(Document dom, String schemaLocation) throws SAXException, IOException {
        final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new File(schemaLocation));
        checkValidationErrors(dom, schema);
    }

    /**
     * Given a dom and a schema, checks that the dom validates against the schema 
     * of the validation errors instead
     * @param validationErrors
     * @throws IOException 
     * @throws SAXException 
     */
    protected void checkValidationErrors(Document dom, Schema schema) throws SAXException, IOException {
        final Validator validator = schema.newValidator();
        final List<Exception> validationErrors = new ArrayList<Exception>();
        validator.setErrorHandler(new ErrorHandler() {
            
            public void warning(SAXParseException exception) throws SAXException {
                System.out.println(exception.getMessage());
            }

            public void fatalError(SAXParseException exception) throws SAXException {
                validationErrors.add(exception);
            }

            public void error(SAXParseException exception) throws SAXException {
                validationErrors.add(exception);
            }

          });
        validator.validate(new DOMSource(dom));
        if (validationErrors != null && validationErrors.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Exception ve : validationErrors) {
                sb.append(ve.getMessage()).append("\n");
            }
            fail(sb.toString());
        }
    }
    
    /**
     * Performs basic checks on an OWS 1.1 exception, to ensure it's well formed
     */
    protected void checkOws11Exception(Document dom) throws Exception {
        checkOws11Exception(dom,null);
    }
    /**
     * Performs basic checks on an OWS 1.1 exception, to ensure it's well formed
     * and ensuring that a particular exceptionCode is used.
     */
    protected void checkOws11Exception(Document dom, String exceptionCode) throws Exception {
        Element root = dom.getDocumentElement();
        assertEquals("ows:ExceptionReport", root.getNodeName() );
        assertEquals( "1.1.0", root.getAttribute( "version") );
        assertEquals("http://www.opengis.net/ows/1.1", root.getAttribute( "xmlns:ows"));
        
        if ( exceptionCode != null ) {
            assertEquals( 1, dom.getElementsByTagName( "ows:Exception").getLength() );
            Element ex = (Element) dom.getElementsByTagName( "ows:Exception").item(0);
            assertEquals( exceptionCode, ex.getAttribute( "exceptionCode") );
        }
    }
        
    
    /**
     * Parses a stream into a String
     */
    protected String string(InputStream input) throws Exception {
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        char[] buf = new char[8192];
        try {
            reader = new BufferedReader(new InputStreamReader(input));
            String line = null;
            while((line = reader.readLine()) != null)
                sb.append(line);
        } finally {
            if(reader != null)
                reader.close();
        }
        return sb.toString();
    }
    
    /**
     * Utility method to print out a dom.
     */
    protected void print( Document dom ) throws Exception {
        TransformerFactory txFactory = TransformerFactory.newInstance();
        try {
            txFactory.setAttribute("{http://xml.apache.org/xalan}indent-number", new Integer(2));
        } catch(Exception e) {
            // some 
        }
        
        Transformer tx = txFactory.newTransformer();
        tx.setOutputProperty(OutputKeys.METHOD,"xml");
        tx.setOutputProperty( OutputKeys.INDENT, "yes" );
          
        tx.transform( new DOMSource( dom ), new StreamResult(new OutputStreamWriter(System.out, "utf-8") ));
    }

    /**
     * Utility method to print out the contents of an input stream.
     */
    protected void print( InputStream in ) throws Exception {
        BufferedReader r = new BufferedReader( new InputStreamReader( in ) );
        String line = null;
        while( (line = r.readLine()) != null ) {
            System.out.println( line );
        }
    }
    
    /**
     * Utility method to print out the contents of a json object.
     */
    protected void print( JSON json ) {
        System.out.println(json.toString(2));
    }
    
    /**
     * Convenience method for element.getElementsByTagName() to return the 
     * first element in the resulting node list.
     */
    protected Element getFirstElementByTagName( Element element, String name ) {
        NodeList elements = element.getElementsByTagName(name);
        if ( elements.getLength() > 0 ) {
            return (Element) elements.item(0);
        }
        
        return null;
    }
    
    /**
     * Convenience method for element.getElementsByTagName() to return the 
     * first element in the resulting node list.
     */
    protected Element getFirstElementByTagName( Document dom, String name ) {
        return getFirstElementByTagName( dom.getDocumentElement(), name );
    }
    
    /*
     * Helper method to create the kvp params from the query string.
     */
    private void kvp(MockHttpServletRequest request, String path) {
        int index = path.indexOf('?');

        if (index == -1) {
            return;
        }

        String queryString = path.substring(index + 1);
        StringTokenizer st = new StringTokenizer(queryString, "&");

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            String[] keyValuePair = token.split("=");
            
            //check for any special characters
            if ( keyValuePair.length > 1 ) {
                //replace any equals or & characters
            	try {
            		// if this one does not work first check if the url encoded content is really
            		// properly encoded. I had good success with this: http://meyerweb.com/eric/tools/dencoder/
            		keyValuePair[1] = URLDecoder.decode(keyValuePair[1], "ISO-8859-1");
            	} catch(UnsupportedEncodingException e) {
            		throw new RuntimeException("Totally unexpected... is your JVM busted?", e);
            	}
                
            }
            request.setupAddParameter(keyValuePair[0], keyValuePair.length > 1 ?  keyValuePair[1]: "");
        }
    }

    private MockHttpServletResponse dispatch( HttpServletRequest request ) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse() {
            public void setCharacterEncoding( String encoding ) {
                    
            }
        };

        dispatch(request, response);
        return response;
    } 
 
    private void dispatch(HttpServletRequest request, HttpServletResponse response) throws Exception{
        //create an instance of the spring dispatcher
        ServletContext context = applicationContext.getServletContext();
        
        MockServletConfig config = new MockServletConfig();
        config.setServletContext(context);
        config.setServletName("dispatcher");
        
        DispatcherServlet dispatcher = new DispatcherServlet();
        
        dispatcher.setContextConfigLocation(GeoServerAbstractTestSupport.class.getResource("dispatcher-servlet.xml").toString());
        dispatcher.init(config);
        
        
        //look up the handler
//        Dispatcher dispatcher = 
//                (Dispatcher) applicationContext.getBean( "dispatcher" ); 
        //dispatcher.setApplicationContext( getGeoServer().getApplicationContext() );
        
        //excute the pre handler step
        Collection interceptors = 
            GeoServerExtensions.extensions(HandlerInterceptor.class, applicationContext );
        for ( Iterator i = interceptors.iterator(); i.hasNext(); ) {
            HandlerInterceptor interceptor = (HandlerInterceptor) i.next();
            interceptor.preHandle( request, response, dispatcher );
        }
        
        //execute 
        //dispatcher.handleRequest( request, response );
        dispatcher.service(request, response);
        
        //execute the post handler step
        for ( Iterator i = interceptors.iterator(); i.hasNext(); ) {
            HandlerInterceptor interceptor = (HandlerInterceptor) i.next();
            interceptor.postHandle( request, response, dispatcher, null );
        }
    }

    /**
     * Assert that a GET request to a path will have a particular status code for the response.
     * @param code the number of the HTTP status code that is expected
     * @param path the path to which a GET request should be made, without the protocol, server and servlet context.
     * For example, to make a request to "http://localhost:8080/geoserver/ows" the path would be "ows"
     *
     * @throws Exception on test failure
     */
    protected void assertStatusCodeForGet(int code, String path) throws Exception{
        assertStatusCodeForRequest(code, "GET", path, "", "");
    }

    /**
     * Assert that a POST request to a path will have a particular status code for the response.
     * @param code the number of the HTTP status code that is expected
     * @param path the path to which a POST request should be made, without the protocol, server and servlet context.
     * For example, to make a request to "http://localhost:8080/geoserver/ows" the path would be "ows"
     * @param body the body to send with the request. May be empty, but must not be null.
     * @param type the mimetype to report for the body
     *
     * @throws Exception on test failure
     */
    protected void assertStatusCodeForPost(int code, String path, String body, String type) throws Exception {
        assertStatusCodeForRequest(code, "POST", path, body, type);
    }

    /**
     * Assert that a PUT request to a path will have a particular status code for the response.
     * @param code the number of the HTTP status code that is expected
     * @param path the path to which a PUT request should be made, without the protocol, server and servlet context.
     * For example, to make a request to "http://localhost:8080/geoserver/ows" the path would be "ows"
     * @param body the body to send with the request. May be empty, but must not be null.
     * @param type the mimetype to report for the body
     *
     * @throws Exception on test failure
     */
    protected void assertStatusCodeForPut(int code, String path, String body, String type) throws Exception {
        assertStatusCodeForRequest(code, "PUT", path, body, type);
    }

    /**
     * Assert that an HTTP request will have a particular status code for the response.
     * @param code the number of the HTTP status code that is expected
     * @param method the HTTP method for the request (eg, GET, PUT)
     * @param path the path for the request, excluding the protocol, server, port, and servlet context.
     * For example, to make a request to "http://localhost:8080/geoserver/ows" the path would be "ows"
     * @param body the body for the request.  May be empty, but must not be null.
     * @param type the mimetype for the request.
     */
    protected void assertStatusCodeForRequest(int code, String method, String path, String body, String type) throws Exception {
        MockHttpServletRequest request = createRequest(path);
        request.setMethod(method);
        request.setBodyContent(body);
        request.setContentType(type);

        CodeExpectingHttpServletResponse response = new CodeExpectingHttpServletResponse(new MockHttpServletResponse());
        dispatch(request, response);
        assertEquals(code, response.getErrorCode());
    }

    public static class GeoServerMockHttpServletRequest extends MockHttpServletRequest {
        private byte[] myBody;
        
        @Override
        public void setBodyContent(byte[] body) {
            myBody = body;
        }
        
        @Override
        public void setBodyContent(String body) {
            myBody = body.getBytes();
        }
        
        public ServletInputStream getInputStream(){
            return new GeoServerMockServletInputStream(myBody);
        }
    }

    private static class GeoServerMockServletInputStream extends ServletInputStream {
        private byte[] myBody;
        private int myOffset = 0;
        private int myMark = -1;

        public GeoServerMockServletInputStream(byte[] body){
            myBody = body;
        }
        
        public int available() {
            return myBody.length - myOffset;
        }

        public void close(){}

        public void mark(int readLimit){
            myMark = myOffset;
        }
        
        public void reset() {
            if (myMark < 0 || myMark >= myBody.length){
                throw new IllegalStateException("Can't reset when no mark was set.");
            }
            
            myOffset = myMark;
        }

        public boolean markSupported(){ return true; }

            public int read(){
                byte[] b = new byte[1];
                return read(b, 0, 1) == -1 ? -1 : b[0];
            }

        public int read(byte[] b){
            return read(b, 0, b.length);
        }

        public int read(byte[] b, int offset, int length){
            int realOffset = offset + myOffset;
            int i;

            if ( realOffset >= myBody.length ) {
                return -1;
            }
            for (i = 0; (i < length) && (i + myOffset < myBody.length); i++){
                b[offset + i] = myBody[myOffset + i];
            }

            myOffset += i;

            return i;
        }

        public int readLine(byte[] b, int offset, int length){
            int realOffset = offset + myOffset;
            int i;

            for (i = 0; (i < length) && (i + myOffset < myBody.length); i++){
                b[offset + i] = myBody[myOffset + i];
                if (myBody[myOffset + i] == '\n') break;
            }

            myOffset += i;

            return i;
        }
    }
}
