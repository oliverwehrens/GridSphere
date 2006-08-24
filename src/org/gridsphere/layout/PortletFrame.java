/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: PortletFrame.java 5032 2006-08-17 18:15:06Z novotny $
 */
package org.gridsphere.layout;

import org.gridsphere.layout.event.PortletComponentEvent;
import org.gridsphere.layout.event.PortletFrameEvent;
import org.gridsphere.layout.event.PortletFrameListener;
import org.gridsphere.layout.event.PortletTitleBarEvent;
import org.gridsphere.layout.event.impl.PortletFrameEventImpl;
import org.gridsphere.layout.view.FrameView;
import org.gridsphere.portlet.*;
import org.gridsphere.portlet.impl.SportletProperties;
import org.gridsphere.portlet.impl.StoredPortletResponseImpl;
import org.gridsphere.portlet.service.PortletServiceException;
import org.gridsphere.portlet.service.spi.PortletServiceFactory;
import org.gridsphere.portletcontainer.*;
import org.gridsphere.portletcontainer.impl.PortletInvoker;
import org.gridsphere.services.core.cache.CacheService;
import org.gridsphere.services.core.messaging.TextMessagingService;
import org.gridsphere.services.core.portal.PortalConfigService;
import org.gridsphere.services.core.security.role.RoleManagerService;
import org.gridsphere.services.core.security.role.PortletRole;
import org.gridsphere.services.core.tracker.TrackerService;
import org.gridsphere.services.core.registry.PortletRegistryService;
import org.gridsphere.tmf.message.MailMessage;

import javax.portlet.RenderResponse;
import javax.servlet.RequestDispatcher;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.security.Principal;
import java.text.DateFormat;
import java.util.*;

/**
 * <code>PortletFrame</code> provides the visual representation of a portlet. A portlet frame
 * contains a portlet title bar unless visible is set to false.
 */
public class PortletFrame extends BasePortletComponent implements Serializable, Cloneable {

    public static final String FRAME_CLOSE_OK_ACTION = "close";

    public static final String FRAME_CLOSE_CANCEL_ACTION = "cancelClose";

    private transient CacheService cacheService = null;

    private transient PortalConfigService portalConfigService = null;
    private transient TrackerService trackerService = null;
    private transient PortletRegistryService portletRegistryService = null;

    private transient PortletInvoker portletInvoker = null;

    // renderPortlet is true in doView and false on minimized
    private boolean renderPortlet = true;
    private String portletClass = null;

    private PortletTitleBar titleBar = null;
    //private PortletErrorFrame errorFrame = new PortletErrorFrame();
    private boolean transparent = false;
    private String innerPadding = "";   // has to be empty and not 0!
    private String outerPadding = "";   // has to be empty and not 0!

    private long cacheExpiration = 0;

    // keep track of the original width
    private String originalWidth = "";

    // switch to determine if the user wishes to close this portlet
    private boolean isClosing = false;

    // render params are the persistent per portlet parameters stored as key names and string[] values
    private Map renderParams = new HashMap();
    private boolean onlyRender = true;

    private transient FrameView frameView = null;

    private String lastFrame = "";


    private String portletName = "Untitled";

    private String windowId = "unknown";

    /**
     * Constructs an instance of PortletFrame
     */
    public PortletFrame() {
    }

    public String getPortletName() {
        return portletName;
    }

    /**
     * Sets the portlet title bar contained by this portlet frame
     *
     * @param titleBar the portlet title bar
     */
    public void setPortletTitleBar(PortletTitleBar titleBar) {
        this.titleBar = titleBar;
    }

    /**
     * Returns the portlet title bar contained by this portlet frame
     *
     * @return the portlet title bar
     */
    public PortletTitleBar getPortletTitleBar() {
        return titleBar;
    }

    /**
     * Sets the portlet class contained by this portlet frame
     *
     * @param portletClass the fully qualified portlet classname
     */
    public void setPortletClass(String portletClass) {
        this.portletClass = portletClass;
    }

    /**
     * Returns the portlet class contained by this portlet frame
     *
     * @return the fully qualified portlet classname
     */
    public String getPortletClass() {
        return portletClass;
    }

    /**
     * Sets the inner padding of the portlet frame
     *
     * @param innerPadding the inner padding
     */
    public void setInnerPadding(String innerPadding) {
        this.innerPadding = innerPadding;
    }

    /**
     * Returns the inner padding of the portlet frame
     *
     * @return the inner padding
     */
    public String getInnerPadding() {
        return innerPadding;
    }

    /**
     * Sets the outer padding of the portlet frame
     *
     * @param outerPadding the outer padding
     */
    public void setOuterPadding(String outerPadding) {
        this.outerPadding = outerPadding;
    }

    /**
     * Returns the outer padding of the portlet frame
     *
     * @return the outer padding
     */
    public String getOuterPadding() {
        return outerPadding;
    }

    /**
     * If set to <code>true</code> the portlet is rendered transparently without a
     * defining border and title bar. This is used for example for the LogoutPortlet
     *
     * @param transparent if set to <code>true</code>, portlet frame is displayed transparently, <code>false</code> otherwise
     */
    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    /**
     * If set to <code>true</code> the portlet is rendered transparently without a
     * defining border and title bar. This is used for example for the LogoutPortlet
     *
     * @return <code>true</code> if the portlet frame is displayed transparently, <code>false</code> otherwise
     */
    public boolean getTransparent() {
        return this.transparent;
    }

    /**
     * Initializes the portlet frame component. Since the components are isolated
     * after Castor unmarshalls from XML, the ordering is determined by a
     * passed in List containing the previous portlet components in the tree.
     *
     * @param list a <code>List</code> of component identifiers
     * @return a <code>List</code> of updated component identifiers
     * @see ComponentIdentifier
     */
    public List init(PortletRequest req, List list) {
        try {
            cacheService = (CacheService)PortletServiceFactory.createPortletService(CacheService.class, true);
            portalConfigService = (PortalConfigService)PortletServiceFactory.createPortletService(PortalConfigService.class, true);
            trackerService = (TrackerService)PortletServiceFactory.createPortletService(TrackerService.class, true);
            portletRegistryService = (PortletRegistryService)PortletServiceFactory.createPortletService(PortletRegistryService.class, true);
        } catch (PortletServiceException e) {
            log.error("Unable to init services! ", e);
        }
        list = super.init(req, list);

        portletInvoker = new PortletInvoker();
        frameView = (FrameView)getRenderClass(req, "Frame");

        ComponentIdentifier compId = new ComponentIdentifier();
        compId.setPortletComponent(this);

        compId.setPortletClass(portletClass);

        compId.setComponentID(list.size());
        compId.setComponentLabel(label);
        compId.setClassName(this.getClass().getName());
        list.add(compId);
        this.originalWidth = width;

        titleBar = new PortletTitleBar();

        // if title bar is not assigned a label and we have one then use it
        if ((!label.equals("")) && (titleBar.getLabel().equals(""))) titleBar.setLabel(label + "TB");
        titleBar.setPortletClass(portletClass);

        titleBar.setCanModify(canModify);

        list = titleBar.init(req, list);
        titleBar.addComponentListener(this);
        titleBar.setParentComponent(this);

        //System.err.println("useDiv= " + useDiv);

        // invalidate cache
        req.setAttribute(CacheService.NO_CACHE, "true");

	    if (windowId.equals("")) windowId = componentIDStr;

        doConfig();
        if (windowId.equalsIgnoreCase("unknown")) windowId = componentIDStr;

        return list;
    }

    protected void doConfig() {

        String appID = portletRegistryService.getApplicationPortletID(portletClass);

        ApplicationPortlet appPortlet = portletRegistryService.getApplicationPortlet(appID);
        if (appPortlet != null) {
            ApplicationPortletConfig appConfig = appPortlet.getApplicationPortletConfig();
            if (appConfig != null) {
                portletName = appConfig.getPortletName();
                cacheExpiration = appConfig.getCacheExpires();
                //System.err.println("Cache for " + portletClass + "expires: " + cacheExpiration);
            }
        }
    }

    public void remove(PortletComponent pc, PortletRequest req) {
        if (parent != null) parent.remove(this, req);
    }

    /**
     * Fires a frame event notification
     *
     * @param event a portlet frame event
     */
    protected void fireFrameEvent(PortletFrameEvent event) {
        Iterator it = listeners.iterator();
        PortletFrameListener l;
        while (it.hasNext()) {
            l = (PortletFrameListener) it.next();
            l.handleFrameEvent(event);
        }
    }

    /**
     * Performs an action on this portlet frame component
     *
     * @param event a gridsphere event
     */
    public void actionPerformed(GridSphereEvent event) {
        super.actionPerformed(event);

        PortletRequest request = event.getPortletRequest();
        String id = request.getPortletSession(true).getId();

        // remove cached output
        cacheService.removeCached(this.getComponentID() + portletClass + id);
        //frame = null;

        PortletComponentEvent titleBarEvent = event.getLastRenderEvent();

        if ((titleBarEvent != null) && (titleBarEvent instanceof PortletTitleBarEvent)) {
            PortletTitleBarEvent tbEvt = (PortletTitleBarEvent) titleBarEvent;
            if (tbEvt.hasWindowStateAction()) {

                PortletWindow.State state = tbEvt.getState();
                PortletFrameEventImpl frameEvent = null;
                if (state == PortletWindow.State.MINIMIZED) {
                    renderPortlet = false;
                    frameEvent = new PortletFrameEventImpl(this, request, PortletFrameEvent.FrameAction.FRAME_MINIMIZED, COMPONENT_ID);
                } else if (state == PortletWindow.State.RESIZING) {
                    renderPortlet = true;
                    frameEvent = new PortletFrameEventImpl(this, request, PortletFrameEvent.FrameAction.FRAME_RESTORED, COMPONENT_ID);
                    frameEvent.setOriginalWidth(originalWidth);
                } else if (state == PortletWindow.State.MAXIMIZED) {
                    renderPortlet = true;
                    frameEvent = new PortletFrameEventImpl(this, request, PortletFrameEvent.FrameAction.FRAME_MAXIMIZED, COMPONENT_ID);
                } else if (state == PortletWindow.State.CLOSED) {
                    renderPortlet = true;
                    isClosing = true;

                    // check for portlet closing action
                    if (event.hasAction()) {
                        if (event.getAction().getName().equals(FRAME_CLOSE_OK_ACTION)) {
                            isClosing = false;
                            frameEvent = new PortletFrameEventImpl(this, request, PortletFrameEvent.FrameAction.FRAME_CLOSED, COMPONENT_ID);
                            request.setAttribute(SportletProperties.INIT_PAGE, "true");
                        }
                        if (event.getAction().getName().equals(FRAME_CLOSE_CANCEL_ACTION)) {
                            isClosing = false;
                        }
                    }
                }


                Iterator it = listeners.iterator();
                PortletComponent comp;
                while (it.hasNext()) {
                    comp = (PortletComponent) it.next();
                    event.addNewRenderEvent(frameEvent);
                    comp.actionPerformed(event);
                }


            }

        } else {
            // now perform actionPerformed on Portlet if it has an action
            titleBar.actionPerformed(event);
            String compVar = this.getComponentIDVar(request);
            request.setAttribute(compVar, componentIDStr);

	        request.setAttribute(SportletProperties.PORTLET_WINDOW_ID, windowId);

            PortletResponse res = event.getPortletResponse();

            request.setAttribute(SportletProperties.PORTLETID, portletClass);

            // Override if user is a guest
            Principal principal = request.getUserPrincipal();
            String userName = "";
            if (principal == null) {
                request.setMode(Mode.VIEW);
                userName = "guest";
            } else {
                Mode mode = titleBar.getPortletMode();
                request.setMode(mode);
                userName = principal.getName();
            }

            titleBar.setPortletMode(request.getMode());

            //System.err.println("in PortletFrame action invoked for " + portletClass);
            if (event.hasAction()
                    && (!event.getAction().getName().equals(FRAME_CLOSE_OK_ACTION))
                    && (!event.getAction().getName().equals(FRAME_CLOSE_CANCEL_ACTION))) {
                DefaultPortletAction action = event.getAction();

                renderParams.clear();
                onlyRender = false;
                String pid = (String)request.getAttribute(SportletProperties.PORTLETID);

                String isCounterEnabled = portalConfigService.getProperty("ENABLE_PORTAL_COUNTER");
                if ((isCounterEnabled != null) && (Boolean.valueOf(isCounterEnabled).booleanValue())) {
                    trackerService.trackURL(portletClass, request.getClient().getUserAgent(), userName);
                }

                try {
                    portletInvoker.actionPerformed(pid, action, request, res);
                } catch (Exception e) {
                    log.error("An error occured performing action on: " + pid, e);
                    // catch it and keep processing
                }

                // see if mode has been set
                String mymodeStr = (String)request.getAttribute(SportletProperties.PORTLET_MODE);
                Mode mymode = Mode.toMode(mymodeStr);
                if (mymode != null) {
                    //System.err.println("setting title mode to " + mymode);
                    titleBar.setPortletMode(mymode);
                }

                // see if state has been set
                PortletFrameEventImpl frameEvent = null;
                PortletWindow.State mystate  = (PortletWindow.State)request.getAttribute(SportletProperties.PORTLET_WINDOW);
                if (mystate != null) {
                    //System.err.println("setting title state to " + mystate);
                    titleBar.setWindowState(mystate);

                    if (mystate == PortletWindow.State.MINIMIZED) {
                        renderPortlet = false;
                    } else if ((mystate == PortletWindow.State.RESIZING) || (mystate == PortletWindow.State.NORMAL)) {
                        renderPortlet = true;
                        frameEvent = new PortletFrameEventImpl(this, request, PortletFrameEvent.FrameAction.FRAME_RESTORED, COMPONENT_ID);
                        frameEvent.setOriginalWidth(originalWidth);
                    } else if (mystate == PortletWindow.State.MAXIMIZED) {
                        renderPortlet = true;
                        frameEvent = new PortletFrameEventImpl(this, request, PortletFrameEvent.FrameAction.FRAME_MAXIMIZED, COMPONENT_ID);
                    }


                    Iterator it = listeners.iterator();
                    PortletComponent comp;
                    while (it.hasNext()) {
                        comp = (PortletComponent) it.next();
                        event.addNewRenderEvent(frameEvent);
                        comp.actionPerformed(event);
                    }

                }

            }

            // see if render params are set from actionResponse
            Map tmpParams = (Map)request.getAttribute(SportletProperties.RENDER_PARAM_PREFIX + portletClass + "_" + componentIDStr);
            if (tmpParams != null) renderParams = tmpParams;

            addRenderParams(request);

            Iterator it = listeners.iterator();
            PortletComponent comp;
            while (it.hasNext()) {
                comp = (PortletComponent) it.next();
                event.addNewRenderEvent(titleBarEvent);
                comp.actionPerformed(event);
            }

        }

    }

    private void addRenderParams(PortletRequest req) {
        // first get rid of existing render params
        Iterator it;
        if (onlyRender) {
            it = renderParams.keySet().iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                if (key.startsWith(SportletProperties.RENDER_PARAM_PREFIX)) {
                    if (req.getParameter(key) == null) {
                        //System.err.println("removing existing render param " + key);
                        it.remove();
                    }
                }
            }
        }
        Map tmpParams = req.getParameterMap();
        if (tmpParams != null) {
            it = tmpParams.keySet().iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                ///String[] paramValues = req.getParameterValues( key );
                if (key.startsWith(SportletProperties.RENDER_PARAM_PREFIX)) {
                    //System.err.println("replacing render param " + key);
                    renderParams.put(key, tmpParams.get(key));
                }
            }
        }
    }



    /**
     * Renders the portlet frame component
     *
     * @param event a gridsphere event
     */
    public void doRender(GridSphereEvent event) {
        super.doRender(event);

        PortletRequest req = event.getPortletRequest();
        PortletResponse res = event.getPortletResponse();

        // check permissions
        User user = req.getUser();
        if (user != null) {
            if (!requiredRoleName.equals("") && (!req.getRoles().contains(requiredRoleName))) return;
        } else {
            if (!requiredRoleName.equals("")) return;
        }

        req.setAttribute(SportletProperties.PORTLET_WINDOW_ID, windowId);
        if (req.getAttribute(SportletProperties.LAYOUT_EDIT_MODE) != null) {
            StringBuffer content = new StringBuffer();
            String extraQuery = (String)req.getAttribute(SportletProperties.EXTRA_QUERY_INFO);
            if (extraQuery != null) {
                PortletURI portletURI = res.createURI();
                String link = portletURI.toString() + extraQuery;
                content.append("<br/><fieldset><a href=\"" + link + "\">" + portletName + "</a></fieldset>");
                setBufferedOutput(req, content);
            }
            return;
        }

        // check for render params
        if (onlyRender)  {
            if ((event.getPortletComponentID().equals(componentIDStr))) {
                addRenderParams(req);
            }
        }
        onlyRender = true;

        String id = event.getPortletRequest().getPortletSession(true).getId();

        StringBuffer frame = (StringBuffer) cacheService.getCached(this.getComponentID() + portletClass + id);
        String nocache = (String) req.getAttribute(CacheService.NO_CACHE);
        if ((frame != null) && (nocache == null)) {
            setBufferedOutput(req, frame);
            return;
        }
        frame = new StringBuffer();

        req.setAttribute(SportletProperties.PORTLETID, portletClass);


        StringBuffer preframe = frameView.doStart(event, this);
        StringBuffer postframe = new StringBuffer();

        // Render title bar
        if (!transparent) {
            titleBar.doRender(event);
        } else {
            req.setMode(titleBar.getPortletMode());
            req.setAttribute(SportletProperties.PREVIOUS_MODE, titleBar.getPreviousMode());
            req.setAttribute(SportletProperties.PORTLET_WINDOW, titleBar.getWindowState());
        }

        if (req.getAttribute(SportletProperties.RESPONSE_COMMITTED) != null) {
            renderPortlet = false;
        }

	    req.setAttribute(SportletProperties.PORTLET_WINDOW_ID, windowId);

        StringWriter storedWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(storedWriter);
        if (renderPortlet) {
            if (!transparent) {
                postframe.append(titleBar.getBufferedOutput(req));
            }

            postframe.append(frameView.doStartBorder(event, this));

            PortletResponse wrappedResponse = new StoredPortletResponseImpl(res, writer);

            if (isClosing) {
                postframe.append(frameView.doRenderCloseFrame(event, this));
            } else {
                //System.err.println("in portlet frame render: class= " + portletClass + " setting prev mode= " + req.getPreviousMode() + " cur mode= " + req.getMode());
                if (hasError(req)) {
                    doRenderError(event.getPortletContext(), req, wrappedResponse);
                    postframe.append(storedWriter.toString());
                } else if ((titleBar != null) && (titleBar.hasRenderError())) {
                    postframe.append(titleBar.getErrorMessage());
                } else {
                    try {
                        if (!renderParams.isEmpty()) {
                            //System.err.println("PortletFrame: in " + portletClass + " sending render params");
                            //System.err.println("in render " + portletClass + " there are render params in the frame setting in request! key= " + SportletProperties.RENDER_PARAM_PREFIX + portletClass + "_" + componentIDStr);
                            req.setAttribute(SportletProperties.RENDER_PARAM_PREFIX + portletClass + "_" + componentIDStr, renderParams);
                        }
                        portletInvoker.service((String)req.getAttribute(SportletProperties.PORTLETID), req, wrappedResponse);
                        lastFrame = storedWriter.toString();
                        postframe.append(lastFrame);
                    } catch (Exception e) {
                        doRenderError(event.getPortletContext(), req, wrappedResponse);
                        postframe.append(storedWriter.toString());
                    }
                }
            }
            postframe.append(frameView.doEndBorder(event, this));
        } else {
            postframe.append(frameView.doRenderMinimizeFrame(event, this));
        }
        postframe.append(frameView.doEnd(event, this));

        if (req.getAttribute(SportletProperties.RESPONSE_COMMITTED) != null) {
            renderPortlet = true;
        }

        // piece together portlet frame + title depending on whether title was set during doXXX method
        // or not
        frame.append(preframe);
        if (!transparent) {
            String titleStr = (String) req.getAttribute(SportletProperties.PORTLET_TITLE);
            if (titleStr == null) {
                titleStr = titleBar.getTitle();
            }
            frame.append(titleBar.getPreBufferedTitle(req));
            frame.append(titleStr);
            frame.append(titleBar.getPostBufferedTitle(req));
        }
        req.removeAttribute(SportletProperties.PORTLET_TITLE);

        frame.append(postframe);

        setBufferedOutput(req, frame);

        // check if expiration was set in render response
        Map props = (Map)req.getAttribute(SportletProperties.PORTAL_PROPERTIES);
        if (props != null) {
            List vals = (List)props.get(RenderResponse.EXPIRATION_CACHE);
            if (vals != null) {
                String cacheExpiryStr = (String)vals.get(0);
                if (cacheExpiryStr != null) {
                    try {
                        cacheExpiration = Integer.valueOf(cacheExpiryStr).intValue();
                    } catch (IllegalArgumentException e) {
                        // do nothing
                    }
                }
            }
        }

        if (nocache == null) {
            if ((cacheExpiration > 0) || (cacheExpiration == -1)) {
                cacheService.cache(this.getComponentID() + portletClass + id, frame, cacheExpiration);
            }
        }
    }

    public boolean isTargetedPortlet(PortletRequest req) {
        String compVar = this.getComponentIDVar(req);
        return (req.getParameter(compVar).equals(req.getAttribute(compVar)));
    }

    public boolean hasError(PortletRequest req) {
        return (req.getAttribute(SportletProperties.PORTLETERROR + portletClass) != null);
    }

    protected String getLocalizedText(PortletRequest req, String key) {
        Locale locale = req.getLocale();
        ResourceBundle bundle = ResourceBundle.getBundle("gridsphere.resources.Portlet", locale);
        return bundle.getString(key);
    }

    public void doRenderError(PortletContext ctx, PortletRequest req, PortletResponse res) {
        Throwable ex = (Throwable)req.getAttribute(SportletProperties.PORTLETERROR + portletClass);
        if (ex == null) return;
        Throwable cause = ex.getCause();
        if (cause == null) {
            cause = ex;
        }
        try {
            TextMessagingService tms = (TextMessagingService)PortletServiceFactory.createPortletService(TextMessagingService.class, true);
            RoleManagerService roleManagerService = (RoleManagerService)PortletServiceFactory.createPortletService(RoleManagerService.class, true);
            Boolean sendMail = Boolean.valueOf(portalConfigService.getProperty("ENABLE_ERROR_HANDLING"));
            if (sendMail.booleanValue()) {
                MailMessage mailToUser = tms.getMailMessage();
                List superUsers = roleManagerService.getUsersInRole(PortletRole.ADMIN);
                User superUser = (User)superUsers.get(0);
                mailToUser.setTo(superUser.getEmailAddress());
                mailToUser.setSubject(getLocalizedText(req, "PORTAL_ERROR_SUBJECT"));
                StringBuffer body = new StringBuffer();
                body.append(getLocalizedText(req, "PORTAL_ERROR_BODY"));
                body.append("\n\n");
                body.append("portlet title: ");
                body.append(titleBar.getTitle());
                body.append("\n\n");
                User user = req.getUser();
                body.append(DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
                body.append("\n\n");
                if (user != null) {
                    body.append(user);
                    body.append("\n\n");
                }
                StringWriter sw = new StringWriter();
                PrintWriter pout = new PrintWriter(sw);
                cause.printStackTrace(pout);
                body.append(sw.getBuffer());
                mailToUser.setBody(body.toString());
                mailToUser.setServiceid("mail");
                try {
                    tms.send(mailToUser);
                    req.setAttribute("lastFrame", lastFrame);
                    RequestDispatcher dispatcher = ctx.getRequestDispatcher("/jsp/errors/custom_error.jsp");
                    dispatcher.include(req, res);
                    return;
                } catch (Exception e) {
                    log.error("Unable to send mail message!", e);
                }
            }
        } catch (PortletServiceException e) {
            log.error("Unable to get instance of needed portlet services", e);
        }
        try {
            req.setAttribute("error", cause);
            RequestDispatcher dispatcher = ctx.getRequestDispatcher("/jsp/errors/custom_error.jsp");
            dispatcher.include(req, res);
        } catch (Exception e) {
            System.err.println("Unable to include custom error page!!");
            e.printStackTrace();
        }
    }

    public Object clone() throws CloneNotSupportedException {
        PortletFrame f = (PortletFrame) super.clone();
        f.titleBar = (this.titleBar == null) ? null : (PortletTitleBar) this.titleBar.clone();
        f.outerPadding = this.outerPadding;
        f.transparent = this.transparent;
        f.innerPadding = this.innerPadding;
        f.portletClass = this.portletClass;
        f.renderPortlet = this.renderPortlet;
        return f;
    }


    /* (non-Javadoc)
    * @see org.gridsphere.layout.PortletComponent#messageEvent(java.lang.String, org.gridsphere.portlet.PortletMessage, org.gridsphere.portletcontainer.GridSphereEvent)
    */
    public void messageEvent(String concPortletID, PortletMessage msg, GridSphereEvent event) {

        if (portletClass.equals(concPortletID)) {
            PortletRequest req = event.getPortletRequest();


            PortletResponse res = event.getPortletResponse();


            req.setAttribute(SportletProperties.PORTLETID, portletClass);


            // Override if user is a guest
            Principal principal = req.getUserPrincipal();
            if (principal == null) {
                req.setMode(Mode.VIEW);
            } else {
                if (titleBar != null) {
                    Mode mode = titleBar.getPortletMode();
                    //System.err.println("setting mode in " + portletClass + " to " + mode.toString());
                    req.setMode(mode);
                } else {
                    req.setMode(Mode.VIEW);
                }
            }

            try {
                portletInvoker.messageEvent(portletClass, msg, req, res);
            } catch (Exception ioex) {
                // do nothing the render will take care of displaying the error
            }

        } else {
            super.messageEvent(concPortletID, msg, event);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append("\nportlet class=").append(portletClass);
        return sb.toString();
    }
}