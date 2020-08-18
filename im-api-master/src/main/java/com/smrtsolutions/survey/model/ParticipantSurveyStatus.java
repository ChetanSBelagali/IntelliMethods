/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 *
 * @author lenny
 */
@Entity
@XmlRootElement
@NoSql(dataType="participant_survey_status", dataFormat=DataFormatType.MAPPED)
public class ParticipantSurveyStatus extends EntityBase //extends XmlAdapter<SurveyResult, SurveyResult> 
implements Serializable, Cloneable {
    
    private static final long serialVersionUID = 1L;
    
    @Field(name="result_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date resultTime;
    
    
    @Field(name="survey_id")
    private String surveyId;
    
    @Field(name="participant_id")
    private String participantId;

    @Field(name="status")
    private int status;
    
    @Field(name="survey_type")
    private String surveyType;
    
    @Transient 
    private String surveyName;
    
    @Transient
    private String surveyDescription;
    
    @Transient
    private String surveyLabel;
    


    /**
     * @return the participantId
     */
    public String getParticipantId() {
        return participantId;
    }

    /**
     * @param participantId the participantId to set
     */
    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    /**
     * @return the resultTime
     */
    public Date getResultTime() {
        return resultTime;
    }

    /**
     * @param resultTime the resultTime to set
     */
    public void setResultTime(Date resultTime) {
        this.resultTime = resultTime;
    }

    /**
     * @return the surveyName
     */
    public String getSurveyName() {
        return surveyName;
    }

    /**
     * @param surveyName the surveyName to set
     */
    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
    }

    /**
     * @return the surveyDescription
     */
    public String getSurveyDescription() {
        return surveyDescription;
    }

    /**
     * @param surveyDescription the surveyDescription to set
     */
    public void setSurveyDescription(String surveyDescription) {
        this.surveyDescription = surveyDescription;
    }

    /**
     * @return the surveyLabel
     */
    public String getSurveyLabel() {
        return surveyLabel;
    }

    /**
     * @param surveyLabel the surveyLabel to set
     */
    public void setSurveyLabel(String surveyLabel) {
        this.surveyLabel = surveyLabel;
    }
    
    //public enum STATUS { DISABLED, OPEN, STARTED, IN_PROGRESS, COMPLETE }
    public static enum STATUS { 
         NOT_APPLICABLE, PENDING, IN_PROGRESS, COMPLETE, RESERVED1, RESERVED2, RESERVED3, RESERVED4, RESERVED5
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

    public String getSurveyType() {
        return surveyType;
    }

    public void setSurveyType(String surveyType) {
        this.surveyType = surveyType;
    }


    /**
     * @return the surveyId
     */
    public String getSurveyId() {
        return surveyId;
    }

    /**
     * @param surveyId the surveyId to set
     */
    public void setSurveyId(String surveyId) {
        this.surveyId = surveyId;
    }

     @Transient
    private List<NameValuePair> surveySettings;
        /**
     * @return the settings
     */
    public List<NameValuePair> getSurveySettings() {
        return surveySettings;
    }

    /**
     * @param settings the settings to set
     */
    public void setSurveySettings(List<NameValuePair> settings) {
        this.surveySettings = settings;
    }
    









    
}
