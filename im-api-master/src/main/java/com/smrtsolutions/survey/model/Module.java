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
@NoSql(dataType="module", dataFormat=DataFormatType.MAPPED)
public class Module extends EntityBase implements Serializable {
       
    @Field(name="name")
    private String name;
    
    @Field(name="type")
    private String type;
    
    @Field(name="survey_type")
    private String surveytype;

    @Field(name="parent_id")
    private String parentid;

    
    @Field(name="add")
    private Boolean add;

    @Field(name="edit")
    private Boolean edit;

    @Field(name="view")
    private Boolean view;
    
    @Field(name="display_order")
    private int displayorder;
    
    @Field(name="tabId")
    private String tabId;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
      
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSurveytype() {
        return surveytype;
    }

    public void setSurveytype(String surveytype) {
        this.surveytype = surveytype;
    }
    
    
    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public Boolean getAdd() {
        return add;
    }

    public void setAdd(Boolean add) {
        this.add = add;
    }

    public Boolean getEdit() {
        return edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public Boolean getView() {
        return view;
    }

    public void setView(Boolean view) {
        this.view = view;
    }

    public int getDisplayorder() {
        return displayorder;
    }

    public void setDisplayorder(int displayorder) {
        this.displayorder = displayorder;
    }

    public String getTabId() {
        return tabId;
    }

    public void setTabId(String tabId) {
        this.tabId = tabId;
    }

  

    
    
    
    
    
}
