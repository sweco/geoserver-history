package org.geoserver.wfs.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.opengis.wfs.FeatureCollectionType;
import net.opengis.wfs.WFSFactory;

import org.geoserver.data.feature.DataStoreInfo;
import org.geoserver.data.test.MockGeoServerDataDirectory;
import org.geoserver.wfs.WFSTestSupport;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.w3c.dom.Document;

public class GML3FeatureProducerTest extends WFSTestSupport {

	
	public void testSingle() throws Exception {
		DataStoreInfo dataStore = catalog.dataStore( MockGeoServerDataDirectory.CDF_PREFIX );
		FeatureSource source = 
			dataStore.getDataStore().getFeatureSource( MockGeoServerDataDirectory.SEVEN_TYPE );
		FeatureCollection features = source.getFeatures();
	
		FeatureCollectionType fcType = WFSFactory.eINSTANCE.createFeatureCollectionType();
		
		fcType.getFeature().add( features );
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		GML3FeatureProducer2 producer = new GML3FeatureProducer2( wfs, catalog, loader );
		producer.produce( null, fcType, output );
		
		System.out.println( new String( output.toByteArray() ) );
		
		DocumentBuilder docBuilder = 
			DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = docBuilder.parse( new ByteArrayInputStream( output.toByteArray() ) );
		assertEquals( 7, document.getElementsByTagName( "cdf:Seven" ).getLength() );
		
	}
	
	public void testMultipleSameNamespace() throws Exception {
		DataStoreInfo dataStore = catalog.dataStore( MockGeoServerDataDirectory.CDF_PREFIX );
 
		FeatureCollectionType fcType = WFSFactory.eINSTANCE.createFeatureCollectionType();
		fcType.getFeature().add( 
			dataStore.getDataStore().getFeatureSource( MockGeoServerDataDirectory.SEVEN_TYPE ).getFeatures()
		);
		fcType.getFeature().add( 
			dataStore.getDataStore().getFeatureSource( MockGeoServerDataDirectory.FIFTEEN_TYPE ).getFeatures()
		);
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		GML3FeatureProducer2 producer = new GML3FeatureProducer2( wfs, catalog , loader);
		producer.produce( null, fcType, output );
		
		System.out.println( new String( output.toByteArray() ) );
		
		DocumentBuilder docBuilder = 
			DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = docBuilder.parse( new ByteArrayInputStream( output.toByteArray() ) );
		assertEquals( 
			7+15, document.getElementsByTagName( "cdf:Seven" ).getLength() + 
				document.getElementsByTagName( "cdf:Fifteen" ).getLength() );
	}
	
	public void testMultipleDifferentNamespace() throws Exception {
		DataStoreInfo seven = catalog.dataStore( MockGeoServerDataDirectory.CDF_PREFIX );
		DataStoreInfo polys = catalog.dataStore( MockGeoServerDataDirectory.CGF_PREFIX );
		
		FeatureCollectionType fcType = WFSFactory.eINSTANCE.createFeatureCollectionType();
		fcType.getFeature().add( 
			seven.getDataStore().getFeatureSource( MockGeoServerDataDirectory.SEVEN_TYPE ).getFeatures()
		);
		fcType.getFeature().add( 
			polys.getDataStore().getFeatureSource( MockGeoServerDataDirectory.POLYGONS_TYPE ).getFeatures()
		);
		int npolys = polys.getDataStore().getFeatureSource( MockGeoServerDataDirectory.POLYGONS_TYPE ).getFeatures().size();
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		GML3FeatureProducer2 producer = new GML3FeatureProducer2( wfs, catalog, loader );
		producer.produce( null, fcType, output );
		
		System.out.println( new String( output.toByteArray() ) );
		
		DocumentBuilder docBuilder = 
			DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = docBuilder.parse( new ByteArrayInputStream( output.toByteArray() ) );
		assertEquals( 
			7+npolys, document.getElementsByTagName( "cdf:Seven" ).getLength() + 
				document.getElementsByTagName( "cgf:Polygons" ).getLength() );
	}
	
}
