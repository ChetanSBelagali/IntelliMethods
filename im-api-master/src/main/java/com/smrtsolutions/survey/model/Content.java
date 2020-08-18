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
import javax.persistence.Entity;
import javax.persistence.Transient;
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
@NoSql(dataType="content", dataFormat=DataFormatType.MAPPED)
public class Content extends EntityBase implements Serializable {
    
    @Field(name="name")
    private String name;
    
    @Field(name="description")
    private String description;
    
    
    @Field(name="htmlTemplate")
    private String htmlTemplate ;
    
    @Field(name="variables")
    @ElementCollection
    private List<NameValuePair> variables = new ArrayList<NameValuePair>();
    
    @Field(name="settings")
    @ElementCollection
    private List<NameValuePair> settings = new ArrayList<NameValuePair>();

    
    @Transient
    private List<NameValuePair> customerSettings;
    /**
     * @return the htmlTemplate
     */
    public String getHtmlTemplate() {
        return htmlTemplate;
    }

    /**
     * @param htmlTemplate the htmlTemplate to set
     */
    public void setHtmlTemplate(String htmlTemplate) {
        this.htmlTemplate = htmlTemplate;
    }

    /**
     * @return the variables
     */
    public List<NameValuePair> getVariables() {
        return variables;
    }

    /**
     * @param variables the variables to set
     */
    public void setVariables(List<NameValuePair> variables) {
        this.variables = variables;
    }

    /**
     * @return the settings
     */
    public List<NameValuePair> getSettings() {
        return settings;
    }

    /**
     * @param settings the settings to set
     */
    public void setSettings(List<NameValuePair> settings) {
        this.settings = settings;
    }
    
    //@Transient 
    //@XmlTransient
    public Map<String,String> getVariablesMap(){
        Map<String,String> m = new HashMap<String,String>();
        if ( this.variables == null) return m;
        for ( NameValuePair nvp : this.variables){          
                m.put(nvp.getName(), nvp.getValue());
        }
        return m;
    }
    
    //@Transient 
    //@XmlTransient
    public String getVariable(String name, String def){
        if ( this.variables == null) return def;
        for ( NameValuePair nvp : this.variables){
            if ( nvp.getName().equalsIgnoreCase(name)) {
                return Util.getValue(nvp.getValue(), def);
            }
        }
        return def;
    }
    
     //@Transient 
     //@XmlTransient
    public String getSetting(String name, String def){
        if ( this.settings == null) return def;
        for ( NameValuePair nvp : this.settings){
            if ( nvp.getName().equalsIgnoreCase(name)) {
                return Util.getValue(nvp.getValue(), def);
            }
        }
        return def;
    }
    
    //@Transient 
    //@XmlTransient
    public Map<String,String> getSettingsMap(){
        Map<String,String> m = new HashMap<String,String>();
        if ( this.settings == null) return m;
        for ( NameValuePair nvp : this.settings){          
                m.put(nvp.getName(), nvp.getValue());
        }
        return m;
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
       
    
}
