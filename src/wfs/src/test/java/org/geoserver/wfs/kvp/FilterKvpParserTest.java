package org.geoserver.wfs.kvp;

import java.net.URLDecoder;
import java.util.List;

import junit.framework.TestCase;

import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.PropertyIsEqualTo;

public class FilterKvpParserTest extends TestCase {

    public void test() throws Exception {
        String filter = "%3Cogc%3AFilter+xmlns%3Aogc%3D%22http%3A%2F%2Fwww.opengis.net"
                + "%2Fogc%22+xmlns%3Acdf%3D%22http%3A%2F%2Fwww.opengis.net%2Fcite%2Fdata%22"
                + "%3E%3Cogc%3APropertyIsEqualTo%3E%3Cogc%3APropertyName%3Ecdf%3Aintegers%3C"
                + "%2Fogc%3APropertyName%3E%3Cogc%3AAdd%3E%3Cogc%3ALiteral%3E4%3C%2Fogc%3A"
                + "Literal%3E%3Cogc%3ALiteral%3E3%3C%2Fogc%3ALiteral%3E%3C%2Fogc%3AAdd%3E%3C"
                + "%2Fogc%3APropertyIsEqualTo%3E%3C%2Fogc%3AFilter%3E";
        filter = URLDecoder.decode(filter, "UTF-8");

        List filters = (List) new FilterKvpParser().parse(filter);
        assertNotNull(filters);
        assertEquals(1, filters.size());

        Filter f = (Filter) filters.get(0);
        assertTrue(f instanceof PropertyIsEqualTo);
    }

    public void testMultiFilter() throws Exception {
        String filter = "(%3CFilter%20xmlns=%22http://www.opengis.net/ogc%22%3E"
                + "%3CFeatureId%20fid=%22states.3%22/%3E%3C/Filter%3E)"
                + "(%3CFilter%20xmlns=%22http://www.opengis.net/ogc%22%3E%3CFeatureId"
                + "%20fid=%22tiger_roads.3%22/%3E%3C/Filter%3E)";
        filter = URLDecoder.decode(filter, "UTF-8");

        List filters = (List) new FilterKvpParser().parse(filter);
        assertNotNull(filters);
        assertEquals(2, filters.size());

        Filter f1 = (Filter) filters.get(0);
        assertTrue(f1 instanceof Id);
        String fid = (String) ((Id)f1).getIDs().iterator().next();
        assertEquals("states.3", fid);
        
        Filter f2 = (Filter) filters.get(1);
        assertTrue(f2 instanceof Id);
        fid = (String) ((Id)f2).getIDs().iterator().next();
        assertEquals("tiger_roads.3", fid);
    }

    public void testEmptyAndNonEmptyFilter() throws Exception {
        String param = "()(%3CFilter%20xmlns=%22http://www.opengis.net/ogc"
                + "%22%3E%3CFeatureId%20fid=%22roads.3%22/%3E%3C/Filter%3E)";
        param = URLDecoder.decode(param, "UTF-8");
        
        List filters = (List) new FilterKvpParser().parse(param);
        assertNotNull(filters);
        assertEquals(2, filters.size());
        
        Filter empty = (Filter)filters.get(0);
        Filter fid = (Filter)filters.get(1);
        
        assertNotNull(empty);
        assertEquals(Filter.INCLUDE, empty);
        
        assertNotNull(fid);
        assertTrue(fid instanceof Id);
    }
}