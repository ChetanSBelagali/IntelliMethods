/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;
//import org.eclipse.persistence.annotations.Multitenant;
//import static org.eclipse.persistence.annotations.MultitenantType.SINGLE_TABLE;
//import org.eclipse.persistence.annotations.TenantDiscriminatorColumn;

/**
 *
 * @author lenny
 */
@Embeddable
@NoSql(dataFormat=DataFormatType.MAPPED)
public class SurveySection extends EntityBase  implements Serializable {
    

    
    @ElementCollection
    @Field(name="questions")
    //@OneToMany(mappedBy="surveySection", cascade=CascadeType.ALL)
    private List<SurveySectionQuestion> questions;
    
    @Field(name="name")
    private String name = "";
    
    @Field(name="label")
    private String label = "";
    
    @Field(name="status")
    private int status;
    
    @Field(name="isValidatable")
    private Boolean isValidatable;
    
    @Field(name="isEditableSection")
    private Boolean isEditableSection;
    
    @Field(name="isPrintableSection")
    private Boolean isPrintableSection;
    
    
    @Field(name="submitButton")
    private Boolean submit_button;
    
    
    @Field(name="sectionOrder")
    private String section_order;
    
    @Field(name="morePos")
    private String more_pos;
    
    @Field(name="toggleSection")
    private Boolean toggle_section;
    

    public Boolean getIsValidatable() {
        return isValidatable;
    }

    public void setIsValidatable(Boolean isValidatable) {
        this.isValidatable = isValidatable;
    }

    public Boolean getIsEditableSection() {
        return isEditableSection;
    }

    public void setIsEditableSection(Boolean isEditableSection) {
        this.isEditableSection = isEditableSection;
    }
    
    private String description = "";
    
    private String thumbnailUrl = "";
    



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
     * @return the thumbnailUrl
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * @param thumbnailUrl the thumbnailUrl to set
     */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }



    /**
     * @return the questions
     */
    public List<SurveySectionQuestion> getQuestions() {
        return questions;
    }

    /**
     * @param questions the questions to set
     */
    public void setQuestions(List<SurveySectionQuestion> questions) {
        this.questions = questions;
    }

    /**
     * @return the isPrintableSection
     */
    public Boolean getIsPrintableSection() {
        return isPrintableSection;
    }

    /**
     * @param isPrintableSection the isPrintableSection to set
     */
    public void setIsPrintableSection(Boolean isPrintableSection) {
        this.isPrintableSection = isPrintableSection;
    }

    /**
     * @return the submit_button
     */
    public Boolean getSubmit_button() {
        return submit_button;
    }

    /**
     * @param submit_button the submit_button to set
     */
    public void setSubmit_button(Boolean submit_button) {
        this.submit_button = submit_button;
    }

    /**
     * @return the section_order
     */
    public String getSection_order() {
        return section_order;
    }

    /**
     * @param section_order the section_order to set
     */
    public void setSection_order(String section_order) {
        this.section_order = section_order;
    }

    /**
     * @return the more_pos
     */
    public String getMore_pos() {
        return more_pos;
    }

    /**
     * @param more_pos the more_pos to set
     */
    public void setMore_pos(String more_pos) {
        this.more_pos = more_pos;
    }

    /**
     * @return the toggle_section
     */
    public Boolean getToggle_section() {
        return toggle_section;
    }

    /**
     * @param toggle_section the toggle_section to set
     */
    public void setToggle_section(Boolean toggle_section) {
        this.toggle_section = toggle_section;
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
    
}
