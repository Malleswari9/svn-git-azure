package org.sigmah.client.page.admin.model.orgunit;

import java.util.ArrayList;
import java.util.List;

import org.sigmah.client.EventBus;
import org.sigmah.client.cache.UserLocalCache;
import org.sigmah.client.dispatch.Dispatcher;
import org.sigmah.client.dispatch.monitor.MaskingAsyncMonitor;
import org.sigmah.client.event.NavigationEvent;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.icon.IconImageBundle;
import org.sigmah.client.page.NavigationHandler;
import org.sigmah.client.page.admin.AdminPageState;
import org.sigmah.client.page.admin.model.common.AdminModelActionListener;
import org.sigmah.client.page.admin.model.orgunit.AdminOrgUnitModelsPresenter.AdminModelsStore;
import org.sigmah.client.page.admin.model.orgunit.AdminOrgUnitModelsPresenter.View;
import org.sigmah.client.page.common.toolbar.UIActions;
import org.sigmah.shared.domain.ProjectModelStatus;
import org.sigmah.shared.dto.OrgUnitModelDTO;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.inject.Inject;

public class AdminOrgUnitModelsView extends View {

    private final ContentPanel mainPanel;
    private final Grid<OrgUnitModelDTO> grid;
    private final AdminModelsStore modelsStore;
    private final EventBus eventBus;
    private AdminPageState currentState;
    private final Dispatcher dispatcher;

    @Inject
    public AdminOrgUnitModelsView(Dispatcher dispatcher, UserLocalCache cache, EventBus eventBus) {
        this.dispatcher = dispatcher;
        // this.cache = cache;
        this.eventBus = eventBus;

        mainPanel = new ContentPanel(new FitLayout());

        mainPanel.setHeaderVisible(false);
        mainPanel.setBorders(false);
        mainPanel.setBodyBorder(false);

        modelsStore = new AdminModelsStore();
        grid = buildModelsListGrid();

        grid.setAutoHeight(true);
        grid.getView().setForceFit(true);

        mainPanel.setTopComponent(initToolBar());
        mainPanel.setScrollMode(Style.Scroll.AUTO);

        mainPanel.add(grid);
    }

    private Grid<OrgUnitModelDTO> buildModelsListGrid() {

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig("name", I18N.CONSTANTS.adminOrgUnitsModelName(), 200);
        column.setRenderer(new GridCellRenderer<OrgUnitModelDTO>() {

            @Override
            public Object render(final OrgUnitModelDTO model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<OrgUnitModelDTO> store, Grid<OrgUnitModelDTO> grid) {

                // Name hyperlink
                final Anchor nameHyperlink;
                nameHyperlink = new Anchor(model.getName(), true);
                nameHyperlink.setStyleName("orgunitModel-grid-nameLink");
                nameHyperlink.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {

                        final AdminPageState derivation =
                                new AdminPageState(AdminOrgUnitModelsView.this.currentState.getCurrentSection());
                        derivation.setModel(model.getId());
                        // FIXME
                        derivation.setSubModel(I18N.CONSTANTS.adminProjectModelFields());
                        derivation.setIsProject(false);
                        AdminOrgUnitModelsView.this.eventBus.fireEvent(new NavigationEvent(
                            NavigationHandler.NavigationRequested, derivation, null));
                    }

                });

                return nameHyperlink;
            }

        });
        configs.add(column);

        column = new ColumnConfig("title", I18N.CONSTANTS.adminOrgUnitsModelTitle(), 200);
        configs.add(column);

        column = new ColumnConfig("hasBudget", I18N.CONSTANTS.adminOrgUnitsModelHasBudget(), 75);
        configs.add(column);

        column = new ColumnConfig("canContainProjects", I18N.CONSTANTS.adminOrgUnitsModelContainProjects(), 75);
        configs.add(column);

        column = new ColumnConfig("status", I18N.CONSTANTS.adminProjectModelsStatus(), 200);
        column.setRenderer(new GridCellRenderer<OrgUnitModelDTO>() {

            @Override
            public Object render(OrgUnitModelDTO model, String property, ColumnData config, int rowIndex, int colIndex,
                    ListStore<OrgUnitModelDTO> store, Grid<OrgUnitModelDTO> grid) {
                return model.getStatus() != null ? ProjectModelStatus.getName(model.getStatus()) : "";
            }
        });
        configs.add(column);

        column = new ColumnConfig();
        column.setWidth(75);
        column.setAlignment(Style.HorizontalAlignment.RIGHT);
        column.setRenderer(new GridCellRenderer<OrgUnitModelDTO>() {

            @Override
            public Object render(final OrgUnitModelDTO model, final String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<OrgUnitModelDTO> store, Grid<OrgUnitModelDTO> grid) {

                Button button = new Button(I18N.CONSTANTS.delete());
                button.setItemId(UIActions.deleteModel);
                button.disable();
                button.addListener(Events.OnClick, new Listener<ButtonEvent>() {

                    @Override
                    public void handleEvent(ButtonEvent be) {

                        // Do deletion
                        AdminModelActionListener listener =
                                new AdminModelActionListener(AdminOrgUnitModelsView.this, dispatcher, false);
                        listener.setModelId(model.getId());
                        listener.setIsOrgUnit(true);
                        listener.setOrgUnit(model);
                        listener.onUIAction(UIActions.deleteModel);

                    }
                });

                // Only the OrgUnit model with status "Draft" can be deleted.
                if (!model.isTopOrgUnitModel() && model.getStatus().equals(ProjectModelStatus.DRAFT))
                    button.enable();

                return button;
            }
        });
        configs.add(column);

        column = new ColumnConfig();
        column.setWidth(75);
        column.setAlignment(Style.HorizontalAlignment.RIGHT);
        column.setRenderer(new GridCellRenderer<OrgUnitModelDTO>() {

            @Override
            public Object render(final OrgUnitModelDTO model, final String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<OrgUnitModelDTO> store, Grid<OrgUnitModelDTO> grid) {

                Button buttonExport = new Button(I18N.CONSTANTS.export());
                buttonExport.setItemId(UIActions.exportModel);
                buttonExport.addListener(Events.OnClick, new Listener<ButtonEvent>() {

                    @Override
                    public void handleEvent(ButtonEvent be) {
                        AdminModelActionListener listener =
                                new AdminModelActionListener(AdminOrgUnitModelsView.this, dispatcher, false);
                        listener.setModelId(model.getId());
                        listener.setIsOrgUnit(true);
                        listener.setIsReport(false);
                        listener.onUIAction(UIActions.exportModel);
                    }
                });
                return buttonExport;
            }
        });
        configs.add(column);

        column = new ColumnConfig();
        column.setWidth(60);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        column.setRenderer(new GridCellRenderer<OrgUnitModelDTO>() {

            @Override
            public Object render(final OrgUnitModelDTO model, final String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<OrgUnitModelDTO> store, Grid<OrgUnitModelDTO> grid) {

                Button buttonCopy = new Button(I18N.CONSTANTS.adminModelCopy());
                buttonCopy.setItemId(UIActions.copyModel);
                buttonCopy.addListener(Events.OnClick, new Listener<ButtonEvent>() {

                    @Override
                    public void handleEvent(ButtonEvent be) {
                        AdminModelActionListener listener =
                                new AdminModelActionListener(AdminOrgUnitModelsView.this, dispatcher, false);
                        listener.setModelId(model.getId());
                        listener.setIsOrgUnit(true);
                        listener.setOrgUnit(model);
                        listener.onUIAction(UIActions.copyModel);
                    }
                });
                return buttonCopy;
            }
        });
        configs.add(column);

        ColumnModel cm = new ColumnModel(configs);

        Grid<OrgUnitModelDTO> grid = new Grid<OrgUnitModelDTO>(modelsStore, cm);

        return grid;
    }

    private ToolBar initToolBar() {

        ToolBar toolbar = new ToolBar();

        Button button = new Button(I18N.CONSTANTS.addItem(), IconImageBundle.ICONS.add());
        button.setItemId(UIActions.add);
        button.addListener(Events.OnClick, new Listener<ButtonEvent>() {

            @Override
            public void handleEvent(ButtonEvent be) {
                AdminModelActionListener listener =
                        new AdminModelActionListener(AdminOrgUnitModelsView.this, dispatcher, false);
                listener.onUIAction(UIActions.add);
            }

        });

        toolbar.add(button);

        Button buttonImport = new Button(I18N.CONSTANTS.importItem());
        buttonImport.setItemId(UIActions.importModel);
        buttonImport.addListener(Events.Select, new Listener<ButtonEvent>() {

            @Override
            public void handleEvent(ButtonEvent be) {
                AdminModelActionListener listener =
                        new AdminModelActionListener(AdminOrgUnitModelsView.this, dispatcher, false);
                listener.setIsOrgUnit(true);
                listener.setIsReport(false);
                listener.onUIAction(UIActions.importModel);
            }

        });
        toolbar.add(buttonImport);

        return toolbar;
    }

    @Override
    public MaskingAsyncMonitor getOrgUnitModelsLoadingMonitor() {
        return new MaskingAsyncMonitor(grid, I18N.CONSTANTS.loading());
    }

    @Override
    public AdminModelsStore getAdminModelsStore() {
        return modelsStore;
    }

    @Override
    public Component getMainPanel(int id) {
        return mainPanel;
    }

    @Override
    public void setCurrentState(AdminPageState currentState) {
        this.currentState = currentState;
    }

    @Override
    public Grid<OrgUnitModelDTO> getOrgUnitModelGrid() {
        return this.grid;
    }
}
