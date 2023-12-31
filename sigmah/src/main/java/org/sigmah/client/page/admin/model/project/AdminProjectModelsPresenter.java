package org.sigmah.client.page.admin.model.project;

import org.sigmah.client.EventBus;
import org.sigmah.client.cache.UserLocalCache;
import org.sigmah.client.dispatch.Dispatcher;
import org.sigmah.client.dispatch.monitor.MaskingAsyncMonitor;
import org.sigmah.client.dispatch.remote.Authentication;
import org.sigmah.client.page.admin.AdminPageState;
import org.sigmah.client.page.admin.AdminSubPresenter;
import org.sigmah.client.page.admin.AdminUtil;
import org.sigmah.shared.command.GetProjectModels;
import org.sigmah.shared.command.result.ProjectModelListResult;
import org.sigmah.shared.dto.ProjectModelDTOLight;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;

public class AdminProjectModelsPresenter implements AdminSubPresenter {

    private final View view;
    private static boolean alert = false;
    private final Dispatcher dispatcher;
    private final UserLocalCache cache;
    private Authentication authentication;
    private AdminPageState currentState;
    private int modelId = -1;

    @ImplementedBy(AdminProjectModelsView.class)
    public static abstract class View extends ContentPanel {

        public abstract AdminModelsStore getAdminModelsStore();

        public abstract MaskingAsyncMonitor getProjectModelsLoadingMonitor();

        public abstract Component getMainPanel(int id);

        public abstract void setCurrentState(AdminPageState currentState);

        public abstract Grid<ProjectModelDTOLight> getProjectModelGrid();
    }

    public static class AdminModelsStore extends ListStore<ProjectModelDTOLight> {
    }

    @Inject
    public AdminProjectModelsPresenter(Dispatcher dispatcher, UserLocalCache cache, final Authentication authentication, EventBus eventBus, final AdminPageState currentState) {
        this.currentState = currentState;
        this.cache = cache;
        this.dispatcher = dispatcher;
        this.authentication = authentication;
        this.view = new AdminProjectModelsView(dispatcher, cache, eventBus);
    }

    public static void refreshProjectModelsPanel(Dispatcher dispatcher, final View view) {

        GetProjectModels cmdGetProjectModels = new GetProjectModels();
        cmdGetProjectModels.allProjectModelStatus();

        dispatcher.execute(cmdGetProjectModels, view.getProjectModelsLoadingMonitor(),
            new AsyncCallback<ProjectModelListResult>() {

                @Override
                public void onFailure(Throwable arg0) {
                    AdminUtil.alertPbmData(alert);
                }

                @Override
                public void onSuccess(ProjectModelListResult result) {
                    if (result.getList() != null && !result.getList().isEmpty()) {
                        view.getAdminModelsStore().removeAll();
                        view.getAdminModelsStore().add(result.getList());
                        view.getAdminModelsStore().commitChanges();
                    }

                }
            });
    }

    @Override
    public Component getView() {
        refreshProjectModelsPanel(dispatcher, view);
        if (currentState != null) {
            final Integer mod = currentState.getModel();
            if (mod != null) {
                modelId = mod;
            }
        }

        return view.getMainPanel(modelId);
    }

    @Override
    public void setCurrentState(AdminPageState currentState) {
        this.currentState = currentState;
        view.setCurrentState(currentState);
    }

    @Override
    public void discardView() {
    }

    @Override
    public void viewDidAppear() {
    }

    @Override
    public boolean hasValueChanged() {
        return false;
    }

    @Override
    public void forgetAllChangedValues() {
    }

}
