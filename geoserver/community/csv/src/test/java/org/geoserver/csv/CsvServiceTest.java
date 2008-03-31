package org.geoserver.csv;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.geoserver.csv.LayerResult.LayerOperation;
import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;

public class CsvServiceTest extends AbstractPostgisTest {

    private CsvService csv;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        csv = new CsvService(store, ddl) {

            @Override
            public String getLayerDescription(String layerId) {
                return "";
            }

        };
    }
    
    public void testImport() throws Exception {
        List<LayerResult> results = csv.configureCsvFile("road", "id",
                getCsvFile("/resources/csv/roadOneAttribute.csv"));

        // check the results are the expected ones
        assertEquals(1, results.size());
        assertEquals("surface_view", results.get(0).getLayerName());
        assertEquals(LayerOperation.CREATED, results.get(0).getOperation());

        // grab the geom column
        FeatureType roadType = store.getSchema("road");

        // grab the surface type and view
        FeatureType surfaceType = store.getSchema("surface");
        FeatureType surfaceViewType = store.getSchema("surface_view");
        assertEquals(2, surfaceType.getAttributeCount());
        assertEquals(2 + roadType.getAttributeCount(), surfaceViewType
                .getAttributeCount());
        
        // now check the features from the table
        FeatureIterator it = store.getFeatureSource("surface").getFeatures().features();
        assertTrue(it.hasNext());
        Feature f = it.next();
        assertEquals("rd1", f.getAttribute("id"));
        assertEquals("paved", f.getAttribute("surface"));
        assertFalse(it.hasNext());
        it.close();
    }

    public void testImportReplace() throws Exception {
        List<LayerResult> results = csv.configureCsvFile("road", "id",
                getCsvFile("/resources/csv/roadOneAttribute.csv"));

        // check the results are the expected ones
        assertEquals(1, results.size());
        assertEquals("surface_view", results.get(0).getLayerName());
        assertEquals(LayerOperation.CREATED, results.get(0).getOperation());

        // grab the geom column
        FeatureType roadType = store.getSchema("road");

        // grab the surface type and view
        FeatureType surfaceType = store.getSchema("surface");
        FeatureType surfaceViewType = store.getSchema("surface_view");
        assertEquals(2, surfaceType.getAttributeCount());
        assertEquals(2 + roadType.getAttributeCount(), surfaceViewType
                .getAttributeCount());
        
        // make sure there is only one feature
        assertEquals(1, store.getFeatureSource("surface").getCount(Query.ALL));
        assertEquals(1, store.getFeatureSource("surface_view").getCount(Query.ALL));

        // now re-import, make sure it works and we have overwritten the tables
        results = csv.configureCsvFile("road", "id",
                getCsvFile("/resources/csv/roadTwoAttributes.csv"));

        // check the results are the expected ones
        assertEquals(2, results.size());
        assertEquals("surface_view", results.get(0).getLayerName());
        assertEquals(LayerOperation.REPLACED, results.get(0).getOperation());
        assertEquals("maxSpeed_view", results.get(1).getLayerName());
        assertEquals(LayerOperation.CREATED, results.get(1).getOperation());

        // grab the surface type and view
        surfaceType = store.getSchema("surface");
        surfaceViewType = store.getSchema("surface_view");
        assertEquals(2, surfaceType.getAttributeCount());
        assertEquals(2 + roadType.getAttributeCount(), surfaceViewType
                .getAttributeCount());

        // grab the max speed type and view
        FeatureType maxSpeedType = store.getSchema("maxSpeed");
        FeatureType maxSpeedViewType = store.getSchema("maxSpeed_view");
        assertEquals(2, maxSpeedType.getAttributeCount());
        assertEquals(2 + roadType.getAttributeCount(), maxSpeedViewType
                .getAttributeCount());
        
        // make sure we have three features per table this time
        assertEquals(3, store.getFeatureSource("surface").getCount(Query.ALL));
        assertEquals(3, store.getFeatureSource("surface_view").getCount(Query.ALL));
        assertEquals(3, store.getFeatureSource("maxSpeed").getCount(Query.ALL));
        assertEquals(3, store.getFeatureSource("maxSpeed_view").getCount(Query.ALL));
    }

    private File getCsvFile(final String fileLocation)
            throws URISyntaxException {
        return new File(CsvFileReaderHeaderTest.class.getResource(fileLocation)
                .toURI());
    }
}
