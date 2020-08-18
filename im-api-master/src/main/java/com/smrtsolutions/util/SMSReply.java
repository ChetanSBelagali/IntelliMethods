/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.util;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SMSReply implements Serializable {
    private String _id;
    private String source_number; 
    private String destination_number; 
    private String message_id; 
    private Boolean replystatus; 
    private String date_received;
    private String userId; 

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    //private String metadata; 
    private String reply_id; 
    //private String vendor_account_id; 
    private String content; 

    public Boolean getReplystatus() {
        return replystatus;
    }

    public void setReplystatus(Boolean replystatus) {
        this.replystatus = replystatus;
    }
    

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }
    

    public String getSource_number() {
        return source_number;
    }

    public void setSource_number(String source_number) {
        this.source_number = source_number;
    }

    public String getDestination_number() {
        return destination_number;
    }

    public void setDestination_number(String destination_number) {
        this.destination_number = destination_number;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getDate_received() {
        return date_received;
    }

    public void setDate_received(String date_received) {
        this.date_received = date_received;
    }

    public String getReply_id() {
        return reply_id;
    }

    public void setReply_id(String reply_id) {
        this.reply_id = reply_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

