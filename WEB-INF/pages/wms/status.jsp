<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<span class="statusLabel">
	<small><bean:message key="label.wms"/>:<br></small>
</span>
<span class="statusbar">
    <logic:notEqual name="GeoServer.ApplicationState" property="wmsGood" value="0">
	    <span width="<bean:write name="GeoServer.ApplicationState" property="wfsGood"/>%" class="statusBarGood">
	    </span>
	</logic:notEqual>
    <logic:notEqual name="GeoServer.ApplicationState" property="wmsBad" value="0">		    	
        <span class="statusBarBad" width="<bean:write name="GeoServer.ApplicationState" property="wfsBad"/>%">
        </span>
	</logic:notEqual>		            
    <logic:notEqual name="GeoServer.ApplicationState" property="wmsDisabled" value="0">		    	
        <span class="statusBarDisabled" width="<bean:write name="GeoServer.ApplicationState" property="wfsDisabled"/>%">
        </span>     
	</logic:notEqual>		                 
</span>