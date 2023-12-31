package org.sigmah.client.page.orgunit;

import java.util.ArrayList;

import org.sigmah.client.EventBus;
import org.sigmah.client.cache.UserLocalCache;
import org.sigmah.client.dispatch.AsyncMonitor;
import org.sigmah.client.dispatch.Dispatcher;
import org.sigmah.client.dispatch.remote.Authentication;
import org.sigmah.client.event.NavigationEvent;
import org.sigmah.client.event.NavigationEvent.NavigationError;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.page.Frame;
import org.sigmah.client.page.NavigationCallback;
import org.sigmah.client.page.NavigationHandler;
import org.sigmah.client.page.Page;
import org.sigmah.client.page.PageId;
import org.sigmah.client.page.PageState;
import org.sigmah.client.page.TabPage;
import org.sigmah.client.page.orgunit.calendar.OrgUnitCalendarPresenter;
import org.sigmah.client.page.orgunit.dashboard.OrgUnitDashboardPresenter;
import org.sigmah.client.page.orgunit.details.OrgUnitDetailsPresenter;
import org.sigmah.client.page.orgunit.reports.OrgUnitReportsPresenter;
import org.sigmah.client.page.project.SubPresenter;
import org.sigmah.client.ui.ToggleAnchor;
import org.sigmah.shared.command.GetOrgUnit;
import org.sigmah.shared.command.GetValue;
import org.sigmah.shared.command.result.ValueResult;
import org.sigmah.shared.domain.profile.GlobalPermissionEnum;
import org.sigmah.shared.dto.OrgUnitBannerDTO;
import org.sigmah.shared.dto.OrgUnitDTO;
import org.sigmah.shared.dto.element.BudgetElementDTO;
import org.sigmah.shared.dto.element.DefaultFlexibleElementDTO;
import org.sigmah.shared.dto.element.FlexibleElementDTO;
import org.sigmah.shared.dto.layout.LayoutConstraintDTO;
import org.sigmah.shared.dto.layout.LayoutDTO;
import org.sigmah.shared.dto.layout.LayoutGroupDTO;
import org.sigmah.shared.dto.profile.ProfileUtils;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;

public class OrgUnitPresenter implements Frame, TabPage {

	public static final PageId PAGE_ID = new PageId("orgunit");

	public static final int REPORT_TAB_INDEX = 3;

	/**
	 * Description of the view managed by this presenter.
	 */
	@ImplementedBy(OrgUnitView.class)
	public interface View {

		public ContentPanel getPanelBanner();

		public ContentPanel getTabPanel();

		public void setMainPanel(Widget widget);

		public void insufficient();

		public void sufficient();
	}

	private final View view;
	private final Dispatcher dispatcher;
	private final Authentication authentication;
	private final UserLocalCache cache;
	private Page activePage;
	private OrgUnitState currentState;
	private ToggleAnchor currentTab;
	private OrgUnitDTO currentOrgUnitDTO;

	private final static ArrayList<String> MAIN_TABS = new ArrayList<String>();

	private final static ArrayList<SubPresenter> presenters = new ArrayList<SubPresenter>();

	@Inject
	public OrgUnitPresenter(final Dispatcher dispatcher, View view, Authentication authentication,
					final EventBus eventBus, final UserLocalCache cache) {

		this.dispatcher = dispatcher;
		this.view = view;
		this.authentication = authentication;
		this.cache = cache;

		if (MAIN_TABS.isEmpty()) {
			MAIN_TABS.add(I18N.CONSTANTS.orgUnitTabOverview());
			MAIN_TABS.add(I18N.CONSTANTS.orgUnitTabInformations());

			if (ProfileUtils.isGranted(authentication, GlobalPermissionEnum.VIEW_AGENDA))
				MAIN_TABS.add(I18N.CONSTANTS.projectTabCalendar());
			MAIN_TABS.add(I18N.CONSTANTS.projectTabReports());
		}

		presenters.add(new OrgUnitDashboardPresenter(dispatcher, eventBus, authentication, this));
		presenters.add(new OrgUnitDetailsPresenter(dispatcher, authentication, this, cache, eventBus));

		if (ProfileUtils.isGranted(authentication, GlobalPermissionEnum.VIEW_AGENDA))
			presenters.add(new OrgUnitCalendarPresenter(dispatcher, authentication, this));
		presenters.add(new OrgUnitReportsPresenter(authentication, dispatcher, eventBus, this));

		for (int i = 0; i < MAIN_TABS.size(); i++) {
			final int index = i;

			String tabTitle = MAIN_TABS.get(i);

			final HBoxLayoutData layoutData = new HBoxLayoutData();
			layoutData.setMargins(new Margins(0, 10, 0, 0));

			final ToggleAnchor anchor = new ToggleAnchor(tabTitle);
			anchor.setAnchorMode(true);

			anchor.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					eventBus.fireEvent(new NavigationEvent(NavigationHandler.NavigationRequested, currentState
									.deriveTo(index), null));
				}
			});

			this.view.getTabPanel().add(anchor, layoutData);
		}
	}

	private void selectTab(int index, boolean force) {

		if (index >= presenters.size())
			return;
		final ToggleAnchor anchor = (ToggleAnchor) this.view.getTabPanel().getWidget(index);

		if (currentTab != anchor) {
			if (currentTab != null)
				currentTab.toggleAnchorMode();

			anchor.toggleAnchorMode();
			currentTab = anchor;

			OrgUnitPresenter.this.view.setMainPanel(presenters.get(index).getView());
			presenters.get(index).viewDidAppear();
		} else if (force) {
			OrgUnitPresenter.this.view.setMainPanel(presenters.get(index).getView());
			presenters.get(index).viewDidAppear();
		}
	}

	@Override
	public boolean navigate(final PageState place) {

		final OrgUnitState state = (OrgUnitState) place;
		final int id = state.getOrgUnitId();

		view.sufficient();

		if (currentOrgUnitDTO == null || id != currentOrgUnitDTO.getId()) {

			if (Log.isDebugEnabled()) {
				Log.debug("Loading org unit #" + id + "...");
			}

			dispatcher.execute(new GetOrgUnit(id), null, new AsyncCallback<OrgUnitDTO>() {

				@Override
				public void onFailure(Throwable throwable) {
					Log.error("Error, org unit #" + id + " not loaded.");
				}

				@Override
				public void onSuccess(OrgUnitDTO orgUnitDTO) {

					if (orgUnitDTO == null) {
						Log.error("Org unit not loaded : " + id);
						view.insufficient();
					} else {

						if (Log.isDebugEnabled()) {
							Log.debug("Org unit loaded : " + orgUnitDTO.getName());
						}

						currentState = state;

						boolean orgUnitChanged = !orgUnitDTO.equals(currentOrgUnitDTO);

						state.setTabTitle(orgUnitDTO.getName());
						loadOrgUnitOnView(orgUnitDTO);

						selectTab(state.getCurrentSection(), orgUnitChanged);
					}
				}
			});
		} else {
			boolean change = false;

			if (!currentState.equals(state)) {
				change = true;
				currentState = state;
			}

			selectTab(state.getCurrentSection(), change);
		}

		return true;
	}

	/**
	 * Loads a {@link OrgUnitDTO} object on the view.
	 * 
	 * @param orgUnitDTO
	 *            the {@link OrgUnitDTO} object loaded on the view
	 */
	private void loadOrgUnitOnView(OrgUnitDTO orgUnitDTO) {

		currentOrgUnitDTO = orgUnitDTO;
		refreshBanner();
	}

	/**
	 * Refreshes the org unit banner for the current org unit.
	 */
	public void refreshBanner() {

		// Panel.
		final ContentPanel panel = view.getPanelBanner();
		panel.setHeading(currentOrgUnitDTO.getOrgUnitModel().getTitle() + ' ' + currentOrgUnitDTO.getName() + " ("
						+ currentOrgUnitDTO.getFullName() + ")");
		panel.removeAll();

		final Grid gridPanel = new Grid(1, 2);
		gridPanel.addStyleName("banner");
		gridPanel.setCellPadding(0);
		gridPanel.setCellSpacing(0);
		gridPanel.setWidth("100%");
		gridPanel.setHeight("100%");

		// Logo.
		final Image logo = OrgUnitImageBundle.ICONS.orgUnitLarge().createImage();
		gridPanel.setWidget(0, 0, logo);
		gridPanel.getCellFormatter().addStyleName(0, 0, "banner-logo");

		// Banner.
		final OrgUnitBannerDTO banner = currentOrgUnitDTO.getOrgUnitModel().getBanner();
		final LayoutDTO layout = banner.getLayout();

		// Executes layout.
		if (banner != null && layout != null && layout.getLayoutGroupsDTO() != null
						&& !layout.getLayoutGroupsDTO().isEmpty()) {

			// For visibility constraints, the banner accept a maximum of 2 rows
			// and 4 columns.
			final int rows = layout.getRowsCount() > 2 ? 2 : layout.getRowsCount();
			final int cols = layout.getColumnsCount() > 4 ? 4 : layout.getColumnsCount();

			final Grid gridLayout = new Grid(rows, cols);
			gridLayout.addStyleName("banner-flex");
			gridLayout.setCellPadding(0);
			gridLayout.setCellSpacing(0);
			gridLayout.setWidth("100%");
			gridLayout.setHeight("100%");

			for (int i = 0; i < gridLayout.getColumnCount() - 1; i++) {
				gridLayout.getColumnFormatter().setWidth(i, "325px");
			}

			for (final LayoutGroupDTO groupLayout : layout.getLayoutGroupsDTO()) {

				// Checks group bounds.
				if (groupLayout.getRow() + 1 > rows || groupLayout.getColumn() + 1 > cols) {
					continue;
				}

				final ContentPanel groupPanel = new ContentPanel();
				groupPanel.setLayout(new FormLayout());
				groupPanel.setTopComponent(null);
				groupPanel.setHeaderVisible(false);

				gridLayout.setWidget(groupLayout.getRow(), groupLayout.getColumn(), groupPanel);

				if (groupLayout.getLayoutConstraintsDTO() != null) {
					for (final LayoutConstraintDTO constraint : groupLayout.getLayoutConstraintsDTO()) {

						final FlexibleElementDTO element = constraint.getFlexibleElementDTO();

						// Only default elements are allowed.
						if (!(element instanceof DefaultFlexibleElementDTO)) {
							continue;
						}

						// Builds the graphic component
						final DefaultFlexibleElementDTO defaultElement = (DefaultFlexibleElementDTO) element;
						defaultElement.setService(dispatcher);
						defaultElement.setAuthentication(authentication);
						defaultElement.setCache(cache);
						defaultElement.setCurrentContainerDTO(currentOrgUnitDTO);

						final GetValue command = new GetValue(currentOrgUnitDTO.getId(), defaultElement.getId(),
										defaultElement.getEntityName(), null);
						dispatcher.execute(command, null, new AsyncCallback<ValueResult>() {

							@Override
							public void onFailure(Throwable throwable) {
								Log.error("Error, element value not loaded.");
							}

							@Override
							public void onSuccess(ValueResult valueResult) {

								if (Log.isDebugEnabled()) {
									Log.debug("Element value(s) object : " + valueResult);
								}

								final Component component;
								if (defaultElement instanceof BudgetElementDTO) {
									component = defaultElement.getElementComponentInBanner(valueResult);

								} else {
									component = defaultElement.getElementComponentInBanner(null);
								}

								if (component != null) {
									groupPanel.add(component);
								}
								groupPanel.layout();

							}
						});

						// Only one element per cell.
						break;
					}
				}
			}

			gridPanel.setWidget(0, 1, gridLayout);
		}
		// Default banner.
		else {

			panel.setLayout(new FormLayout());

			final LabelField codeField = new LabelField();
			codeField.setReadOnly(true);
			codeField.setFieldLabel(I18N.CONSTANTS.projectName());
			codeField.setLabelSeparator(":");
			codeField.setValue(currentOrgUnitDTO.getName());

			gridPanel.setWidget(0, 1, codeField);
		}

		panel.add(gridPanel);
		panel.layout();
	}

	public OrgUnitDTO getCurrentOrgUnitDTO() {
		return currentOrgUnitDTO;
	}

	public void setCurrentOrgUnitDTO(OrgUnitDTO currentOrgUnitDTO) {
		this.currentOrgUnitDTO = currentOrgUnitDTO;
	}

	@Override
	public String getTabTitle() {
		return I18N.CONSTANTS.orgunit();
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
		NavigationError navigationError = NavigationError.NONE;
		for (SubPresenter subPresenter : presenters) {
			if (subPresenter.hasValueChanged()) {
				navigationError = NavigationError.WORK_NOT_SAVED;
			}
		}

		Listener<MessageBoxEvent> listener = new Listener<MessageBoxEvent>() {

			@Override
			public void handleEvent(MessageBoxEvent be) {
				if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
					for (SubPresenter subPresenter : presenters) {
						subPresenter.forgetAllChangedValues();
					}
					callback.onDecided(NavigationError.NONE);
				}
			}
		};

		if (navigationError == NavigationError.WORK_NOT_SAVED) {
			MessageBox.confirm(I18N.CONSTANTS.unsavedDataTitle(), I18N.CONSTANTS.unsavedDataMessage(), listener);
		}

		callback.onDecided(navigationError);
	}

	@Override
	public String beforeWindowCloses() {
		return null;
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void setActivePage(Page page) {
		this.activePage = page;
	}

	@Override
	public Page getActivePage() {
		return this.activePage;
	}

	public OrgUnitState getCurrentState() {
		return currentState;
	}

	@Override
	public AsyncMonitor showLoadingPlaceHolder(PageId pageId, PageState loadingPlace) {
		return null;
	}
}
