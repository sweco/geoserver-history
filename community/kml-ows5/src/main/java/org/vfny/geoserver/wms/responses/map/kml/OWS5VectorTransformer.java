package org.vfny.geoserver.wms.responses.map.kml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.geotools.feature.FeatureCollection;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.geotools.xml.transform.Translator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.vfny.geoserver.wms.WMSMapContext;
import org.vfny.geoserver.wms.responses.map.kml.OWS5GeometryTransformer.KML3GeometryTranslator;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

public class OWS5VectorTransformer extends KMLVectorTransformer {

    private boolean extendedDataModule;
    private boolean styleModule;

    public OWS5VectorTransformer(WMSMapContext mapContext, MapLayer mapLayer, 
            boolean extendedDataModule, boolean styleModule) {
        super(mapContext, mapLayer);
        this.extendedDataModule = extendedDataModule;
        this.styleModule = styleModule;
    }

    public Translator createTranslator(ContentHandler handler) {
        return new KML3Translator(handler);
    }

    protected class KML3Translator extends KMLTranslator {
        private boolean kml22DataStyle = false;
        protected String schemaId;

        public KML3Translator(ContentHandler contentHandler) {
            super(contentHandler);
            
            OWS5GeometryTransformer geometryTransformer = new OWS5GeometryTransformer();
            geometryTranslator = (KML3GeometryTranslator) geometryTransformer.createTranslator(contentHandler);

            // we need to make sure the data type converter is registered
            // properly
            DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);

            // We need to create a unique ID for this schema. Given the
            // restrictions of the XML ID
            // type and the fact the same feature type may appear multiple times
            // in the same
            // MapContext, we need to create an artificial ID that's guaranteed
            // to be unique across
            // the encoded KML document. We'll use the layer position as the ID.
            final MapLayer[] layers = OWS5VectorTransformer.this.mapContext.getLayers();
            for (int i = 0; i < layers.length; i++) {
                if (layers[i] == OWS5VectorTransformer.this.mapLayer) {
                    schemaId = "Schema" + (i + 1);
                }
            }
            if (schemaId == null)
                throw new IllegalStateException("Wrapping KMLVectorTransformer holds "
                        + "a layer reference that's not among the MapLayer ones");
        }

        public void encodeSchemas(FeatureCollection collection) {
            // if the extended data module is not active, don't encode schema and extended data
            if(!extendedDataModule)
                return;
            
            // TODO: consider turning this into a Freemarker template
            final SimpleFeatureType schema = collection.getSchema();
            final String[] atts = new String[] { "name", schema.getTypeName(), "id", schemaId };
            start("Schema", KMLUtils.attributes(atts));

            // output all non geometric attribute types
            // TODO: or shall we encode only the types that are well known in SchemaData
            // and jump over the rest? Besides, how does one limit the number of attributes
            // displayed? a PROPERTY=x,y,z like in GetFeature would be beneficial 
            // to GetFeatureInfo as well
            for (int i = 0; i < schema.getAttributeCount(); i++) {
                AttributeDescriptor at = schema.getAttribute(i);
                if (at instanceof GeometryDescriptor)
                    continue;

                final String[] atAttributes = new String[] { "type", getType(at), "name",
                        at.getLocalName() };
                start("SimpleField", KMLUtils.attributes(atAttributes));
                element("displayName", at.getLocalName());
                end("SimpleField");
            }

            end("Schema");
        }

        protected String getType(AttributeDescriptor at) {
            // see
            // http://code.google.com/apis/kml/documentation/kml_tags_beta1.html#simplefield
            // Eventually see if we need to support uint/ushort as well (do we
            // have any standard filter for positive numbers?) and clarify
            // what's the range of int and short
            if (Short.class.equals(at.getType().getBinding()))
                return "short";
            else if (Integer.class.equals(at.getType().getBinding()))
                return "int";
            else if (Float.class.equals(at.getType().getBinding()))
                return "float";
            else if (Double.class.equals(at.getType().getBinding()))
                return "double";
            else if (Boolean.class.equals(at.getType().getBinding()))
                return "bool";
            else
                return "string";
        }

        protected void encodeExtendedData(SimpleFeature feature) {
            // if the extended data module is not active, don't encode schema and extended data
            if(!extendedDataModule)
                return;
            
            if(kml22DataStyle)
                encodeKML22ExtendedData(feature);
            else 
                encodeKMLOWS5ExtendedData(feature);
        }

        private void encodeKML22ExtendedData(SimpleFeature feature) {
            // TODO: consider turning this into a Freemarker template
            start("ExtendedData");
            start("SchemaData", KMLUtils.attributes(new String[] { "schemaUrl", "#" + schemaId }));

            final int count = feature.getAttributeCount();
            final SimpleFeatureType schema = feature.getFeatureType();
            for (int i = 0; i < count; i++) {
                final AttributeDescriptor at = schema.getAttribute(i);
                if(at instanceof GeometryDescriptor)
                    continue;
                
                final Attributes atts = KMLUtils.attributes(new String[] { "name",
                        at.getLocalName() });
                element("SimpleData", encodeValue(feature.getAttribute(i)), atts);
            }

            end("SchemaData");
            end("ExtendedData");
        }
        
        private void encodeKMLOWS5ExtendedData(SimpleFeature feature) {
            // TODO: consider turning this into a Freemarker template
            start("ExtendedData", KMLUtils.attributes(new String[] { "schemaUrl", "#" + schemaId }));

            final int count = feature.getAttributeCount();
            final SimpleFeatureType schema = feature.getFeatureType();
            for (int i = 0; i < count; i++) {
                final AttributeDescriptor at = schema.getAttribute(i);
                if(at instanceof GeometryDescriptor)
                    continue;
                
                final Attributes atts = KMLUtils.attributes(new String[] { "name",
                        at.getLocalName() });
                element("Data", encodeValue(feature.getAttribute(i)), atts);
            }

            end("ExtendedData");
        }

        protected String encodeValue(Object o) {
            if (o == null) {
                return "";
            } else if (o instanceof Number) {
                if (o instanceof Byte)
                    return DatatypeConverter.printByte(((Byte) o).byteValue());
                else if (o instanceof Short)
                    return DatatypeConverter.printShort(((Short) o).shortValue());
                else if (o instanceof Integer)
                    return DatatypeConverter.printInt((((Integer) o).intValue()));
                else if (o instanceof Long)
                    return DatatypeConverter.printLong(((Long) o).intValue());
                else if (o instanceof Float)
                    return DatatypeConverter.printInt((((Float) o).intValue()));
                else if (o instanceof Double)
                    return DatatypeConverter.printInt(((Double) o).intValue());
                else if (o instanceof BigDecimal)
                    return DatatypeConverter.printDecimal((BigDecimal) o);
                else
                    return DatatypeConverter.printString(o.toString());
            } else if (o instanceof Boolean) {
                return DatatypeConverter.printBoolean((((Boolean) o).booleanValue()));
            } else if (o instanceof Date) {
                final Date d = (Date) o;
                final Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                if (d instanceof java.sql.Date)
                    return DatatypeConverter.printDate(cal);
                else if (d instanceof java.sql.Time)
                    return DatatypeConverter.printTime(cal);
                else
                    return DatatypeConverter.printDateTime(cal);
            } else {
                return DatatypeConverter.printString(o.toString());
            }

        }
        
        /**
         * Encodes a KML Placemark geometry from a geometry + centroid.
         */
        protected void encodePlacemarkGeometry(Geometry geometry, Coordinate centroid) {
            //if point, just encode a single point, otherwise encode the geometry
            // + centroid
            if ( geometry instanceof Point || 
                    (geometry instanceof MultiPoint) && ((MultiPoint)geometry).getNumPoints() == 1 ) {
                encodeGeometry( geometry );
            }
            else {
                start("MultiGeometry");

                //the centroid
                start("Point");

                if (!Double.isNaN(centroid.z)) {
                    element("pos", centroid.x + " " + centroid.y + " " + centroid.z);
                } else {
                    element("pos", centroid.x + " " + centroid.y);
                }

                end("Point");

                //the actual geometry
                encodeGeometry(geometry);

                end("MultiGeometry");
            }
            
        }
        
        
        /**
         * Encodes the provided set of rules as KML styles. Override that handles
         * the style definition and encodes it only if the style module is enabled
         */
        protected boolean encodeStyle(SimpleFeature feature, FeatureTypeStyle[] styles) {
           
            //encode hte Line/Poly styles
            List symbolizerList = new ArrayList();
            for ( int j = 0; j < styles.length ; j++ ) {
               Rule[] rules = filterRules(styles[j], feature);
                
                for (int i = 0; i < rules.length; i++) {
                    symbolizerList.addAll(Arrays.asList(rules[i].getSymbolizers()));
                }
            }
            
            if ( !symbolizerList.isEmpty() ) {
                if(styleModule) {
                    //start the style
                    start("Style",
                        KMLUtils.attributes(new String[] { "id", "GeoServerStyle" + feature.getID() }));
    
                    //encode the icon
                    encodeIconStyle(feature, styles);
    
                    Symbolizer[] symbolizers = (Symbolizer[]) symbolizerList.toArray(new Symbolizer[symbolizerList.size()]);
                    encodeStyle(feature, symbolizers);
                    
                    //end the style
                    end("Style");
                }
                
                // we return true anyways because otherwise the geometry won't be encoded
                return true;
            }
            else {
                //dont encode
                return false;
            }

        }


    }

}
