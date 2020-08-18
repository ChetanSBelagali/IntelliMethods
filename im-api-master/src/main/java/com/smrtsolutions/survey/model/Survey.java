/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;
//import org.eclipse.persistence.annotations.Multitenant;
//import static org.eclipse.persistence.annotations.MultitenantType.SINGLE_TABLE;
//import org.eclipse.persistence.annotations.TenantDiscriminatorColumn;
import com.smrtsolutions.util.Util;
import java.util.Date;
/**
 *
 * @author lenny
 */
@Entity
/*@Multitenant(SINGLE_TABLE)
@TenantDiscriminatorColumn(name = "customer_id", contextProperty = "participant-tenant.id")*/
@XmlRootElement
@NoSql(dataType="survey", dataFormat=DataFormatType.MAPPED)
public class Survey extends EntityBase implements Serializable {
    private static final long serialVersionUID = 1L;

    //settings keys
    public static final String IS_PERIODIC_TAG = "is_periodic";
    public static final String PERIODICITY_TAG = "periodicity";
    public static final String FLOW_TAG = "flow";
    public static final String FLOW_CONTROL_TAG = "controlled_for";
    public static final String PERIODICITY_RANGE_TAG = "annual_range";
    public static final String PARTICIPANT_TYPE_TAG = "participant_type";
    
    //settings values
    public static final String PERIODICITY_CALENDAR_ANNUAL = "calendar_annual";
    //public static final String PERIODICITY_ANNUAL = "annual";
    public static final String PERIODICITY_RANGE = "range";
    public static final String PERIODICITY_ONCE = "once";
    public static final String PERIODICITY_MONTHLY = "monthly";
    public static final String PERIODICITY_TIMESTAMP = "timestamp";
    public static final String FLOW_CONTROLLED = "controlled"; // manually enabled / disbaled by case managers
    public static final String FLOW_SEQUENTIAL = "sequential"; // surveys are enabled in sequence after completion
    public static final String FLOW_DEPENDENT = "dependent"; // controlled by postSaveAction
    public static final String FLOW_NONE = "none"; // no worklow
    public static final String FLOW_CONTROLLED_ALL_PARTICIPANTS = "all_participants";
    public static final String FLOW_CONTROLLED_PARTICIPANT = "participant";
    public static final String PARTICIPANT_TYPE_NONE = "none";
    public static final String PARTICIPANT_TYPE_USER = "user";
    
    

    @Field(name="name")
    private String name = "";
    
    @Field(name="status")
    private int status;
    
    @Field(name="version")
    private int version;
    
    @Field(name="description")
    private String description = "";
    
    @Field(name="url")
    private String url = "";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    @Field
    private String thumbnailUrl = "";
    
    @Field(name="label")
    private String label;
    
    //@OneToMany(mappedBy="survey", cascade=CascadeType.ALL)
    @ElementCollection
    @Field(name="sections")
    private List<SurveySection> sections;
    
    @Field(name="pre_condition")
    private String preCondition = "";
    
    @Field(name="survey_type")
    private String surveyType;
    
    @Field(name="type")
    private String type;
    
    @Field(name="sort_order")
    private int sortOrder;
    

    @Field(name="settings")
    @ElementCollection
    private List<NameValuePair> settings = new ArrayList<NameValuePair>();
    
    @Transient
    private List<NameValuePair> availableStates = new ArrayList<NameValuePair>();
    
    @Transient
    private String statusDescription = "";
    
    @Field(name="logicalKey")
    private String logicalKey = "";
    
    @Field(name="metaDataFileName")
    private String metaDataFileName="";
    
    @Field(name="displayTab")
    private String displayTab;
    
     @Field(name="createdOn")
    private Date createdOn;

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    @Field(name="displayOrder")
    private String displayOrder;

    @Field(name="isDefault")
    private String isDefault;
    
    @Field(name="allocationType")
    private String allocationType;

    @Field (name="surveyEntrySequence")
    private String surveyEntrySequence;
    
    @Field (name="recruitment")
    private String recruitment;
    
    @Field (name = "intake")
    private String intake;

    public String getRecruitment() {
        return recruitment;
    }

    public void setRecruitment(String recruitment) {
        this.recruitment = recruitment;
    }

    public String getIntake() {
        return intake;
    }

    public void setIntake(String intake) {
        this.intake = intake;
    }
    
    public String getSurveyEntrySequence() {
        return surveyEntrySequence;
    }

    public void setSurveyEntrySequence(String surveyEntrySequence) {
        this.surveyEntrySequence = surveyEntrySequence;
    }

    
    

    
    
    
    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }
    
    public String getAllocationType() {
        return allocationType;
    }

    public void setAllocationType(String allocationType) {
        this.allocationType = allocationType;
    }

    

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }





    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the thumbnailUrl
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * @param thumbnailUrl the thumbnailUrl to set
     */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * @return the sections
     */
    public List<SurveySection> getSections() {
        return sections;
    }

    /**
     * @param sections the sections to set
     */
    public void setSections(List<SurveySection> sections) {
        this.sections = sections;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the preCondition
     */
    public String getPreCondition() {
        return preCondition;
    }

    /**
     * @param preCondition the preCondition to set
     */
    public void setPreCondition(String preCondition) {
        this.preCondition = preCondition;
    }

    /**
     * @return the sortOrder
     */
    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * @param sortOrder the sortOrder to set
     */
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSurveyType() {
        return surveyType;
    }

    public void setSurveyType(String surveyType) {
        this.surveyType = surveyType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
    
    @Transient


    /**
     * @return the settings
     */
    public List<NameValuePair> getSettings() {
        return settings;
    }

    /**
     * @param settings the settings to set
     */
    public void setSettings(List<NameValuePair> settings) {
        this.settings = settings;
    }
    
    @Transient 
    public String getSetting(String name, String def){
        if ( this.settings == null) return def;
        for ( NameValuePair nvp : this.settings){
            if ( nvp.getName().equalsIgnoreCase(name)) {
                return Util.getValue(nvp.getValue(), def);
            }
        }
        return def;
    }

    /**
     * @return the availableStates
     */
    public List<NameValuePair> getAvailableStates() {
        return availableStates;
    }

    /**
     * @param availableStates the availableStates to set
     */
    public void setAvailableStates(List<NameValuePair> availableStates) {
        this.availableStates = availableStates;
    }

    /**
     * @return the statusDescription
     */
    public String getStatusDescription() {
        return statusDescription;
    }

    /**
     * @param statusDescription the statusDescription to set
     */
    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    /**
     * @return the logicalKey
     */
    public String getLogicalKey() {
        return logicalKey;
    }

    /**
     * @param logicalKey the logicalKey to set
     */
    public void setLogicalKey(String logicalKey) {
        this.logicalKey = logicalKey;
    }

    
    public String getMetaDataFileName() {
            return metaDataFileName;
    }
/**
     * @param metaDataFileName the CSV metadatafilename to set
     */
    public void setMetaDataFileName(String metaDataFileName) {
            this.metaDataFileName = metaDataFileName;
    } 
    
//    for menu bar 

    public String getDisplayTab() {
        return displayTab;
    }

    public void setDisplayTab(String displayTab) {
        this.displayTab = displayTab;
    }

    public String getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(String displayOrder) {
        this.displayOrder = displayOrder;
    }
    
   
}
