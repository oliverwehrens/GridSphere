/**
 * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.provider.portletui.beans;

import org.gridlab.gridsphere.portlet.PortletURI;
import org.gridlab.gridsphere.portlet.DefaultPortletAction;
import org.gridlab.gridsphere.portlet.PortletRequest;
import org.gridlab.gridsphere.provider.ui.beans.TextBean;
import org.gridlab.gridsphere.provider.ui.beans.Link;
import org.gridlab.gridsphere.provider.ui.beans.ParamBean;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class ActionBean extends BaseComponentBean implements TagBean {

    protected String action = "no action specified";
    protected PortletURI portletURI = null;
    protected List paramBeanList = new ArrayList();

    public ActionBean() {
    }

    public ActionBean(String name) {
        super(name);
    }

    public ActionBean(PortletRequest req, String beanId) {
        this.request = req;
        this.beanId = beanId;
    }

    /**
     * Sets the uri for the link.
     */
    public void setPortletURI(PortletURI portletURI) {
        this.portletURI = portletURI;
    }

    /**
     * Gets the uri for the link.
     * @return returns the action
     */
    public PortletURI getPortletURI() {
        return portletURI;
    }

    /**
     * Sets the action for the link.
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Gets the action for the link.
     * @return returns the action
     */
    public String getAction() {
        return action;
    }

    public void setParamBeanList(List paramBeanList) {
        this.paramBeanList = paramBeanList;
    }

    public List getParamBeanList() {
        return paramBeanList;
    }

    public void addParamBean(ParamBean paramBean) {
        paramBeanList.add(paramBean);
    }

    public void addParamBean(String paramName, String paramValue) {
        ParamBean paramBean = new ParamBean(paramName, paramValue);
        paramBeanList.add(paramBean);
    }

    public void removeParamBean(ParamBean paramBean) {
        paramBeanList.remove(paramBean);
    }

    protected void createLink() {
        DefaultPortletAction portletAction = new DefaultPortletAction(action);
        Iterator it = paramBeanList.iterator();
        ParamBean paramBean = null;
        while (it.hasNext()) {
            paramBean = (ParamBean)it.next();
            portletAction.addParameter(paramBean.getName(), paramBean.getValue());
        }
        portletURI.addAction(portletAction);
        action = portletURI.toString();
    }

    protected String createSubmitName() {
        Iterator it = paramBeanList.iterator();
        ParamBean paramBean = null;
        StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            paramBean = (ParamBean)it.next();
            sb.append("pname="+paramBean.getName()+"pvalue="+paramBean.getValue());
        }
        return sb.toString();
    }

}
