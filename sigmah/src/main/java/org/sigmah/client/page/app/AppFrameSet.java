/*
 * All Sigmah code is released under the GNU General Public License v3 See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.page.app;

import org.sigmah.client.EventBus;
import org.sigmah.client.dispatch.AsyncMonitor;
import org.sigmah.client.dispatch.remote.Authentication;
import org.sigmah.client.event.NavigationEvent;
import org.sigmah.client.event.NavigationEvent.NavigationError;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.icon.IconImageBundle;
import org.sigmah.client.offline.ui.AppCacheMenu;
import org.sigmah.client.offline.ui.OfflineView;
import org.sigmah.client.page.Frame;
import org.sigmah.client.page.NavigationCallback;
import org.sigmah.client.page.NavigationHandler;
import org.sigmah.client.page.Page;
import org.sigmah.client.page.PageId;
import org.sigmah.client.page.PageState;
import org.sigmah.client.page.charts.ChartPageState;
import org.sigmah.client.page.common.widget.LoadingPlaceHolder;
import org.sigmah.client.page.config.DbListPageState;
import org.sigmah.client.page.entry.SiteGridPageState;
import org.sigmah.client.page.map.MapPageState;
import org.sigmah.client.page.report.ReportListPageState;
import org.sigmah.client.page.table.PivotPageState;
import org.sigmah.client.page.welcome.WelcomePageState;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppFrameSet implements Frame {

    private EventBus eventBus;
    private Viewport viewport;

    private ToolBar topBar;
    private Authentication auth;
    private OfflineView offlineMenu;

    private Widget activeWidget;
    private Page activePage;

    @Inject
    public AppFrameSet(EventBus eventBus, Authentication auth, OfflineView offlineMenu) {

        Log.trace("AppFrameSet constructor starting");

        this.eventBus = eventBus;
        this.auth = auth;
        this.offlineMenu = offlineMenu;

        viewport = new Viewport();
        viewport.setLayout(new RowLayout());

        createToolBar();

        Log.trace("AppFrameSet constructor finished, about to add to RootPanel");

        RootPanel.get().add(viewport);

        Log.trace("AppFrameSet now added to RootPanel");

    }

    private void createToolBar() {

        topBar = new ToolBar();

        AppCacheMenu appCacheMenu = new AppCacheMenu();
        topBar.add(appCacheMenu);

        topBar.add(new SeparatorToolItem());

        addNavLink(I18N.CONSTANTS.welcome(), null, new WelcomePageState());
        addNavLink(I18N.CONSTANTS.dataEntry(), IconImageBundle.ICONS.dataEntry(), new SiteGridPageState());
        addNavLink(I18N.CONSTANTS.reports(), IconImageBundle.ICONS.report(), new ReportListPageState());
        addNavLink(I18N.CONSTANTS.charts(), IconImageBundle.ICONS.barChart(), new ChartPageState());
        addNavLink(I18N.CONSTANTS.maps(), IconImageBundle.ICONS.map(), new MapPageState());
        addNavLink(I18N.CONSTANTS.tables(), IconImageBundle.ICONS.table(), new PivotPageState());
        addNavLink(I18N.CONSTANTS.setup(), IconImageBundle.ICONS.setup(), new DbListPageState());

        topBar.add(new FillToolItem());

        LabelToolItem emailLabel = new LabelToolItem(auth.getEmail());
        emailLabel.setStyleAttribute("fontWeight", "bold");
        topBar.add(emailLabel);

        topBar.add(offlineMenu);

        Button logoutTool = new Button(I18N.CONSTANTS.logout(), new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                // TODO: this needs to go elsewhere
                Cookies.removeCookie("authToken");
                Cookies.removeCookie("email");
                Window.Location.reload();
            }
        });
        topBar.add(logoutTool);

        viewport.add(topBar, new RowData(1.0, 30));
    }

    private void addNavLink(String text, AbstractImagePrototype icon, final PageState place) {
        Button button = new Button(text, icon, new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                eventBus.fireEvent(new NavigationEvent(NavigationHandler.NavigationRequested, place, null));
            }
        });
        topBar.add(button);
    }

    public void setWidget(Widget widget) {

        if (activeWidget != null) {
            viewport.remove(activeWidget);
        }
        viewport.add(widget, new RowData(1.0, 1.0));
        activeWidget = widget;
        viewport.layout();
    }

    @Override
    public void setActivePage(Page page) {
        setWidget((Widget) page.getWidget());
        activePage = page;
    }

    @Override
    public Page getActivePage() {
        return activePage;
    }

    @Override
    public AsyncMonitor showLoadingPlaceHolder(PageId pageId, PageState loadingPlace) {
        activePage = null;
        LoadingPlaceHolder placeHolder = new LoadingPlaceHolder();
        setWidget(placeHolder);
        return placeHolder;
    }

    @Override
    public PageId getPageId() {
        return null;
    }

    @Override
    public Object getWidget() {
        return viewport;
    }

    @Override
    public void requestToNavigateAway(PageState place, final NavigationCallback callback) {
        callback.onDecided(NavigationError.NONE);
    }

    @Override
    public String beforeWindowCloses() {
        return null;
    }

    @Override
    public boolean navigate(PageState place) {
        return true;
    }

    @Override
    public void shutdown() {

    }

}
