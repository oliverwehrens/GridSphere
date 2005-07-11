/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.provider.portletui.beans;

import javax.servlet.http.HttpServletRequest;
import javax.portlet.PortletRequest;

/**
 * A <code>HiddenFieldBean</code> represents a hidden field element
 */
public class HiddenFieldBean extends TextFieldBean {

    public static final String NAME = "hf";

    /**
     * Constructs a default hidden field bean
     */
    public HiddenFieldBean() {
        super(NAME);
    }

    /**
     * Constructs a hidden field bean with the supplied bean identifier
     *
     * @param beanId the bean identifier
     */
    public HiddenFieldBean(String beanId) {
        super(NAME);
        this.beanId = beanId;
    }

    /**
     * Constructs a hidden field bean with the supplied portlet request and bean identifier
     *
     * @param req    the portlet request
     * @param beanId the bean identifier
     */
    public HiddenFieldBean(Object req, String beanId) {
        super(NAME);
        if (req instanceof HttpServletRequest) {
            this.request = (HttpServletRequest)req;
        }
        if (req instanceof PortletRequest) {
            this.portletRequest = (PortletRequest)req;
        }
        this.beanId = beanId;
    }

    public String toStartString() {
        this.inputtype = "hidden";
        return super.toStartString();
    }

}
