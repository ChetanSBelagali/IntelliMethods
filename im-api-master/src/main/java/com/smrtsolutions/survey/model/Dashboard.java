/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.survey.model;

import com.smrtsolutions.util.Util;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 *
 * @author lenny
 */
 @Entity
@XmlRootElement
@NoSql(dataType="dashboard", dataFormat=DataFormatType.MAPPED)
public class Dashboard extends EntityBase implements Serializable {
    
    @Field(name="name")
    private String name;
    
    @Field(name="surveyId")
    private String surveyId;
    
    @Field(name="description")
    private String description;
    
    
    @Field(name="template")
    private String template;
    
    //@ElementCollection
    @Field(name="content")
    private String content ;
    
    @Transient
    private List<NameValuePair> customerSettings;
    
    
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
     * @return the template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    
           /**
     * @return the customerSettings
     */
    public List<NameValuePair> getCustomerSettings() {
        return customerSettings;
    }

    /**
     * @param customerSettings the customerSettings to set
     */
    public void setCustomerSettings(List<NameValuePair> customerSettings) {
        this.customerSettings = customerSettings;
    }
    
    public String getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(String surveyId) {
        this.surveyId = surveyId;
    }
    
    
}
