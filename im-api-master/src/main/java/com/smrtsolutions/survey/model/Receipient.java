/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embeddable;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 *
 * @author lenny
 */
@Embeddable
@XmlRootElement
@NoSql(dataType="receipient", dataFormat=DataFormatType.MAPPED)
public class Receipient extends EntityBase  
implements Serializable, Cloneable {
    
    private static final long serialVersionUID = 1L;
    
    @Field(name="createdAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    
    @Field(name="target")
    private SMRTUser target;
    


    @Field(name="status")
    private int status;
    
    

    /**
     * @return the createdAt
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt the createdAt to set
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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
     * @return the target
     */
    public SMRTUser getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(SMRTUser target) {
        this.target = target;
    }


    
    //public enum STATUS { DISABLED, OPEN, STARTED, IN_PROGRESS, COMPLETE }
    public static enum STATUS { 
         UNOPENED, OPENED, ACKNOWLEDGED
     }

    public static String[] STATUS_DESCRIPTION  = { 
         "Unopened", "Opened", "Acknowledged"
     };
   
    @Transient
    private String statusDescription;
    

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
    

    
}
