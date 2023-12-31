package org.sigmah.client.page.project.details;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sigmah.client.EventBus;
import org.sigmah.client.cache.UserLocalCache;
import org.sigmah.client.dispatch.Dispatcher;
import org.sigmah.client.dispatch.monitor.MaskingAsyncMonitor;
import org.sigmah.client.dispatch.remote.Authentication;
import org.sigmah.client.event.ProjectEvent;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.page.project.ProjectPresenter;
import org.sigmah.client.page.project.SubPresenter;
import org.sigmah.client.util.Notification;
import org.sigmah.shared.command.GetOrgUnit;
import org.sigmah.shared.command.GetValue;
import org.sigmah.shared.command.UpdateProject;
import org.sigmah.shared.command.result.ValueResult;
import org.sigmah.shared.command.result.ValueResultUtils;
import org.sigmah.shared.command.result.VoidResult;
import org.sigmah.shared.domain.profile.GlobalPermissionEnum;
import org.sigmah.shared.dto.CountryDTO;
import org.sigmah.shared.dto.OrgUnitDTO;
import org.sigmah.shared.dto.ProjectDTO;
import org.sigmah.shared.dto.ProjectDTOLight;
import org.sigmah.shared.dto.ProjectDetailsDTO;
import org.sigmah.shared.dto.ProjectFundingDTO;
import org.sigmah.shared.dto.UserDTO;
import org.sigmah.shared.dto.element.BudgetElementDTO;
import org.sigmah.shared.dto.element.BudgetSubFieldDTO;
import org.sigmah.shared.dto.element.DefaultFlexibleElementDTO;
import org.sigmah.shared.dto.element.FlexibleElementDTO;
import org.sigmah.shared.dto.element.handler.ValueEvent;
import org.sigmah.shared.dto.element.handler.ValueHandler;
import org.sigmah.shared.dto.layout.LayoutConstraintDTO;
import org.sigmah.shared.dto.layout.LayoutDTO;
import org.sigmah.shared.dto.layout.LayoutGroupDTO;
import org.sigmah.shared.dto.profile.ProfileUtils;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;

public class ProjectDetailsPresenter implements SubPresenter {

	/**
	 * Description of the view managed by this presenter.
	 */
	public static abstract class View extends ContentPanel {

		public abstract Button getSaveButton();

		public abstract ContentPanel getMainPanel();
	}

	/**
	 * This presenter view.
	 */
	private View view;

	/**
	 * The dispatcher.
	 */
	private final Dispatcher dispatcher;

	private final EventBus eventBus;

	private final Authentication authentication;

	private final UserLocalCache cache;

	/**
	 * The main project presenter.
	 */
	private final ProjectPresenter projectPresenter;

	/**
	 * List of values changes.
	 */
	private ArrayList<ValueEvent> valueChanges = new ArrayList<ValueEvent>();

	/**
	 * The counter before the main panel is unmasked.
	 */
	private int maskCount;

	/*
	 * Project id to be used for excel export
	 */
	@SuppressWarnings("unused")
	private int projectId;

	public ProjectDetailsPresenter(EventBus eventBus, Dispatcher dispatcher, Authentication authentication,
					ProjectPresenter projectPresenter, UserLocalCache cache) {
		this.eventBus = eventBus;
		this.dispatcher = dispatcher;
		this.projectPresenter = projectPresenter;
		this.authentication = authentication;
		this.cache = cache;
	}

	@Override
	public Component getView() {

		if (view == null) {
			view = new ProjectDetailsView();
			addListeners();
		}

		valueChanges.clear();
		view.getSaveButton().disable();

		load(projectPresenter.getCurrentProjectDTO().getProjectModelDTO().getProjectDetailsDTO());
		projectId = projectPresenter.getCurrentProjectDTO().getId();
		return view;
	}

	@Override
	public void discardView() {
		this.view = null;
	}

	@Override
	public void viewDidAppear() {
		// nothing to do.
	}

	@Override
	public boolean hasValueChanged() {
		return !valueChanges.isEmpty();
	}

	@Override
	public void forgetAllChangedValues() {
		valueChanges.clear();
	}

	/**
	 * Initializes the presenter.
	 */
	private void addListeners() {

		// Save action.
		view.getSaveButton().addListener(Events.Select, new Listener<ButtonEvent>() {

			@Override
			public void handleEvent(ButtonEvent be) {

				view.getSaveButton().disable();

				final UpdateProject updateProject = new UpdateProject(projectPresenter.getCurrentProjectDTO().getId(),
								valueChanges);

				dispatcher.execute(updateProject,
								new MaskingAsyncMonitor(view.getMainPanel(), I18N.CONSTANTS.loading()),
								new AsyncCallback<VoidResult>() {

									@Override
									public void onFailure(Throwable caught) {

										MessageBox.alert(I18N.CONSTANTS.save(), I18N.CONSTANTS.saveError(), null);
									}

									@Override
									public void onSuccess(VoidResult result) {

										Notification.show(I18N.CONSTANTS.infoConfirmation(),
														I18N.CONSTANTS.saveConfirm());

										// Checks if there is any update needed
										// to the
										// local project instance.
										boolean refreshBanner = false;
										ProjectDTO newProject = null;

										for (ValueEvent event : valueChanges) {
											if (event.getSource() instanceof DefaultFlexibleElementDTO) {
												newProject = updateCurrentProject(
																((DefaultFlexibleElementDTO) event.getSource()),
																event.getSingleValue(), event.isProjectCountryChanged());
												projectPresenter.setCurrentProjectDTO(newProject);
												projectPresenter.ReloadProjectOnView(newProject);
												refreshBanner = true;
											}
										}

										valueChanges.clear();

										if (refreshBanner) {
											projectPresenter.refreshBanner();
										}

										// avoid tight coupling with other
										// project events
										eventBus.fireEvent(new ProjectEvent(ProjectEvent.CHANGED, projectPresenter
														.getCurrentProjectDTO().getId()));

										if (newProject != null) {
											load(newProject.getProjectModelDTO().getProjectDetailsDTO());
										}
									}
								});
			}
		});
	}

	/**
	 * Loads the presenter with the project details.
	 * 
	 * @param details
	 *            The details.
	 */
	private void load(ProjectDetailsDTO details) {

		// Clear panel.
		view.getMainPanel().removeAll();

		// Layout.
		final LayoutDTO layout = details.getLayoutDTO();

		// If the element are read only.
		final boolean readOnly = !ProfileUtils.isGranted(authentication, GlobalPermissionEnum.EDIT_PROJECT);

		// Counts elements.
		int count = 0;
		for (final LayoutGroupDTO groupDTO : layout.getLayoutGroupsDTO()) {
			count += groupDTO.getLayoutConstraintsDTO().size();
		}

		// Executes layout.
		if (count != 0) {

			// Masks the main panel.
			mask(count);

			final Grid gridLayout = (Grid) layout.getWidget();

			for (final LayoutGroupDTO groupLayout : layout.getLayoutGroupsDTO()) {

				// Creates the fieldset and positions it.
				final FieldSet formPanel = (FieldSet) groupLayout.getWidget();
				gridLayout.setWidget(groupLayout.getRow(), groupLayout.getColumn(), formPanel);

				// For each constraint in the current layout group.
				if (groupLayout.getLayoutConstraintsDTO() != null) {
					for (final LayoutConstraintDTO constraintDTO : groupLayout.getLayoutConstraintsDTO()) {

						// Gets the element managed by this constraint.
						final FlexibleElementDTO elementDTO = constraintDTO.getFlexibleElementDTO();

						// --
						// -- ELEMENT VALUE
						// --

						// Retrieving the current amendment id
						Integer amendmentId = null;
						if (projectPresenter.getCurrentProjectDTO().getCurrentAmendment() != null)
							amendmentId = projectPresenter.getCurrentProjectDTO().getCurrentAmendment().getId();

						// Remote call to ask for this element value.
						final GetValue command = new GetValue(projectPresenter.getCurrentProjectDTO().getId(),
										elementDTO.getId(), elementDTO.getEntityName(), amendmentId);
						dispatcher.execute(command, null, new AsyncCallback<ValueResult>() {

							@Override
							public void onFailure(Throwable throwable) {
								Log.error("Error, element value not loaded.");
								unmask();
							}

							@Override
							public void onSuccess(ValueResult valueResult) {

								if (Log.isDebugEnabled()) {
									Log.debug("Element value(s) object : " + valueResult);
								}

								// --
								// -- ELEMENT COMPONENT
								// --

								// Configures the flexible element for the
								// current application state before generating
								// its component.
								elementDTO.setService(dispatcher);
								elementDTO.setAuthentication(authentication);
								elementDTO.setEventBus(eventBus);
								elementDTO.setCache(cache);
								elementDTO.setCurrentContainerDTO(projectPresenter.getCurrentProjectDTO());
								elementDTO.assignValue(valueResult);

								// Generates element component (with the value).
								elementDTO.init();
								final Component elementComponent = elementDTO.getElementComponent(valueResult,
												!readOnly && !valueResult.isAmendment());

								// Component width.
								final FormData formData;
								if (elementDTO.getPreferredWidth() == 0) {
									formData = new FormData("100%");
								} else {
									formData = new FormData(elementDTO.getPreferredWidth(), -1);
								}

								if (elementComponent != null) {
									formPanel.add(elementComponent, formData);
								}
								formPanel.layout();

								// --
								// -- ELEMENT HANDLERS
								// --

								// Adds a value change handler to this element.
								elementDTO.addValueHandler(new ValueHandlerImpl());

								unmask();
							}
						});
					}
				}
			}

			view.getMainPanel().add(gridLayout);
		}
		// Default details page.
		else {
			final Label l = new Label(I18N.CONSTANTS.projectDetailsNoDetails());
			l.addStyleName("project-label-10");
			view.getMainPanel().add(l);
		}

		view.layout();
	}

	/**
	 * Mask the main panel and set the mask counter.
	 * 
	 * @param count
	 *            The mask counter.
	 */
	private void mask(int count) {

		if (count <= 0) {
			return;
		}

		maskCount = count;
		view.getMainPanel().mask(I18N.CONSTANTS.loading());
	}

	/**
	 * Decrements the mask counter and unmask the main panel if the counter
	 * reaches <code>0</code>.
	 */
	private void unmask() {
		maskCount--;
		if (maskCount == 0) {
			view.getMainPanel().unmask();
		}
	}

	/**
	 * Updates locally the DTO to avoid a remote server call.
	 * 
	 * @param element
	 *            The default flexible element.
	 * @param value
	 *            The new value.
	 */
	private ProjectDTO updateCurrentProject(DefaultFlexibleElementDTO element, String value,
					boolean isProjectCountryChanged) {

		final ProjectDTO currentProjectDTO = projectPresenter.getCurrentProjectDTO();

		switch (element.getType()) {
		case CODE:
			currentProjectDTO.setName(value);
			break;
		case TITLE:
			currentProjectDTO.setFullName(value);
			break;
		case START_DATE:
			if ("".equals(value)) {
				currentProjectDTO.setStartDate(null);
			} else {
				try {
					final long timestamp = Long.parseLong(value);
					currentProjectDTO.setStartDate(new Date(timestamp));
				} catch (NumberFormatException e) {
					// nothing, invalid date.
				}
			}
			break;
		case END_DATE:
			if ("".equals(value)) {
				currentProjectDTO.setEndDate(null);
			} else {
				try {
					final long timestamp = Long.parseLong(value);
					currentProjectDTO.setEndDate(new Date(timestamp));
				} catch (NumberFormatException e) {
					// nothing, invalid date.
				}
			}
			break;
		case BUDGET:
			try {
				BudgetElementDTO budgetElement = (BudgetElementDTO) element;
				final Map<Integer, String> values = ValueResultUtils.splitMapElements(value);

				double plannedBudget = 0.0;
				double spendBudget = 0.0;
				double receivedBudget = 0.0;

				for (BudgetSubFieldDTO bf : budgetElement.getBudgetSubFieldsDTO()) {
					if (bf.getType() != null) {
						switch (bf.getType()) {
						case PLANNED:
							plannedBudget = Double.parseDouble(values.get(bf.getId()));
							break;
						case RECEIVED:
							receivedBudget = Double.parseDouble(values.get(bf.getId()));
							break;
						case SPENT:
							spendBudget = Double.parseDouble(values.get(bf.getId()));
							break;
						default:
							break;

						}

					}
				}

				currentProjectDTO.setPlannedBudget(plannedBudget);
				currentProjectDTO.setSpendBudget(spendBudget);
				currentProjectDTO.setReceivedBudget(receivedBudget);

				/**
				 * Update funding projects - Reflect to funded project in
				 * funding projects
				 * 
				 * currentProjectDTO |-- getFunding() --> <ProjectFundingDTO> //
				 * list of funding projects |-- getPercentage() // no updates
				 * from here |-- getFunded() --> ProjectDTOLight // funded
				 * project details light |--getPlannedBudget() // update budget
				 * details
				 */
				List<ProjectFundingDTO> fundingProjects = currentProjectDTO.getFunding();
				if (fundingProjects != null && !fundingProjects.isEmpty()) {
					for (ProjectFundingDTO projectFundingDTO : fundingProjects) {
						ProjectDTOLight fundedProject = projectFundingDTO.getFunded();
						if (fundedProject != null && fundedProject.getId() == currentProjectDTO.getId()) {
							fundedProject.setPlannedBudget(plannedBudget);
							fundedProject.setSpendBudget(spendBudget);
							fundedProject.setReceivedBudget(receivedBudget);
						}
					}
				}
			} catch (Exception e) {
				// nothing, invalid budget.
			}
			break;
		case COUNTRY:
			final CountryDTO country = element.getCountriesStore().findModel("id", Integer.parseInt(value));
			if (country != null) {
				currentProjectDTO.setCountry(country);
			} else {
				// nothing, invalid country.
			}
			break;
		case OWNER:
			// The owner component doesn't fire any event for now.
			break;
		case MANAGER:
			final UserDTO manager = element.getManagersStore().findModel("id", Integer.parseInt(value));
			if (manager != null) {
				currentProjectDTO.setManager(manager);
			} else {
				// nothing, invalid user.
			}
			break;
		case ORG_UNIT:
			currentProjectDTO.setOrgUnit(Integer.parseInt(value));
			if (isProjectCountryChanged == true) {
				GetOrgUnit getOrgUnitCmd = new GetOrgUnit(currentProjectDTO.getOrgUnitId());
				dispatcher.execute(getOrgUnitCmd, null, new AsyncCallback<OrgUnitDTO>() {

					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSuccess(OrgUnitDTO result) {

						if (result != null) {
							currentProjectDTO.setCountry(result.getCountry());
						}

					}

				});
			}

			break;
		default:
			// Nothing, unknown type.
			break;
		}

		return currentProjectDTO;
	}

	/**
	 * Internal class handling the value changes of the flexible elements.
	 */
	private class ValueHandlerImpl implements ValueHandler {

		@Override
		public void onValueChange(ValueEvent event) {

			// Stores the change to be saved later.
			valueChanges.add(event);

			if (!projectPresenter.getCurrentDisplayedPhaseDTO().isEnded()) {

				// Enables the save action.
				view.getSaveButton().enable();
			}
		}
	}
}
