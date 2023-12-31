/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.shared.command;

import java.util.List;

import org.sigmah.shared.command.result.ProjectListResult;
import org.sigmah.shared.domain.ProjectModelType;
import org.sigmah.shared.dto.CountryDTO;

/**
 * Retrieves the list of projects available to the user.
 */
public class GetProjects implements Command<ProjectListResult> {

    private static final long serialVersionUID = 4355275610740881552L;

    /**
     * Defines the different of returns that that command handles.
     * 
     * @author tmi
     */
    public static enum ProjectResultType {

        /**
         * Returns the projects as light DTO.
         */
        PROJECT_LIGHT,

        /**
         * Returns the projects.
         */
        PROJECT,

        /**
         * Returns the projects ids.
         */
        ID;
    }

    /**
     * List of countries in which the projects will be searched (set to
     * <code>null</code> to ignore this filter).
     */
    private List<CountryDTO> countries;

    /**
     * The type of model of the projects for the current user organization (set
     * to <code>null</code> to ignore this filter).
     */
    private ProjectModelType modelType;

    /**
     * List of organizational units ids in which the projects will be searched
     * (set to <code>null</code> to ignore this filter).
     */
    private List<Integer> orgUnitsIds;

    /**
     * If the project that the current user own or manage must be retrieved.
     */
    private boolean viewOwnOrManage;

    /**
     * The command return type (default to
     * {@link ProjectResultType#PROJECT_LIGHT}).
     */
    private ProjectResultType returnType;

    public GetProjects() {
        this(null, null);
    }

    public GetProjects(ProjectModelType modelType) {
        this(null, modelType);
    }

    public GetProjects(List<Integer> orgUnitsIds) {
        this(orgUnitsIds, null);
    }

    public GetProjects(List<Integer> orgUnitsIds, ProjectModelType modelType) {
        this.countries = null;
        this.modelType = modelType;
        this.orgUnitsIds = orgUnitsIds;
        this.returnType = ProjectResultType.PROJECT_LIGHT;
    }

    public List<CountryDTO> getCountries() {
        return countries;
    }

    /**
     * Sets the list of countries in which the projects will be searched (set to
     * <code>null</code> to ignore this filter).
     * 
     * @param countries
     *            The countries.
     */
    public void setCountries(List<CountryDTO> countries) {
        this.countries = countries;
    }

    public ProjectModelType getModelType() {
        return modelType;
    }

    /**
     * Sets the type of model of the projects for the current user organization
     * (set to <code>null</code> to ignore this filter).
     * 
     * @param modelType
     *            The type.
     */
    public void setModelType(ProjectModelType modelType) {
        this.modelType = modelType;
    }

    public List<Integer> getOrgUnitsIds() {
        return orgUnitsIds;
    }

    public void setViewOwnOrManage(boolean viewOwnOrManage) {
        this.viewOwnOrManage = viewOwnOrManage;
    }

    public boolean getViewOwnOrManage() {
        return viewOwnOrManage;
    }

    public void setReturnType(ProjectResultType returnType) {
        this.returnType = returnType;
    }

    public ProjectResultType getReturnType() {
        return returnType;
    }

    /**
     * Sets the list of organizational units ids in which the projects will be
     * searched (set to <code>null</code> to ignore this filter).
     * 
     * @param orgUnits
     *            The list.
     */
    public void setOrgUnitsIds(List<Integer> orgUnitsIds) {
        this.orgUnitsIds = orgUnitsIds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((countries == null) ? 0 : countries.hashCode());
        result = prime * result + ((modelType == null) ? 0 : modelType.hashCode());
        result = prime * result + ((orgUnitsIds == null) ? 0 : orgUnitsIds.hashCode());
        result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
        result = prime * result + (viewOwnOrManage ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GetProjects other = (GetProjects) obj;
        if (countries == null) {
            if (other.countries != null)
                return false;
        } else if (!countries.equals(other.countries))
            return false;
        if (modelType != other.modelType)
            return false;
        if (orgUnitsIds == null) {
            if (other.orgUnitsIds != null)
                return false;
        } else if (!orgUnitsIds.equals(other.orgUnitsIds))
            return false;
        if (returnType != other.returnType)
            return false;
        if (viewOwnOrManage != other.viewOwnOrManage)
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[GetProjects] command: ");
        sb.append("countries [");
        sb.append(countries);
        sb.append("] ; ");
        sb.append("model type [");
        sb.append(modelType);
        sb.append("] ; ");
        sb.append("org units ids [");
        sb.append(orgUnitsIds);
        sb.append("] ; ");
        sb.append("view own or manage [");
        sb.append(viewOwnOrManage);
        sb.append("] ; ");
        sb.append("return type [");
        sb.append(returnType);
        sb.append("]");
        return sb.toString();
    }
}
