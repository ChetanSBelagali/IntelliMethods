/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.survey.model;

import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import org.eclipse.persistence.nosql.annotations.Field;

/**
 *
 * @author lenny
 */
@MappedSuperclass  
public abstract class EntityBase {
    
    @Id
    @GeneratedValue
    @Field(name="_id")
    private String id;
    
    @Basic
    @Field(name="customerId")
    private String customerId;
    
    //@Temporal(name="created_at")
    //private Date createtAt;
    
    //@Temporal(name="last_updated_at")
    //private Date lastUpdatedAt;
    
    @Field(name="createdBy")
    private String createdBy;
    
    @Field(name="lastUpdatedBy")
    private String lastUpdatedBy;
    
    @Field(name="notes")
    private String notes;
    
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public boolean equals(Object obj) {
           // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(obj instanceof EntityBase)) {
            return false;
        } else {
            EntityBase other = (EntityBase) obj;
            if (!this.id.equals(other.getId())) {
               return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        
        return id ;
    }

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
     * @return the lastUpdatedBy
     */
    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    /**
     * @param lastUpdatedBy the lastUpdatedBy to set
     */
    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
}
