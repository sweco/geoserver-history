.. _app-schema.wms-support:

WMS Support
========

App-schema supports WMS requests as well as WFS requests. 
This page provides some useful examples for configuring the WMS service to work with complex features.

Remark that the rendering performance of WMS can be significantly slower when using app-schema data stores (depending on the kind of mapping).
If the desired performance level cannot be attained we suggest to use flattened database views rather than app-schema.

GetMap
-------

Read :ref:`wms_getmap` for general information on the GetMap request.
Read :ref:`styling` for general information on how to style WMS maps with SLD files.
When styling complex features, you can use X-paths to specify nested properties in your filters, as explained in :ref:`app-schema.filtering-nested`.
However,  in WMS styling filters it is not possible to follow nested properties by reference (*xlink:href*), unlike in WFS filters
(because the filters are applied after building the features rather than before.)
The prefix/namespace context that is used in the X-path expression is defined locally in the XML tags of the style file.
This is an example of an Style file for complex features:

.. code-block:: xml 
   :linenos: 

   <?xml version="1.0" encoding="UTF-8"?>
   <StyledLayerDescriptor version="1.0.0" 
       xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" 
       xmlns:ogc="http://www.opengis.net/ogc" 
       xmlns:xlink="http://www.w3.org/1999/xlink" 
       xmlns:gml="http://www.opengis.net/gml" 
       xmlns:gsml="urn:cgi:xmlns:CGI:GeoSciML:2.0"
       xmlns:sld="http://www.opengis.net/sld"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <sld:NamedLayer>
     <sld:Name>geology-lithology</sld:Name>
     <sld:UserStyle>
       <sld:Name>geology-lithology</sld:Name>
       <sld:Title>Geological Unit Lithology Theme</sld:Title>
       <sld:Abstract>The colour has been creatively adapted from Moyer,Hasting
            and Raines, 2005 (http://pubs.usgs.gov/of/2005/1314/of2005-1314.pdf) 
            which provides xls spreadsheets for various color schemes. 
            plus some creative entries to fill missing entries.
       </sld:Abstract>
       <sld:IsDefault>1</sld:IsDefault>
       <sld:FeatureTypeStyle>
         <sld:Rule>
           <sld:Name>acidic igneous material</sld:Name>
           <sld:Abstract>Igneous material with more than 63 percent SiO2.  
                          (after LeMaitre et al. 2002)
           </sld:Abstract>
           <ogc:Filter>
             <ogc:PropertyIsEqualTo>
               <ogc:PropertyName>gsml:specification/gsml:GeologicUnit/gsml:composition/
                    gsml:CompositionPart/gsml:lithology/@xlink:href</ogc:PropertyName>
               <ogc:Literal>urn:cgi:classifier:CGI:SimpleLithology:200811:
                            acidic_igneous_material</ogc:Literal>
             </ogc:PropertyIsEqualTo>
           </ogc:Filter>
           <sld:PolygonSymbolizer>
             <sld:Fill>
               <sld:CssParameter name="fill">#FFCCB3</sld:CssParameter>
             </sld:Fill>
           </sld:PolygonSymbolizer>
         </sld:Rule>
         <sld:Rule>
           <sld:Name>acidic igneous rock</sld:Name>
           <sld:Abstract>Igneous rock with more than 63 percent SiO2.  
                        (after LeMaitre et al. 2002)
           </sld:Abstract>
           <ogc:Filter>
             <ogc:PropertyIsEqualTo>
               <ogc:PropertyName>gsml:specification/gsml:GeologicUnit/gsml:composition/
                    gsml:CompositionPart/gsml:lithology/@xlink:href</ogc:PropertyName>
               <ogc:Literal>urn:cgi:classifier:CGI:SimpleLithology:200811:
                            acidic_igneous_rock</ogc:Literal>
               </ogc:PropertyIsEqualTo>
           </ogc:Filter>
           <sld:PolygonSymbolizer>
             <sld:Fill>
               <sld:CssParameter name="fill">#FECDB2</sld:CssParameter>
             </sld:Fill>
           </sld:PolygonSymbolizer>
         </sld:Rule>
         ...
       </sld:FeatureTypeStyle>
     </sld:UserStyle>
    </sld:NamedLayer>
   </sld:StyledLayerDescriptor>
  

GetFeatureInfo
--------------

Read :ref:`wms_getfeatureinfo` for general information on the GetFeatureInfo request. 
Read the tutorial on :ref:`tutorials_getfeatureinfo` for information on how to template the html output.
If you want to store a separate standard template for complex feature collections, save it under the filename
``complex_content.ftl`` in the template directory.

Read the tutorial on :ref:`tutorial_freemarkertemplate` for more information on how to use the freemarker templates.
Freemarker templates support recursive calls, which can be useful for templating complex content.
For example, the following freemarker template creates a table of features with a column for each property, 
and will create another table inside each cell that contains a feature as property:

.. code-block:: html

  <#-- 
  Macro's used for content
  -->

  <#macro property node>
      <#if !node.isGeometry>
        <#if node.isComplex>      
        <td> <@feature node=node.rawValue type=node.type /> </td>  
        <#else>
        <td>${node.value?string}</td>
        </#if>
      </#if>
  </#macro>

  <#macro header typenode>
  <caption class="featureInfo">${typenode.name}</caption>
    <tr>
    <th>fid</th>
  <#list typenode.attributes as attribute>
    <#if !attribute.isGeometry>
      <#if attribute.prefix == "">      
          <th >${attribute.name}</th>
      <#else>
          <th >${attribute.prefix}:${attribute.name}</th>
      </#if>
    </#if>
  </#list>
    </tr>
  </#macro>

  <#macro feature node type>
  <table class="featureInfo">
    <@header typenode=type />
    <tr>
    <td>${node.fid}</td>    
    <#list node.attributes as attribute>
        <@property node=attribute />
    </#list>
    </tr>
  </table>
  </#macro>
    
  <#-- 
  Body section of the GetFeatureInfo template, it's provided with one feature collection, and
  will be called multiple times if there are various feature collections
  -->
  <table class="featureInfo">
    <@header typenode=type />

  <#assign odd = false>
  <#list features as feature>
    <#if odd>
      <tr class="odd">
    <#else>
      <tr>
    </#if>
    <#assign odd = !odd>

    <td>${feature.fid}</td>    
    <#list feature.attributes as attribute>
      <@property node=attribute />
    </#list>
    </tr>
  </#list>
  </table>
  <br/>



