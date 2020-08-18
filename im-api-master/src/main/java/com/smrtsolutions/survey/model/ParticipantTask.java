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
 * @author joy
 */
@Entity
@XmlRootElement
@NoSql(dataType="participant_task", dataFormat=DataFormatType.MAPPED)
public class ParticipantTask extends EntityBase  
implements Serializable, Cloneable {
    
    private static final long serialVersionUID = 1L;

    public ParticipantTask() {
    }

    public ParticipantTask(ParticipantTask another) {
        this.setCustomerId(another.getCustomerId());
        this.createdAt = another.createdAt;
        this.toDODate = another.toDODate;
        this.taskAssignment = another.taskAssignment;
        this.assignedBy = another.assignedBy;
        this.assignedTo = another.assignedTo;
        this.relatesTo = another.relatesTo;
        this.status = another.status;
        this.assignWhere = another.assignWhere;
        this.taskType = another.taskType;
        this.adminTaskType = another.adminTaskType;
        this.priority = another.priority;
        this.reasonAction = another.reasonAction;
        this.content = another.content;
    }
    
    
    
    @Field(name="createdAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Field(name="toDODate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date toDODate;
    
//    @Field(name="isGroupTask")
//    private Boolean isGroupTask;
//    
//    @Embedded
//    @Field(name="groupTaskType")
//    private NameValuePair groupTaskType;
    
    @Field(name="operationType")
    private String operationType;
    
    @Embedded
    @Field(name="taskAssignment")
    private NameValuePair taskAssignment;
    
//    @Field(name="assignedById")
//    private String assignedById;
//    
//    @Field(name="assignedByName")
//    private String assignedByName;
    @Embedded
    @Field(name="assignedBy")
    private NameValuePair assignedBy;
    
//    @Field(name="assignedToId")
//    private String assignedToId;
//    
//    @Field(name="assignedToName")
//    private String assignedToName;
    @ElementCollection
    @Field(name="assignedTo")
    private List<NameValuePair> assignedTo;
    
//    @Field(name="relatesToId")
//    private String relatesToId;
//    
//    @Field(name="relatesToName")
//    private String relatesToName;
    @Embedded
    @Field(name="relatesTo")
    private NameValuePair relatesTo;
    
    @Field(name="status")
    private int status;
    
//    @Field(name="assignWhereName")
//    private String assignWhereName;
//    
//    @Field(name="assignWhereValue")
//    private String assignWhereValue;
    @Embedded
    @Field(name="assignWhere")
    private NameValuePair assignWhere;

//    @Field(name="typeName")
//    private String typeName;
//    
//    @Field(name="typeValue")
//    private String typeValue;
    @Embedded
    @Field(name="taskType")
    private NameValuePair taskType;
    
    @Embedded
    @Field(name="clientServiceType")
    private NameValuePair clientServiceType;
    
    @Embedded
    @Field(name="clientServiceCIPCode")
    private NameValuePair clientServiceCIPCode;
    
//    @Field(name="adminTaskTypeName")
//    private String adminTaskTypeName;
//    
//    @Field(name="adminTaskTypeValue")
//    private String adminTaskTypeValue;
    @Embedded
    @Field(name="adminTaskType")
    private NameValuePair adminTaskType;
    
//    @Field(name="priorityName")
//    private String priorityName;
//    
//    @Field(name="priorityValue")
//    private String priorityValue;
    @Embedded
    @Field(name="priority")
    private NameValuePair priority;
    
//    @Field(name="reasonActionName")
//    private String reasonActionName;
//    
//    @Field(name="reasonActionValue")
//    private String reasonActionValue;
    @Embedded
    @Field(name="reasonAction")
    private NameValuePair reasonAction;

    @Field(name="content")
    private String content;
    
    @Field(name="messageComplete")
    private String messageComplete;

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getToDODate() {
        return toDODate;
    }

    public void setToDODate(Date toDODate) {
        this.toDODate = toDODate;
    }

    public NameValuePair getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(NameValuePair assignedBy) {
        this.assignedBy = assignedBy;
    }

    public List<NameValuePair> getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(List<NameValuePair> assignedTo) {
        this.assignedTo = assignedTo;
    }

    public NameValuePair getTaskType() {
        return taskType;
    }

    public void setTaskType(NameValuePair taskType) {
        this.taskType = taskType;
    }

    public NameValuePair getRelatesTo() {
        return relatesTo;
    }

    public void setRelatesTo(NameValuePair relatesTo) {
        this.relatesTo = relatesTo;
    }

    public NameValuePair getAssignWhere() {
        return assignWhere;
    }

    public void setAssignWhere(NameValuePair assignWhere) {
        this.assignWhere = assignWhere;
    }

    public NameValuePair getAdminTaskType() {
        return adminTaskType;
    }

    public void setAdminTaskType(NameValuePair adminTaskType) {
        this.adminTaskType = adminTaskType;
    }

    public NameValuePair getPriority() {
        return priority;
    }

    public void setPriority(NameValuePair priority) {
        this.priority = priority;
    }

    public NameValuePair getReasonAction() {
        return reasonAction;
    }

    public void setReasonAction(NameValuePair reasonAction) {
        this.reasonAction = reasonAction;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageComplete() {
        return messageComplete;
    }

    public void setMessageComplete(String messageComplete) {
        this.messageComplete = messageComplete;
    }

    public NameValuePair getTaskAssignment() {
        return taskAssignment;
    }

    public void setTaskAssignment(NameValuePair taskAssignment) {
        this.taskAssignment = taskAssignment;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public NameValuePair getClientServiceType() {
        return clientServiceType;
    }

    public void setClientServiceType(NameValuePair clientServiceType) {
        this.clientServiceType = clientServiceType;
    }

    public NameValuePair getClientServiceCIPCode() {
        return clientServiceCIPCode;
    }

    public void setClientServiceCIPCode(NameValuePair clientServiceCIPCode) {
        this.clientServiceCIPCode = clientServiceCIPCode;
    }





    
    

    


    /**
     * @return the statusDescription
     */
    public String getStatusDescription() {
        if ( this.getStatus() >= 0 && this.getStatus() < ParticipantTask.STATUS_DESCRIPTION.length) {
            return ParticipantTask.STATUS_DESCRIPTION[this.getStatus()];
        }
        return "";
    }

    /**
     * @param statusDescription the statusDescription to set
     */
    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }




    
    //public enum STATUS { DISABLED, OPEN, STARTED, IN_PROGRESS, COMPLETE }
    public static enum STATUS { 
         UNOPENED, OPENED, NOTCOMPLETED_CLOSED, COMPLETE
    }

    public static String[] STATUS_DESCRIPTION  = { 
         "Unopened", "Opened", "Not completed but closed", "Complete"
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

