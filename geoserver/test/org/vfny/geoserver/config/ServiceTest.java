/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.vfny.geoserver.config;

import junit.framework.TestCase;
/**
 * ServiceTest purpose.
 * <p>
 * Description of ServiceTest ...
 * <p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @version $Id: ServiceTest.java,v 1.1.2.3 2004/01/08 18:44:29 dmzwiers Exp $
 */
public class ServiceTest extends TestCase {

	private ServiceConfig a,b;
	
	/**
	 * Constructor for ServiceTest.
	 * @param arg0
	 */
	public ServiceTest(String arg0) {
		super(arg0);
		a = new ServiceConfig();
		a.setName("test 1");
		a.setMaintainer("tester 1");
	}

	/*
	 * Test for void NameSpaceConfig(NameSpaceConfig)
	 */
	/*public void testNameSpaceNameSpace() {
		//test requires equals.
		b = new ServiceConfig(a);
		assertTrue("Testing ContactConfig(ContactConfig)\nRelies on ContactConfig.equals.",a.equals(b));
	}*/

	/*
	 * Test for Object clone()
	 */
	/*public void testClone() {
		//test requires equals.
		b =(ServiceConfig)a.clone();
		assertTrue("Testing ContactConfig(ContactConfig)\nRelies on ContactConfig.equals.",a.equals(b));
	}*/

	/*
	 * Test for boolean equals(Object)
	 */
	public void testEqualsObject() {
		b = new ServiceConfig();
		b.setName("test 1");
		b.setMaintainer("tester 1");
		assertTrue(a.equals(b));
		
		b.setName("test 2");
		assertTrue(!a.equals(b));
		
		b.setName("test 1");
		b.setMaintainer("tester 2");
		assertTrue(!a.equals(b));
	}
}
