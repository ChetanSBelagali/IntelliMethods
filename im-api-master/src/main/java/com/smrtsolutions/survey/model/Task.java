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
@NoSql(dataType = "Task", dataFormat = DataFormatType.MAPPED)
public class Task extends EntityBase implements Serializable {
    //private static final long serialVersionUID = 1L;

   // public static String SYSTEM_MASTER_CUSTOMER_ID = "1";
    
    
    
    @Field(name="category")
    private String category;

    
    @Field(name="type")
    private String type;

    
    
    @Field(name="taskFor")
    private String taskFor;

    
    
    @Field(name="clientId")
    private String clientId;

   
    @Field(name="customerId")
    private String customerId;

       
    @Field(name="assignmentType")
    private String assignmentType;

    
    @Field(name="groupId")
    private String groupId;
 
    @Field(name="groupTaskType")
    private String groupTaskType;
    
    @Field(name="partnerId")
    private String partnerId;
    
    @Field(name="taskNature")
    private String taskNature;
        
    @Field(name="priority")
    private String priority;

    @Field(name="dueDate")
    @Temporal(TemporalType.DATE)
    private Date dueDate;
    
    @Field(name="desc")
    private String desc;

    @Field(name="createdBy")
    private String createdBy;

   
    
    @Field(name="createdOn")
    @Temporal(TemporalType.DATE) 
    private Date createdOn;

   
    @Field(name="status")
    private String status;

    
    @Field(name="completedOn")
    private String completedOn;
    
    @Field(name="adminTaskType")
    private String adminTaskType;
    
    @Field(name="cipCode")
    private NameValuePair cipCode;
    
    @Field(name="taskCategory")
    private String taskCategory;
    @Transient
    //private List<SMRTRole> roles;
    private List<TaskAssignment> assignedTo;

    public List<TaskAssignment> getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(List<TaskAssignment> assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    
 //   public static String SYSTEM_USER_ID = "1";
    

    public String getCategory() {
        return category;
    }

    public void setCategary(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTaskFor() {
        return taskFor;
    }

    public void setTaskFor(String taskFor) {
        this.taskFor = taskFor;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupTaskType() {
        return groupTaskType;
    }

    public void setGroupTaskType(String groupTaskType) {
        this.groupTaskType = groupTaskType;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getTaskNature() {
        return taskNature;
    }

    public void setTaskNature(String taskNature) {
        this.taskNature = taskNature;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    
    public String getAdminTaskType() {
        return adminTaskType;
    }

   
    public void setAdminTaskType(String adminTaskType) {
        this.adminTaskType = adminTaskType;
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

    
    
    /**
     * @return the taskCategory
     */
    public String getTaskCategory() {
        return taskCategory;
    }

    /**
     * @param taskCategory the taskCategory to set
     */
    public void setTaskCategory(String taskCategory) {
        this.taskCategory = taskCategory;
    }

    /**
     * @return the cipCode
     */
    public NameValuePair getCipCode() {
        return cipCode;
    }

    /**
     * @param cipCode the cipCode to set
     */
    public void setCipCode(NameValuePair cipCode) {
        this.cipCode = cipCode;
    }
    
    
    
  }