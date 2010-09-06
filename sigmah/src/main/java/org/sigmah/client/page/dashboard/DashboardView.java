/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sigmah.client.page.dashboard;

import java.util.Arrays;
import java.util.HashMap;

import org.sigmah.client.EventBus;
import org.sigmah.client.dispatch.Dispatcher;
import org.sigmah.client.event.NavigationEvent;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.icon.IconImageBundle;
import org.sigmah.client.page.NavigationHandler;
import org.sigmah.client.page.PageState;
import org.sigmah.client.page.charts.ChartPageState;
import org.sigmah.client.page.common.toolbar.ActionListener;
import org.sigmah.client.page.common.toolbar.ActionToolBar;
import org.sigmah.client.page.common.toolbar.UIActions;
import org.sigmah.client.page.config.DbListPageState;
import org.sigmah.client.page.entry.SiteGridPageState;
import org.sigmah.client.page.map.MapPageState;
import org.sigmah.client.page.project.ProjectPresenter;
import org.sigmah.client.page.report.ReportListPageState;
import org.sigmah.client.page.table.PivotPageState;
import org.sigmah.shared.command.CreateProject;
import org.sigmah.shared.command.GetCountries;
import org.sigmah.shared.command.GetProjectModels;
import org.sigmah.shared.command.GetProjects;
import org.sigmah.shared.command.result.CountryResult;
import org.sigmah.shared.command.result.ProjectListResult;
import org.sigmah.shared.command.result.ProjectModelListResult;
import org.sigmah.shared.command.result.VoidResult;
import org.sigmah.shared.dto.CountryDTO;
import org.sigmah.shared.dto.ProjectDTO;
import org.sigmah.shared.dto.ProjectModelDTO;
import org.sigmah.shared.dto.ProjectModelDTOLight;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.WidgetTreeGridCellRenderer;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Displays the dashboard.
 * 
 * @author Raphaël Calabro (rcalabro@ideia.fr)
 */
public class DashboardView extends ContentPanel {
    public DashboardView(final EventBus eventBus, final Dispatcher dispatcher, final TreeStore<ProjectDTO> projectStore, final ListStore<CountryDTO> countryStore) {
        // The dashboard itself
        setLayout(new BorderLayout());
        setHeaderVisible(false);
        setBorders(false);
        
        // Left bar
        final ContentPanel leftPanel = new ContentPanel();
        final VBoxLayout leftPanelLayout = new VBoxLayout();
        leftPanelLayout.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        leftPanelLayout.setPadding(new Padding(0));
        leftPanel.setLayout(leftPanelLayout);
        leftPanel.setHeaderVisible(false);
        leftPanel.setBorders(false);
        leftPanel.setBodyBorder(false);
        
            // Left bar content
            final VBoxLayoutData vBoxLayoutData = new VBoxLayoutData();
            vBoxLayoutData.setFlex(1.0);
        
            final ContentPanel remindersPanel = new ContentPanel(new FitLayout());
            remindersPanel.setHeading(I18N.CONSTANTS.reminders());
            leftPanel.add(remindersPanel, vBoxLayoutData);
            
            final ContentPanel importantPointsPanel = new ContentPanel(new FitLayout());
            importantPointsPanel.setHeading(I18N.CONSTANTS.importantPoints());
            leftPanel.add(importantPointsPanel, vBoxLayoutData);
            
            final ContentPanel menuPanel = new ContentPanel();
            final VBoxLayout menuPanelLayout = new VBoxLayout();
            menuPanelLayout.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
            menuPanel.setLayout(menuPanelLayout);
            menuPanel.setHeading(I18N.CONSTANTS.menu());
            
                // Menu
                addNavLink(eventBus, menuPanel, I18N.CONSTANTS.createProjectNewProject(), IconImageBundle.ICONS.add(), new Listener<ButtonEvent>() {
                    
                    private final Window window;
                    private final FormPanel formPanel;
                    private final TextField<String> nameField;
                    private final TextField<String> fullNameField;
                    private final ComboBox<ProjectModelDTOLight> modelsField;
                    private final ListStore<ProjectModelDTOLight> modelsStore;
                    private final ListStore<CountryDTO> countriesStore;
                    private final ComboBox<CountryDTO> countriesField;
                    
                    {
                        
                        // Name field.
                        nameField = new TextField<String>();
                        nameField.setFieldLabel(I18N.CONSTANTS.projectName());
                        nameField.setAllowBlank(false);
                        
                        // Full name field.
                        fullNameField = new TextField<String>();
                        fullNameField.setFieldLabel(I18N.CONSTANTS.projectFullName());
                        fullNameField.setAllowBlank(false);
                        
                        // Models list.
                        modelsField = new ComboBox<ProjectModelDTOLight>();
                        modelsField.setFieldLabel(I18N.CONSTANTS.projectModel());
                        modelsField.setAllowBlank(false);
                        modelsField.setValueField("id");
                        modelsField.setDisplayField("name");
                        modelsField.setEditable(false);
                        
                        // Models list store.
                        modelsStore = new ListStore<ProjectModelDTOLight>();
                        modelsStore.addListener(Events.Add, new Listener<StoreEvent<ProjectModelDTO>>() {
                            
                            @Override
                            public void handleEvent(StoreEvent<ProjectModelDTO> be) {
                                modelsField.setEnabled(true);
                            }
                        });
                        
                        modelsStore.addListener(Events.Clear, new Listener<StoreEvent<ProjectModelDTO>>() {
                            
                            @Override
                            public void handleEvent(StoreEvent<ProjectModelDTO> be) {
                                modelsField.setEnabled(false);
                            }
                        });
                        modelsField.setStore(modelsStore);
                        
                        // Countries list.
                        countriesField = new ComboBox<CountryDTO>();
                        countriesField.setFieldLabel(I18N.CONSTANTS.projectCountry());
                        countriesField.setAllowBlank(false);
                        countriesField.setValueField("id");
                        countriesField.setDisplayField("name");
                        countriesField.setEditable(false);
                        
                        // Countries list store.
                        countriesStore = new ListStore<CountryDTO>();
                        countriesStore.addListener(Events.Add, new Listener<StoreEvent<CountryDTO>>() {
                            
                            @Override
                            public void handleEvent(StoreEvent<CountryDTO> be) {
                                countriesField.setEnabled(true);
                            }
                        });
                        
                        countriesStore.addListener(Events.Clear, new Listener<StoreEvent<CountryDTO>>() {
                            
                            @Override
                            public void handleEvent(StoreEvent<CountryDTO> be) {
                                countriesField.setEnabled(false);
                            }
                        });
                        countriesField.setStore(countriesStore);
                        
                        // Create button.
                        final Button createButton = new Button(I18N.CONSTANTS.createProjectCreateButton());
                        createButton.addListener(Events.OnClick, new Listener<ButtonEvent>() {
                            @Override
                            public void handleEvent(ButtonEvent be) {
                                createProject();
                            }
                        });
                        
                        // Form panel.
                        formPanel = new FormPanel();
                        formPanel.setBodyBorder(false);
                        formPanel.setHeaderVisible(false);
                        formPanel.setPadding(5);
                        
                        formPanel.add(nameField);
                        formPanel.add(fullNameField);
                        formPanel.add(modelsField);
                        formPanel.add(countriesField);
                        formPanel.addButton(createButton);

                        // Main window panel.
                        final ContentPanel mainPanel = new ContentPanel();
                        mainPanel.setHeaderVisible(false);
                        mainPanel.setLayout(new FitLayout());
                        mainPanel.add(formPanel);
                        
                        // Window.
                        window = new Window();
                        window.setHeading(I18N.CONSTANTS.createProject());
                        window.setSize(350, 200);
                        window.setPlain(true);
                        window.setModal(true);
                        window.setBlinkModal(true);
                        window.setLayout(new FitLayout());
                        window.add(mainPanel);
                    }
                    
                    /**
                     * Creates a project for the given fields.
                     */
                    private void createProject() {
                        
                        // Checks the form completion.
                        if(!formPanel.isValid()) {
                            MessageBox.alert(I18N.CONSTANTS.createProjectFormIncomplete(), 
                                    I18N.CONSTANTS.createProjectFormIncompleteDetails(), 
                                    null);
                            return;
                        }
                        
                        // Gets values.
                        final String name = nameField.getValue();
                        final String fullName = fullNameField.getValue();
                        final long modelId = modelsField.getValue().getId();
                        final int countryId = countriesField.getValue().getId();
                        
                        if (Log.isDebugEnabled()) {
                            
                            final StringBuilder sb = new StringBuilder();
                            sb.append("Create a new project with parameters: ");
                            sb.append("name=");
                            sb.append(name);
                            sb.append(" ; full name=");
                            sb.append(fullName);
                            sb.append(" ; model id=");
                            sb.append(modelId);
                            sb.append(" ; country id=");
                            sb.append(countryId);
                            
                            Log.debug(sb.toString());
                        }
                       
                        // Creates the project.
                        dispatcher.execute(new CreateProject(name, fullName, modelId, countryId), null, new AsyncCallback<VoidResult>() {
                            
                            @Override
                            public void onFailure(Throwable arg0) {
                                MessageBox.alert(I18N.CONSTANTS.createProjectFailed(), 
                                        I18N.CONSTANTS.createProjectFailedDetails(), 
                                        null);
                            }
                            
                            @Override
                            public void onSuccess(VoidResult result) {
                                MessageBox.info(I18N.CONSTANTS.createProjectSucceeded(), 
                                        I18N.CONSTANTS.createProjectSucceededDetails(), 
                                        null);
                                
                                // Refreshes the countries list if needed.
                                dispatcher.execute(new GetCountries(true), null, new AsyncCallback<CountryResult>() {
                                    
                                    @Override
                                    public void onFailure(Throwable throwable) {
                                        // Nothing to do, need hard refreshing.
                                    }

                                    @Override
                                    public void onSuccess(CountryResult countryResult) {
                                        
                                        if (countryResult == null || countryResult.getData() == null || countryResult.getData().isEmpty()) {
                                            return;
                                        }
                                        
                                        if (Log.isDebugEnabled()) {
                                            Log.debug("Refreshes the countries list.");
                                        }
                                        
                                        // Checks if some new countries have projects.
                                        if(countryStore.getCount() != countryResult.getData().size()) {
                                            
                                            if (Log.isDebugEnabled()) {
                                                Log.debug("Some new countries have been added.");
                                            }
                                            
                                            // Temporally stores the actual displayed countries.
                                            final HashMap<Integer, CountryDTO> refreshedCountries = new HashMap<Integer, CountryDTO>();
                                            for (final CountryDTO country : countryStore.getModels()) {
                                                refreshedCountries.put(country.getId(), country);
                                            }
                                            
                                            // The widget needs to be refreshed.
                                            for(final CountryDTO country : countryResult.getData()) {
                                                // New country.
                                                if(refreshedCountries.get(country.getId()) == null) {
                                                    countryStore.add(country);
                                                    if (Log.isDebugEnabled()) {
                                                        Log.debug("Adds the country: " + country.getName() + ".");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                                
                            }
                        });
                        
                        window.hide();
                    }
                    
                    /**
                     * Informs the user that some required data cannot be recovered. The project cannot be created.
                     */
                    private void missingRequiredData() {
                        
                        MessageBox.alert(I18N.CONSTANTS.createProjectDisable(), 
                                I18N.CONSTANTS.createProjectDisableDetails(), 
                                null);
                        
                        window.hide();
                    }
                    
                    @Override
                    public void handleEvent(ButtonEvent be) {
                        
                        // Resets window state.
                        nameField.setValue(null);
                        fullNameField.setValue(null);
                        modelsField.setValue(null);
                        countriesField.setValue(null);
                        modelsStore.removeAll();
                        countriesStore.removeAll();
                        window.show();
                        
                        // Retrieves project models.
                        dispatcher.execute(new GetProjectModels(), null, new AsyncCallback<ProjectModelListResult>() {
                            
                            @Override
                            public void onFailure(Throwable arg0) {
                                missingRequiredData();
                            }
                            
                            @Override
                            public void onSuccess(ProjectModelListResult result) {
                                
                                if(result.getList() == null || result.getList().isEmpty()) {
                                    missingRequiredData();
                                }
                                
                                modelsStore.add(result.getList());
                                modelsStore.commitChanges();
                            }
                        });
                        
                        // Retrieves countries.
                        dispatcher.execute(new GetCountries(), null, new AsyncCallback<CountryResult>() {
                            
                            @Override
                            public void onFailure(Throwable arg0) {
                                missingRequiredData();
                            }
                            
                            @Override
                            public void onSuccess(CountryResult result) {
                                
                                if(result.getData() == null || result.getData().isEmpty()) {
                                    missingRequiredData();
                                }
                                
                                countriesStore.add(result.getData());
                                countriesStore.commitChanges();
                            }
                        });
                    }
                });
                
                // Temporary code to hide/show activityInfo menus
            	Dictionary sigmahParams;
            	boolean showActivityInfoMenus = false;
                try {
                    sigmahParams = Dictionary.getDictionary("SigmahParams");
                    showActivityInfoMenus = Boolean.parseBoolean(sigmahParams.get("showActivityInfoMenus"));
                    if (Log.isDebugEnabled()) {
                    	Log.debug("[DashboardView] Show activityInfo menus ? " + showActivityInfoMenus);
                    }
                } catch (Exception e) {
                    Log.fatal("DictionaryAuthenticationProvider: exception retrieving dictionary 'SigmahParams' from page", e);
                    throw new Error();
                }
                if (showActivityInfoMenus) {
                	addNavLink(eventBus, menuPanel, I18N.CONSTANTS.dataEntry(), IconImageBundle.ICONS.dataEntry(), new SiteGridPageState());
                	addNavLink(eventBus, menuPanel, I18N.CONSTANTS.reports(), IconImageBundle.ICONS.report(), new ReportListPageState());
                	addNavLink(eventBus, menuPanel, I18N.CONSTANTS.charts(), IconImageBundle.ICONS.barChart(), new ChartPageState());
                	addNavLink(eventBus, menuPanel, I18N.CONSTANTS.maps(), IconImageBundle.ICONS.map(), new MapPageState());
                	addNavLink(eventBus, menuPanel, I18N.CONSTANTS.tables(), IconImageBundle.ICONS.table(), new PivotPageState());
                	addNavLink(eventBus, menuPanel, I18N.CONSTANTS.setup(), IconImageBundle.ICONS.setup(), new DbListPageState());
                }
        
            leftPanel.add(menuPanel, vBoxLayoutData);
            
        final BorderLayoutData leftLayoutData = new BorderLayoutData(LayoutRegion.WEST, 250);
        leftLayoutData.setSplit(true);
        add(leftPanel, leftLayoutData);
            
        // Main panel
        final ContentPanel mainPanel = new ContentPanel(new VBoxLayout());
        final VBoxLayout mainPanelLayout = new VBoxLayout();
        mainPanelLayout.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        mainPanel.setLayout(mainPanelLayout);
        mainPanel.setHeaderVisible(false);
        mainPanel.setBorders(false);
        mainPanel.setBodyBorder(false);
        
            // Country list panel
            final ContentPanel missionTreePanel = new ContentPanel(new FitLayout());
            missionTreePanel.setHeading(I18N.CONSTANTS.location());
            final VBoxLayoutData smallVBoxLayoutData = new VBoxLayoutData();
            smallVBoxLayoutData.setFlex(1.0);
            mainPanel.add(missionTreePanel, smallVBoxLayoutData);
            
                // Country list
                final CheckBoxSelectionModel<CountryDTO> selectionModel = new CheckBoxSelectionModel<CountryDTO>();
                
                final ColumnConfig countryName = new ColumnConfig("name", I18N.CONSTANTS.name(), 200);
                final ColumnModel countryColumnModel = new ColumnModel(Arrays.asList(selectionModel.getColumn(), countryName));
                    
                final Grid countryGrid = new Grid(countryStore, countryColumnModel);
                countryGrid.setAutoExpandColumn("name");
                countryGrid.setSelectionModel(selectionModel);
                countryGrid.addPlugin(selectionModel);
                
                missionTreePanel.add(countryGrid);
                
                // Refresh button
                final ActionToolBar countryToolbar = new ActionToolBar(new ActionListener() {
                    @Override
                    public void onUIAction(String actionId) {
                        if(UIActions.refresh.equals(actionId)) {
                            dispatcher.execute(new GetProjects(selectionModel.getSelectedItems()), null, new AsyncCallback<ProjectListResult>() {
                                @Override
                                public void onFailure(Throwable throwable) {
                                    // TODO: Handle the failure
                                }

                                @Override
                                public void onSuccess(ProjectListResult projectList) {
                                    Log.debug("How");
                                    
                                    projectStore.removeAll();
                                    
                                    Log.debug("are you");
                                    
                                    projectStore.add(projectList.getList(), true);
                                    
                                    Log.debug("BONG ?");
                                }
                            });
                        }
                    }
                });
                
                countryToolbar.addRefreshButton(); 
                
                missionTreePanel.setTopComponent(countryToolbar);
            
            // Project tree panel
            final ContentPanel projectTreePanel = new ContentPanel(new FitLayout());
            projectTreePanel.setHeading(I18N.CONSTANTS.projects());
            final VBoxLayoutData largeVBoxLayoutData = new VBoxLayoutData();
            largeVBoxLayoutData.setFlex(2.0);
            mainPanel.add(projectTreePanel, largeVBoxLayoutData);
            
                // Project list
                final ColumnConfig icon = new ColumnConfig("favorite", "-", 24);
                icon.setRenderer(new GridCellRenderer<ProjectDTO>() {
                    private final DashboardImageBundle imageBundle = GWT.create(DashboardImageBundle.class);
                
                    @Override
                    public Object render(final ProjectDTO model, String property, ColumnData config, int rowIndex, int colIndex, final ListStore<ProjectDTO> store, final Grid<ProjectDTO> grid) {
                        final Image icon;
                        
                        if(model.isFavorite())
                            icon = imageBundle.star().createImage();
                        else
                            icon = imageBundle.emptyStar().createImage();
                        
                        icon.addClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                model.setFavorite(!model.isFavorite());
                                // TODO: Save the changes
                            }
                        });
                        
                        return icon;
                    }
                });
                
                final ColumnConfig name = new ColumnConfig("name", I18N.CONSTANTS.name(), 200);
                name.setRenderer(new WidgetTreeGridCellRenderer() {
                    @Override
                    public Widget getWidget(ModelData model, String property, ColumnData config, int rowIndex, int colIndex, ListStore store, Grid grid) {
                        return new Hyperlink((String)model.get(property), true, ProjectPresenter.PAGE_ID.toString()+'!'+model.get("id").toString());
                    }
                });
        
                final ColumnConfig phase = new ColumnConfig("phase", "Phase", 100);
                final ColumnConfig topic = new ColumnConfig("topic", "Topic", 100);
                final ColumnModel columnModel = new ColumnModel(Arrays.asList(icon, name, phase, topic));
                    
                final TreeGrid projectTreeGrid = new TreeGrid(projectStore, columnModel);
                
                projectTreePanel.add(projectTreeGrid);
                
        final BorderLayoutData mainLayoutData = new BorderLayoutData(LayoutRegion.CENTER);
        add(mainPanel, mainLayoutData);
    }

    /**
     * Creates a navigation button in the given panel.
     * 
     * @param eventBus
     *            Event bus of the application
     * @param panel
     *            Placeholder of the button
     * @param text
     *            Label of the button
     * @param icon
     *            Icon displayed next to the label
     * @param place
     *            The user will be redirected there when the button is clicked
     */
    private void addNavLink(final EventBus eventBus, final ContentPanel panel, final String text,
            final AbstractImagePrototype icon, final PageState place) {
        final Button button = new Button(text, icon, new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                eventBus.fireEvent(new NavigationEvent(NavigationHandler.NavigationRequested, place));
            }
        });

        final VBoxLayoutData vBoxLayoutData = new VBoxLayoutData();
        vBoxLayoutData.setFlex(1.0);
        panel.add(button, vBoxLayoutData);
    }

    /**
     * Creates a navigation button in the given panel.
     * 
     * @param eventBus
     *            Event bus of the application
     * @param panel
     *            Placeholder of the button
     * @param text
     *            Label of the button
     * @param icon
     *            Icon displayed next to the label
     * @param clickHandler
     *            The action executed when the button is clicked
     */
    private void addNavLink(final EventBus eventBus, final ContentPanel panel, final String text,
            final AbstractImagePrototype icon, final Listener<ButtonEvent> clickHandler) {

        final Button button = new Button(text, icon);
        button.addListener(Events.OnClick, clickHandler);

        final VBoxLayoutData vBoxLayoutData = new VBoxLayoutData();
        vBoxLayoutData.setFlex(1.0);
        panel.add(button, vBoxLayoutData);
    }
}
