/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.server.policy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sigmah.client.page.project.logframe.ProjectLogFramePresenter;
import org.sigmah.server.dao.Transactional;
import org.sigmah.shared.command.result.ValueResultUtils;
import org.sigmah.shared.domain.Amendment;
import org.sigmah.shared.domain.Country;
import org.sigmah.shared.domain.OrgUnit;
import org.sigmah.shared.domain.Phase;
import org.sigmah.shared.domain.PhaseModel;
import org.sigmah.shared.domain.Project;
import org.sigmah.shared.domain.ProjectFunding;
import org.sigmah.shared.domain.ProjectModel;
import org.sigmah.shared.domain.ProjectModelStatus;
import org.sigmah.shared.domain.User;
import org.sigmah.shared.domain.UserDatabase;
import org.sigmah.shared.domain.calendar.PersonalCalendar;
import org.sigmah.shared.domain.element.BudgetElement;
import org.sigmah.shared.domain.element.BudgetSubField;
import org.sigmah.shared.domain.element.BudgetSubFieldType;
import org.sigmah.shared.domain.layout.LayoutConstraint;
import org.sigmah.shared.domain.layout.LayoutGroup;
import org.sigmah.shared.domain.logframe.LogFrame;
import org.sigmah.shared.domain.logframe.LogFrameGroup;
import org.sigmah.shared.domain.logframe.LogFrameGroupType;
import org.sigmah.shared.domain.logframe.LogFrameModel;
import org.sigmah.shared.domain.reminder.MonitoredPointList;
import org.sigmah.shared.domain.reminder.ReminderList;
import org.sigmah.shared.domain.value.Value;
import org.sigmah.shared.dto.element.BudgetSubFieldDTO;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class ProjectPolicy implements EntityPolicy<Project> {

	private static final Log log = LogFactory.getLog(ProjectPolicy.class);

	private final EntityManager em;
	private final Injector injector;

	@Inject
	public ProjectPolicy(EntityManager em, Injector injector) {
		this.em = em;
		this.injector = injector;
	}

	@Override
	public Object create(User user, PropertyMap properties) {
		Project project = createProject(properties, user);
		return project;
	}

	@Transactional
	protected Project createProject(PropertyMap properties, User user) {

		if (log.isDebugEnabled()) {
			log.debug("[createProject] Starting project creation.");
		}

		// Creates a new calendar
		PersonalCalendar calendar = new PersonalCalendar();
		calendar.setName(properties.<String> get("calendarName"));
		em.persist(calendar);

		// Creates the new project
		Project project = new Project();

		// Userdatabase attributes.
		project.setStartDate(new Date());
		final User owner = em.getReference(User.class, user.getId());
		project.setOwner(owner);

		// Manager.
		// The default manager is the owner of the project.
		project.setManager(owner);

		// Monitored points.
		project.setPointsList(new MonitoredPointList());

		// Reminders.
		project.setRemindersList(new ReminderList());
		OrgUnit orgunit = null;
		// No organizational unit for the testProjects
		if (properties.get("orgUnitId") != null) {
			// Org unit.
			int orgUnitId = Integer.parseInt("" + properties.get("orgUnitId"));
			orgunit = em.find(OrgUnit.class, orgUnitId);
			project.getPartners().add(orgunit);
		}

		// Country
		final Country country = getProjectCountry(orgunit);
		project.setCountry(country);

		// Amendment
		project.setAmendmentState(Amendment.State.DRAFT);
		project.setAmendmentVersion(1);
		project.setAmendmentRevision(1);

		if (log.isDebugEnabled()) {
			log.debug("[createProject] Selected country: " + country.getName() + ".");
		}

		// Considers name length constraints.
		final String name = properties.<String> get("name");
		if (name != null) {
			if (name.length() > 50) {
				project.setName(name.substring(0, 50));
			} else {
				project.setName(name);
			}
		} else {
			project.setName("noname");
		}

		// Considers name length constraints.
		final String fullName = properties.<String> get("fullName");
		if (fullName != null) {
			if (fullName.length() > 500) {
				project.setFullName(fullName.substring(0, 500));
			} else {
				project.setFullName(fullName);
			}
		} else {
			project.setFullName("");
		}

		project.setLastSchemaUpdate(new Date());
		project.setCalendarId(calendar.getId());

		// Project attributes.
		ProjectModel model = em.getReference(ProjectModel.class, properties.<Long> get("modelId"));
		if (ProjectModelStatus.READY.equals(model.getStatus())) {
			model.setStatus(ProjectModelStatus.USED);
		}
		model = em.merge(model);
		project.setProjectModel(model);
		project.setLogFrame(null);

		// Creates and adds phases.
		for (final PhaseModel phaseModel : model.getPhases()) {

			final Phase phase = new Phase();
			phase.setModel(phaseModel);

			project.addPhase(phase);

			if (log.isDebugEnabled()) {
				log.debug("[createProject] Creates and adds phase instance for model: " + phaseModel.getName() + ".");
			}

			// Searches the root phase.
			if (model.getRootPhase() != null && phaseModel.getId() == model.getRootPhase().getId()) {

				// Sets it.
				phase.setStartDate(new Date());
				project.setCurrentPhase(phase);

				if (log.isDebugEnabled()) {
					log.debug("[createProject] Sets the first phase: " + phaseModel.getName() + ".");
				}
			}
		}

		// The model doesn't define a root phase, select the first declared
		// phase as the first one.
		if (model.getRootPhase() == null) {

			if (log.isDebugEnabled()) {
				log.debug("[createProject] No root phase defined for this model, active the first phase by default.");
			}

			// Selects the first phase by default.
			final Phase phase = project.getPhases().get(0);

			// Sets it.
			phase.setStartDate(new Date());
			project.setCurrentPhase(phase);

			if (log.isDebugEnabled()) {
				log.debug("[createProject] Sets the first phase: " + phase.getModel().getName() + ".");
			}
		}

		em.persist(project);

		if (log.isDebugEnabled()) {
			log.debug("[createProject] Project successfully created.");
		}

		// Updates the project with a default log frame.
		final LogFrame logFrame = createDefaultLogFrame(project);
		project.setLogFrame(logFrame);
		final Double budgetPlanned = properties.<Double> get("budget");
		Map<BudgetSubFieldDTO, Double> budgetValues = new HashMap<BudgetSubFieldDTO, Double>();
		if (budgetPlanned != null) {
			BudgetElement budgetElement = getBudgetElement(model);
			if (budgetElement != null) {
				for (BudgetSubField budgetSubField : budgetElement.getBudgetSubFields()) {
					BudgetSubFieldDTO budgetSubFieldDTO = new BudgetSubFieldDTO();
					budgetSubFieldDTO.setId(budgetSubField.getId().intValue());
					if (budgetSubField.getType() != null & BudgetSubFieldType.PLANNED.equals(budgetSubField.getType())) {
						budgetValues.put(budgetSubFieldDTO, budgetPlanned);
					} else {
						budgetValues.put(budgetSubFieldDTO, 0.0);
					}
				}
				Value value = new Value();
				value.setContainerId(project.getId());
				value.setElement(budgetElement);
				value.setLastModificationAction('C');
				value.setLastModificationDate(new Date());
				value.setLastModificationUser(user);
				value.setValue(ValueResultUtils.mergeElements(budgetValues));
				em.persist(value);
			}
			
		}
		project = em.merge(project);

		return project;
	}

	/**
	 * Creates a well-configured default log frame for the new project.
	 * 
	 * @param project
	 *            The project.
	 * @return The log frame.
	 */
	private LogFrame createDefaultLogFrame(Project project) {

		// Creates a new log frame (with a default model)
		final LogFrame logFrame = new LogFrame();
		logFrame.setParentProject(project);

		// Default groups.
		final ArrayList<LogFrameGroup> groups = new ArrayList<LogFrameGroup>();

		LogFrameGroup group = new LogFrameGroup();
		group.setType(LogFrameGroupType.SPECIFIC_OBJECTIVE);
		group.setParentLogFrame(logFrame);
		group.setLabel(ProjectLogFramePresenter.DEFAULT_GROUP_LABEL);
		groups.add(group);

		group = new LogFrameGroup();
		group.setType(LogFrameGroupType.EXPECTED_RESULT);
		group.setParentLogFrame(logFrame);
		group.setLabel(ProjectLogFramePresenter.DEFAULT_GROUP_LABEL);
		groups.add(group);

		group = new LogFrameGroup();
		group.setType(LogFrameGroupType.ACTIVITY);
		group.setParentLogFrame(logFrame);
		group.setLabel(ProjectLogFramePresenter.DEFAULT_GROUP_LABEL);
		groups.add(group);

		group = new LogFrameGroup();
		group.setType(LogFrameGroupType.PREREQUISITE);
		group.setParentLogFrame(logFrame);
		group.setLabel(ProjectLogFramePresenter.DEFAULT_GROUP_LABEL);
		groups.add(group);

		logFrame.setGroups(groups);

		// Links to the log frame model.
		ProjectModel projectModel = project.getProjectModel();
		LogFrameModel logFrameModel = projectModel.getLogFrameModel();

		if (logFrameModel == null) {
			logFrameModel = ProjectModelPolicy.createDefaultLogFrameModel(projectModel);
			logFrameModel.setName("Auto-created default model at " + new Date());
			em.persist(logFrameModel);

			projectModel.setLogFrameModel(logFrameModel);
			em.merge(projectModel);
		}

		logFrame.setLogFrameModel(logFrameModel);

		em.persist(logFrame);

		return logFrame;
	}

	/**
	 * Searches the country for the given org unit.
	 * 
	 * @param orgUnit
	 *            The org unit.
	 * @return The country
	 */
	private Country getProjectCountry(OrgUnit orgUnit) {

		if (orgUnit == null) {
			return getDefaultCountry();
		}

		Country country = null;
		OrgUnit current = orgUnit;

		while (country == null || current != null) {

			if ((country = current.getOfficeLocationCountry()) != null) {
				return country;
			} else {
				current = current.getParent();
			}

			// Root reached
			if (current == null) {
				break;
			}
		}

		return getDefaultCountry();
	}

	/**
	 * Gets the default country for all the application.
	 * 
	 * @return The default country.
	 */
	private Country getDefaultCountry() {

		final Query q = em.createQuery("SELECT c FROM Country c WHERE c.name = :defaultName");
		// FIXME France by default
		q.setParameter("defaultName", "France");

		try {
			return (Country) q.getSingleResult();
		} catch (NoResultException e) {

			try {
				return (Country) em.createQuery("SELECT c FROM Country c").getResultList().get(0);
			} catch (Throwable e2) {
				throw new IllegalStateException("There is no country in database, unable to create a project.", e2);
			}
		}
	}

	private BudgetElement getBudgetElement(ProjectModel model) {
		BudgetElement budgetElement = null;
		if (model.getProjectBanner().getLayout() != null) {
			for (LayoutGroup lg : model.getProjectBanner().getLayout().getGroups()) {
				for (LayoutConstraint lc : lg.getConstraints()) {
					if (lc.getElement() instanceof BudgetElement) {
						budgetElement = (BudgetElement) lc.getElement();
					}
				}
			}
		}

		if (model.getProjectDetails().getLayout() != null) {
			for (LayoutGroup lg : model.getProjectDetails().getLayout().getGroups()) {
				for (LayoutConstraint lc : lg.getConstraints()) {
					if (lc.getElement() instanceof BudgetElement) {
						budgetElement = (BudgetElement) lc.getElement();
					}
				}
			}
		}

		for (PhaseModel phase : model.getPhases()) {
			for (LayoutGroup lg : phase.getLayout().getGroups()) {
				for (LayoutConstraint lc : lg.getConstraints()) {
					if (lc.getElement() instanceof BudgetElement) {
						budgetElement = (BudgetElement) lc.getElement();
					}
				}
			}
		}
		return budgetElement;
	}

	@Override
	public void update(User user, Object entityId, PropertyMap changes) {

		for (Map.Entry<String, Object> entry : changes.entrySet()) {

			if ("fundingId".equals(entry.getKey())) {

				// Get the current project
				Project project = em.find(Project.class, entityId);

				// Get the project funding relation entity object
				ProjectFunding projectFunding = em.find(ProjectFunding.class, entry.getValue());

				// Remove it from the current project
				project.getFunding().remove(projectFunding);

				// Save
				em.merge(project);
				em.remove(projectFunding);

			}

			else if ("fundedId".equals(entry.getKey())) {
				// Get the current project
				Project project = em.find(Project.class, entityId);

				// Get the project funding relation entity object
				ProjectFunding projectFunding = em.find(ProjectFunding.class, entry.getValue());

				// Remove it from the current project
				project.getFunded().remove(projectFunding);

				// Save
				em.merge(project);
				em.remove(projectFunding);

			} else if ("dateDeleted".equals(entry.getKey())) {
				// Get the current project
				UserDatabase project = em.find(UserDatabase.class, entityId);

				// Mark the project in the state "deleted" (but don't delete it
				// really)
				project.delete();

				final List<ProjectFunding> listfundingsToDelete = new ArrayList<ProjectFunding>();

				// Saves all the projectFundings that need to be deleted
				// before deleting them from the deleted project
				if (project instanceof Project) {
					Project pr = (Project) project;

					listfundingsToDelete.addAll(pr.getFunded());
					listfundingsToDelete.addAll(pr.getFunding());

					((Project) project).getFunded().clear();
					((Project) project).getFunding().clear();
				}

				// Save
				em.merge(project);

				for (ProjectFunding pf : listfundingsToDelete) {
					em.remove(pf);
				}
				/*
				 * [UserPermission trigger] Deletes related entries in
				 * UserPermission table after project deleted
				 */
				UserPermissionPolicy policy = injector.getInstance(UserPermissionPolicy.class);
				policy.deleteUserPemissionByProject((Integer) entityId);
			}

		}

	}
}
