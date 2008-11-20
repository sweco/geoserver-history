/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.catalog;

import java.util.Collection;
import java.util.List;

import org.geoserver.catalog.event.CatalogListener;

/**
 * The GeoServer catalog which provides access to meta information about the 
 * data served by GeoServer.
 * <p>
 * The following types of metadata are stored:
 * <ul>
 *   <li>namespaces and workspaces
 *   <li>coverage (raster) and data (vector) stores
 *   <li>coverages and feature resoures
 *   <li>styles 
 * </ul>
 * </p>
 * <h2>Workspaces</h2>
 * <p>
 * A workspace is a container for any number of stores. All workspaces can be
 * obtained with the {@link #getWorkspaces()}. A workspace is identified by its 
 * name ({@link WorkspaceInfo#getName()}). A workspace can be looked up by its 
 * name with the {@link #getWorkspaceByName(String)} method. 
 * </p>
 * <h2>Stores</h2>
 * <p>
 *  The {@link #getStores(Class)} method provides access to all the stores in 
 *  the catalog of a specific type. For instance, the following would obtain 
 *  all datstores from the catalog:
 *  <pre>
 *  //get all datastores
 *  List<DataStoreInfo> dataStores = catalog.getStores( DataStoreInfo.class );
 *  </pre>
 *  The methods {@link #getDataStores()} and {@link #getCoverageStores()} provide
 *  a convenience for the two well known types.
 * </p>
 * <p>
 *  A store is contained within a workspace (see {@link StoreInfo#getWorkspace()}).
 *  The {@link #getStoresByWorkspace(WorkspaceInfo, Class)} method for only stores 
 *  contained with a specific workspace. For instance, the following would obtain 
 *  all datastores store within a particular workspace:
 *  <pre>
 *  //get a workspace
 *  WorkspaceInfo workspace = catalog.getWorkspace( "myWorkspace" );
 * 
 *  //get all datastores in that workspace
 *  List<DataStoreInfo> dataStores = catalog.getStoresByWorkspace( workspace, DataStoreInfo.class );
 *  </pre>
 * </p>
 * <h2>Resources</h2>
 * <p>
 * The {@link #getResources(Class)} method provides access to all resources in 
 * the catalog of a particular type. For instance, to acess all feature types in 
 * the catalog:
 * <pre>
 *  List<FeatureTypeInfo> featureTypes = catalog.getResources( FeatureTypeInfo.class );
 * </pre>
 * The {@link #getFeatureTypes()} and {@link #getCoverages()} methods are a convenience
 * for the well known types.
 * </p>
 * <p>
 * A resource is contained within a namespace, therefore it is identified by a 
 * namespace uri, local name pair. The {@link #getResourceByName(String, String, Class)} 
 * method provides access to a resource by its namespace qualified name. The method
 * {@link #getResourceByName(String, Class)} provides access to a resource by its 
 * unqualified name. The latter method will do an exhaustive search of all namespaces
 * for a resource with the specified name. If only a single resoure with the name
 * is found it is returned. Some examples:
 * <pre>
 *   //get a feature type by its qualified name
 *   FeatureTypeInfo ft = catalog.getResourceByName( 
 *       "http://myNamespace.org", "myFeatureType", FeatureTypeInfo.class );
 *       
 *   //get a feature type by its unqualified name
 *   ft = catalog.getResourceByName( "myFeatureType", FeatureTypeInfo.class );
 *   
 *   //get all feature types in a namespace
 *   NamespaceInfo ns = catalog.getNamespaceByURI( "http://myNamespace.org" );
 *   List<FeatureTypeInfo> featureTypes = catalog.getResourcesByNamespace( ns, FeatureTypeINfo.class );
 *  </pre>
 * </p>
 * <h2>Layers</h2>
 * <p>
 * A layers is used to publish a resource. The {@link #getLayers()} provides access 
 * to all layers in the catalog. A layer is uniquely identified by its name. The
 * {@link #getLayerByName(String)} method provides access to a layer by its name.
 * The {@link #getLayers(ResourceInfo)} return all the layers publish a specific 
 * resource. Some examples:
 * <pre>
 *  //get a layer by its name
 *  LayerInfo layer = catalog.getLayer( "myLayer" );
 * 
 *  //get all the layers for a particualr feature type
 *  FeatureTypeInfo ft = catalog.getFeatureType( "http://myNamespace", "myFeatureType" );
 *  List<LayerInfo> layers = catalog.getLayers( ft );
 *  
 * </pre>
 *
 * </p>
 * <h2>Modifing the Catalog</h2>
 * <p>
 * Catalog objects such as stores and resoures are mutable and can be modified.
 * However, any modifications made on an object do not apply until they are saved.
 * For instance, consider the following example of modifying a feature type: 
 * <pre>
 *  //get a feature type
 *  FeatureTypeInfo featureType = catalog.getFeatureType( "http://myNamespace.org", "myFeatureType" );
 *  
 *  //modify it
 *  featureType.setBoundingBox( new Envelope(...) );
 *  
 *  //save it
 *  catalog.save( featureType );
 * </pre>
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning project
 */
public interface Catalog {

    /**
     * The factory used to create catalog objects.
     * 
     */
    CatalogFactory getFactory();

    /**
     * Adds a new store.
     */
    void add(StoreInfo store);

    /**
     * Removes an existing store.
     */
    void remove(StoreInfo store);

    /**
     * Saves a store that has been modified.
     */
    void save(StoreInfo store);

    /**
     * Returns the store with the specified id.
     * <p>
     * <tt>clazz</td> is used to determine the implementation of StoreInfo 
     * which should be returned. An example which would return a data store.
     * <pre>
     *   <code>
     * DataStoreInfo dataStore = catalog.getStore(&quot;id&quot;, DataStoreInfo.class);
     * </code>
     * </pre>
     * </p>
     * @param id The id of the store.
     * @param clazz The class of the store to return.
     * 
     * @return The store matching id, or <code>null</code> if no such store exists.
     */
    <T extends StoreInfo> T getStore(String id, Class<T> clazz);

    /**
     * Returns the store with the specified name.
     * <p>
     * <tt>clazz</td> is used to determine the implementation of StoreInfo 
     * which should be returned. An example which would return a data store.
     * <pre>
     *   <code>
     * DataStoreInfo dataStore = catalog.getStore(&quot;name&quot;, DataStoreInfo.class);
     * </code>
     * </pre>
     * </p>
     * @param name The name of the store.
     * @param clazz The class of the store to return.
     * 
     * @return The store matching name, or <code>null</code> if no such store exists.
     */
    <T extends StoreInfo> T getStoreByName(String name, Class<T> clazz);

    /**
     * All stores in the catalog of the specified type.
     * 
     * <p>
     * The <tt>clazz</tt> parameter is used to filter the types of stores
     * returned. An example which would return all data stores:
     * 
     * <pre> 
     *   <code>
     * catalog.getStores(DataStoreInfo.class);
     * </code> 
     * </pre>
     * 
     * </p>
     * 
     */
    <T extends StoreInfo> List<T> getStores(Class<T> clazz);

    /**
     * All stores in the specified workspace of the given type.
     * <p>
     * The <tt>clazz</tt> parameter is used to filter the types of stores
     * returned. An example which would return all data stores in a specific 
     * workspace:
     * <pre> 
     *   <code>
     * WorkspaceInfo workspace = ...;
     * List<DataStoreInfo> dataStores = 
     *     catalog.getStores(workspace, DataStoreInfo.class);
     * </code> 
     * </pre>
     * 
     * </p>
     * 
     * @param workspace The workspace containing returned stores.
     * @param clazz The type of stores to lookup.
     * 
     */
    <T extends StoreInfo> List<T> getStoresByWorkspace(WorkspaceInfo workspace,Class<T> clazz);
    
    /**
     * Returns a datastore matching a particular id, or <code>null</code> if
     * no such data store could be found.
     * <p>
     * This method is convenience for:
     * <pre>
     * getStore( id, DataStoreInfo.class );
     * </pre>
     * </p>
     */
    DataStoreInfo getDataStore(String id);

    /**
     * Returns a datastore matching a particular name, or <code>null</code> if
     * no such data store could be found.
     * <p>
     * This method is a convenience for:
     *  <pre>
     *  getStoreByName(name, DataStoreInfo.class)
     * </pre>
     * </p>
     */
    DataStoreInfo getDataStoreByName(String name);

    /**
     * All data stores in the catalog.
     * <p>
     * The resulting list should not be modified to add or remove stores, the 
     * {@link #add(StoreInfo)} and {@link #remove(StoreInfo)} are used for this
     * purpose.
     * </p>
     */
    List<DataStoreInfo> getDataStores();

    /**
     * Returns a coverage store matching a particular id, or <code>null</code>
     * if no such coverage store could be found.
     * <p>
     * This method is convenience for:
     * <pre>
     * getStore( id, CoverageStoreInfo.class );
     * </pre>
     * </p>
     */
    CoverageStoreInfo getCoverageStore(String id);

    /**
     * Returns a coverage store matching a particular name, or <code>null</code>
     * if no such coverage store could be found.
     * <p>
     * This method is a convenience for: <code>
     *   <pre>
     * getStoreByName(name, CoverageStoreInfo.class)
     * </pre>
     * </code>
     * </p>
     * 
     */
    CoverageStoreInfo getCoverageStoreByName(String name);

    /**
     * All coverage stores in the catalog.
     *<p>
     * The resulting list should not be modified to add or remove stores, the 
     * {@link #add(StoreInfo)} and {@link #remove(StoreInfo)} are used for this
     * purpose.
     * </p>
     */
    List<CoverageStoreInfo> getCoverageStores();

    /**
     * Returns the resource with the specified id.
     * <p>
     * <tt>clazz</td> is used to determine the implementation of ResourceInfo 
     * which should be returned. An example which would return a feature type.
     * <pre>
     *   <code>
     * FeatureTypeInfo ft = catalog.getResource(&quot;id&quot;, FeatureTypeInfo.class);
     * </code>
     * </pre>
     * </p>
     * @param id The id of the resource.
     * @param clazz The class of the resource to return.
     * 
     * @return The resource matching id, or <code>null</code> if no such resource exists.
     */
    <T extends ResourceInfo> T getResource(String id, Class<T> clazz);

    /**
     * Looks up a resource by qualified name.
     * <p>
     * <tt>ns</tt> may be specified as a namespace prefix or uri.
     * </p>
     * <p>
     * <tt>clazz</td> is used to determine the implementation of ResourceInfo 
     * which should be returned.
     * </p>
     * @param ns
     *                The prefix or uri to which the resource belongs, may be
     *                <code>null</code>.
     * @param name
     *                The name of the resource.
     * @param clazz
     *                The class of the resource.
     * 
     * @return The resource matching the name, or <code>null</code> if no such
     *         resource exists.
     */
    <T extends ResourceInfo> T getResourceByName(String ns, String name,
            Class<T> clazz);

    /**
     * Looks up a resource by its unqualified name.
     * <p>
     * The lookup rules used by this method are as follows:
     * <ul>
     *  <li>If a resource in the default namespace is found matching the 
     *  specified name, it is returned.
     *  <li>If a single resource among all non-default namespaces is found 
     *  matching the the specified name, it is returned.  
     * </ul>
     * Care should be taken when using this method, use of {@link #getResourceByName(String, String, Class)}
     * is preferred.
     * </p>
     * 
     * @param name The name of the resource.
     * @param clazz The type of the resource.
     * 
     */
    <T extends ResourceInfo> T getResourceByName(String name, Class<T> clazz);
    
    /**
     * Adds a new resource.
     */
    void add(ResourceInfo resource);

    /**
     * Removes an existing resource.
     */
    void remove(ResourceInfo resource);

    /**
     * Saves a resource which has been modified.
     */
    void save(ResourceInfo resource);

    /**
     * All resources in the catalog of the specified type.
     * <p>
     * The <tt>clazz</tt> parameter is used to filter the types of resources
     * returned. An example which would return all feature types:
     * 
     * <pre> 
     *   <code>
     * catalog.getResources(FeatureTypeInfo.class);
     * </code> 
     * </pre>
     * 
     * </p>
     * 
     */
    <T extends ResourceInfo> List<T> getResources(Class<T> clazz);

    /**
     * All resources in the specified namespace of the specified type.
     * <p>
     * The <tt>clazz</tt> parameter is used to filter the types of resources
     * returned. An example which would return all feature types:
     * </p>
     * 
     * @param namespace
     *                The namespace.
     * @param clazz
     *                The class of resources returned.
     * 
     * @return List of resources of the specified type in the specified
     *         namespace.
     */
    <T extends ResourceInfo> List<T> getResourcesByNamespace(
            NamespaceInfo namespace, Class<T> clazz);

    /**
     * Returns the feature type matching a particular id, or <code>null</code>
     * if no such feature type could be found.
     * <p>
     * This method is convenience for:
     * <pre>
     * getResource( id, FeatureTypeInfo.class );
     * </pre>
     * </p>
     * @return The feature type matching the id, or <code>null</code> if no
     *         such resource exists.
     */
    FeatureTypeInfo getFeatureType(String id);

    /**
     * Looks up a feature type by qualified name.
     * <p>
     * <tt>ns</tt> may be specified as a namespace prefix or uri.
     * </p>
     * <p>
     * This method is convenience for:
     * <pre>
     * getResourceByName( ns, name, FeatureTypeInfo.class );
     * </pre>
     * </p>
     * 
     * @param ns
     *                The prefix or uri to which the feature type belongs, may
     *                be <code>null</code>.
     * @param name
     *                The name of the feature type.
     * 
     * @return The feature type matching the name, or <code>null</code> if no
     *         such resource exists.
     */
    FeatureTypeInfo getFeatureTypeByName(String ns, String name);
    
    /**
     * Looks up a feature type by an unqualified name.
     * <p>
     * The lookup rules used by this method are as follows:
     * <ul>
     *  <li>If a feature type in the default namespace is found matching the 
     *  specified name, it is returned.
     *  <li>If a single feature type among all non-default namespaces is found 
     *  matching the the specified name, it is returned.  
     * </ul>
     * Care should be taken when using this method, use of {@link #getFeatureTypeByName(String, String)}
     * is preferred.
     * </p>
     * 
     * @param name The name of the feature type.
     * 
     * @return The single feature type matching the specified name, or <code>null</code>
     * if either none could be found or multiple were found.
     */
    FeatureTypeInfo getFeatureTypeByName( String name );

    /**
     * ALl feature types in the catalog.
     * <p>
     * This method is a convenience for:
     * 
     * <pre>
     * 	<code>
     * getResources(FeatureTypeInfo.class);
     * </code>
     * </pre>
     * 
     * </p>
     * <p>
     * The resulting list should not be used to add or remove resources from 
     * the catalog, the {@link #add(ResourceInfo)} and {@link #remove(ResourceInfo)}
     * methods are used for this purpose.
     * </p>
     */
    List<FeatureTypeInfo> getFeatureTypes();

    /**
     * All feature types in the specified namespace.
     * <p>
     * This method is a convenience for: <code>
     *   <pre>
     * getResourcesByNamespace(namespace, FeatureTypeInfo.class);
     * </pre>
     * </code>
     * </p>
     * 
     * @param namespace
     *                The namespace of feature types to return.
     * 
     * @return All the feature types in the specified namespace.
     */
    List<FeatureTypeInfo> getFeatureTypesByNamespace(NamespaceInfo namespace);

    /**
     * Returns the coverage matching a particular id, or <code>null</code> if
     * no such coverage could be found.
     * <p>
     * This method is a convenience for:
     * <pre>
     * getResource( id, CoverageInfo.class );
     * </pre>
     * </p>
     */
    CoverageInfo getCoverage(String id);

    /**
     * Looks up a coverage by qualified name.
     * <p>
     * <tt>ns</tt> may be specified as a namespace prefix or uri.
     * </p>
     * <p>
     * This method is convenience for:
     * <pre>
     * getResourceByName(ns,name,CoverageInfo.class);
     * </pre>
     * </p>
     * 
     * @param ns
     *                The prefix or uri to which the coverage belongs, may be
     *                <code>null</code>.
     * @param name
     *                The name of the coverage.
     * 
     * @return The coverage matching the name, or <code>null</code> if no such
     *         resource exists.
     */
    CoverageInfo getCoverageByName(String ns, String name);

    
    /**
     * Looks up a coverage by an unqualified name.
     * <p>
     * The lookup rules used by this method are as follows:
     * <ul>
     *  <li>If a coverage in the default namespace is found matching the 
     *  specified name, it is returned.
     *  <li>If a single coverage among all non-default namespaces is found 
     *  matching the the specified name, it is returned.  
     *  
     * </ul>
     * Care should be taken when using this method, use of {@link #getCoverageByName(String, String)}
     * is preferred.
     * </p>
     * 
     * @param name The name of the coverage.
     * 
     * @return The single coverage matching the specified name, or <code>null</code>
     * if either none could be found or multiple were found. 
     */
    CoverageInfo getCoverageByName( String name );
    
    /**
     * All coverages in the catalog.
     * <p>
     * This method is a convenience for:
     * 
     * <pre>
     * 	<code>
     * getResources(CoverageInfo.class);
     * </code>
     * </pre>
     * 
     * </p>
     * 
     * <p>
     * This method should not be used to add or remove coverages from the 
     * catalog. The {@link #add(ResourceInfo)} and {@link #remove(ResourceInfo)} 
     * methods are used for this purpose. 
     * </p>
     */
    List<CoverageInfo> getCoverages();

    /**
     * Adds a new layer.
     */
    void add(LayerInfo layer);

    /**
     * Removes an existing layer.
     */
    void remove(LayerInfo layer);

    /**
     * Saves a layer which has been modified.
     */
    void save(LayerInfo layer);
    
    /**
     * All coverages in the specified namespace.
     * <p>
     * This method is a convenience for: <code>
     *   <pre>
     * getResourcesByNamespace(namespace, CoverageInfo.class);
     * </pre>
     * </code>
     * </p>
     * 
     * @param namespace
     *                The namespace of coverages to return.
     * 
     * @return All the coverages in the specified namespace.
     */
    List<CoverageInfo> getCoveragesByNamespace(NamespaceInfo namespace);

    /**
     * Returns the layer matching a particular id, or <code>null</code> if no
     * such layer could be found.
     */
    LayerInfo getLayer(String id);

    /**
     * Returns the layer matching a particular name, or <code>null</code> if no
     * such layer could be found. 
     */
    LayerInfo getLayerByName( String name );
    
    /**
     * All layers in the catalog.
     * <p>
     * The resulting list should not be used to add or remove layers to or from
     * the catalog, the {@link #add(LayerInfo)} and {@link #remove(LayerInfo)}
     * methods are used for this purpose.
     * </p>
     * 
     */
    List<LayerInfo> getLayers();

    /**
     * All layers in the catalog that publish the specified resource.
     * 
     * @param resource The resource.
     * 
     * @return A list of layers for the resource, or an empty list.
     */
    List<LayerInfo> getLayers( ResourceInfo resource );
    
    /**
     * Adds a new map.
     */
    void add(MapInfo map);

    /**
     * Removes an existing map.
     */
    void remove(MapInfo map);

    /**
     * Saves a map which has been modified.
     */
    void save( MapInfo map);

    /**
     * All maps in the catalog.
     * <p>
     * The resulting list should not be used to add or remove maps to or from
     * the catalog, the {@link #add(MapInfo)} and {@link #remove(MapInfo)} methods
     * are used for this purpose.
     * </p>
     */
    List<MapInfo> getMaps();

    /**
     * Returns the map matching a particular id, or <code>null</code> if no
     * such map could be found.
     */
    MapInfo getMap(String id);
    
    /**
     * Returns the map matching a particular name, or <code>null</code> if no
     * such map could be found.
     */
    MapInfo getMapByName(String name);

    /**
     * Adds a layer group to the catalog.
     */
    void add(LayerGroupInfo layerGroup);
    
    /**
     * Removes a layer group from the catalog.
     */
    void remove(LayerGroupInfo layerGroup);
    
    /**
     * Saves changes to a modified layer group.
     */
    void save(LayerGroupInfo layerGroup);
    
    /**
     * All layer groups in the catalog.
     */
    List<LayerGroupInfo> getLayerGroups();

    /**
     * Returns the layer group matching a particular id, or <code>null</code> if no
     * such group could be found.
     */
    LayerGroupInfo getLayerGroup(String id);
    
    /**
     * Returns the layer group matching a particular name, or <code>null</code> if no
     * such group could be found.
     */
    LayerGroupInfo getLayerGroupByName(String name);
    
    /**
     * Adds a new style.
     * 
     */
    void add(StyleInfo style);

    /**
     * Removes a style.
     */
    void remove(StyleInfo style);

    /**
     * Saves a style which has been modified.
     */
    void save(StyleInfo style);
    
    /**
     * Returns the style matching a particular id, or <code>null</code> if no
     * such style could be found.
     */
    StyleInfo getStyle(String id);

    /**
     * Returns the style matching a particular name, or <code>null</code> if
     * no such style could be found.
     * 
     * @param name
     *                The name of the style to return.
     */
    StyleInfo getStyleByName(String name);

    /**
     * All styles in the catalog.
     * <p>
     * The resulting list should not be used to add or remove styles, the methods
     * {@link #add(StyleInfo)} and {@link #remove(StyleInfo)} are used for that 
     * purpose.
     *  </p>
     */
    List<StyleInfo> getStyles();

    /**
     * Adds a new namespace.
     */
    void add(NamespaceInfo namespace);

    /**
     * Removes an existing namespace.
     */
    void remove(NamespaceInfo namespace);

    /**
     * Saves a namespace which has been modified.
     */
    void save(NamespaceInfo namespace);
    
    /**
     * Returns the namespace matching the specified id.
     * 
     */
    NamespaceInfo getNamespace(String id);

    /**
     * Looks up a namespace by its prefix.
     * 
     * @see NamespaceInfo#getPrefix()
     */
    NamespaceInfo getNamespaceByPrefix(String prefix);

    /**
     * Looks up a namespace by its uri.
     * 
     * @see NamespaceInfo#getURI()
     */
    NamespaceInfo getNamespaceByURI(String uri);

    /**
     * The default namespace of the catalog.
     * 
     */
    NamespaceInfo getDefaultNamespace();

    /**
     * Sets the default namespace of the catalog.
     * 
     * @param defaultNamespace
     *                The defaultNamespace to set.
     */
    void setDefaultNamespace(NamespaceInfo defaultNamespace);

    /**
     * All namespaces in the catalog.
     * <p>
     * The resulting list should not be used to add or remove namespaces from 
     * the catalog, the methods {@link #add(NamespaceInfo)} and {@link #remove(NamespaceInfo)}
     * should be used for that purpose.
     * </p>
     */
    List<NamespaceInfo> getNamespaces();

    /**
     * Adds a new workspace
     */
    void add(WorkspaceInfo workspace);
    
    /**
     * Removes an existing workspace.
     */
    void remove(WorkspaceInfo workspace);
    
    /**
     * Saves changes to an existing workspace.
     */
    void save(WorkspaceInfo workspace);
    
    /**
     * The default workspace for the catalog.
     */
    WorkspaceInfo getDefaultWorkspace();
    
    /**
     * Sets the default workspace for the catalog.
     */
    void setDefaultWorkspace( WorkspaceInfo workspace );

    /**
     * All workspaces in the catalog.
     * <p>
     * The resulting list should not be used to add or remove workspaces from 
     * the catalog, the methods {@link #add(WorkspaceInfo)} and {@link #remove(WorkspaceInfo)}
     * should be used for that purpose.
     * </p>
     */
    List<WorkspaceInfo> getWorkspaces();
    
    /**
     * Returns a workspace by id, or <code>null</code> if no such workspace
     * exists.
     */
    WorkspaceInfo getWorkspace( String id );
    
    /**
     * Returns a workspace by name, or <code>null</code> if no such workspace
     * exists.
     */
    WorkspaceInfo getWorkspaceByName( String name );
    
    /**
     * Adds a new model.
     */
    void add(ModelInfo model);
    /**
     * Adds a new model-run.
     */
    void add(ModelRunInfo modelRun);

    /**
     * Removes an existing model.
     */
    void remove(ModelInfo model);
    /**
     * Removes an existing model-run.
     */
    void remove(ModelRunInfo modelRun);

    /**
     * Saves a model which has been modified.
     */
    void save(ModelInfo model);
    /**
     * Saves a model-run which has been modified.
     */
    void save(ModelRunInfo modelRun);
    
    /**
     * Returns the model matching a particular id, or <code>null</code> if no
     * such model could be found.
     */
    ModelInfo getModel(String id);
    /**
     * Returns the model-run matching a particular id, or <code>null</code> if no
     * such model-run could be found.
     */
    ModelRunInfo getModelRun(String id);

    /**
     * 
     * @param variableName
     * @return
     */
    GeophysicParamInfo getGeophysicParamByName(String variableName);
    /**
     * Returns the model matching a particular name, or <code>null</code> if no
     * such model could be found. 
     */
    ModelInfo getModelByName(String name);
    /**
     * Returns the model-run matching a particular name, or <code>null</code> if no
     * such model-run could be found. 
     */
    ModelRunInfo getModelRunByName(String name);
    
    /**
     * All models in the catalog.
     * <p>
     * The resulting list should not be used to add or remove models to or from
     * the catalog, the {@link #add(ModelInfo)} and {@link #remove(ModelInfo)}
     * methods are used for this purpose.
     * </p>
     * 
     */
    List<GeophysicParamInfo> getGeophysicParams();
    /**
     * All models in the catalog.
     * <p>
     * The resulting list should not be used to add or remove models to or from
     * the catalog, the {@link #add(ModelInfo)} and {@link #remove(ModelInfo)}
     * methods are used for this purpose.
     * </p>
     * 
     */
    List<ModelInfo> getModels();
    /**
     * All model-runs in the catalog.
     * <p>
     * The resulting list should not be used to add or remove model-runs to or from
     * the catalog, the {@link #add(ModelRunInfo)} and {@link #remove(ModelRunInfo)}
     * methods are used for this purpose.
     * </p>
     * 
     */
    List<ModelRunInfo> getModelRuns();
    /**
     * 
     * @param theModel
     * @return
     */
    List<ModelRunInfo> getModelRuns(ModelInfo model);
    /**
     * 
     * @param mr
     * @return
     */
    List<CoverageInfo> getGridCoverages(ModelRunInfo modelRun);
    /**
     * 
     * @param param
     * @return
     */
    List<ModelInfo> getModels(GeophysicParamInfo param);
    /**
     * 
     * @param coverage
     * @return
     */
    List<GeophysicParamInfo> getGeophysicalParams(CoverageInfo coverage);

    /**
     * catalog listeners.
     * 
     */
    Collection<CatalogListener> getListeners();

    /**
     * Adds a listener to the catalog.
     */
    void addListener(CatalogListener listener);

    /**
     * Removes a listener from the catalog.
     */
    void removeListener(CatalogListener listener);

    /**
     * Returns the pool or cache for resources.
     * <p>
     * This object is used to load physical resources like data stores, feature
     * types, styles, etc...
     * </p>
     */
    ResourcePool getResourcePool();

    /**
     * Sets the resource pool reference.
     */
    void setResourcePool( ResourcePool resourcePool );
    
    /**
     * Disposes the catalog, freeing up any resources.
     */
    void dispose();

}
