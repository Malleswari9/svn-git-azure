package org.sigmah.client.page.project.logframe;

import org.sigmah.client.EventBus;
import org.sigmah.client.dispatch.Dispatcher;
import org.sigmah.client.dispatch.monitor.MaskingAsyncMonitor;
import org.sigmah.client.dispatch.remote.Authentication;
import org.sigmah.client.event.IndicatorEvent;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.page.common.dialog.FormDialogCallback;
import org.sigmah.client.page.project.ProjectPresenter;
import org.sigmah.client.page.project.SubPresenter;
import org.sigmah.client.ui.ExportSpreadsheetFormButton;
import org.sigmah.client.util.Notification;
import org.sigmah.shared.command.CopyLogFrame;
import org.sigmah.shared.command.UpdateLogFrame;
import org.sigmah.shared.command.result.LogFrameResult;
import org.sigmah.shared.domain.Amendment;
import org.sigmah.shared.domain.logframe.IndicatorCopyStrategy;
import org.sigmah.shared.domain.profile.GlobalPermissionEnum;
import org.sigmah.shared.dto.ExportUtils;
import org.sigmah.shared.dto.ProjectDTO;
import org.sigmah.shared.dto.logframe.LogFrameDTO;
import org.sigmah.shared.dto.profile.ProfileUtils;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Presenter for the project log frame.
 * 
 * @author tmi
 */
public class ProjectLogFramePresenter implements SubPresenter {

	/**
	 * The label used for the no-name groups.
	 */
	public static final String DEFAULT_GROUP_LABEL = "-";

	/**
	 * Description of the view managed by this presenter.
	 */
	public static abstract class View extends ContentPanel {

		public abstract ProjectLogFrameGrid getLogFrameGrid();

		public abstract Button getSaveButton();

		public abstract Button getCopyButton();

		public abstract Button getPasteButton();

		public abstract ExportSpreadsheetFormButton getExcelExportFormButton();

		public abstract Label getLogFrameTitleContentLabel();

		public abstract TextField<String> getLogFrameMainObjectiveTextBox();
	}

	private static Integer logFrameIdCopySource;

	/**
	 * This presenter view.
	 */
	private View view;

	/**
	 * The dispatcher.
	 */
	private final Dispatcher dispatcher;

	private final EventBus eventBus;

	/**
	 * The authentication.
	 */
	private final Authentication authentication;

	private boolean logFrameUpdated;

	/**
	 * The main project presenter.
	 */
	private final ProjectPresenter projectPresenter;

	public ProjectDTO getCurrentProjectDTO() {
		return currentProjectDTO;
	}

	public void setCurrentProjectDTO(ProjectDTO currentProjectDTO) {
		this.currentProjectDTO = currentProjectDTO;
	}

	/**
	 * The current displayed project.
	 */
	private ProjectDTO currentProjectDTO;

	/**
	 * The displayed log frame.
	 */
	private LogFrameDTO logFrame;

	public ProjectLogFramePresenter(EventBus eventBus, Dispatcher dispatcher, Authentication authentication,
	                ProjectPresenter projectPresenter) {
		this.eventBus = eventBus;
		this.dispatcher = dispatcher;
		this.authentication = authentication;
		this.projectPresenter = projectPresenter;
		this.currentProjectDTO = projectPresenter.getCurrentProjectDTO();
		this.logFrameUpdated = false;

	}

	@Override
	public Component getView() {

		if (view == null) {
			view = new ProjectLogFrameView(eventBus, dispatcher);
			if (projectPresenter.getCurrentProjectDTO().getCurrentAmendment() == null)
				logFrame = projectPresenter.getCurrentProjectDTO().getLogFrameDTO();
			else
				logFrame = projectPresenter.getCurrentProjectDTO().getCurrentAmendment().getLogFrameDTO();

			currentProjectDTO = projectPresenter.getCurrentProjectDTO();
			fillAndInit();
			addListeners();
		}

		// If the current project has changed, clear the view
		if (projectPresenter.getCurrentProjectDTO() != currentProjectDTO) {
			if (projectPresenter.getCurrentProjectDTO().getCurrentAmendment() == null)
				logFrame = projectPresenter.getCurrentProjectDTO().getLogFrameDTO();
			else
				logFrame = projectPresenter.getCurrentProjectDTO().getCurrentAmendment().getLogFrameDTO();

			currentProjectDTO = projectPresenter.getCurrentProjectDTO();
			fillAndInit();
		}

		return view;
	}

	@Override
	public void discardView() {
		this.view = null;
	}

	@Override
	public void viewDidAppear() {

		// Make sure when the currentProjectDTO's title is changed, reset the
		// log title's value
		view.getLogFrameTitleContentLabel().setText(projectPresenter.getCurrentProjectDTO().getFullName());

	}

	@Override
	public boolean hasValueChanged() {
		return logFrameUpdated;
	}

	@Override
	public void forgetAllChangedValues() {
		logFrameUpdated = false;
	}

	/**
	 * Initializes the presenter.
	 */
	private void addListeners() {

		// Enable the save button when the log frame is edited.
		view.getLogFrameGrid().addListener(new ProjectLogFrameGrid.LogFrameGridListener() {

			@Override
			public void logFrameEdited() {
				logFrameUpdated = true;
				view.getSaveButton().setEnabled(true);
			}
		});

		// Log frame main objective box listener.
		view.getLogFrameMainObjectiveTextBox().addListener(Events.OnKeyUp, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				if (logFrame != null) {
					logFrame.setMainObjective(view.getLogFrameMainObjectiveTextBox().getValue());
					logFrameUpdated = true;
					view.getSaveButton().setEnabled(true);
				}
			}
		});

		// Save action.
		view.getSaveButton().addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {

				// Logs the modified log frame.
				if (Log.isDebugEnabled()) {
					Log.debug("[handleEvent] Merges the log frame : \n" + logFrame.toString());
				}

				// Sends the merge action to the server.
				dispatcher.execute(new UpdateLogFrame(logFrame, currentProjectDTO.getId()), new MaskingAsyncMonitor(
				                view, I18N.CONSTANTS.loading()), new AsyncCallback<LogFrameResult>() {

					@Override
					public void onFailure(Throwable e) {

						// Informs of the error.
						Log.error("[execute] Error when saving the log frame.", e);
						MessageBox.alert(I18N.CONSTANTS.save(), I18N.CONSTANTS.saveError(), null);
					}

					@Override
					public void onSuccess(LogFrameResult r) {
						if (Log.isDebugEnabled()) {
							Log.debug("[execute] Log frame successfully saved.");
						}

						// Updates local entities with the new returned log
						// frame (to get the generated ids).
						final LogFrameDTO updated = r.getLogFrame();
						currentProjectDTO.setLogFrameDTO(updated);
						view.getLogFrameGrid().updateLogFrame(updated);
						logFrame = updated;

						// Informs of the success.
						Notification.show(I18N.CONSTANTS.infoConfirmation(), I18N.CONSTANTS.saveConfirm());
						logFrameUpdated = false;
						view.getSaveButton().setEnabled(false);

						// broadcast an indicator change event to be safe
						eventBus.fireEvent(new IndicatorEvent(IndicatorEvent.CHANGED, ProjectLogFramePresenter.this));
					}
				});
			}
		});

		// Copy action.
		view.getCopyButton().addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				logFrameIdCopySource = logFrame.getId();
				view.getPasteButton().setEnabled(isEditable() && currentProjectDTO.getCurrentAmendment() == null);

				Notification.show(I18N.CONSTANTS.copy(), I18N.CONSTANTS.logFrameCopied());
			}
		});

		// Paste action.
		view.getPasteButton().addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				final ConfirmPasteDialog dialog = new ConfirmPasteDialog();
				dialog.show(new FormDialogCallback() {

					@Override
					public void onValidated() {
						final CopyLogFrame copyLogFrame = CopyLogFrame
						                .from(logFrameIdCopySource)
						                .to(currentProjectDTO)
						                .with(dialog.isLinkIndicatorsChecked() ? IndicatorCopyStrategy.DUPLICATE_AND_LINK
						                                : IndicatorCopyStrategy.DUPLICATE);

						dispatcher.execute(copyLogFrame, null, new AsyncCallback<LogFrameDTO>() {

							@Override
							public void onFailure(Throwable caught) {
								MessageBox.alert(I18N.CONSTANTS.paste(), I18N.CONSTANTS.logFramePasteError(), null);
							}

							@Override
							public void onSuccess(LogFrameDTO result) {
								logFrame = result;
								currentProjectDTO.setLogFrameDTO(result);

								fillAndInit();
								Notification.show(I18N.CONSTANTS.paste(), I18N.CONSTANTS.logFramePasted());
							}

						});
					}

				});

			}
		});

		// Excel action.
		view.getExcelExportFormButton().getButton().addListener(Events.OnClick, new Listener<ButtonEvent>() {

			@Override
			public void handleEvent(ButtonEvent be) {
				view.getExcelExportFormButton().getFieldMap()
				                .put(ExportUtils.PARAM_EXPORT_TYPE, ExportUtils.ExportType.PROJECT_LOG_FRAME.name());
				view.getExcelExportFormButton().getFieldMap()
				                .put(ExportUtils.PARAM_EXPORT_PROJECT_ID, String.valueOf(currentProjectDTO.getId()));
				view.getExcelExportFormButton().triggerExport();
			}
		});

	}

	private boolean isEditable() {
		return logFrame != null && currentProjectDTO.getAmendmentState() == Amendment.State.DRAFT
		                && currentProjectDTO.getCurrentAmendment() == null
		                && ProfileUtils.isGranted(authentication, GlobalPermissionEnum.EDIT_PROJECT)
		                && ProfileUtils.isGranted(authentication, GlobalPermissionEnum.EDIT_LOGFRAME);
	}

	/**
	 * Fills the view with the current log frame and initializes buttons state.
	 */
	private void fillAndInit() {

		// Fill the log frame title with the project's title
		view.getLogFrameTitleContentLabel().setText(currentProjectDTO.getFullName());

		if (logFrame != null) {
			// Fill the log frame main objective.
			view.getLogFrameMainObjectiveTextBox().setValue(logFrame.getMainObjective());

			final boolean editable = isEditable();

			if (!editable) {
				view.getLogFrameMainObjectiveTextBox().setEnabled(false);
			}

			// Fill the grid.
			view.getLogFrameGrid().displayLogFrame(currentProjectDTO.getId(), logFrame, editable);
		}

		// Default buttons states.
		view.getSaveButton().setEnabled(false);
		view.getCopyButton().setEnabled(true);
		view.getPasteButton()
		                .setEnabled(isEditable() && logFrameIdCopySource != null
		                                && currentProjectDTO.getCurrentAmendment() == null);
	}

}
