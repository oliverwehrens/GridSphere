package org.gridlab.gridsphere.provider.portletui.beans;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.portlet.PortletRequest;

/*
 * @author <a href="mailto:russell@aei.mpg.de">Michael Russell</a>
 * @version $Id$
 * <p>
 * Includes jsp pages from any web application.
 */

public class IncludeBean extends BaseBean implements TagBean {

    protected ServletContext servletContext = null;
    protected JspWriter jspWriter = null;
    protected String page = null;

    /**
     * Constructs default include bean
     */
    public IncludeBean() {
        super();
    }

    /**
     * Constructs an include bean
     */
    public IncludeBean(String beanId) {
        super(beanId);
    }

    /**
     * Constructs an include bean
     */
    public IncludeBean(Object req, String beanId) {
        if (req instanceof HttpServletRequest) {
            this.request = (HttpServletRequest)req;
        }
        if (req instanceof PortletRequest) {
            this.portletRequest = (PortletRequest)req;
        }
        this.beanId = beanId;
    }


    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public JspWriter getJspWriter() {
        return jspWriter;
    }

    public void setJspWriter(JspWriter jspWriter) {
        this.jspWriter = jspWriter;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String toStartString() {
        return "";
    }

    public String toEndString() {
        return "";
    }

    public void store(HttpServletRequest request) {
        this.request = request;
        super.store();
    }

}
