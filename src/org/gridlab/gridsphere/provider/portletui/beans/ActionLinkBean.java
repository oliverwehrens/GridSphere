/**
 * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.provider.portletui.beans;

public class ActionLinkBean extends ActionBean implements TagBean {

    public static final String ACTION_STYLE = "portlet-frame-label";

    public ActionLinkBean() {
        this.cssStyle = ACTION_STYLE;
    }

    public String toString() {
        //if (value == null) createLink();
        return "<a href=\"" + action + "\"/>" + value + "</a>";
    }

}
