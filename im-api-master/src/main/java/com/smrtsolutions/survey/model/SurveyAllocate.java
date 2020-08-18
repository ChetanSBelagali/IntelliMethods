

/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.model;

import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 *
 * @author Srinath
 */
@Entity
@XmlRootElement
@NoSql(dataType="surveyallocate",dataFormat=DataFormatType.MAPPED)

public class SurveyAllocate extends EntityBase implements Serializable {
    
  @Field(name="survey_id")
  private String survey_id;
  
  @Transient
  private String allocatedTo;
  
//  @ElementCollection
//  @Field(name="userId")
//  private List<String> userId;
  @ElementCollection
    @Field(name = "userId")
    private List<String> userId;
  
  @Field(name="customerId")
  private String customerId;
 
  @Field(name="createdBy")
  private String createdBy;

   @Field(name="createdOn")
   @Temporal(TemporalType.DATE) 
   private Date createdOn;
   

    /**
     * @return the allocatedTo
     */
    public String getAllocatedTo() {
        return allocatedTo;
    }

    /**
     * @param allocatedTo the allocatedTo to set
     */
    public void setAllocatedTo(String allocatedTo) {
        this.allocatedTo = allocatedTo;
    }

    /**
     * @return the userId
     */
 

    /**
     * @return the customerId
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * @param customerId the customerId to set
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the userId
     */
    public List<String> getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(List<String> userId) {
        this.userId = userId;
    }

    /**
     * @return the survey_id
     */
    public String getSurvey_id() {
        return survey_id;
    }

    /**
     * @param survey_id the survey_id to set
     */
    public void setSurvey_id(String survey_id) {
        this.survey_id = survey_id;
    }

    /**
     * @return the createdOn
     */
    public Date getCreatedOn() {
        return createdOn;
    }

    /**
     * @param createdOn the createdOn to set
     */
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

   
  
}