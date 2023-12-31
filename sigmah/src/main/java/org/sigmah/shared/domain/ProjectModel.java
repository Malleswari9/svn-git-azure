package org.sigmah.shared.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.sigmah.shared.domain.logframe.LogFrameModel;

import com.extjs.gxt.ui.client.data.BaseModelData;

/**
 * Project model entity.
 * 
 * @author Denis Colliot (dcolliot@ideia.fr)
 */
@Entity
@Table(name = "project_model")
@org.hibernate.annotations.FilterDefs({ @org.hibernate.annotations.FilterDef(name = "hideDeleted") })
@org.hibernate.annotations.Filters({ @org.hibernate.annotations.Filter(name = "hideDeleted", condition = "date_deleted is null") })
public class ProjectModel extends BaseModelData implements Serializable {

    private static final long serialVersionUID = -1266259112071917788L;

    private Long id;
    private String name;
    private PhaseModel rootPhase;
    private List<PhaseModel> phases = new ArrayList<PhaseModel>();
    private ProjectBanner projectBanner;
    private ProjectDetails projectDetails;
    private List<ProjectModelVisibility> visibilities;
    private ProjectModelStatus status;
    private LogFrameModel logFrameModel;
    private Date dateDeleted;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_project_model")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "name", nullable = false, length = 8192)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToOne(optional = true)
    @JoinColumn(name = "id_root_phase_model", nullable = true)
    public PhaseModel getRootPhase() {
        return rootPhase;
    }

    public void setRootPhase(PhaseModel rootPhase) {
        this.rootPhase = rootPhase;
    }

    @OneToMany(mappedBy = "parentProjectModel", cascade = CascadeType.ALL)
    public List<PhaseModel> getPhases() {
        return phases;
    }

    public void setPhases(List<PhaseModel> phases) {
        this.phases = phases;
    }

    public void addPhase(PhaseModel phase) {
        if (phase != null) {
            phase.setParentProjectModel(this);
            phases.add(phase);
        }
    }

    @OneToOne(mappedBy = "projectModel", cascade = CascadeType.ALL)
    public ProjectBanner getProjectBanner() {
        return projectBanner;
    }

    public void setProjectBanner(ProjectBanner projectBanner) {
        this.projectBanner = projectBanner;
    }

    @OneToOne(mappedBy = "projectModel", cascade = CascadeType.ALL)
    public ProjectDetails getProjectDetails() {
        return projectDetails;
    }

    public void setProjectDetails(ProjectDetails projectDetails) {
        this.projectDetails = projectDetails;
    }

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL)
    public List<ProjectModelVisibility> getVisibilities() {
        return visibilities;
    }

    public void setVisibilities(List<ProjectModelVisibility> visibilities) {
        this.visibilities = visibilities;
    }

    @OneToOne(mappedBy = "projectModel", cascade = CascadeType.ALL)
    public LogFrameModel getLogFrameModel() {
        return logFrameModel;
    }

    public void setLogFrameModel(LogFrameModel logFrameModel) {
        this.logFrameModel = logFrameModel;
    }

    public void setStatus(ProjectModelStatus status) {
        this.status = status;
    }

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    public ProjectModelStatus getStatus() {
        return status;
    }
    
    /**
     * 
     * @return The date on which this project model was deleted by the user, or null
     *         if this project model is not deleted.
     */
    @Column(name="date_deleted")
    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getDateDeleted() {
        return this.dateDeleted;
    }

    protected void setDateDeleted(Date date) {
        this.dateDeleted = date;
    }

    /**
     * Marks this database as deleted. (Though the row is not removed from the
     * database)
     */
    public void delete() {
        Date now = new Date();
        setDateDeleted(now);
    }

    /**
     * Gets the type of this model for the given organization. If this model
     * isn't visible for this organization, <code>null</code> is returned.
     * 
     * @param organization
     *            The organization.
     * @return The type of this model for the given organization,
     *         <code>null</code> otherwise.
     */
    public ProjectModelType getVisibility(Organization organization) {

        if (organization == null || visibilities == null) {
            return null;
        }

        for (final ProjectModelVisibility visibility : visibilities) {
            if (visibility.getOrganization().getId() == organization.getId()) {
                return visibility.getType();
            }
        }
        return null;
    }

    /**
     * Reset the identifiers of the object
     */
    public void resetImport(HashMap<Object, Object> modelesReset, HashSet<Object> modelesImport) {
        this.id = null;
        if (this.rootPhase != null) {
            this.rootPhase.resetImport(this);
        }

        if (phases != null) {
            for (PhaseModel phase : this.phases) {
                phase.resetImport(null);
            }
        }

        if (this.projectBanner != null) {
            this.projectBanner.resetImport(this);
        }

        if (this.projectDetails != null) {
            this.projectDetails.resetImport(this);
        }

        if (this.logFrameModel != null) {
            this.logFrameModel.resetImport();
        }
    }
}
