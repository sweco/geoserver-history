/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.filters;

import java.io.BufferedReader;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import junit.framework.Test;

public class BufferedRequestWrapperTest extends RequestWrapperTestSupport{

	/**
     * This is a READ ONLY TEST so we can use one time setup
     */
    public static Test suite() {
        return new OneTimeTestSetup(new BufferedRequestWrapperTest());
    }

	public void testGetInputStream() throws Exception{
		for (int i = 0; i < testStrings.length; i++){
			doInputStreamTest(testStrings[i]);
		}
	}

	public void testGetReader() throws Exception{
	    for (int i = 0; i < testStrings.length; i++){
			doGetReaderTest(testStrings[i]);
		}
	}

	public void doInputStreamTest(String testString) throws Exception{
		HttpServletRequest req = makeRequest(testString, null);

		BufferedRequestWrapper wrapper = new BufferedRequestWrapper(req, testString);
		ServletInputStream sis = req.getInputStream();
		byte b[] = new byte[32];
		int amountRead;

		while (( sis.readLine(b, 0, 32)) > 0){ /*clear out the request body*/ }

		sis = wrapper.getInputStream();
		StringBuffer buff = new StringBuffer();

		while ((amountRead = sis.readLine(b, 0, 32)) != 0){
			buff.append(new String(b, 0, amountRead));
		}

		assertEquals(buff.toString(), testString);
	}

    public void doGetReaderTest(String testString) throws Exception{
		HttpServletRequest req = makeRequest(testString, null);

		BufferedReader br = req.getReader();
		while ((br.readLine()) != null){ /* clear out the body */ }

		BufferedRequestWrapper wrapper = new BufferedRequestWrapper(req, testString);
		StringBuffer buff = new StringBuffer();
        int c;
		br = wrapper.getReader();
		
		while ((c = br.read()) != -1){
			buff.append((char)c);
		}

		assertEquals(buff.toString(), testString);
	}
    
    public void testMixedRequest() throws Exception {
        String body = "a=1&b=2";
        String queryString = "c=3&d=4";
        HttpServletRequest req = makeRequest(body, queryString);

        BufferedReader br = req.getReader();
        while ((br.readLine()) != null){ /* clear out the body */ }

        BufferedRequestWrapper wrapper = new BufferedRequestWrapper(req, body);
        Map params = wrapper.getParameterMap();
        assertEquals(4, params.size());
        assertEquals("1", ((String[]) params.get("a"))[0]);
        assertEquals("2", ((String[]) params.get("b"))[0]);
        assertEquals("3", ((String[]) params.get("c"))[0]);
        assertEquals("4", ((String[]) params.get("d"))[0]);
    }
}
