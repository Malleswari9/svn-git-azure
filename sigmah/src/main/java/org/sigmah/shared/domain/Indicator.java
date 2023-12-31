/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.shared.domain;


import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;
import org.sigmah.shared.domain.quality.QualityCriterion;

/**
 * Defines an Indicator, a numeric value that can change over time.
 *
 * @author Alex Bertram
 *
 */
@Entity
@org.hibernate.annotations.Filter(
		name="hideDeleted",
		condition="DateDeleted is null"
)
public class Indicator implements java.io.Serializable, Orderable, Deleteable, SchemaElement {

	private static final long serialVersionUID = 5978350531347182242L;
	private int id;
	private Date dateDeleted;

	private String name;
	private String units;
	private Double objective;
	private String description;

	private String category;

	private boolean collectIntervention;
	private boolean collectMonitoring;

	private Activity activity;

	
	private int aggregation;

	private int sortOrder;
	private String code;
	private UserDatabase database;

	private QualityCriterion qualityCriterion;

	private List<String> labels;

	private Set<Indicator> dataSources = new HashSet<Indicator>(0);
	
	private String sourceOfVerification;
	
	private boolean directDataEntryEnabled;
	
	public Indicator() {

	}

	/**
	 *
	 * @return the id of this Indicator
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "IndicatorId", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	/**
	 * Sets the id of this Indicator
	 */
	public void setId(int indicatorId) {
		this.id = indicatorId;
	}

	/**
	 * @return the name of this Indicator
	 */
	@Column(name = "Name", nullable = false, length = 1024)
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name of the Indicator
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets a description of the units in which this Indicator is expressed. Note that this
	 * is for descriptive purpose only for the user, it does not carry any semantics for our system.
	 *
	 * @return description of the units in which this indicator is expressed. Examples: "households", "%"
	 * "cm"
	 */
	@Column(name = "Units", nullable = true, length = 15)
	public String getUnits() {
		return this.units;
	}

	/**
	 * Sets the description of the units in which this indicator is expressed.
	 * @param units a description of the units
	 */
	public void setUnits(String units) {
		this.units = units;
	}


	/**
	 * Gets the numerical objective for this Indicator. 
	 *
	 * @return the objective for this Indicator
	 */
	@Column(name = "Objective", precision = 15, scale = 0, nullable=true )
	public Double getObjective() {
		return objective;
	}


	/**
	 * Sets the numerical objective for this Indicator.
	 * 
	 * @param objective
	 */
	public void setObjective(Double objective) {
		this.objective = objective;
	}


	/**
	 * @return a full description of this indicator, containing perhaps detailed instructions on how
	 * it is to be collected or calculated.
	 */
	@Lob
	public String getDescription() {
		return this.description;
	}

	/**
	 * Sets the description of this Indicator.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the Activity which is implemented at this Site
	 */
	@ManyToOne(fetch = FetchType.LAZY, cascade={CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name = "ActivityId", nullable = true)
	public Activity getActivity() {
		return this.activity;
	}

	/**
	 * Sets the Activity which is implemented at this Site
	 */
	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	/**
	 *
	 * @return true if this Indicator is collected during the actual intervention. (Some indicators
	 * are only collected afterwords, during the monitoring phase)
	 */
	@Column(name = "CollectIntervention", nullable = false)
	public boolean getCollectIntervention() {
		return this.collectIntervention;
	}

	/**
	 * Sets whether this Indicator is collected during the actual intervention.
	 */
	public void setCollectIntervention(boolean collectIntervention) {
		this.collectIntervention = collectIntervention;
	}

	/**
	 * @return  the method by which this Indicator is aggregated
	 */
	@Column(name = "Aggregation", nullable = false)
	public int getAggregation() {
		return this.aggregation;
	}

	/**
	 * Sets the method by which this Indicator is aggregated.
	 */
	public void setAggregation(int aggregation) {
		this.aggregation = aggregation;
	}

	/**
	 * 
	 * @return true if the user can associate indicator values with this project,
	 * or false if this indicator takes its value exclusively from its data sources
	 */
	@Column(columnDefinition="boolean not null default true")
	public boolean isDirectDataEntryEnabled() {
		return directDataEntryEnabled;
	}

	public void setDirectDataEntryEnabled(boolean directDataEntryEnabled) {
		this.directDataEntryEnabled = directDataEntryEnabled;
	}

	/**
	 * @return true if this Indicator is collected during the monitoring phase
	 */
	@Column(name = "CollectMonitoring", nullable = false)
	public boolean isCollectMonitoring() {
		return this.collectMonitoring;
	}

	/**
	 * Sets whether this Indicator is collected during the monitoring phase
	 */
	public void setCollectMonitoring(boolean collectMonitoring) {
		this.collectMonitoring = collectMonitoring;
	}

	/**
	 * @return the sort order of this Indicator within its Activity
	 */
	@Column(name = "SortOrder", nullable = false)
	public int getSortOrder() {
		return this.sortOrder;
	}

	/**
	 * Sets the sort order of this Indicator within its Activity
	 */
	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	/**
	 * @return a short list header that is used when this Indicator's values are displayed in a
	 * grid
	 */
	@Column(name = "ListHeader", length = 30)
	public String getCode() {
		return this.code;
	}

	/**
	 * Sets the short list header that is used when this Indicator's values are displayed within
	 * a grid
	 *
	 */
	public void setCode(String listHeader) {
		this.code = listHeader;
	}

	/**
	 * Gets this Indicator's category. Categories are just strings that are used for organizing the
	 * display of Indicators in the user interface.
	 *
	 * @return the name of the category
	 */
	@Column(name = "Category", length = 1024)
	public String getCategory() {
		return this.category;
	}

	/**
	 * Sets this Indicator's category.
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the time at which this Indicator was deleted
	 */
	@Column
	@Temporal(value = TemporalType.TIMESTAMP)
	public Date getDateDeleted() {
		return this.dateDeleted;
	}

	/**
	 * Sets the time at which this Indicator was deleted.
	 */
	public void setDateDeleted(Date deleteTime) {
		this.dateDeleted = deleteTime;
	}

	/**
	 * Marks this Indicator as deleted.
	 */
	public void delete() {
		setDateDeleted(new Date());
	}

	/**
	 *
	 * @return true if this Indicator has been deleted.
	 */
	@Override
	@Transient
	public boolean isDeleted() {
		return getDateDeleted() != null;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_quality_criterion", nullable = true)
	public QualityCriterion getQualityCriterion() {
		return qualityCriterion;
	}

	public void setQualityCriterion(QualityCriterion qualityCriterion) {
		this.qualityCriterion = qualityCriterion;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DatabaseId", nullable = true)
	public UserDatabase getDatabase() {
		return this.database;
	}

	public void setDatabase(UserDatabase database) {
		this.database = database;
	}

	@CollectionOfElements
	@IndexColumn(name = "code", base=1)
	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(name = "indicator_datasource",
			joinColumns = { @JoinColumn(name = "IndicatorId", nullable = false, updatable = false) },
			inverseJoinColumns = { @JoinColumn(name = "IndicatorSourceId", nullable = false, updatable = false) })
	public Set<Indicator> getDataSources() {
		return dataSources;
	}

	public void setDataSources(Set<Indicator> dataSources) {
		this.dataSources = dataSources;
	}
	
	/**
	 * 
	 * @return text description of how this indicator will be verified
	 */
	@Lob
	public String getSourceOfVerification() {
		return sourceOfVerification;
	}

	public void setSourceOfVerification(String sourceOfVerification) {
		this.sourceOfVerification = sourceOfVerification;
	}

	/**
	 * Copies a indicator to another database.
	 * 
	 * @param databaseCopy
	 * @return
	 */
	public Indicator copy(UserDatabase databaseCopy) {
		Indicator copy = new Indicator();
		copy.name = this.name;
		copy.code = this.code;
		copy.units = this.units;
		copy.objective = this.objective;
		copy.description = this.description;
		copy.category = this.category;
		copy.collectIntervention = this.collectIntervention;
		copy.collectMonitoring = this.collectMonitoring;
		copy.aggregation = this.aggregation;
		copy.sortOrder = this.sortOrder;
		copy.database = databaseCopy;
		copy.labels = this.labels;
		copy.sourceOfVerification = this.sourceOfVerification;
		
		return copy;
	}
}
