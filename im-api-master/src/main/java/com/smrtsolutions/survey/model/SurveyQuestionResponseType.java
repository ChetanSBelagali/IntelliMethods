/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author lenny
 */
@Entity
@XmlRootElement
@Table ( name="survey_question_response_type")
public class SurveyQuestionResponseType implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE)
    @Column(name="survey_question_response_type_id")
    private long id;
    
    @ManyToOne(optional=false)
    @JoinColumn(name="customer_id",referencedColumnName="customer_id")
    private Customer customer;
    
  
    @Column(name="name")
    private String name = "";
    
    @Column(name="status")
    private int status;
      
    private String description = "";
    
    private String thumbnailUrl = "";
    
    @Column (length=5000)
    private String customScript = "";
    

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += id;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SurveyQuestionResponseType)) {
            return false;
        }
        SurveyQuestionResponseType other = (SurveyQuestionResponseType) object;
        if (this.id != other.id ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        //return "com.smrtsolutions.survey.Customer[ id=" + id + " ]";
        return id+"" ;
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
     * @return the customer
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * @param customer the customer to set
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
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
     * @return the customScript
     */
    public String getCustomScript() {
        return customScript;
    }

    /**
     * @param customScript the customScript to set
     */
    public void setCustomScript(String customScript) {
        this.customScript = customScript;
    }
    
}
