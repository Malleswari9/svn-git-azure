/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */

package org.sigmah.client.page.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.sigmah.client.EventBus;
import org.sigmah.client.cache.UserLocalCache;
import org.sigmah.client.dispatch.Dispatcher;
import org.sigmah.client.dispatch.monitor.MaskingAsyncMonitor;
import org.sigmah.client.dispatch.remote.Authentication;
import org.sigmah.client.event.NavigationEvent.NavigationError;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.page.NavigationCallback;
import org.sigmah.client.page.Page;
import org.sigmah.client.page.PageId;
import org.sigmah.client.page.PageState;
import org.sigmah.shared.command.GetMonitoredPoints;
import org.sigmah.shared.command.GetReminders;
import org.sigmah.shared.command.result.MonitoredPointsResultList;
import org.sigmah.shared.command.result.RemindersResultList;
import org.sigmah.shared.dto.OrgUnitDTOLight;
import org.sigmah.shared.dto.reminder.MonitoredPointDTO;
import org.sigmah.shared.dto.reminder.ReminderDTO;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;

/**
 * Home screen of sigmah. Displays the main menu and a reminder of urgent tasks.
 * 
 * @author Raphaël Calabro (rcalabro@ideia.fr)
 */
public class DashboardPresenter implements Page {

    public static final PageId PAGE_ID = new PageId("welcome");

    /**
     * Description of the view managed by this presenter.
     */
    @ImplementedBy(DashboardView.class)
    public interface View {

        public ProjectsListPanel getProjectsListPanel();

        public TreeStore<OrgUnitDTOLight> getOrgUnitsStore();

        public TreeGrid<OrgUnitDTOLight> getOrgUnitsTree();

        public ContentPanel getOrgUnitsPanel();

        public ContentPanel getReminderListPanel();

        public ListStore<ReminderDTO> getReminderStore();

        public ContentPanel getMonitoredPointListPanel();

        public ListStore<MonitoredPointDTO> getMonitoredPointStore();
    }

    /**
     * The view.
     */
    private final View view;

    /**
     * The dispatcher.
     */
    private final Dispatcher dispatcher;
    private final UserLocalCache cache;

    @Inject
    public DashboardPresenter(final View view, final UserLocalCache cache, final Authentication authentication, final Dispatcher dispatcher, final EventBus eventBus) {
        this.dispatcher = dispatcher;
        this.view = view;
        this.cache = cache;

    }

    @Override
    public PageId getPageId() {
        return PAGE_ID;
    }

    @Override
    public Object getWidget() {
        return view;
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

        // Gets user's organization.
        cache.getOrganizationCache().get(new AsyncCallback<OrgUnitDTOLight>() {

            @Override
            public void onFailure(Throwable e) {
                // nothing
            }

            @Override
            public void onSuccess(OrgUnitDTOLight result) {

                if (result != null) {
                    view.getOrgUnitsStore().removeAll();
                    view.getOrgUnitsPanel().setHeading(
                        I18N.CONSTANTS.orgunitTree() + " - " + result.getName() + " (" + result.getFullName() + ")");

                    view.getProjectsListPanel().refresh(true, result.getId());
                    view.getOrgUnitsStore().add(result, true);

                    view.getOrgUnitsTree().setExpanded(view.getOrgUnitsStore().getRootItems().get(0), true, false);
                }
            }
        });

        // Reloads the reminders/moniroted points.

        dispatcher.execute(new GetReminders(),
            new MaskingAsyncMonitor(view.getReminderListPanel(), I18N.CONSTANTS.loading()),
            new AsyncCallback<RemindersResultList>() {

                @Override
                public void onFailure(Throwable caught) {
                    Log.error("[navigate] Error while retrieving reminders.", caught);
                }

                @Override
                public void onSuccess(RemindersResultList result) {

                    List<ReminderDTO> reminderListToLoad = new ArrayList<ReminderDTO>();
                    // Only show the undeleted reminders
                    for (ReminderDTO r : result.getList()) {
                        Boolean isDeleted = (Boolean) r.isDeleted();
                        Log.debug("The reminder is deleted ? :" + isDeleted + " Value original: " + r.isDeleted());
                        if (isDeleted == null || isDeleted == Boolean.FALSE) {
                            reminderListToLoad.add(r);
                        }
                    }
                    view.getReminderStore().removeAll();
                    view.getReminderStore().add(reminderListToLoad);
                }
            });

        dispatcher.execute(new GetMonitoredPoints(), new MaskingAsyncMonitor(view.getMonitoredPointListPanel(),
            I18N.CONSTANTS.loading()), new AsyncCallback<MonitoredPointsResultList>() {

            @Override
            public void onFailure(Throwable caught) {
                Log.error("[navigate] Error while retrieving monitored points.", caught);
            }

            @Override
            public void onSuccess(MonitoredPointsResultList result) {

                List<MonitoredPointDTO> pointListToLoad = new ArrayList<MonitoredPointDTO>();
                // Only show the undeleted monitored points
                for (MonitoredPointDTO p : result.getList()) {
                    Boolean isDeleted = (Boolean) p.isDeleted();
                    Log.debug("The reminder is deleted ? :" + isDeleted + " Value original: " + p.isDeleted());
                    if (isDeleted == null || isDeleted == Boolean.FALSE) {
                        pointListToLoad.add(p);
                    }
                }
                view.getMonitoredPointStore().removeAll();
                view.getMonitoredPointStore().add(pointListToLoad);
            }
        });

        return true;
    }

    @Override
    public void shutdown() {
    }
}
