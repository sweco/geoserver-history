/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.vfny.geoserver.action.data;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.vfny.geoserver.action.ConfigAction;
import org.vfny.geoserver.action.HTMLEncoder;
import org.vfny.geoserver.config.DataConfig;
import org.vfny.geoserver.config.FeatureTypeConfig;
import org.vfny.geoserver.form.data.DataFeatureTypesSelectForm;
import org.vfny.geoserver.global.UserContainer;


/**
 * DOCUMENT ME!
 *
 * @author User To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DataFeatureTypesSelectAction extends ConfigAction {
    public ActionForward execute(ActionMapping mapping,
        ActionForm incomingForm, UserContainer user, HttpServletRequest request,
         HttpServletResponse response) throws IOException, ServletException {

        DataFeatureTypesSelectForm form = (DataFeatureTypesSelectForm) incomingForm;

        String selectedFeatureType = form.getSelectedFeatureTypeName();
        String buttonAction = form.getButtonAction();

        DataConfig dataConfig = (DataConfig) getServlet().getServletContext()
                                                 .getAttribute(DataConfig.CONFIG_KEY);
        
        FeatureTypeConfig ftConfig = dataConfig.getFeatureTypeConfig(selectedFeatureType);
        request.getSession().removeAttribute(DataConfig.SELECTED_ATTRIBUTE_TYPE);
        
        Locale locale = (Locale) request.getLocale();
        MessageResources messages = servlet.getResources();
        String edit = HTMLEncoder.decode(messages.getMessage(locale, "label.edit"));
        String delete = HTMLEncoder.decode(messages.getMessage(locale, "label.delete"));

        if (edit.equals(buttonAction)) {
            request.getSession().setAttribute(DataConfig.SELECTED_FEATURE_TYPE,
                ftConfig);
            
            user.setFeatureTypeConfig( ftConfig );                       
	    LOGGER.info("setting session and user ftConfig to : " + ftConfig);
            return mapping.findForward("config.data.type.editor");
        } else if (delete.equals(buttonAction)) {
            dataConfig.removeFeatureType(selectedFeatureType);
            request.getSession().removeAttribute(DataConfig.SELECTED_FEATURE_TYPE);
            getApplicationState().notifyConfigChanged();

            return mapping.findForward("config.data.type");
        }

        throw new ServletException(
            "Action must be a MessageResource key value of either 'label.edit' or 'label.delete'");
    }
}
