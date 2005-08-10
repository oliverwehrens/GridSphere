/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.layout;

import org.gridlab.gridsphere.portlet.PortletMessage;
import org.gridlab.gridsphere.portlet.PortletRequest;
import org.gridlab.gridsphere.portlet.PortletRole;
import org.gridlab.gridsphere.portlet.impl.SportletProperties;
import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * <code>BasePortletComponent</code> represents an abstract portlet component with a particular
 * size, layout and theme and is subclasses by concrete portlet component instances.
 */
public abstract class BasePortletComponent extends BaseComponentLifecycle implements PortletComponent, Serializable {

    protected PortletComponent parent;
    protected String defaultWidth = "";
    protected String width = "";
    protected String height = "";
    protected String label = "";
    protected String name = "";
    protected String theme = "";
    protected boolean isVisible = true;
    protected String roleString = PortletRole.GUEST.toString();
    protected PortletRole requiredRole = PortletRole.GUEST;
    protected List listeners = null;
    //protected StringBuffer bufferedOutput = new StringBuffer();
    protected boolean canModify = false;
    protected boolean useDiv = false;
    /**
     * css Style of the table
     */
    protected String style = null;


    /**
     * Initializes the portlet component. Since the components are isolated
     * after Castor unmarshalls from XML, the ordering is determined by a
     * passed in List containing the previous portlet components in the tree.
     *
     * @param list a list of component identifiers
     * @return a list of updated component identifiers
     * @see ComponentIdentifier
     */
    public List init(PortletRequest req, List list) {
        listeners = new Vector();
        defaultWidth = width;
        if (roleString != null) {
            try {
                requiredRole = PortletRole.toPortletRole(roleString);
            } catch (IllegalArgumentException e) {
                requiredRole = PortletRole.GUEST;
            }
        }
        if (parent != null) {
        if (parent.getRequiredRole().compare(parent.getRequiredRole(), requiredRole) > 0) {
            requiredRole = parent.getRequiredRole();
        }
        }
        if ((label == null) || label.equals("")) {
            return super.init(req, list);

        } else {
            this.COMPONENT_ID = list.size();
            componentIDStr = label;
            return list;
        }
    }

    /**
     * Returns the portlet component name
     *
     * @return the portlet component name
     */
    public String getName() {
        return name;
    }


    /**
     * Sets the portlet component name
     *
     * @param name the portlet component name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the portlet component label
     *
     * @return the portlet component label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the portlet component label
     *
     * @param label the portlet component label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Allows a required role to be associated with viewing this portlet
     *
     * @param roleString the required portlet role expresses as a <code>String</code>
     */
    public void setRequiredRoleAsString(String roleString) {
        this.roleString = roleString;
    }

    /**
     * Allows a required role to be associated with viewing this portlet
     *
     * @return the required portlet role expresses as a <code>String</code>
     */
    public String getRequiredRoleAsString() {
        return roleString;
    }

    /**
     * Allows a required role to be associated with viewing this portlet
     *
     * @param requiredRole the required portlet role expresses as a <code>String</code>
     */
    public void setRequiredRole(PortletRole requiredRole) {
        this.requiredRole = requiredRole;
    }

    /**
     * Allows a required role to be associated with viewing this portlet
     *
     * @return the required portlet role expresses as a <code>PortletRole</code>
     */
    public PortletRole getRequiredRole() {
        return requiredRole;
    }

    /**
     * Returns the layout mechanism
     * true : use DIVs for the layout
     * false : default : use TABLEs
     *
     * @return the boolean attribute
     */
    public boolean getUseDiv() {
        return useDiv;
    }
    /**
     * Sets the layout mechanism
     * true : use DIVs for the layout
     * false : default : use TABLEs
     *
     * @param useDiv use DIV tags if true
     */
    public void setUseDiv(boolean useDiv) {
        this.useDiv = useDiv;
    }

    /**
     * Returns the CSS style name for the grid-layout.
     *
     * @return css style name
     */
    public String getStyle() {
        return style;
    }

    /**
     * Sets the CSS style name for the grid-layout.
     * This needs to be set if you want to have transparent portlets, if there is
     * no background there can't be a real transparent portlet.
     * Most likely one sets just the background in that one.
     *
     * @param style css style of the that layout
     */
    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * Sets the portlet component width
     *
     * @param width the portlet component width
     */
    public void setWidth(String width) {
        this.width = width;
    }

    /**
     * Returns the portlet component width
     *
     * @return the portlet component width
     */
    public String getWidth() {
        return width;
    }

    /**
     * Sets the portlet component height
     *
     * @param height the portlet component height
     */
    public void setHeight(String height) {
        this.height = height;
    }

    /**
     * Returns the portlet component width
     *
     * @return the portlet component width
     */
    public String getHeight() {
        return width;
    }

    public void setCanModify(boolean canModify) {
        this.canModify = canModify;
    }

    public boolean getCanModify() {
        return canModify;
    }

    /**
     * Returns the default portlet component width
     *
     * @return the default portlet component width
     */
    public String getDefaultWidth() {
        return defaultWidth;
    }

    /**
     * When set to true the portlet component is visible and will be rendered
     *
     * @param isVisible if <code>true</code> portlet component is rendered,
     *                  <code>false</code> otherwise
     */
    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    /**
     * Return true if the portlet component visibility is true
     *
     * @return the portlet component visibility
     */
    public boolean getVisible() {
        return isVisible;
    }

    /**
     * Sets the theme of this portlet component
     *
     * @param theme the theme of this portlet component
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * Return the theme of this portlet component
     *
     * @return the theme of this portlet component
     */
    public String getTheme() {
        return theme;
    }

    public PortletComponent getParentComponent() {
        return parent;
    }

    public void setParentComponent(PortletComponent parent) {
        this.parent = parent;
    }

    public void remove(PortletComponent pc, PortletRequest req) {

    }

    /**
     * Performs an action on this portlet component
     *
     * @param event a gridsphere event
     * @throws PortletLayoutException if a layout error occurs during rendering
     * @throws IOException            if an I/O error occurs during rendering
     */
    public void actionPerformed(GridSphereEvent event) throws PortletLayoutException, IOException {
        super.actionPerformed(event);
    }

    /**
     * Renders the portlet component
     *
     * @param event a gridsphere event
     * @throws PortletLayoutException if a layout error occurs during rendering
     * @throws IOException            if an I/O error occurs during rendering
     */
    public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
        PortletRequest req = event.getPortletRequest();
        req.setAttribute(SportletProperties.COMPONENT_ID, componentIDStr);
        //isVisible = false;
    }

    public void doRenderHTML(GridSphereEvent event) throws PortletLayoutException, IOException {

    }

    public void doRenderWML(GridSphereEvent event) throws PortletLayoutException, IOException {

    }

    public StringBuffer getBufferedOutput(PortletRequest req) {
        StringBuffer sb =  (StringBuffer)req.getAttribute(SportletProperties.RENDER_OUTPUT + componentIDStr);
        req.removeAttribute(SportletProperties.RENDER_OUTPUT + componentIDStr);
        return ((sb != null) ? sb : new StringBuffer());
    }

    public void addComponentListener(PortletComponent component) {
        listeners.add(component);
    }

    public Object clone() throws CloneNotSupportedException {
        BasePortletComponent b = (BasePortletComponent) super.clone();
        b.width = this.width;
        b.isVisible = this.isVisible;
        b.name = this.name;
        b.theme = this.theme;
        b.label = this.label;
        b.roleString = this.roleString;
        b.requiredRole = ((this.requiredRole != null) ? (PortletRole) this.requiredRole.clone() : null);
        return b;
    }


    /* (non-Javadoc)
     * @see org.gridlab.gridsphere.layout.PortletComponent#messageEvent(java.lang.String, org.gridlab.gridsphere.portlet.PortletMessage, org.gridlab.gridsphere.portletcontainer.GridSphereEvent)
     */
    public void messageEvent(String concPortletID, PortletMessage msg, GridSphereEvent event) {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            PortletComponent comp = (PortletComponent) iter.next();
            comp.messageEvent(concPortletID, msg, event);
        }

    }

}
