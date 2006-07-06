<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<logic:notPresent name="org.apache.struts.action.MESSAGE" scope="application">
  <span class="error">
    ERROR:  Application resources not loaded -- check servlet container
    logs for error messages.
  </span>
</logic:notPresent>

</span>

<p>
<bean:message key="text.welcome1"/>
</p>

<!--p>
<bean:message key="text.welcome2"/>
</p-->

<!--p>
<bean:message key="text.welcome3"/>
</p-->

<p>
<bean:message key="text.welcome5"/>
</p>

<ul>
  <li>
    <a href="http://docs.codehaus.org/display/GEOSDOC/Documentation">
      Documentation
    </a>
  </li>
  <li>
    <a href="http://docs.codehaus.org/display/GEOS/Home">
      Wiki
    </a>
  </li>
  <li>
    <a href="http://jira.codehaus.org/secure/BrowseProject.jspa?id=10311">
      Task Tracker
    </a>
   </li>
   <li>
    <a href="http://www.moximedia.com:8080/imf-ows/imf.jsp?site=gs_users">
      User Map
    </a>
  </li>
</ul>

<p>
	<bean:message key="text.visitDemoPage"/>
</p>

<h2>
  <bean:write name="WCS" property="title"/>
</h2>
<pre><code><bean:write name="WCS" property="abstract"/>
</code></pre>
<ul>
  <li>
    <a href="<%=org.vfny.geoserver.util.Requests.getBaseUrl(request)%>wcs?service=WCS&version=1.0.0&request=GetCapabilities">WCS GetCapabilities</a>
  </li>
  <li>
	<a href="<%=org.vfny.geoserver.util.Requests.getBaseUrl(request)%>wcs?service=WCS&version=1.0.0&request=DescribeCoverage&coveragename=Arc_Sample">
	  describeCoverage
	</a>
  </li>
  <li>
  	<a href="<%=org.vfny.geoserver.util.Requests.getBaseUrl(request)%>wcs?service=WCS&version=1.0.0&request=GetCoverage&coverage=Arc_Sample&BBOX=42.2,9.42,43.9,11.82&format=png&crs=EPSG:4326&response_crs=EPSG:32626&interpolation=nearest_neighbor">
  	  getCoverage
  	</a>
  </li>
</ul>

<p>
	<bean:message key="text.newFeatureLogo"/>&nbsp;
	<a href="<%=org.vfny.geoserver.util.Requests.getBaseUrl(request)%>mapPreview.do"/><bean:message key="text.newFeature1"/></a>
</p>
<h2>
  <bean:write name="WFS" property="title"/>
</h2>
<pre><code><bean:write name="WFS" property="abstract"/>
</code></pre>
<ul>
  <li>
	<a href="<%=org.vfny.geoserver.util.Requests.getBaseUrl(request)%>wfs?request=GetCapabilities&service=WFS&version=1.0.0">WFS GetCapabilities</a><br>
  </li>
  <li>
    <a href="<%=org.vfny.geoserver.util.Requests.getBaseUrl(request)%>wfs/TestWfsPost">
      TestWfsPost
    </a>
  </li>
</ul>

<h2>
  <bean:write name="WMS" property="title"/>
</h2>
<pre><code><bean:write name="WMS" property="abstract"/>
</code></pre>

<ul>
  <li>
	<a href="<%=org.vfny.geoserver.util.Requests.getBaseUrl(request)%>wms?request=GetCapabilities&service=WMS&version=1.0.0">WMS GetCapabilities</a>
  </li>
  <li>
  	<a href="<%=org.vfny.geoserver.util.Requests.getBaseUrl(request)%>wms?bbox=-130,24,-66,50&styles=raster,population&Format=image/png&request=GetMap&layers=Img_Sample,topp:states&width=550&height=250&srs=EPSG:4326">
  	  getMap
  	</a>
  </li>
  <li>
  	<a href="<%=org.vfny.geoserver.util.Requests.getBaseUrl(request)%>data/quickWMS/demo.jsp">
  	  <strong>quickWMS</strong>
  	</a>
  </li>
</ul>

<p>
	<bean:message key="text.visitDemoPage"/>
<bean:message key="text.welcome4"/>
</p>

<h2>
  <bean:message key="text.welcome.mapbuilder"/>
</h2>

<p>
<bean:message key="text.welcome.mapbuilder.detail"/>
</p>
<ul>
  <li>
    <a href="<%=org.vfny.geoserver.util.Requests.getBaseUrl(request)%>data/mbdemos/demo/wfs-t/index.html">
      Mapbuilder/Geoserver (Tasmania)
    </a>
  </li>
  <li>
    <a href="<%=org.vfny.geoserver.util.Requests.getBaseUrl(request)%>data/mbdemos/demo/cite/index.html">
      Mapbuilder/Geoserver (CITE)
    </a>
  </li>
</ul>

<br>
