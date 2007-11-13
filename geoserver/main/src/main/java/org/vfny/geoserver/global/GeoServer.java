/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.RecyclingTileFactory;
import javax.servlet.ServletContext;

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.geotools.data.DataStoreFactorySpi;
import org.springframework.beans.factory.DisposableBean;
import org.vfny.geoserver.global.dto.ContactDTO;
import org.vfny.geoserver.global.dto.GeoServerDTO;
import org.vfny.geoserver.util.Requests;

import com.sun.media.jai.util.SunTileCache;


/**
 * Complete configuration set for the whole server
 *
 * @author Gabriel Roldan
 * @author dzwiers
 * @version $Id$
 */
public class GeoServer extends GlobalLayerSupertype implements DisposableBean {
    /**
     * Simple listener interface.
     * 
     * JD: this is a temporary substitute until we have a decent config system.
     */
    public interface Listener {
        /**
         * Callback fired when application state has changed.
         */
        void changed();
        
    }
    
    /**
     * For finding the instance of this class to use from the web container
     *
     * <p>
     * ServletContext sc = ... GeoServer gs =
     * (GeoServer)sc.getAttribute(GeoServer.WEB_CONTAINER_KEY);
     * </p>
     */
    public static final String WEB_CONTAINER_KEY = "GeoServer";
    private String title;
    private int maxFeatures = Integer.MAX_VALUE;
    private boolean verbose = true;
    private int numDecimals = 4;
    private Charset charSet = Charset.forName("UTF-8");
    private final JAI jaiDef = JAI.getDefaultInstance();
    private SunTileCache jaiCache;
    private String adminUserName = "admin";
    private String adminPassword;
    private String schemaBaseUrl;
    private String proxyBaseUrl;
    private String contactPerson;
    private String contactOrganization;
    private String contactPosition;
    private String addressType;
    private String address;
    private String addressCity;
    private String addressState;
    private String addressPostalCode;
    private String addressCountry;
    private String contactVoice;
    private String contactFacsimile;
    private String contactEmail;
    private String onlineResource;
    private double memoryCapacity;
    private double memoryThreshold;
    private int tileThreads;
    private int tilePriority;
    private String tileCache;
    private Boolean recycling;
    private Boolean imageIOCache;
    private Boolean JPEGnativeAcc;
    private Boolean PNGnativeAcc;

    /** Should we throw the stack traces back in responses? */
    private boolean verboseExceptions = false;

    private String log4jConfigFile;
    private boolean suppressStdOutLogging = false;
    private String logLocation = null;

    private List listeners;
    private Config config;
    
    /**
     * Default constructor only to facilitate unit testing mock ups; real
     * uses shall create an instance through {@link #GeoServer(Config)}.
     */
    public GeoServer() {
        //do nothing
    }

    /**
     * Creates a GeoServer instance and loads its configuration.
     *
     * @throws ConfigurationException
     */
    public GeoServer(Config config) throws ConfigurationException {
        LOGGER.fine("Creating GeoServer");
        load(config.getGeoServer());
        this.config = config;
        
        listeners = new ArrayList();    
    }

    /**
     * Adds a listener to be notified of state change.
     */
    public void addListener( Listener listener ) {
        listeners.add( listener );
    }

    /**
     * Removes a listener.
     */
    public void removeListener( Listener listener ) {
        listeners.remove( listener );
    }
    
    /**
     * Notifies all listeners of a change.
     */
    public void fireChange() {
        for ( Iterator l = listeners.iterator(); l.hasNext(); ) {
            Listener listener = (Listener) l.next();
            try {
                listener.changed();
            }
            catch( Throwable t ) {
                LOGGER.warning( "listener threw exception, turn logging to FINE to view stack trace" );
                LOGGER.log( Level.FINE, t.getLocalizedMessage(), t );
            }
        }
    }
    /**
     * getAddress purpose.
     *
     * <p>
     * Returns the contact Address.
     * </p>
     *
     * @return String the contact Address.
     */
    public String getAddress() {
        return notNull(address);
    }

    /**
     * getAddressCity purpose.
     *
     * <p>
     * Returns the contact City.
     * </p>
     *
     * @return String the contact City.
     */
    public String getAddressCity() {
        return notNull(addressCity);
    }

    /**
     * getAddressCountry purpose.
     *
     * <p>
     * Returns the contact Country.
     * </p>
     *
     * @return String the contact Country.
     */
    public String getAddressCountry() {
        return notNull(addressCountry);
    }

    /**
     * getAddressPostalCode purpose.
     *
     * <p>
     * Returns the contact PostalCode.
     * </p>
     *
     * @return String the contact PostalCode.
     */
    public String getAddressPostalCode() {
        return notNull(addressPostalCode);
    }

    /**
     * getAddressState purpose.
     *
     * <p>
     * Returns the contact State.
     * </p>
     *
     * @return String the contact State.
     */
    public String getAddressState() {
        return notNull(addressState);
    }

    /**
     * getAddressType purpose.
     *
     * <p>
     * Returns the contact Address Type.
     * </p>
     *
     * @return String the contact Address Type.
     */
    public String getAddressType() {
        return notNull(addressType);
    }

    /**
     * getCharSet purpose.
     *
     * <p>
     * Returns the default charset for this server instance.
     * </p>
     *
     * @return Charset the default charset for this server instance.
     */
    public Charset getCharSet() {
        if (charSet != null) {
            return charSet;
        }

        return Charset.forName("UTF-8");
    }

    /**
     * getContactEmail purpose.
     *
     * <p>
     * Returns the contact Email.
     * </p>
     *
     * @return String the contact Email.
     */
    public String getContactEmail() {
        return notNull(contactEmail);
    }

    /**
     * getContactFacsimile purpose.
     *
     * <p>
     * Returns the contact Facsimile.
     * </p>
     *
     * @return String the contact Facsimile.
     */
    public String getContactFacsimile() {
        return notNull(contactFacsimile);
    }

    /**
     * getContactOrganization purpose.
     *
     * <p>
     * Returns the contact Organization.
     * </p>
     *
     * @return String the contact Organization.
     */
    public String getContactOrganization() {
        return notNull(contactOrganization);
    }

    /**
     * getContactPerson purpose.
     *
     * <p>
     * Returns the contact Person.
     * </p>
     *
     * @return String the contact Person.
     */
    public String getContactPerson() {
        return notNull(contactPerson);
    }

    /**
     * getContactPosition purpose.
     *
     * <p>
     * Returns the contact Position.
     * </p>
     *
     * @return String the contact Position.
     */
    public String getContactPosition() {
        return notNull(contactPosition);
    }

    /**
     * getContactVoice purpose.
     *
     * <p>
     * Returns the contact Phone.
     * </p>
     *
     * @return String the contact Phone.
     */
    public String getContactVoice() {
        return notNull(contactVoice);
    }

    /**
     * getOnlineResource purpose.
     *
     * <p>
     * Returns the online Resource.
     * </p>
     *
     * @return String the online Resource.
     */
    public String getOnlineResource() {
        return notNull(onlineResource);
    }

    /**
    * getLoggingLevel purpose.
    *
    * <p>
    * Returns the Logging Level.
    * </p>
    *
    * @return String the Logging Level.
    */
    public String getLog4jConfigFile() {
        return log4jConfigFile;
    }

    /**
     * getMaxFeatures purpose.
     *
     * <p>
     * Returns the max number of features supported.
     * </p>
     *
     * @return String the max number of features supported.
     */
    public int getMaxFeatures() {
        return maxFeatures;
    }

    /**
     * getMimeType purpose.
     *
     * <p>
     * Returns the server default mimetype.
     * </p>
     *
     * @return String the server default mimetype.
     */
    public String getMimeType() {
        return "text/xml; charset=" + getCharSet().name();
    }

    /**
     * getNumDecimals purpose.
     *
     * <p>
     * The default number of decimals allowed in the data.
     * </p>
     *
     * @return int the default number of decimals allowed in the data.
     */
    public int getNumDecimals() {
        return numDecimals;
    }

    /**
     * getSchemaBaseUrl purpose.
     *
     * <p>
     * The Schema Base URL for this instance.  This should generally be a local
     * reference, as GeoServer by default puts up the schemas that it needs
     * and references them.  It could be used to specify an alternate site for
     * the schemas, however, for example if a user didn't want their servlet
     * container hit every time someone did a validation, they could instead
     * store it on another machine.  I don't really know if this is useful to
     * anyone...
     * </p>
     *
     * @return String the Schema Base URL for this instance.
     *
     * @task TODO: Right now this is broken, and I'm not quite sure if there's
     *       an elegant way to have this return the local schemas.  Perhaps we
     *       should just have it return 'local', and then the users of this
     *       method can do the local referencing themselves.  For now no one
     *       is using this  method, perhaps we should just leave it out for
     *       1.2.0, as it's very  obscure.  I think I only added it originally
     *       because I didn't want to  go through the busy work of cleaning up
     *       and figuring out how to copy over the ogc schemas.
     */
    public String getSchemaBaseUrl() {
        return schemaBaseUrl;
    }

    /**
     * Used when Geoserver is running behind a reverse-proxy so that url
     * in getCapabilities documents are fine
     * @return
     */
    public String getProxyBaseUrl() {
        return proxyBaseUrl;
    }

    /**
     * whether xml documents should be pretty formatted
     *
     * @return true when verbose
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * <p>
     * Loads the GeoServerDTO into the current instance as a GeoServer object
     * </p>
     *
     * @param dto
     * @throws ConfigurationException
     */
    public void load(GeoServerDTO dto) throws ConfigurationException {
        if (dto != null) {
            address = dto.getContact().getAddress();
            addressCity = dto.getContact().getAddressCity();
            addressCountry = dto.getContact().getAddressCountry();
            addressPostalCode = dto.getContact().getAddressPostalCode();
            addressState = dto.getContact().getAddressState();
            addressType = dto.getContact().getAddressType();
            charSet = dto.getCharSet();
            contactEmail = dto.getContact().getContactEmail();
            contactFacsimile = dto.getContact().getContactFacsimile();
            contactOrganization = dto.getContact().getContactOrganization();
            contactPerson = dto.getContact().getContactPerson();
            contactPosition = dto.getContact().getContactPosition();
            contactVoice = dto.getContact().getContactVoice();
            
            log4jConfigFile = dto.getLog4jConfigFile();
            suppressStdOutLogging = dto.getSuppressStdOutLogging();
            logLocation = dto.getLogLocation();
            try {
                configureGeoServerLogging(log4jConfigFile, suppressStdOutLogging, logLocation);
            } catch (IOException ioe) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE,"",ioe);
                }
                throw new ConfigurationException("", ioe);
            }
            
            memoryCapacity = dto.getJaiMemoryCapacity();
            memoryThreshold = dto.getJaiMemoryThreshold();
            tileThreads = dto.getJaiTileThreads();
            tilePriority = dto.getJaiTilePriority();
            tileCache = dto.getTileCache();
            recycling = dto.getJaiRecycling();
            imageIOCache = dto.getImageIOCache();
            JPEGnativeAcc = dto.getJaiJPEGNative();
            PNGnativeAcc = dto.getJaiPNGNative();

            initJAI(memoryCapacity, memoryThreshold, recycling, imageIOCache);

            maxFeatures = dto.getMaxFeatures();
            numDecimals = dto.getNumDecimals();
            onlineResource = dto.getContact().getOnlineResource();
            schemaBaseUrl = dto.getSchemaBaseUrl();
            proxyBaseUrl = dto.getProxyBaseUrl();
            verbose = dto.isVerbose();
            adminUserName = dto.getAdminUserName();
            adminPassword = dto.getAdminPassword();
            verboseExceptions = dto.isVerboseExceptions();
        } else {
            throw new ConfigurationException("load(GeoServerDTO) expected a non-null value");
        }
    }

    /**
     *
     * load purpose.
     *
     * <p>
     * Loads the GeoServerDTO into the current instance as a GeoServer object.
     * As GeoServer moves to Spring, we want to move away from storing state
     * in the servlet context, so this method is deprecated.
     * </p>
     *
     * @param dto GeoServerDTO
     *
     * @throws ConfigurationException If an error occurs
     *
     * @deprecated use {@link #load(GeoServerDTO)}
     */
    public final void load(GeoServerDTO dto, ServletContext context)
        throws ConfigurationException {
        load(dto);
    }

    /**
     * Convenience method for determining the actual location on the local file
     * system of the log file based an arbirtrary path. Relative paths are
     * appended to the geoserver data directory.
     *
     * @param location The log file path, this can be an absolute or relative
     * path.
     * @param context The servlet context
     *
     * @return The file containing the absolute path to the log file.
     * @throws IOException
     */
    public static File getLogLocation(String logLocation)
        throws IOException {
        File f = new File(logLocation);

        if (f.exists()) {
            if (f.isDirectory()) {
                //attach a file to the end of the directory
                if (!logLocation.endsWith(File.separator)) {
                    logLocation += File.separator;
                }

                logLocation += "geoserver.log";
            }
        } else {
            //could be a relative path
            if (!f.isAbsolute()) {
                //append to data dir
                File data = GeoserverDataDirectory.getGeoserverDataDirectory();
                f = new File(data, f.getPath());
            }

            //make sure parent directory exists
            if ((f.getParentFile() != null) && !f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }

            f.createNewFile();
        }

        return f;
    }
    
    public static void configureGeoServerLogging(String log4jConfigFileStr, boolean suppressStdOutLogging, String logFileName) throws IOException, ConfigurationException {
        
        //to initialize logging we need to do a couple of things:
        // 1)  Figure out whether the user has 'overridden' some configuration settings
        // in the logging system (not using log4j in commons-logging.properties or perhaps
        // has set up their own 'custom' log4j.properties file.
        // 2)  If they *have*, then we don't worry about configuring logging
        // 3)  If they haven't, then we configure logging to use the log4j config file
        // specified, and remove console appenders if the suppressstdoutlogging is true.
        LOGGER.fine("CONFIGURING GEOSERVER LOGGING -------------------------");
        
        if (log4jConfigFileStr == null) {
            log4jConfigFileStr = "DEFAULT_LOGGING.properties";
            LOGGER.warning("No log4jConfigFile defined in services.xml:  using 'DEFAULT_LOGGING.properties'");
        }
        
        File log4jConfigFile = GeoserverDataDirectory.findConfigFile("logs"+ File.separator + log4jConfigFileStr);
        
        if (log4jConfigFile == null) {
            //hmm, well, we don't have a log4j config file and this could be due to the fact
            //that this is a data-dir upgrade.  We can count on the DEFAULT_LOGGING.properties file
            //being present on the classpath, so we'll upgrade their data_dir and then use the
            //default DEFAULT_LOGGING.properties configuration.
            LOGGER.warning("log4jConfigFile '" + log4jConfigFileStr + "' couldn't be found in the data dir, so GeoServer will " +
            "install the various logging config file into the data dir, and then try to find it again.");
            
            //this forces the data_dir/logs directory to be present (if it wasn't already)
            File lcdir = GeoserverDataDirectory.findCreateConfigDir("logs");
            
            //now we copy in the various logging config files from the base repo location on the classpath
            final String[] lcfiles = new String[] { "DEFAULT_LOGGING.properties",
                    "VERBOSE_LOGGING.properties",
                    "PRODUCTION_LOGGING.properties",
                    "GEOTOOLS_DEVELOPER_LOGGING.properties",
                    "GEOSERVER_DEVELOPER_LOGGING.properties" };
            
            for (int i = 0; i < lcfiles.length; i++) {
                File target = new File(lcdir.getAbsolutePath(), lcfiles[i]);
                if (!target.exists()) {
                    copyResourceToFile(lcfiles[i], target);
                }
            }
            
            //ok, the possibly-new 'logs' directory is in-place, with all the various configs there.
            // Is the originally configured log4jconfigfile there now?
            log4jConfigFile = GeoserverDataDirectory.findConfigFile("logs" + File.separator + log4jConfigFileStr);
            if (log4jConfigFile == null) {
                LOGGER.warning("Still couldn't find log4jConfigFile '" + log4jConfigFileStr + "'.  Using DEFAULT_LOGGING.properties instead.");
            }
            
            log4jConfigFile = GeoserverDataDirectory.findConfigFile("logs" + File.separator + "DEFAULT_LOGGING.properties");
        }

        if (log4jConfigFile == null) {
            throw new ConfigurationException("Unable to load logging configuration '" + log4jConfigFileStr + "'.  In addition, an attempt " +
                    "was made to create the 'logs' directory in your data dir, and to use the DEFAULT_LOGGING configuration, but" +
                    "this failed as well.  Is your data dir writeable?");
        }

        // reconfiguring log4j logger levels by resetting and loading a new set of configuration properties
        InputStream loggingConfigStream = new FileInputStream(log4jConfigFile);
        if (loggingConfigStream == null) {
            LOGGER.warning("Couldn't find Log4J configuration file '" + log4jConfigFileStr + "' in the 'loggingConfigs' directory.");
            return;
        } else {
            LOGGER.fine("GeoServer logging profile '" + log4jConfigFile.getName() + "' enabled.");
        }

        Properties lprops = new Properties();
        lprops.load(loggingConfigStream);
        LogManager.resetConfiguration();
        PropertyConfigurator.configure(lprops);
        
        // configuring the log4j file logger
        Appender gslf = org.apache.log4j.Logger.getRootLogger().getAppender("geoserverlogfile");
        if (gslf instanceof org.apache.log4j.RollingFileAppender) {
            if (logFileName == null ) {
                logFileName = new File(GeoserverDataDirectory.findCreateConfigDir("logs"),  "geoserver.log").getAbsolutePath();
            } else { 
                if (!new File(logFileName).isAbsolute()) {
                    logFileName = new File(GeoserverDataDirectory.getGeoserverDataDirectory(), logFileName).getAbsolutePath();
                    LOGGER.fine("Non-absolute pathname detected for logfile.  Setting logfile relative to data dir.");
                }
            }
            lprops.setProperty("log4j.appender.geoserverlogfile.File", logFileName);
            PropertyConfigurator.configure(lprops);
            LOGGER.fine("Logging output to file '" + logFileName + "'");
        } else if (gslf != null) {
            LOGGER.warning("'log4j.appender.geoserverlogfile' appender is defined, but isn't a RollingFileAppender.  GeoServer won't control the file-based logging.");
        } else {
            LOGGER.warning("'log4j.appender.geoserverlogfile' appender isn't defined.  GeoServer won't control the file-based logging.");
        }
        
        // ... and the std output logging too
        if (suppressStdOutLogging) {
            LOGGER.warning("Suppressing StdOut logging.  If you want to see GeoServer logs, be sure to look in '" + logFileName + "'");
            Enumeration allAppenders = org.apache.log4j.Logger.getRootLogger().getAllAppenders();
            Appender curApp;
            while (allAppenders.hasMoreElements()) {
                curApp = (Appender)allAppenders.nextElement();
                if (curApp instanceof org.apache.log4j.ConsoleAppender) {
                    org.apache.log4j.Logger.getRootLogger().removeAppender(curApp);
                }
            }
        } else {
            LOGGER.info("StdOut logging enabled.  Log file also output to '" + logFileName + "'");
        }
        LOGGER.fine("FINISHED CONFIGURING GEOSERVER LOGGING -------------------------");
    }

    private static void copyResourceToFile(String resource, File target) throws ConfigurationException {
        InputStream is = null; 
        OutputStream os = null;
        byte[] buffer = new byte[4096];
        int read;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            os = new FileOutputStream(target);
            while((read = is.read(buffer)) > 0)
                os.write(buffer, 0, read);
        } catch (FileNotFoundException targetException) {
            throw new ConfigurationException("Can't write to file " + target.getAbsolutePath() + 
                    ". Check write permissions on target folder for user " + System.getProperty("user.name"));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error trying to copy logging configuration file", e);
        } finally {
            try {
                if(is != null){
                    is.close();
                }
                if(os != null){
                    os.close();
                }
            } catch(IOException e) {
                // we tried...
            }
        }
    }
    

    
    public void initJAI(final double memCapacity, final double memoryThreshold,
        final Boolean recycling, final Boolean ImageIOCache) {
        // setting JAI wide hints
        jaiDef.setRenderingHint(JAI.KEY_CACHED_TILE_RECYCLING_ENABLED, recycling);

        // tile factory and recycler
        final RecyclingTileFactory recyclingFactory = new RecyclingTileFactory();
        jaiDef.setRenderingHint(JAI.KEY_TILE_FACTORY, recyclingFactory);
        jaiDef.setRenderingHint(JAI.KEY_TILE_RECYCLER, recyclingFactory);

        // Setting up Cache Capacity
        jaiCache = (SunTileCache) jaiDef.getTileCache();

        long jaiMemory = (long) (memCapacity * Runtime.getRuntime().maxMemory());
        jaiCache.setMemoryCapacity(jaiMemory);

        // Setting up Cahce Threshold
        jaiCache.setMemoryThreshold((float) memoryThreshold);

        jaiDef.getTileScheduler().setParallelism(tileThreads);
        jaiDef.getTileScheduler().setPrefetchParallelism(tileThreads);
        jaiDef.getTileScheduler().setPriority(tilePriority);
        jaiDef.getTileScheduler().setPrefetchPriority(tilePriority);

        // ImageIO Caching
        ImageIO.setUseCache(ImageIOCache.booleanValue());
    }

    /**
     * toDTO purpose.
     *
     * <p>
     * This method is package visible only, and returns a reference to the
     * GeoServerDTO. This method is unsafe, and should only be used with
     * extreme caution.
     * </p>
     *
     * @return DTO the generated object
     */
    public Object toDTO() {
        GeoServerDTO dto = new GeoServerDTO();
        dto.setCharSet(charSet);
        dto.setLog4jConfigFile(log4jConfigFile);
        dto.setMaxFeatures(maxFeatures);
        dto.setNumDecimals(numDecimals);
        dto.setSchemaBaseUrl(schemaBaseUrl);
        dto.setProxyBaseUrl(proxyBaseUrl);
        dto.setVerbose(verbose);
        dto.setAdminUserName(adminUserName);
        dto.setAdminPassword(adminPassword);
        dto.setVerboseExceptions(verboseExceptions);
        dto.setSuppressStdOutLogging(suppressStdOutLogging);
        dto.setLogLocation(logLocation);
        dto.setJaiMemoryCapacity(memoryCapacity);
        dto.setJaiMemoryThreshold(memoryThreshold);
        dto.setJaiTileThreads(tileThreads);
        dto.setJaiTilePriority(tilePriority);
        dto.setTileCache(tileCache);
        dto.setJaiRecycling(recycling);
        dto.setImageIOCache(imageIOCache);
        dto.setJaiJPEGNative(JPEGnativeAcc);
        dto.setJaiPNGNative(PNGnativeAcc);

        ContactDTO cdto = new ContactDTO();
        dto.setContact(cdto);

        cdto.setAddress(address);
        cdto.setAddressCity(addressCity);
        cdto.setAddressCountry(addressCountry);
        cdto.setAddressPostalCode(addressPostalCode);
        cdto.setAddressState(addressState);
        cdto.setAddressType(addressType);
        cdto.setContactEmail(contactEmail);
        cdto.setContactFacsimile(contactFacsimile);
        cdto.setContactOrganization(contactOrganization);
        cdto.setContactPerson(contactPerson);
        cdto.setContactPosition(contactPosition);
        cdto.setContactVoice(contactVoice);
        cdto.setOnlineResource(onlineResource);

        return dto;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * DOCUMENT ME!
     *
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Property representing the contact party (person, position or
     * organization).
     *
     * <p>
     * This is a derived property.
     * </p>
     *
     * @return Contact party (person, position or organization), null if
     *         unknown
     */
    public String getContactParty() {
        if ((getContactPerson() != null) && (getContactPerson().length() != 0)) {
            return getContactPerson(); // ie Chris Holmes 
        }

        if ((getContactPosition() != null) && (getContactPosition().length() != 0)) {
            return getContactPosition(); // ie Lead Developer 
        }

        if ((getContactOrganization() != null) && (getContactOrganization().length() != 0)) {
            return getContactOrganization(); // ie TOPP 
        }

        return null;
    }

    public String getAdminUserName() {
        return adminUserName;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public String toString() {
        StringBuffer geoserver = new StringBuffer("[GeoServer: \n");
        geoserver.append("   maxFeatures - " + maxFeatures);
        geoserver.append("\n   verbose - " + verbose);
        geoserver.append("\n   numDecimals - " + numDecimals);
        geoserver.append("\n   charSet - " + charSet);
        geoserver.append("\n   log4jConfigFile - " + log4jConfigFile);
        geoserver.append("\n   adminUserName - " + adminUserName);
        geoserver.append("\n   adminPassword - " + adminPassword);

        return geoserver.toString();
    }

    /**
     * Should we display stackTraces or not? (And give them a nice
    * little message instead?)
     *
     * @return Returns the showStackTraces.
     */
    public boolean isVerboseExceptions() {
        return verboseExceptions;
    }

    /**
     * If set to true, response exceptions will throw their stack trace
    * back to the end user.
     *
     * @param showStackTraces The showStackTraces to set.
     */
    public void setVerboseExceptions(boolean showStackTraces) {
        this.verboseExceptions = showStackTraces;
    }

    /**
     * Returns the location of where the server ouputs logs. Note that this may
     * not reference an actual physical location on disk.
     * Call {@link GeoServer#getLogLocation(String, ServletContext)} to map this
     * string to a file on disk.
     *
     */
    public String getLogLocation() {
        return logLocation;
    }

    /**
     * @param logLocation The string representation of the path on disk in which
     * the server logs to.
     */
    public void setLogLocation(String logLocation) {
        this.logLocation = logLocation;
    }

    /**
     * @return True if the server is logging to file, otherwise false.
     */
    public boolean getSuppressStdOutLogging() {
        return suppressStdOutLogging;
    }

    /**
     * Toggles server logging to file.
     */
    public void setSuppressStdOutLogging(boolean loggingToFile) {
        this.suppressStdOutLogging = loggingToFile;
    }

    public JAI getJAIDefault() {
        return jaiDef;
    }

    public SunTileCache getJaiCache() {
        return jaiCache;
    }

    public double getMemoryCapacity() {
        return memoryCapacity;
    }

    public Boolean getRecycling() {
        return recycling;
    }

    public Boolean getJPEGNativeAcceleration() {
        return JPEGnativeAcc;
    }

    public Boolean getPNGNativeAcceleration() {
        return PNGnativeAcc;
    }

    public double getMemoryThreshold() {
        return memoryThreshold;
    }

    /**
     * @return Returns the imageIOCache.
     */
    public Boolean getImageIOCache() {
        return imageIOCache;
    }

    public int getTilePriority() {
        return tilePriority;
    }

    public int getTileThreads() {
        return tileThreads;
    }

    /**
     * Used when GeoServer is running beheind tile caching server.
     * <p>
     * This value should be used when writing out a url which is a getmap
     * request to reference the tile caching server and not GeoServer itself.
     * </p>
     *<p>
     * This value can be:
     * <ol>
     *  <li>A fully qualified host name + path (URL)
     *  <li>A partial path which is interpreted as relative to the host running
     *  GeoServer
     *  <li><code>null</code>
     * </ol>
     * </p>
     * <p>
     *
     * </p>
     * @see Requests#getTileCacheBaseUrl(javax.servlet.http.HttpServletRequest, GeoServer)
     */
    public String getTileCache() {
        return tileCache;
    }

    public void setTileCache(String tileCache) {
        this.tileCache = tileCache;
    }

    /**
     * Implements {@link DisposableBean#destroy()} to release resources being held
     * by the server at server shutdown, such as JDBC connection pools and ArcSDE
     * connection pools.
     * <p>
     * Note this process would greately benefit if {@link DataStoreFactorySpi} API
     * had some sort of resource releasing method, so we could just traverse
     * the available datastore factories asking them to release any resource
     * needed.
     * </p>
     */
    public void destroy() throws Exception {
        final Data catalog = (Data) config.getApplictionContext().getBean("data");
        final Set dataStores = catalog.getDataStores();
        LOGGER.info("Disposing DataStores at GeoServer shutdown...");
        for (Iterator it = dataStores.iterator(); it.hasNext();) {
            DataStoreInfo dataStoreInfo = (DataStoreInfo) it.next();
            LOGGER.fine("Disposing " + dataStoreInfo.getId());
            try {
                dataStoreInfo.dispose();
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "Caught exception while disposing datastore "
                        + dataStoreInfo.getId(), e);
            }
        }
        LOGGER.info("Done disposing datastores.");
        
        /*
         *  HACK: we must get a standard API way for releasing resources...
         *  UPDATE: now we do have a standard API to release resources, though ArcSDE does not
         *  properly implements DataStore.dispose() yet.
         */
        try {
            Class sdepfClass = Class.forName("org.geotools.arcsde.pool.ArcSDEConnectionPoolFactory");

            LOGGER.fine("SDE datasource found, releasing resources");

            java.lang.reflect.Method m = sdepfClass.getMethod("getInstance", new Class[0]);
            Object pfInstance = m.invoke(sdepfClass, new Object[0]);

            LOGGER.fine("got sde connection pool factory instance: " + pfInstance);

            java.lang.reflect.Method closeMethod = pfInstance.getClass()
                                                             .getMethod("clear", new Class[0]);

            closeMethod.invoke(pfInstance, new Object[0]);
            LOGGER.info("Asking ArcSDE datasource to release connections.");
        } catch (ClassNotFoundException cnfe) {
            LOGGER.fine("No ArcSDE datasource found.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
