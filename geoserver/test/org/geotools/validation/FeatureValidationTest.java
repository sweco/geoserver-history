/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.validation;

import org.geotools.data.DataTestCase;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.validation.spatial.IsValidGeometryFeatureValidation;


/**
 * FeatureValidationTest purpose.
 * 
 * <p>
 * Description of FeatureValidationTest ...
 * </p>
 * 
 * <p></p>
 *
 * @author jgarnett, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: FeatureValidationTest.java,v 1.4 2004/01/21 18:42:25 jive Exp $
 */
public class FeatureValidationTest extends DataTestCase {
    MemoryDataStore store;

    /**
     * FeatureValidationTest constructor.
     * 
     * <p>
     * Run test <code>testName</code>.
     * </p>
     *
     * @param testName
     */
    public FeatureValidationTest(String testName) {
        super(testName);
    }

    /**
     * Construct data store for use.
     *
     * @throws Exception
     *
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        store = new MemoryDataStore();
        store.addFeatures(roadFeatures);
        store.addFeatures(riverFeatures);
    }

    /**
     * Override tearDown.
     *
     * @throws Exception
     *
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        store = null;
        super.tearDown();
    }

    public void testIsValidFeatureValidation() {
        // the visitor
        RoadValidationResults validationResults = new RoadValidationResults();

        IsValidGeometryFeatureValidation validator = new IsValidGeometryFeatureValidation("isValidRoad",
                "Tests to see if a road is valid",
                IsValidGeometryFeatureValidation.ALL);
        validationResults.setValidation(validator);
        assertTrue(validator.validate(this.newRoad, this.roadType,
                validationResults));

        try {
            this.newRoad = this.roadType.create(new Object[] {
                        new Integer(2), line(new int[] { 1, 2, 1, 2 }), "r4"
                    }, "road.rd4");
        } catch (IllegalAttributeException e) {
        }

        assertTrue(!validator.validate(this.newRoad, this.roadType,
                validationResults)); // validate will return false
    }

    /**
     * testABunchOfValidations purpose.
     * 
     * <p>
     * Description ...
     * </p>
     */
    public void testABunchOfValidations() {
        // the visitor
        RoadNetworkValidationResults roadValidationResults = new RoadNetworkValidationResults();

        // various FeatureType tests
        IsValidGeometryFeatureValidation isValidValidator1 = new IsValidGeometryFeatureValidation("isValidRoad",
                "Tests to see if a road is valid", new String[] { "roads" });
        IsValidGeometryFeatureValidation isValidValidator2 = new IsValidGeometryFeatureValidation("isValidRail",
                "Tests to see if a railway is valid",
                new String[] { "roads", "rails" });
        IsValidGeometryFeatureValidation isValidValidator3 = new IsValidGeometryFeatureValidation("isValidRiver",
                "Tests to see if a river is valid", new String[] { "rivers" });
        IsValidGeometryFeatureValidation isValidValidator4 = new IsValidGeometryFeatureValidation("isValidAll",
                "Tests to see if all geometries are valid",
                IsValidGeometryFeatureValidation.ALL);

        // various Integrity tests
        //
        //
        //
        // tell the RoadValidator what tests to use
        roadValidationResults.setValidation(isValidValidator1);
        roadValidationResults.setValidation(isValidValidator2);
        roadValidationResults.setValidation(isValidValidator3);
        roadValidationResults.setValidation(isValidValidator4);

        // run each feature validation test on the featureTypes it tests
        String[] types1 = isValidValidator1.getTypeNames();

        for (int i = 0; i < types1.length; i++) {
            //isValidValidator1.validate(featuresToTest(types[i]), featureType(types[i]), roadValidationResults);
        }

        String[] types2 = isValidValidator1.getTypeNames();

        for (int i = 0; i < types2.length; i++) {
            //isValidValidator2.validate(featuresToTest(types[i]), featureType(types[i]), roadValidationResults);
        }

        String[] types3 = isValidValidator1.getTypeNames();

        for (int i = 0; i < types3.length; i++) {
            //isValidValidator3.validate(featuresToTest(types[i]), featureType(types[i]), roadValidationResults);
        }

        String[] types4 = isValidValidator1.getTypeNames();

        for (int i = 0; i < types4.length; i++) {
            //isValidValidator4.validate(featuresToTest(types[i]), featureType(types[i]), roadValidationResults);
        }

        // check the results of the roadValidator
        String[] roadFailures = roadValidationResults.getFailedMessages();
        String[] roadWarnings = roadValidationResults.getWarningMessages();
        boolean roadsPassed = true;

        if (roadFailures.length != 0) {
            roadsPassed = false;

            for (int i = 0; i < roadFailures.length; i++) {
                System.out.println(roadFailures[i]);
            }
        }

        if (roadWarnings.length != 0) {
            for (int i = 0; i < roadWarnings.length; i++) {
                System.out.println(roadWarnings[i]);
            }
        }

        assertTrue(roadsPassed);
    }
}
