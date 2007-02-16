/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.requests;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.geotools.filter.FidFilter;
import org.vfny.geoserver.util.requests.readers.KvpRequestReader;
import org.vfny.geoserver.wfs.requests.DeleteRequest;
import org.vfny.geoserver.wfs.requests.TransactionRequest;
import org.vfny.geoserver.wfs.requests.readers.DeleteKvpReader;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Tests the Delete request handling.
 *
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 * @version $Id: DeleteSuite.java,v 1.13 2004/01/31 00:17:52 jive Exp $
 */
public class DeleteSuiteTest extends TransactionSuiteTest {
    // Initializes the logger. Uncomment to see log messages.
    //static {
    //    org.vfny.geoserver.config.Log4JFormatter.init("org.vfny.geoserver", Level.FINEST);
    //}
    /** Class logger */
    private static final Logger LOGGER = Logger.getLogger("org.vfny.geoserver.requests");

    /**
         * Constructor with super.
         *
         * @param testName DOCUMENT ME!
         */
    public DeleteSuiteTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Delete tests");
        suite.addTestSuite(DeleteSuiteTest.class);

        return suite;
    }

    /**
     * Handles actual KVP test running details.
     *
     * @param kvps Base request, for comparison.
     *
     * @return <tt>true</tt> if the test passed.
     */

    /*    private static boolean runKvpTest(TransactionRequest baseRequest,
       String requestString, boolean match) throws Exception {
       // Read the file and parse it
       DeleteKvpReader reader = new DeleteKvpReader(requestString);
       TransactionRequest request = reader.getRequest();
       LOGGER.finer("base request: " + baseRequest);
       LOGGER.finer("read request: " + request);
       LOGGER.fine("KVP test passed: " + baseRequest.equals(request));
       // Compare parsed request to base request
       if (match) {
           //return baseRequest.equals(request);
           return baseRequest.equals(request);
       } else {
           return !baseRequest.equals(request);
       }
       }*/
    protected KvpRequestReader getKvpReader(Map kvps) {
        return new DeleteKvpReader(kvps, service);
    }

    /* ********************************************************************
     * KVP TESTS
     * KVP GetFeature parsing tests.  Each test reads from a specific KVP
     * string and compares it to the base request defined in the test itself.
     * Tests are run via the static methods in this suite.  The tests
     * themselves are quite generic, so documentation is minimal.
     * *********************************************************************/

    /**
     * Example 1 from the WFS 1.0 specification.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testKVP1() throws Exception {
        String testRequest = "VERSION=1.0.0&" + "SERVICE=WFS&" + "REQUEST=TRANSACTION&"
            + "OPERATION=delete&" + "TYPENAME=rail&" + "featureID=123";

        // make base comparison objects        
        TransactionRequest baseRequest = new TransactionRequest(service);
        DeleteRequest internalRequest = new DeleteRequest();
        internalRequest.setTypeName("rail");

        FidFilter filter = factory.createFidFilter("123");
        internalRequest.setFilter(filter);
        baseRequest.addSubRequest(internalRequest);

        // run test       
        assertTrue(runKvpTest(baseRequest, testRequest, true));
    }

    public void testXml1() throws Exception {
        // make base comparison objects        
        DeleteRequest delete = new DeleteRequest();
        delete.setFilter(factory.createFidFilter("123"));

        TransactionRequest baseRequest = new TransactionRequest(service);
        baseRequest.addSubRequest(delete);

        // run test       
        assertTrue(runXmlTest(baseRequest, "22", true));
    }

    public void testXml2() throws Exception {
        // make base comparison objects        
        DeleteRequest delete = new DeleteRequest();
        FidFilter tempFilter = factory.createFidFilter("123");
        tempFilter.addFid("124");
        tempFilter.addFid("1023");
        tempFilter.addFid("16");
        tempFilter.addFid("5001");
        delete.setFilter(tempFilter);

        TransactionRequest baseRequest = new TransactionRequest(service);
        baseRequest.addSubRequest(delete);

        // run test       
        assertTrue(runXmlTest(baseRequest, "23", true));
    }

    /*  Need updated geotools jar...big fix takes care of this problem
       The fix is in cvs right now, hopefully release will come soon. */
    public void testXml3() throws Exception {
        // make base comparison objects
        DeleteRequest delete1 = new DeleteRequest();
        FidFilter temp1 = factory.createFidFilter("123");
        temp1.addFid("124");
        delete1.setFilter(temp1);

        DeleteRequest delete2 = new DeleteRequest();
        FidFilter temp2 = factory.createFidFilter("1023");
        temp2.addFid("16");
        delete2.setFilter(temp2);

        DeleteRequest delete3 = new DeleteRequest();
        delete3.setFilter(factory.createFidFilter("5001"));

        TransactionRequest baseRequest = new TransactionRequest(service);
        baseRequest.addSubRequest(delete1);
        baseRequest.addSubRequest(delete2);
        baseRequest.addSubRequest(delete3);

        // run test
        assertTrue(runXmlTest(baseRequest, "24", true));
    }
}
