/*
 * Created on Feb 9, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.vfny.geoserver.action.data;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureType;
import org.vfny.geoserver.action.ConfigAction;
import org.vfny.geoserver.config.DataConfig;
import org.vfny.geoserver.config.DataStoreConfig;
import org.vfny.geoserver.config.FeatureTypeConfig;
import org.vfny.geoserver.global.UserContainer;
import org.vfny.geoserver.util.DataStoreUtils;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * CalculateBoundingBoxAction purpose.<p>Description of
 * CalculateBoundingBoxAction ...</p>
 *  <p>Capabilities:</p>
 *  <ul>
 *      <li>Feature: description</li>
 *  </ul>
 *  <p>Example Use:</p>
<pre><code>CalculateBoundingBoxAction x = new CalculateBoundingBoxAction(...);
 * </code></pre>
 *
 * @author rgould, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: CalculateBoundingBoxAction.java,v 1.3 2004/03/09 01:37:40 dmzwiers Exp $
 */
public final class CalculateBoundingBoxAction extends ConfigAction {
    public ActionForward execute(ActionMapping mapping, ActionForm form, UserContainer user,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        FeatureTypeConfig ftConfig = (FeatureTypeConfig) request.getSession()
                                                                .getAttribute(DataConfig.SELECTED_FEATURE_TYPE);
        DataConfig dataConfig = getDataConfig();
        DataStoreConfig dsConfig = dataConfig.getDataStore(ftConfig.getDataStoreId());
        DataStore dataStore = dsConfig.findDataStore(request.getSession().getServletContext());
        FeatureType featureType = dataStore.getSchema(ftConfig.getName());
        FeatureSource fs = dataStore.getFeatureSource(featureType.getTypeName());

        ftConfig.setLatLongBBox(DataStoreUtils.getBoundingBoxEnvelope(fs));
        request.getSession().setAttribute(DataConfig.SELECTED_FEATURE_TYPE, ftConfig);

        return mapping.findForward("config.data.type.editor");
    }
}
