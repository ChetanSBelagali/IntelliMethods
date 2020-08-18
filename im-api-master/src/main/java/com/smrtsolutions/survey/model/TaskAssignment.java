/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.survey.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
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
 * @author srinath
 */

@Entity
@XmlRootElement
@NoSql(dataType = "TaskAssignment", dataFormat = DataFormatType.MAPPED)
public class TaskAssignment extends EntityBase implements Serializable {
    //private static final long serialVersionUID = 1L;

   // public static String SYSTEM_MASTER_CUSTOMER_ID = "1";

    @Field(name="taskId")
    private String taskId;
  
    @Field(name="assignedTo")
    private String assignedTo;

    
    @Field(name="dueDate")
    @Temporal(TemporalType.DATE) 
    private Date dueDate;

    
    @Field(name="status")
    private String status;

    
    @Field(name="completedOn")
    private String completedOn;

    
    @Field(name="customerId")
    private String customerId;
    
    @Field(name="userType")
    private String userType;

    
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(String completedOn) {
        this.completedOn = completedOn;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
    
    
 
}