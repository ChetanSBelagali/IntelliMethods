/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

@Entity
@NoSql(dataType="group", dataFormat=DataFormatType.MAPPED)
@XmlRootElement
public class Group extends EntityBase implements Serializable {
    //private static final long serialVersionUID = 1L;
    
    @Field(name="customerId")
    private String customerId;
    
    @Field(name="name")
    private String name;
    
    @Field(name="isDefault")
    private Boolean isDefault; 
    
    @Field(name="description")
    private String description;
    
    @ElementCollection
    @Field(name="contents")
    private List<String> contents;

    @ElementCollection
    @Field(name="groupOwner")
    private List<String> groupOwner;

    @ElementCollection
    @Field(name="textnotification")
    private List<String> textnotification;

    public List<String> getTextnotification() {
        return textnotification;
    }

    public void setTextnotification(List<String> textnotification) {
        this.textnotification = textnotification;
    }
    
    
    @Field(name="groupType")
    private String groupType;
  
    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }
    
    
      public List<String> getGroupOwner() {
        return groupOwner;
    }

    public void setGroupOwner(List<String> groupOwner) {
        this.groupOwner = groupOwner;
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
     * @return the contents
     */
    public List<String> getContents() {
        return contents;
    }

    /**
     * @param contents the contents to set
     */
    public void setContents(List<String> contents) {
        this.contents = contents;
    }
    public Boolean getisDefault() {
        return isDefault;
    }

    public void setisDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}
