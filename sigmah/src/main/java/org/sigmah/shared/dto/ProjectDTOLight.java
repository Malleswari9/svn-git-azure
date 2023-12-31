package org.sigmah.shared.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sigmah.client.page.project.dashboard.funding.FundingIconProvider;
import org.sigmah.client.page.project.dashboard.funding.FundingIconProvider.IconSize;
import org.sigmah.client.util.DateUtils;
import org.sigmah.client.util.NumberUtils;
import org.sigmah.shared.domain.ProjectModelType;
import org.sigmah.shared.domain.element.BudgetSubFieldType;
import org.sigmah.shared.dto.category.CategoryElementDTO;

import com.extjs.gxt.ui.client.data.ChangeEvent;
import com.extjs.gxt.ui.client.data.ChangeEventSupport;
import com.extjs.gxt.ui.client.data.ChangeListener;
import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.user.client.ui.Image;

/**
 * Light DTO mapping class for entity Project.
 * 
 * @author tmi
 * 
 */
public class ProjectDTOLight extends DeRpcSafeBaseTreeModel implements EntityDTO {

	private static final long serialVersionUID = -4898072895587927460L;

	public ProjectDTOLight() {
		changeEventSupport = new ChangeEventSupport();
	}

	@Override
	public void addChangeListener(ChangeListener... listener) {
		// Fix for the Mac JVM
		if (changeEventSupport == null)
			changeEventSupport = new ChangeEventSupport();

		changeEventSupport.addChangeListener(listener);
	}

	@Override
	public void notify(ChangeEvent evt) {
		// Fix for the Mac JVM
		if (changeEventSupport == null)
			changeEventSupport = new ChangeEventSupport();

		changeEventSupport.notify(evt);
	}

	@Override
	public String getEntityName() {
		return "Project";
	}

	// Project id
	@Override
	public int getId() {
		return (Integer) get("id");
	}

	public void setId(int id) {
		set("id", id);
	}

	public int getProjectId() {
		return (Integer) get("pid");
	}

	public void setProjectId(int id) {
		set("pid", id);
	}

	// Project name
	public String getName() {
		return get("name");
	}

	public void setName(String name) {
		set("name", name);
	}

	// Project full name
	public String getFullName() {
		return get("fullName");
	}

	public void setFullName(String fullName) {
		set("fullName", fullName);
	}

	// Complete name
	public String getCompleteName() {
		return get("completeName");
	}

	public void setCompleteName(String completeName) {
		set("completeName", completeName);
	}

	public void generateCompleteName() {
		setCompleteName(getName() + " - " + getFullName());
	}

	// Reference to the current phase
	public String getCurrentPhaseName() {
		return get("currentPhaseName");
	}

	public void setCurrentPhaseName(String currentPhaseName) {
		set("currentPhaseName", currentPhaseName);
	}

	// Project visibilities
	public List<ProjectModelVisibilityDTO> getVisibilities() {
		return get("visibilities");
	}

	public void setVisibilities(List<ProjectModelVisibilityDTO> visibilities) {
		set("visibilities", visibilities);
	}

	/**
	 * Gets the type of this model for the given organization. If this model
	 * isn't visible for this organization, <code>null</code> is returned.
	 * 
	 * @param organizationId
	 *            The organization.
	 * @return The type of this model for the given organization,
	 *         <code>null</code> otherwise.
	 */
	public ProjectModelType getVisibility(int organizationId) {

		if (getVisibilities() == null) {
			return null;
		}

		for (final ProjectModelVisibilityDTO visibility : getVisibilities()) {
			if (visibility.getOrganizationId() == organizationId) {
				return visibility.getType();
			}
		}

		return null;
	}

	// Budget
	public Double getPlannedBudget() {
		final Double b = (Double) get("plannedBudget");
		return b != null ? b : 0.0;
	}

	public void setPlannedBudget(Double plannedBudget) {
		set("plannedBudget", plannedBudget);
	}

	public Double getSpendBudget() {
		final Double b = (Double) get("spendBudget");
		return b != null ? b : 0.0;
	}

	public void setSpendBudget(Double spendBudget) {
		set("spendBudget", spendBudget);
	}

	public Double getReceivedBudget() {
		final Double b = (Double) get("receivedBudget");
		return b != null ? b : 0.0;
	}

	public void setReceivedBudget(Double receivedBudget) {
		set("receivedBudget", receivedBudget);
	}

	// Activities advancement
	public Integer getActivityAdvancement() {
		return get("activityAdvancement");
	}

	public void setActivityAdvancement(Integer activityAdvancement) {
		set("activityAdvancement", activityAdvancement);
	}

	// Org Unit
	public String getOrgUnitName() {
		return get("orgUnitName");
	}

	public void setOrgUnitName(String orgUnitName) {
		set("orgUnitName", orgUnitName);
	}

	// Project start date
	public Date getStartDate() {
		return get("startDate");
	}

	public void setStartDate(Date startDate) {
		set("startDate", startDate);
	}

	// Project end date
	public Date getEndDate() {
		return get("endDate");
	}

	public void setEndDate(Date endDate) {
		set("endDate", endDate);
	}

	// Closed date.
	public void setCloseDate(Date closeDate) {
		set("closeDate", closeDate);
	}

	public Date getCloseDate() {
		return get("closeDate");
	}

	public boolean isClosed() {
		return getCloseDate() != null;
	}

	// Categories.
	public Set<CategoryElementDTO> getCategoryElements() {
		return get("categoryElements");
	}

	public void setCategoryElements(Set<CategoryElementDTO> categoryElements) {
		set("categoryElements", categoryElements);
	}

	// Children (projects funded by this project)
	public List<ProjectDTOLight> getChildrenProjects() {
		return get("childrenProjects");
	}

	public void setChildrenProjects(List<ProjectDTOLight> childrenProjects) {

		for (final ProjectDTOLight child : childrenProjects) {
			child.setParent(this);
		}

		// Base tree model.
		final ArrayList<ModelData> children = new ArrayList<ModelData>(childrenProjects);
		setChildren(children);

		set("childrenProjects", childrenProjects);
	}

	/**
	 * Gets the type of this model for the given organization. If this model
	 * isn't visible for this organization, <code>null</code> is returned.
	 * 
	 * @param organizationId
	 *            The organization id.
	 * @return The type of this model for the given organization,
	 *         <code>null</code> otherwise.
	 */
	public ProjectModelType getProjectModelType(int organizationId) {

		if (getVisibilities() == null) {
			return null;
		}

		for (final ProjectModelVisibilityDTO visibility : getVisibilities()) {
			if (visibility.getOrganizationId() == organizationId) {
				return visibility.getType();
			}
		}

		return null;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof ProjectDTOLight)) {
			return false;
		}

		final ProjectDTOLight other = (ProjectDTOLight) obj;

		return getId() == other.getId();
	}

	/**
	 * Gets the percentage of the elapsed time for the given project.
	 * 
	 * @param model
	 *            The project.
	 * @return The percentage of the elapsed time.
	 */
	@SuppressWarnings("deprecation")
	public double getElapsedTime() {

		final double ratio;
		final Date start = getStartDate();
		final Date end = getEndDate();
		final Date close = getCloseDate();
		final Date today = new Date();
		final Date comparison;

		if (isClosed()) {
			comparison = new Date(close.getYear(), close.getMonth(), close.getDate());
		} else {
			comparison = new Date(today.getYear(), today.getMonth(), today.getDate());
		}

		// No end date
		if (end == null) {
			ratio = 0d;
		}
		// No start date but with a end date.
		else if (start == null) {

			if (DateUtils.DAY_COMPARATOR.compare(comparison, end) < 0) {
				ratio = 0d;
			} else {
				ratio = 100d;
			}
		}
		// Start date and end date.
		else {

			// The start date is after the end date -> 100%.
			if (DateUtils.DAY_COMPARATOR.compare(start, end) >= 0) {
				ratio = 100d;
			}
			// The start date is after today -> 0%.
			else if (DateUtils.DAY_COMPARATOR.compare(comparison, start) <= 0) {
				ratio = 0d;
			}
			// The start date is before the end date -> x%.
			else {
				final Date sd = new Date(start.getYear(), start.getMonth(), start.getDate());
				final Date ed = new Date(end.getYear(), end.getMonth(), end.getDate());
				final double elapsedTime = comparison.getTime() - sd.getTime();
				final double estimatedTime = ed.getTime() - sd.getTime();
				ratio = NumberUtils.ratio(elapsedTime, estimatedTime);
			}
		}

		return NumberUtils.adjustRatio(ratio);
	}

	// Users who choose this project for their favorite project
	public Set<UserDTO> getFavoriteUsers() {
		return get("favoriteUsers");
	}

	public void setFavoriteUsers(Set<UserDTO> favoriteUsers) {
		set("favoriteUsers", favoriteUsers);
	}

	public String getCountryName() {
		return get("countryName");
	}

	public void setCountryName(String countryName) {
		set("countryName", countryName);
	}

	public void setTypeIconHtml(String typeHtmlIcon) {
		set("typeIconHtml", typeHtmlIcon);
	}

	public String getTypeIconHtml() {
		return get("typeIconHtml");
	}

	public void generateTypeIconHTML(Integer organizationId) {
		if (organizationId == null) {
			return;
		}
		Image img = FundingIconProvider.getProjectTypeIcon(getProjectModelType(organizationId), IconSize.SMALL)
		                .createImage();
		setTypeIconHtml(img.getElement().getString());
	}

	public Double getRatioDividendValue() {
		return get("ratioDividendValue");
	}

	public void setRatioDividendValue(Double ratioDividendValue) {
		set("ratioDividendValue", ratioDividendValue);
	}

	public String getRatioDividendLabel() {
		return get("ratioDividendLabel");
	}

	public void setRatioDividendLabel(String ratioDividendLabel) {
		set("ratioDividendLabel", ratioDividendLabel);
	}
	
	public BudgetSubFieldType getRatioDividendType() {
		return get("ratioDividendType");
	}

	public void setRatioDividendType(BudgetSubFieldType ratioDividendType) {
		set("ratioDividendType", ratioDividendType);
	}

	public Double getRatioDivisorValue() {
		return get("ratioDivisorValue");
	}

	public void setRatioDivisorValue(Double ratioDivisorValue) {
		set("ratioDivisorValue", ratioDivisorValue);
	}

	public String getRatioDivisorLabel() {
		return get("ratioDivisorLabel");
	}

	public void setRatioDivisorLabel(String ratioDivisorLabel) {
		set("ratioDivisorLabel", ratioDivisorLabel);
	}
	
	public BudgetSubFieldType getRatioDivisorType() {
		return get("ratioDivisorType");
	}

	public void setRatioDivisorType(BudgetSubFieldType ratioDivisorType) {
		set("ratioDivisorType", ratioDivisorType);
	}

}
