package org.vfny.geoserver.wms.responses.map;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.renderer.lite.StreamingRenderer;

import org.vfny.geoserver.ServiceException;
import org.vfny.geoserver.config.WMSConfig;
import org.vfny.geoserver.global.Service;
import org.vfny.geoserver.wms.GetMapProducer;
import org.vfny.geoserver.wms.WMSMapContext;
import org.vfny.geoserver.wms.WmsException;
import org.w3c.dom.Document;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Renders svg using the Batik SVG Toolkit. An SVG context is created for a 
 * map and then passed of to {@link org.geotools.renderer.lite.StreamingRenderer}.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class SVGBatikMapProducer implements GetMapProducer {

	StreamingRenderer renderer;
	WMSConfig config;
	
	public SVGBatikMapProducer(WMSConfig config) {
		this.config = config;
	}
	
	public void abort() {
		if (renderer != null)
			renderer.stopRendering();
    }

    public void abort(Service gs) {
    	if (renderer != null)
    		renderer.stopRendering();
    }

    public String getContentType() {
        return SvgMapProducerFactory.MIME_TYPE;
    }

    public String getContentEncoding() {
        return null;
    }

    public void produceMap(WMSMapContext map)
        throws WmsException {
    	
    	renderer = new StreamingRenderer();
    	renderer.setContext(map);
    }

    public void writeTo(OutputStream out) throws ServiceException, IOException {
    	try {
    		MapContext map = renderer.getContext();
    		double width = -1;
    		double height = -1;
    		
    		if (map instanceof WMSMapContext) {
    			WMSMapContext wmsMap = (WMSMapContext)map;
    			width = wmsMap.getMapWidth();
    			height = wmsMap.getMapHeight();
    		}
    		else {
    			//guess a width and height based on the envelope
    			Envelope area = map.getAreaOfInterest();
    			if (area.getHeight() > 0 && area.getWidth() > 0) {
    				if (area.getHeight() >= area.getWidth()) {
        				height = 600;
        				width = height*(area.getWidth()/area.getHeight());
        			} 
        			else {
        				width = 800;
        				height = width*(area.getHeight()/area.getWidth());
        			}
    			}
    		}
    	
    		if (height == -1 || width == -1)
    			throw new IOException("Could not determine map dimensions");
    		
			SVGGeneratorContext context = setupContext();
			SVGGraphics2D g = new SVGGraphics2D(context,true);
			
			g.setSVGCanvasSize(new Dimension((int)width,(int)height));
			
			//turn off/on anti aliasing
			if (config.getSvgAntiAlias())
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			else g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
			
			Rectangle r = new Rectangle(g.getSVGCanvasSize());
			
			Envelope e = renderer.getContext().getAreaOfInterest();
			AffineTransform at = RendererUtilities.worldToScreenTransform(e,r);
			
			renderer.paint(g, r, at);
			
			g.stream(new OutputStreamWriter(out,"UTF-8"));
			
		} 
    	catch(Exception e) {
			new IOException().initCause(e);
		}
    	finally {
    		//free up memory
    		renderer = null;
    	}
    }
    
    private SVGGeneratorContext setupContext() throws FactoryConfigurationError, ParserConfigurationException {
        Document document = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory
            .newInstance();
        
        DocumentBuilder db = dbf.newDocumentBuilder();
        
        // Create an instance of org.w3c.dom.Document
        document = db.getDOMImplementation().createDocument(null, "svg", null);
        
        // Set up the context
        return SVGGeneratorContext.createDefault(document);
    }
}
