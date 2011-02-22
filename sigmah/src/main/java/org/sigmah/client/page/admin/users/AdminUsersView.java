package org.sigmah.client.page.admin.users;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.sigmah.client.cache.UserLocalCache;
import org.sigmah.client.dispatch.Dispatcher;
import org.sigmah.client.dispatch.monitor.MaskingAsyncMonitor;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.icon.IconImageBundle;
import org.sigmah.client.page.admin.users.AdminUsersPresenter.AdminPrivacyGroupsStore;
import org.sigmah.client.page.admin.users.AdminUsersPresenter.AdminProfilesStore;
import org.sigmah.client.page.admin.users.AdminUsersActionListener;
import org.sigmah.client.page.admin.users.AdminUsersPresenter.AdminUsersStore;
import org.sigmah.client.page.admin.users.AdminUsersPresenter.View;
import org.sigmah.client.page.common.grid.ConfirmCallback;
import org.sigmah.client.page.common.toolbar.ActionToolBar;
import org.sigmah.client.page.common.toolbar.UIActions;
import org.sigmah.client.page.config.form.PrivacyGroupSigmahForm;
import org.sigmah.client.page.config.form.ProfileSigmahForm;
import org.sigmah.client.page.config.form.UserSigmahForm;
import org.sigmah.client.ui.StylableVBoxLayout;
import org.sigmah.shared.command.result.CreateResult;
import org.sigmah.shared.domain.profile.GlobalPermissionEnum;
import org.sigmah.shared.domain.profile.PrivacyGroupPermissionEnum;
import org.sigmah.shared.dto.OrgUnitDTO;
import org.sigmah.shared.dto.UserDTO;
import org.sigmah.shared.dto.profile.PrivacyGroupDTO;
import org.sigmah.shared.dto.profile.ProfileDTO;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Displays users administration screen.
 * 
 * @author nrebiai
 * 
 */
public class AdminUsersView extends View {
	
	private final static String DEFAULT_FILTER = "default";
	private final static String STYLE_MAIN_BACKGROUND = "main-background";
	private final static int BORDER = 8;
	
	private final Grid<UserDTO> usersGrid;
	private final Grid<ProfileDTO> profilesGrid;
	private final Grid<PrivacyGroupDTO> privacyGroupsGrid;
	private final Dispatcher dispatcher;
	private final UserLocalCache cache;
	private final ContentPanel usersPanel;
	private final ContentPanel usersListPanel;
	private final LayoutContainer profilesPanel;
	private final ContentPanel profilesListPanel;
	private final ContentPanel privacyGroupsPanel;
	private final AdminUsersPresenter.AdminUsersStore adminUsersStore;
	private final AdminUsersPresenter.AdminProfilesStore adminProfilesStore;
	private final AdminUsersPresenter.AdminPrivacyGroupsStore adminPrivacyGroupsStore;
	private String filterUser = DEFAULT_FILTER;
	
	public AdminUsersView(Dispatcher dispatcher, UserLocalCache cache){
		
		this.dispatcher = dispatcher;
		this.cache = cache;
        
		// Main panel
        usersPanel = new ContentPanel();
        final VBoxLayout mainPanelLayout = new StylableVBoxLayout(STYLE_MAIN_BACKGROUND);
        mainPanelLayout.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        usersPanel.setLayout(mainPanelLayout);
        usersPanel.setHeaderVisible(false);
        usersPanel.setBorders(false);
        usersPanel.setBodyBorder(false);
        
		//Users panel                       
		usersListPanel = new ContentPanel();
		usersListPanel.setScrollMode(Style.Scroll.AUTO);
		usersListPanel.setHeading(I18N.CONSTANTS.adminUsersPanel());		
		adminUsersStore = new AdminUsersPresenter.AdminUsersStore();
		usersGrid = buildUsersGrid();
		usersGrid.setSelectionModel(new GridSelectionModel<UserDTO>());
		usersListPanel.setTopComponent(initToolBar(1));
		usersListPanel.add(usersGrid);
		
		final VBoxLayoutData topVBoxLayoutData = new VBoxLayoutData();
        topVBoxLayoutData.setFlex(1.0);
        usersPanel.add(usersListPanel, topVBoxLayoutData);
		
		//Profiles Panel		
		profilesPanel = new LayoutContainer(new BorderLayout());
		
		
		profilesListPanel = new ContentPanel(new FitLayout());
		profilesListPanel.setScrollMode(Style.Scroll.AUTO);
		profilesListPanel.setHeading(I18N.CONSTANTS.adminProfilesPanel());	
		adminProfilesStore = new AdminUsersPresenter.AdminProfilesStore();
		profilesGrid = buildProfilesGrid();		
		profilesGrid.setSelectionModel(new GridSelectionModel<ProfileDTO>());
		profilesListPanel.add(profilesGrid);
		profilesListPanel.setTopComponent(initToolBar(2));
		final BorderLayoutData leftLayoutData = new BorderLayoutData(LayoutRegion.CENTER);
		leftLayoutData.setMargins(new Margins(BORDER/2, BORDER/2, 0, 0));
        profilesPanel.add(profilesListPanel, leftLayoutData);
		
        //Privacy groups
        adminPrivacyGroupsStore = new AdminUsersPresenter.AdminPrivacyGroupsStore();
        privacyGroupsGrid = buildPrivacyGroupsGrid();	
        privacyGroupsGrid.setSelectionModel(new GridSelectionModel<PrivacyGroupDTO>());
        privacyGroupsPanel = new ContentPanel(new FitLayout());
        privacyGroupsPanel.setScrollMode(Style.Scroll.AUTO);
        privacyGroupsPanel.setWidth(230);
        privacyGroupsPanel.setHeading(I18N.CONSTANTS.adminPrivacyGroups());
        privacyGroupsPanel.add(privacyGroupsGrid);
        privacyGroupsPanel.setTopComponent(initToolBar(3));
		final BorderLayoutData rightLayoutData = new BorderLayoutData(LayoutRegion.EAST, 300);
        rightLayoutData.setMargins(new Margins(BORDER/2, 0, 0, BORDER/2));
        profilesPanel.add(privacyGroupsPanel, rightLayoutData);					
        
        final VBoxLayoutData bottomVBoxLayoutData = new VBoxLayoutData();
        bottomVBoxLayoutData.setFlex(2.0);
        usersPanel.add(profilesPanel, bottomVBoxLayoutData);
	}
	
	
	@Override
	public ContentPanel getMainPanel() {
		return usersPanel;
	}
	
	public ContentPanel getUsersPanel() {
		return usersListPanel;
	}
	
	private Grid<PrivacyGroupDTO> buildPrivacyGroupsGrid() {		
		
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();  
		   
		ColumnConfig column = new ColumnConfig("id",I18N.CONSTANTS.adminPrivacyGroupsId(),30);  
		configs.add(column);
		
		column = new ColumnConfig("code",I18N.CONSTANTS.adminPrivacyGroupsCode(),100);  
		configs.add(column);
		
		column = new ColumnConfig("title",I18N.CONSTANTS.adminPrivacyGroupsName(),100);  
		configs.add(column);
		
		column = new ColumnConfig();    
		column.setWidth(75);  
		column.setAlignment(Style.HorizontalAlignment.RIGHT);
	    column.setRenderer(new GridCellRenderer<PrivacyGroupDTO>(){

			@Override
			public Object render(final PrivacyGroupDTO model, final String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<PrivacyGroupDTO> store, Grid<PrivacyGroupDTO> grid) {
				
				Button button = new Button(I18N.CONSTANTS.edit());
		        button.setItemId(UIActions.edit);
		        button.addListener(Events.OnClick, new Listener<ButtonEvent>(){

					@Override
					public void handleEvent(ButtonEvent be) {
						final Window window = new Window();
						
						final PrivacyGroupSigmahForm form = AdminUsersView.this.showNewPrivacyGroupForm(window, new AsyncCallback<CreateResult>(){

							@Override
							public void onFailure(Throwable arg0) {
								window.hide();
								
							}

							@Override
							public void onSuccess(CreateResult result) {
								window.hide();
								AdminUsersView.this.getAdminPrivacyGroupsStore().remove(model);
								AdminUsersView.this.getAdminPrivacyGroupsStore().add((PrivacyGroupDTO)result.getEntity());
								AdminUsersView.this.getAdminPrivacyGroupsStore().commitChanges();
							}			
						}, model);
						
				        
				        window.add(form);
				        window.show();
						
					}
		        	
		        });		        
		        
				return button;
				
			}
	    	
	    });  
		configs.add(column); 
		
		
		ColumnModel cm = new ColumnModel(configs);	
		final Grid<PrivacyGroupDTO> grid = new Grid<PrivacyGroupDTO>(adminPrivacyGroupsStore, cm); 
		
		grid.getView().setForceFit(true);  
	    grid.setStyleAttribute("borderTop", "none");    
	    grid.setBorders(false);  
	    grid.setStripeRows(true);
	    grid.setAutoHeight(true);
	    
		return grid;
	}
	
	private Grid<ProfileDTO> buildProfilesGrid() {
		
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();  
		   
		ColumnConfig column = new ColumnConfig("id",I18N.CONSTANTS.adminProfilesId(),30);  
		configs.add(column);
		
		column = new ColumnConfig("name",I18N.CONSTANTS.adminProfilesName(),100);  
		configs.add(column);
		
		column = new ColumnConfig("globalPermissions",I18N.CONSTANTS.adminProfilesGlobalPermissions(),200);  
		column.setRenderer(new GridCellRenderer<ProfileDTO>(){

			@Override
			public Object render(ProfileDTO model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<ProfileDTO> store, Grid<ProfileDTO> grid) {
				String content = "";
				//get selected permissions
				Set<GlobalPermissionEnum> selectedGlobalPermissions = model.get(property);
				for(GlobalPermissionEnum gpEnum : selectedGlobalPermissions){
					String perm = ProfileSigmahForm.translateGlobalPermission(gpEnum);
					if(perm!=null && !perm.isEmpty())
						content = perm + ", " + content;
				}
				return createUserGridText(content);
				//init comboBox
				/*CheckBoxComboBox<GlobalPermissionDTO> globalPermissionsCombo = new CheckBoxComboBox<GlobalPermissionDTO>();
				ListStore<GlobalPermissionDTO> permissionsStore = new ListStore<GlobalPermissionDTO>();
				List<GlobalPermissionDTO> selection = new ArrayList<GlobalPermissionDTO>();
				for(GlobalPermissionEnum enumItem : GlobalPermissionEnum.values()){			
					GlobalPermissionDTO gpEnum = new GlobalPermissionDTO();
					gpEnum.setGlobalPermissions(enumItem);				
					permissionsStore.add(gpEnum);
					if(selectedGlobalPermissions.contains(enumItem))
						selection.add(gpEnum);
				}
				
				globalPermissionsCombo.setStore(permissionsStore);
				//globalPermissionsCombo.setSelection(selection);
				globalPermissionsCombo.setDisplayField("globalPermission");
				globalPermissionsCombo.setValueField("globalPermission");
				globalPermissionsCombo.setEditable(true);		
				globalPermissionsCombo.setTriggerAction(TriggerAction.ALL);
				return globalPermissionsCombo;*/
			}
			
		});
		configs.add(column);
		
		
		column = new ColumnConfig("privacyGroups",I18N.CONSTANTS.adminProfilesPrivacyGroups(),100);  
		column.setRenderer(new GridCellRenderer<ProfileDTO>(){

			@Override
			public Object render(ProfileDTO model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<ProfileDTO> store, Grid<ProfileDTO> grid) {
				Map<PrivacyGroupDTO, PrivacyGroupPermissionEnum> privacyGroups = model.get(property);
				String content = "";
				for(Map.Entry<PrivacyGroupDTO, PrivacyGroupPermissionEnum> pg : privacyGroups.entrySet()){
					content = "(" + pg.getKey().getTitle() + ", " + ProfileSigmahForm.translatePGPermission(pg.getValue()) + ")" + ", " + content;
				}
				return createUserGridText(content);
			}
			
		});
		configs.add(column);
		
		column = new ColumnConfig();    
		column.setWidth(75);  
		column.setAlignment(Style.HorizontalAlignment.RIGHT);
	    column.setRenderer(new GridCellRenderer<ProfileDTO>(){

			@Override
			public Object render(final ProfileDTO model, final String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<ProfileDTO> store, Grid<ProfileDTO> grid) {
				
				Button button = new Button(I18N.CONSTANTS.edit());
		        button.setItemId(UIActions.edit);
		        button.addListener(Events.OnClick, new Listener<ButtonEvent>(){

					@Override
					public void handleEvent(ButtonEvent be) {
						final Window window = new Window();
						ProfileSigmahForm form = AdminUsersView.this.showNewProfileForm(window, new AsyncCallback<CreateResult>(){

							@Override
							public void onFailure(Throwable caught) {
								window.hide();								
							}

							@Override
							public void onSuccess(CreateResult result) {
								window.hide();
								//refresh view
								AdminUsersView.this.getAdminProfilesStore().remove(model);
								AdminUsersView.this.getAdminProfilesStore().add((ProfileDTO)result.getEntity());
								AdminUsersView.this.getAdminProfilesStore().commitChanges();
							}			
						}, model);
						
						window.add(form);
				        window.show();
						
					}
		        	
		        });		        
		        
				return button;
				
			}
	    	
	    });  
		configs.add(column); 
	
		ColumnModel cm = new ColumnModel(configs);	
		final Grid<ProfileDTO> grid = new Grid<ProfileDTO>(adminProfilesStore, cm); 
		adminProfilesStore.addListener(Events.Add, new Listener<StoreEvent<UserDTO>>() {

            @Override
            public void handleEvent(StoreEvent<UserDTO> be) {
            }
        });

		adminProfilesStore.addListener(Events.Clear, new Listener<StoreEvent<UserDTO>>() {

            @Override
            public void handleEvent(StoreEvent<UserDTO> be) {
            }
        });
		adminProfilesStore.addListener(Store.Filter, new Listener<StoreEvent<UserDTO>>() {

            @Override
            public void handleEvent(StoreEvent<UserDTO> be) {
            }
        });
     
	    grid.getView().setForceFit(true);  
	    grid.setStyleAttribute("borderTop", "none");    
	    grid.setBorders(false);  
	    grid.setStripeRows(true);
	    grid.setAutoHeight(true);
		return grid;
	}
	
	private Grid<UserDTO> buildUsersGrid(){

		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();  

		ColumnConfig  column = new ColumnConfig("name",I18N.CONSTANTS.adminUsersName(),100);  
		configs.add(column);
		
		column = new ColumnConfig("firstName",I18N.CONSTANTS.adminUsersFirstName(),100);    
		configs.add(column); 
		
		column = new ColumnConfig("active",I18N.CONSTANTS.adminUsersActive(),50);  
		column.setRenderer(new GridCellRenderer<UserDTO>() {
            @Override
            public Object render(UserDTO model, String property, ColumnData config, int rowIndex, int colIndex,
                    ListStore<UserDTO> store, Grid<UserDTO> grid) {
                final boolean isActive = model.getActive();
                return createUserGridText(isActive ? I18N.CONSTANTS.adminUsersIsActive() : I18N.CONSTANTS.adminUsersNotActive());
            }
        });
		configs.add(column);	  
	   
		column = new ColumnConfig("email",I18N.CONSTANTS.adminUsersEmail(),150);   
		configs.add(column);  
		
		column = new ColumnConfig("locale",I18N.CONSTANTS.adminUsersLocale(),50);      
		configs.add(column); 
		
		column = new ColumnConfig("orgUnit",I18N.CONSTANTS.adminUsersOrgUnit(),75);
		column.setRenderer(new GridCellRenderer<UserDTO>() {
            @Override
            public Object render(UserDTO model, String property, ColumnData config, int rowIndex, int colIndex,
                    ListStore<UserDTO> store, Grid<UserDTO> grid) {
                final OrgUnitDTO orgUnit = (OrgUnitDTO) model.get(property);
                return createUserGridText(orgUnit != null ? 
                		(orgUnit.getFullName() != null ? orgUnit.getFullName() : "" ) : "");
            }
        });
		configs.add(column); 
		
		column = new ColumnConfig("pwdChangeKey",I18N.CONSTANTS.adminUsersPasswordChange(),120);   
		configs.add(column);
		
		column = new ColumnConfig("pwdChangeDate",I18N.CONSTANTS.adminUsersDatePasswordChange(),120);  
		final DateTimeFormat format = DateTimeFormat.getFormat(I18N.CONSTANTS.flexibleElementDateFormat());  
		column.setHeader(I18N.CONSTANTS.adminUsersDatePasswordChange());
		column.setDateTimeFormat(format);
        column.setRenderer(new GridCellRenderer<UserDTO>() {
            @Override
            public Object render(UserDTO model, String property, ColumnData config, int rowIndex, int colIndex,
                    ListStore<UserDTO> store, Grid<UserDTO> grid) {
                final Date d = (Date) model.get(property);
                return createUserGridText(d != null ? format.format(d) : "");
            }
        });  
		configs.add(column); 
		
		
		column = new ColumnConfig();  
		column.setId("profiles");  
		column.setHeader(I18N.CONSTANTS.adminUsersProfiles());   
		column.setWidth(300);  
	    column.setRenderer(new GridCellRenderer<UserDTO>(){

			@Override
			public Object render(UserDTO model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<UserDTO> store, Grid<UserDTO> grid) {
				
				String content = "";
				
				if(model.getProfilesDTO()!= null){																							
					for(ProfileDTO oneProfileDTO : model.getProfilesDTO()){
						content = oneProfileDTO.getName() + ", " + content;
					}
				}else{
					content = I18N.CONSTANTS.adminUsersNoProfiles();
				}				
				return createUserGridText(content);
				
			}
	    	
	    });  
		configs.add(column); 
		
		column = new ColumnConfig();    
		column.setWidth(75);  
		column.setAlignment(Style.HorizontalAlignment.RIGHT);
	    column.setRenderer(new GridCellRenderer<UserDTO>(){

			@Override
			public Object render(final UserDTO model, final String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<UserDTO> store, Grid<UserDTO> grid) {
				
				Button button = new Button(I18N.CONSTANTS.edit());
		        button.setItemId(UIActions.edit);
		        button.addListener(Events.OnClick, new Listener<ButtonEvent>(){

					@Override
					public void handleEvent(ButtonEvent be) {
						final Window window = new Window();
						UserSigmahForm form = AdminUsersView.this.showNewUserForm(window, new AsyncCallback<CreateResult>(){

							@Override
							public void onFailure(Throwable caught) {
								window.hide();								
							}

							@Override
							public void onSuccess(CreateResult result) {
								window.hide();
								//refresh view
								AdminUsersView.this.getAdminUsersStore().remove(model);
								AdminUsersView.this.getAdminUsersStore().add((UserDTO)result.getEntity());
								AdminUsersView.this.getAdminUsersStore().commitChanges();
							}			
						}, model);
						
						window.add(form);
				        window.show();
						
					}
		        	
		        });
		        
		        
				return button;
				
			}
	    	
	    });  
		configs.add(column); 
		
		ColumnModel cm = new ColumnModel(configs);
		
		final Grid<UserDTO> grid = new Grid<UserDTO>(adminUsersStore, cm); 
	    adminUsersStore.addListener(Events.Add, new Listener<StoreEvent<UserDTO>>() {

	            @Override
	            public void handleEvent(StoreEvent<UserDTO> be) {
	            }
	        });

	     adminUsersStore.addListener(Events.Clear, new Listener<StoreEvent<UserDTO>>() {

	            @Override
	            public void handleEvent(StoreEvent<UserDTO> be) {
	            }
	        });
	     adminUsersStore.addListener(Store.Filter, new Listener<StoreEvent<UserDTO>>() {

	            @Override
	            public void handleEvent(StoreEvent<UserDTO> be) {
	            }
	        });
	     
	     grid.getView().setForceFit(true);  
	     grid.setStyleAttribute("borderTop", "none");    
	     grid.setBorders(false);  
	     grid.setStripeRows(true);
	     grid.setAutoHeight(true);
	     return grid;
	}
	
	private void addFilterByUser(){		
		StoreFilter<UserDTO> filter = new StoreFilter<UserDTO>(){
			@Override
			public boolean select(Store<UserDTO> store, UserDTO parent,UserDTO item,
	                    String property) {
				boolean selected = false;
				selected = item.getName().toUpperCase().startsWith(filterUser.toUpperCase());					
				return selected;
			}
		};						
		adminUsersStore.addFilter(filter);					
	}
	
	private void applyFilterByUser(){
		if(filterUser != null 
				&& !filterUser.isEmpty() 
				&& !filterUser.trim().equals("") 
				&& !DEFAULT_FILTER.equals(filterUser)){
			adminUsersStore.applyFilters(null);
		}else{
			adminUsersStore.clearFilters();
		}		
	}

	
	
    private ActionToolBar initToolBar(int panel) {
    	
		ActionToolBar toolBar = new ActionToolBar();
		if(panel == 1){
			toolBar.addButton(UIActions.add, I18N.CONSTANTS.addUser(), IconImageBundle.ICONS.addUser());
			toolBar.addButton(UIActions.delete, I18N.CONSTANTS.adminUserDisable(), IconImageBundle.ICONS.deleteUser());
			toolBar.add(new LabelToolItem(I18N.CONSTANTS.adminUsersSearchByName()));	   
	        //Filtering by User name
	        adminUsersStore.clearFilters();
	        final TextField<String> filterUserName = new TextField<String>();        
	        filterUserName.addKeyListener(new KeyListener(){
	        	public void componentKeyUp(ComponentEvent event) {
	        		filterUser = filterUserName.getValue();
	        		applyFilterByUser();
	        	}
	        });
	        addFilterByUser();
	        toolBar.add(filterUserName);
	        
	        /*toolBar.add(new LabelToolItem(I18N.CONSTANTS.adminUsersSelectionMode()));  
	        final SimpleComboBox<String> type = new SimpleComboBox<String>();  
	        type.setTriggerAction(TriggerAction.ALL);  
	        type.setEditable(false);  
	        type.setFireChangeEventOnSetValue(true);  
		    type.setWidth(100);  
		    type.add("Row");  
		    type.add("Cell");  
		    type.setSimpleValue("Row");  
		    type.addListener(Events.Change, new Listener<FieldEvent>() {  
		    	public void handleEvent(FieldEvent be) {  
		    		boolean cell = type.getSimpleValue().equals("Cell");  
		    		usersGrid.getSelectionModel().deselectAll();  
		    		if (cell) {  
		    			usersGrid.setSelectionModel(new CellSelectionModel<UserDTO>());  
		    		} else {  
		    			usersGrid.setSelectionModel(new GridSelectionModel<UserDTO>());  
		    		}  
		    	}  
		    });  
		    
		    toolBar.add(type);*/
		    toolBar.setListener(new AdminUsersActionListener(this, dispatcher));
		}else if(panel == 2){
			toolBar.addButton(UIActions.add, I18N.CONSTANTS.adminProfileAdd(), IconImageBundle.ICONS.add());
			//toolBar.addButton(UIActions.delete, I18N.CONSTANTS.adminProfileDelete(), IconImageBundle.ICONS.delete());
			toolBar.setListener(new AdminProfilesActionListener(this, dispatcher));
		}else if(panel == 3){
			toolBar.addButton(UIActions.add, I18N.CONSTANTS.addItem(), IconImageBundle.ICONS.add());
			//toolBar.addButton(UIActions.delete, I18N.CONSTANTS.adminPrivacyGroupDelete(), IconImageBundle.ICONS.delete());
			toolBar.setListener(new AdminPrivacyGroupsActionListener(this, dispatcher));
		}

	    return toolBar;
    }

	@Override
	public List<UserDTO> getUsersSelection() {
		GridSelectionModel<UserDTO> sm = usersGrid.getSelectionModel();
        return sm.getSelectedItems();
	}
	
	@Override
	public List<ProfileDTO> getProfilesSelection() {
		GridSelectionModel<ProfileDTO> sm = profilesGrid.getSelectionModel();
        return sm.getSelectedItems();
	}
	
	@Override
	public List<PrivacyGroupDTO> getPrivacyGroupsSelection() {
		GridSelectionModel<PrivacyGroupDTO> sm = privacyGroupsGrid.getSelectionModel();
        return sm.getSelectedItems();
	}

	
	private Object createUserGridText(String content) {
        final Text label = new Text(content);
        label.addStyleName("project-grid-leaf");
        
        return label;
    }

	@Override
	public UserSigmahForm showNewUserForm(Window window, AsyncCallback<CreateResult> callback, UserDTO userToUpdate) {		
		
        window.setHeading(I18N.CONSTANTS.newUser());
        window.setSize(550, 350);
        window.setPlain(true);
        window.setModal(true);
        window.setBlinkModal(true);
        window.setLayout(new FitLayout());
        
        final UserSigmahForm form = new UserSigmahForm(dispatcher, cache, callback, userToUpdate);

		return form;

	}

	@Override
	public ProfileSigmahForm showNewProfileForm(Window window,
			AsyncCallback<CreateResult> asyncCallback, ProfileDTO profileToUpdate) {
		window.setHeading(I18N.CONSTANTS.adminProfileAdd());
        window.setSize(550, 500);
        window.setPlain(true);
        window.setModal(true);
        window.setBlinkModal(true);
        window.setLayout(new FitLayout());
        
        final ProfileSigmahForm form = new ProfileSigmahForm(dispatcher, cache, asyncCallback, profileToUpdate);

		return form;
	}
	
	@Override
	public PrivacyGroupSigmahForm showNewPrivacyGroupForm(Window window,
			AsyncCallback<CreateResult> asyncCallback, PrivacyGroupDTO privacyGroupToUpdate) {
		window.setHeading(I18N.CONSTANTS.addItem());
        window.setSize(400, 150);
        window.setPlain(true);
        window.setModal(true);
        window.setBlinkModal(true);
        window.setLayout(new FitLayout());
        
        final PrivacyGroupSigmahForm form = new PrivacyGroupSigmahForm(dispatcher, cache, asyncCallback, privacyGroupToUpdate);

		return form;
	}
	
	@Override
	public AdminUsersStore getAdminUsersStore() {
		return adminUsersStore;
	}
	
	@Override
	public MaskingAsyncMonitor getUsersLoadingMonitor() {
		return new MaskingAsyncMonitor(usersListPanel, I18N.CONSTANTS.loading());
	}
	
	@Override
	public AdminProfilesStore getAdminProfilesStore() {
		return adminProfilesStore;
	}

	@Override
	public MaskingAsyncMonitor getProfilesLoadingMonitor() {
		return new MaskingAsyncMonitor(profilesListPanel, I18N.CONSTANTS.loading());
	}

	@Override
	public AdminPrivacyGroupsStore getAdminPrivacyGroupsStore() {
		return adminPrivacyGroupsStore;
	}



	@Override
	public MaskingAsyncMonitor getPrivacyGroupsLoadingMonitor() {
		return new MaskingAsyncMonitor(privacyGroupsPanel, I18N.CONSTANTS.loading());
	}


	@Override
	public void confirmDeleteSelected(ConfirmCallback confirmCallback) {
		confirmCallback.confirmed();		
	}

}
