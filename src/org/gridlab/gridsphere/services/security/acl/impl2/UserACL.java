/*
 * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
 * @team sonicteam
 * @version $Id$
 */

package org.gridlab.gridsphere.services.security.acl.impl2;

import org.gridlab.gridsphere.core.persistence.BaseObject;
import org.gridlab.gridsphere.portlet.User;
import org.gridlab.gridsphere.portlet.PortletRole;
import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.portlet.impl.SportletLog;

public class UserACL extends BaseObject  {

    protected transient static PortletLog cat = SportletLog.getInstance(UserACL.class);

    private int RoleID;
    private String UserID;
    private String GroupID;
    private int Status;         // 0 not approved; 1 approved
    public static final int STATUS_NOT_APPROVED = 0;
    public static final int STATUS_APPROVED = 1;

    public UserACL() {
        super();
    };

    public UserACL(String userid, int roleid, String groupid) {
        this.UserID = userid;
        this.RoleID = roleid;
        this.GroupID = groupid;
        this.Status = STATUS_NOT_APPROVED;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public int getRoleID() {
        return RoleID;
    }

    public void setRoleID(int roleID) {
        RoleID = roleID;
    }

    public String getGroupID() {
        return GroupID;
    }

    public void setGroupID(String groupID) {
        GroupID = groupID;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }
}

