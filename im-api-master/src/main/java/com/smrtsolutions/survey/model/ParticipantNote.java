/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.model;

import java.io.Serializable;
import java.util.ArrayList;
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
 * @author lenny
 */
@Entity
@XmlRootElement
@NoSql(dataType="participant_note", dataFormat=DataFormatType.MAPPED)
public class ParticipantNote extends EntityBase  
implements Serializable, Cloneable {
    
    private static final long serialVersionUID = 1L;
    
    @Field(name="createdAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    
    @Field(name="author")
    private NameValuePair author;
    
    @Field(name="participantId")
    private String participantId;

    @Field(name="status")
    private int status;
    
    @ElementCollection
    @Field(name="receipient")
    private List<Receipient> receipient = new ArrayList<Receipient>();
    
    @Field(name="reason")
    private NameValuePair reason;
    
    @Field(name="priority")
    private NameValuePair priority;

    @Field(name="content")
    private String content;
    

    
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
     * @return the author
     */
    public NameValuePair getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(NameValuePair author) {
        this.author = author;
    }

    /**
     * @return the reason
     */
    public NameValuePair getReason() {
        return reason;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(NameValuePair reason) {
        this.reason = reason;
    }

    /**
     * @return the priority
     */
    public NameValuePair getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(NameValuePair priority) {
        this.priority = priority;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the statusDescription
     */
    public String getStatusDescription() {
        if ( this.getStatus() >= 0 && this.getStatus() < ParticipantNote.STATUS_DESCRIPTION.length) {
            return ParticipantNote.STATUS_DESCRIPTION[this.getStatus()];
        }
        return "";
    }

    /**
     * @param statusDescription the statusDescription to set
     */
    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    /**
     * @return the receipient
     */
    public List<Receipient> getReceipient() {
        return receipient;
    }

    /**
     * @param receipient the receipient to set
     */
    public void setReceipient(List<Receipient> receipient) {
        this.receipient = receipient;
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
