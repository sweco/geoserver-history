/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.kvp;

import org.geoserver.ows.KvpParser;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.vfny.geoserver.ServiceException;
import java.util.List;


/**
 * Parses the CQL_FILTER parameter into a list of filters
 * @author Andrea Aime - TOPP
 *
 */
public class CQLFilterKvpParser extends KvpParser {
    public CQLFilterKvpParser() {
        super("cql_filter", List.class);
    }

    public Object parse(String value) throws Exception {
        try {
            return CQL.toFilterList(value);
        } catch (CQLException pe) {
            throw new ServiceException("Could not parse CQL filter list."
                + pe.getMessage(), pe);
        }
    }
}
