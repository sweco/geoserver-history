/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.wicket;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * A panel which encapsulates a link containing a image and an optional label.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
@SuppressWarnings("serial")
public abstract class ImageAjaxLink extends Panel {
    protected Image image;
    protected AjaxLink link;

    /**
     * Constructs the panel with a link containing an image.
     */
    public ImageAjaxLink( String id, ResourceReference imageRef ) {
        this( id, imageRef, "" );
    }

    /**
     * Constructs the panel with a link containing an image and a label. 
     */
    public ImageAjaxLink( String id, ResourceReference imageRef, String label ) {
        super( id );
        link = new AjaxLink( "link" ) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                ImageAjaxLink.this.onClick(target);
            }
        };
        add(link);
        link.add(image = new Image( "image", imageRef ) );
        link.add( new Label( "label", label ) );
    }
    
    
    /**
     * Returns the image contained in this link (allows playing with its attributes)
     * @return
     */
    public Image getImage() {
        return image;
    }

    /**
     * Returns the link wrapped by the {@link ImageAjaxLink} panel 
     * (allows playing with its attributes and enable/disable the link)
     * @return
     */
    public AjaxLink getLink() {
        return link;
    }

    /**
     * Handles the onClick() event generated by clicking the link.
     */
    protected abstract void onClick(AjaxRequestTarget target);

}