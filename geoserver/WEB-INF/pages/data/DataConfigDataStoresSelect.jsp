<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<html:form action="/config/data/storeSelect">

<table class="info">
  <tbody>
    <tr>
      <td class="label">
        <bean:message key="label.dataStoreID"/>:
      </td>
      <td class="datum" colspan=2>
        <html:select property="selectedDataStoreId">
			<html:options name="Config.Data" property="dataStoreIds"/>
		</html:select>
      </td>
    </tr>
    <tr>
      <td class="label">&nbsp;</td>
      <td class="datum">
		<html:submit property="buttonAction">
			<bean:message key="label.edit"/>
		</html:submit>
      </td>
      <td>
        <html:submit property="buttonAction">
			<bean:message key="label.delete"/>
		</html:submit>
	  </td>
    </tr>
  </tbody>
</table>
</html:form>
