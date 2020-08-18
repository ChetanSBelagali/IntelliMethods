/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.model;

import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 *
 * @author lenny
 */
@Embeddable
@NoSql(dataFormat=DataFormatType.MAPPED)
//@XmlRootElement
//@Table ( name="survey_section_question")
public class SurveySectionQuestion extends EntityBase implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Field(name="name")
    private String name = "";
    
    @Field(name="alias")
    private String alias = "";
    
    @Field(name="label")
    private String label = "";
    
    @Field (name="type")
    private String type = "text"; //set default to text
    
    @Field (name="placeholder")
    private String placeholder = "";
    
    @Field (name="helper")
    private String helper = "";
    
    @Field (name="required")
    private boolean required = false;
    
    @Field (name="widget")
    private String widget = "text";
    
    @Field ( name="isEditable")
    private Boolean isEditable = false;
    
    @Field ( name="confirmField")
    private Boolean confirmField = false;
    
    @Field(name="headerStyle")
    private String headerStyle;
    
    @Field (name="depends")
    private String depends;
    
    @ElementCollection
    @Field (name="options")
    private List<String> options = new ArrayList<String>();
    
    @ElementCollection
    @Field (name="optionLabels")
    private List<String> optionLabels = new ArrayList<String>();
    
    @Field ( name="postSaveAction")
    private String postSaveAction = "";

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
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the placeHolder
     */
    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * @param placeholder the placeholder to set
     */
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    /**
     * @return the helper
     */
    public String getHelper() {
        return helper;
    }

    /**
     * @param helper the helper to set
     */
    public void setHelper(String helper) {
        this.helper = helper;
    }

    /**
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @return the widget
     */
    public String getWidget() {
        return widget;
    }

    /**
     * @param widget the widget to set
     */
    public void setWidget(String widget) {
        this.widget = widget;
    }

    /**
     * @return the isEditable
     */
    public Boolean getIsEditable() {
        return isEditable;
    }

    /**
     * @param isEditable the isEditable to set
     */
    public void setIsEditable(Boolean isEditable) {
        this.isEditable = isEditable;
    }

    /**
     * @return the headerStyle
     */
    public String getHeaderStyle() {
        return headerStyle;
    }

    /**
     * @param headerStyle the headerStyle to set
     */
    public void setHeaderStyle(String headerStyle) {
        this.headerStyle = headerStyle;
    }

    /**
     * @return the depends
     */
    public String getDepends() {
        return depends;
    }

    /**
     * @param depends the depends to set
     */
    public void setDepends(String depends) {
        this.depends = depends;
    }

    /**
     * @return the options
     */
    public List<String> getOptions() {
        return options;
    }

    /**
     * @param options the options to set
     */
    public void setOptions(List<String> options) {
        this.options = options;
    }

    /**
     * @return the optionLabels
     */
    public List<String> getOptionLabels() {
        return optionLabels;
    }

    /**
     * @param optionLabels the optionLabels to set
     */
    public void setOptionLabels(List<String> optionLabels) {
        this.optionLabels = optionLabels;
    }

    /**
     * @return the postSaveAction
     */
    public String getPostSaveAction() {
        return postSaveAction;
    }

    /**
     * @param postSaveAction the postSaveAction to set
     */
    public void setPostSaveAction(String postSaveAction) {
        this.postSaveAction = postSaveAction;
    }

    /**
     * @return the confirmField
     */
    public Boolean getConfirmField() {
        return confirmField;
    }

    /**
     * @param confirmField the confirmField to set
     */
    public void setConfirmField(Boolean confirmField) {
        this.confirmField = confirmField;
    }
}
