package org.geoserver.web.data.table;

import org.geoserver.catalog.StoreInfo;
import org.geoserver.data.test.MockData;
import org.geoserver.web.GeoServerWicketTestSupport;
import org.geoserver.web.data.layer.NewLayerPageProvider;


public class NewLayerProviderTest extends GeoServerWicketTestSupport {
    
    @Override
    protected void populateDataDirectory(MockData dataDirectory)
            throws Exception {
        super.populateDataDirectory(dataDirectory);
        dataDirectory.addWellKnownCoverageTypes();
    }
    
    public void testFeatureType() {
        StoreInfo cite = getCatalog().getStoreByName( MockData.CITE_PREFIX,StoreInfo.class );
        NewLayerPageProvider provider = new NewLayerPageProvider();
        provider.setStoreId(cite.getId());
        provider.setShowPublished(true);
        assertTrue(provider.size() > 0);
        provider.setShowPublished(false);
        assertEquals(0, provider.size());
    }
    
    public void testCoverages() {
        StoreInfo dem = getCatalog().getStoreByName( MockData.TASMANIA_DEM.getLocalPart(),StoreInfo.class );
        NewLayerPageProvider provider = new NewLayerPageProvider();
        provider.setStoreId(dem.getId());
        provider.setShowPublished(true);
        assertTrue(provider.size() > 0);
        provider.setShowPublished(false);
        assertEquals(0, provider.size());
    }
    
    public void testEmpty() {
        NewLayerPageProvider provider = new NewLayerPageProvider();
        provider.setShowPublished(true);
        assertEquals(0, provider.size());
        provider.setShowPublished(false);
        assertEquals(0, provider.size());
    }

}