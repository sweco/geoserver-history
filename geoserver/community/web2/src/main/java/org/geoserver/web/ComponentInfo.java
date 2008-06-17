package org.geoserver.web;

import java.io.Serializable;

import org.apache.wicket.Component;

/**
 * Information about a component being plugged into a user interface.
 * <p>
 * Subclasses of this class are used to implement user interface "extension points".
 * For an example see {@link MainPageInfo}.
 * </p>
 * 
 * @author Andrea Aime, The Open Planning Project
 * @author Justin Deoliveira, The Open Planning Project
 *
 * @param <C>
 */
public abstract class ComponentInfo<C extends Component> implements Serializable {

    /**
     * the id of the component
     */
    String id;
    /**
     * the title of the component
     */
    String title;
    /**
     * the class of the component
     */
    Class<C> componentClass;
    
    /**
     * The id of the component.
     */
    public String getId() {
        return id;
    }
    /**
     * Sets the id of the component.
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * The i18n key for the title of the component.
     * <p>
     * The exact way this title is used depends one the component. For instance
     * if the component is a page, the title could be the used for a link to the
     * page. If the component is a panel in a tabbed panel, the title might be 
     * the label on the tab.
     * </p>
     */
    public String getTitleKey() {
        return title;
    }
    /**
     * The i18n key for the title of the component.
     */
    public void setTitleKey(String title) {
        this.title = title;
    }
    
    /**
     * The implementation class of the component.
     */
    public Class<C> getComponentClass() {
        return componentClass;
    }
    
    /**
     * Sets the implementation class of the component.
     */
    public void setComponentClass(Class<C> componentClass) {
        this.componentClass = componentClass;
    }
}
