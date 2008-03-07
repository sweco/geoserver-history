/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.data.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.JDBCUtils;
import org.geotools.util.logging.Logging;

/**
 * Extends LiveData to deal with the needs of a data directory that uses a DBMS
 * among its data stores. In particular it provides:
 * <ul>
 * <li>Facilities to replace templates in configuration files, so that the
 * connection parameters can be replaced with the local version (provided as a
 * key, value map)</li>
 * <li>Facilities to setup the database structure from a sql script. The sql
 * must have each command on a single line</li>
 * </ul>
 * 
 * @author Andrea Aime - TOPP
 * 
 */
public class LiveDbmsData extends LiveData {
    private static final Logger LOGGER = Logging.getLogger(LiveDbmsData.class);

    /**
     * The property file containing the token -> value pairs used for filtering
     * and to grab a JDBC datastore connection.
     * 
     * @return
     */
    protected File fixture;

    /**
     * List of file paths (relative to the source data directory) that will be
     * subjected to token filtering. By default only <code>catalog.xml</code>
     * will be filtered.
     */
    protected List<String> filteredPaths = new ArrayList<String>(Arrays.asList("catalog.xml"));

    protected File sqlScript;

    /**
     * Can be used to make sure the datastore used to grab connections is
     * disposed of
     */
    protected DataStore ds;

    /**
     * The identifier of the fixture, which is also the name of the file (followed by .properties)
     * and the system property used to disable the test (prefixed by gs.) 
     */
    protected String fixtureId;

    /**
     * Builds a new LiveDbmsData with the minimal parameter needed:
     * <ul>
     * <li>the source data directory to be copied</li>
     * <li>the path to a property file containing the set of parameters
     * required to grab a connection with a jdbc datastore, which will be also
     * used to filter out the files during copy (by default, only catalog.xml)</li>
     * <li>the location of the sql script used to initialize the database (this
     * one can be null if no initialization is needed)</li>
     * 
     * @param dataDirSourceDirectory
     * @param filterMap
     * @param sqlScript
     */
    public LiveDbmsData(File dataDirSourceDirectory, String fixtureId, File sqlScript) {
        super(dataDirSourceDirectory);
        this.fixture = lookupFixture(fixtureId);
        this.fixtureId = fixtureId;
        this.sqlScript = sqlScript;
    }

    /**
     * Looks up the fixture file in the home directory provided that the 
     * @param fixtureId
     * @return
     */
    private File lookupFixture(String fixtureId) {
        // first of all, make sure the fixture was not disabled using a system
        // variable
        final String property = System.getProperty("gs." + fixtureId);
        if (property != null && "false".equals(property.toLowerCase())) {
            return null;
        }

        // then look in the user home directory
        File base = new File(System.getProperty("user.home"), ".geoserver");
        File fixtureFile = new File(base, fixtureId + ".properties");
        if (!fixtureFile.exists()) {
            final String warning = "Disabling test based on fixture " + fixtureId + " since the file "
                    + fixtureFile + " could not be found";
            disableTest(warning);
            return null;
        }
        
        return fixtureFile;
    }

    public boolean isTestDataAvailable() {
        return fixture != null;
    }

    @Override
    public void setUp() throws Exception {
        // if the test was disabled we don't need to run the setup
        if (fixture == null)
            return;

        super.setUp();

        // load the properties from the fixture path and load them into a
        // Map<String, String>
        Properties p = new Properties();
        p.load(new FileInputStream(fixture));
        Map<String, String> filters = new HashMap(p);

        // filter out the
        if (filteredPaths != null && filteredPaths.size() > 0) {
            for (String path : filteredPaths) {
                File from = new File(source, path);
                File to = new File(data, path);
                IOUtils.filteredCopy(from, to, filters);
            }
        }

        // populate the db
        if (sqlScript != null) {
            DataStore ds = null;
            Connection conn = null;
            Statement st = null;
            BufferedReader reader = null;
            try {
                ds = DataStoreFinder.getDataStore(filters);
                if (ds == null) {
                    final String warning = "Disabling online test based on '" + fixtureId + "', "
                            + "could not find a data store compatible "
                            + "with the following connection properties: " + filters;
                    disableTest(warning);
                    return;
                }

                if (ds instanceof JDBCDataStore) {
                    conn = ((JDBCDataStore) ds).getConnection(Transaction.AUTO_COMMIT);
                }
                // TODO: add a way to extract a connection from the new JDBC
                // datastores

                if (conn == null) {
                    final String warning = "Disabling online test based on '" + fixtureId + "', "
                            + "could not extract a JDBC connection from the datastore '"
                            + ds.getClass() + " obtained using the following "
                            + "connection properties: " + filters;
                    disableTest(warning);
                    return;
                }

                // read the script and run the setup commands
                reader = new BufferedReader(new FileReader(sqlScript));
                st = conn.createStatement();
                String command = null;
                while ((command = reader.readLine()) != null) {
                    command = command.trim();
                    // skip comments and empty lines
                    if ("".equals(command) || command.startsWith("--") || command.startsWith("#"))
                        continue;

                    // execute but do not complain, only log the failures
                    try {
                        st.execute(command);
                    } catch (SQLException e) {
                        LOGGER.warning("Error executing \"" + command + "\": " + e.getMessage());
                    }

                }
            } finally {
                JDBCUtils.close(st);
                JDBCUtils.close(conn, null, null);
                // very important, or we'll leak connection pools during
                // execution
                if (ds != null)
                    ds.dispose();
                if (reader != null)
                    reader.close();
            }
        }

    }

    /**
     * Permanently disable this test logging the specificed warning message (the reason
     * why the test is being disabled)
     * @param warning
     */
    private void disableTest(final String warning) {
        LOGGER.warning(warning);
        fixture = null;
        System.setProperty("gs." + fixtureId, "false");
    }

}
