package org.geoserver.web.importer;

import static org.geoserver.web.importer.ImportSummaryProvider.*;

import java.io.IOException;
import java.util.logging.Level;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.importer.ImportStatus;
import org.geoserver.importer.ImportSummary;
import org.geoserver.importer.LayerSummary;
import org.geoserver.web.CatalogIconFactory;
import org.geoserver.web.GeoServerSecuredPage;
import org.geoserver.web.data.resource.ResourceConfigurationPage;
import org.geoserver.web.demo.PreviewLayer;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.ParamResourceModel;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;
import org.opengis.feature.type.GeometryDescriptor;

@SuppressWarnings("serial")
public class ImportSummaryPage extends GeoServerSecuredPage {

    public ImportSummaryPage(ImportSummary summary) {
        // the synthetic results
        if(summary.getFailures() > 0) {
            add(new Label("summary", new ParamResourceModel("summaryFailures", this, summary.getTotalLayers(), summary.getFailures())));
        } else {
            add(new Label("summary", new ParamResourceModel("summarySuccess", this, summary.getTotalLayers())));
        }

        GeoServerTablePanel<LayerSummary> table = new GeoServerTablePanel<LayerSummary>("importSummary", new ImportSummaryProvider(
                summary.getLayers())) {

            @Override
            protected Component getComponentForProperty(String id, IModel itemModel,
                    Property<LayerSummary> property) {
                final LayerSummary layerSummary = (LayerSummary) itemModel.getObject();
                final CatalogIconFactory icons = CatalogIconFactory.get();
                LayerInfo layer = layerSummary.getLayer();
				if(property == LAYER) {
                    Fragment f = new Fragment(id, "edit", ImportSummaryPage.this);
                    
                    Link editLink = editLink(layerSummary);
                    editLink.setEnabled(layer != null);
                    f.add(editLink);
                    
                    return f;
                } else if(property == STATUS) {
                    ResourceReference icon = layerSummary.getStatus().successful() ? 
                            icons.getEnabledIcon() : icons.getDisabledIcon();
                    Fragment f = new Fragment(id, "iconFragment", ImportSummaryPage.this);
                    f.add(new Image("icon", icon));
                    return f;
                } else if(property == TYPE) {
                    if(layer != null) {
                        ResourceReference icon = icons.getSpecificLayerIcon(layer);
                        Fragment f = new Fragment(id, "iconFragment", ImportSummaryPage.this);
                        Image image = new Image("icon", icon);
                        image.add(new AttributeModifier("title", true, new Model(getTypeTooltip(layer))));
						f.add(image);
                        return f;
                    } else {
                        return new Label(id, "");
                    }
                } else if(property == COMMANDS) {
                    Fragment f = new Fragment(id, "preview", ImportSummaryPage.this);

                    ExternalLink link = new ExternalLink("preview", "#");
                    if(layerSummary.getStatus().successful()) {
                        // TODO: move the preview link generation ability to some utility object
                        PreviewLayer preview = new PreviewLayer(layer);
                        String url = "window.open(\"" + preview.getWmsLink() + "&format=application/openlayers\")";
                        link.add(new AttributeAppender("onclick", new Model(url), ";"));
                    } else {
                        link.setEnabled(false);
                    }
                    f.add(link);
                    
                    return f;
                }
                return null;
            }

            
        };
        table.setOutputMarkupId(true);
        table.setFilterable(false);
        add(table);
    }
    
    Link editLink(final LayerSummary layerSummary) {
        Link link = new Link("edit") {

            @Override
            public void onClick() {
                Page p = new ResourceConfigurationPage(layerSummary.getLayer(), true) {
                    @Override
                    protected void onSuccessfulSave() {
                        setResponsePage(ImportSummaryPage.this);
                        layerSummary.setStatus(ImportStatus.SUCCESS);
                    }
                    
                    @Override
                    protected void onCancel() {
                        setResponsePage(ImportSummaryPage.this);
                    }
                };
                setResponsePage(p);
            }
            
        };
        // keep the last modified name if possible
        if(layerSummary.getLayer() != null)
            link.add(new Label("name", layerSummary.getLayer().getName()));
        else
            link.add(new Label("name", layerSummary.getLayerName()));
        
        return link;
    }
    
    String getTypeTooltip(LayerInfo layer) {
    	try {
	    	String type = null;
	    	FeatureTypeInfo fti = (FeatureTypeInfo) layer.getResource();
	        GeometryDescriptor gd = fti.getFeatureType().getGeometryDescriptor();
	        if(gd != null) {
	            type = gd.getType().getBinding().getSimpleName();
	        }
	        if(type != null)
	        	return new ParamResourceModel("geomtype." + type, ImportSummaryPage.this).getString();
	        else
	        	return "geomtype.null";
    	} catch(Exception e) {
    		LOGGER.log(Level.WARNING, "Could not compute the geom type tooltip", e);
    		return "geomtype.error";
    	}
    }

}
