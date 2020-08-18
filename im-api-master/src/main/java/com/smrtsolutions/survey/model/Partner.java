package com.smrtsolutions.survey.model;

import com.smrtsolutions.survey.model.NameValuePair;
import com.smrtsolutions.util.Util;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

@Entity
@XmlRootElement
@NoSql(dataType="partner", dataFormat=DataFormatType.MAPPED)
public class Partner extends EntityBase implements Serializable {
    
    
    
    @Field(name="partnerCode")
    private String partnerCode="";
    /**
     * @return the partnerCode
     */
    public String getPartnerCode() {
        return partnerCode;
    }
    /**
     * @param partnerCode the partnerCode to set
     */
    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode.substring(0, 1).toUpperCase() + partnerCode.substring(1);
    }
    
    @Field(name="partnerName")
    private String partnerName="";
    /**
     * @return the partnerName
     */
    public String getPartnerName() {
        return partnerName;
    }
    /**
     * @param partnerName the partnerName to set
     */
    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName.substring(0, 1).toUpperCase() + partnerName.substring(1);
    }
    
    @Field(name="address")
    private String address="";
    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
   @Field(name="country")
   private String country="";

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }
    
    @Field(name="state")
   private String state="";

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }
    
     @Field(name="zipCode")
   private String zipCode="";

    /**
     * @return the zipCode
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * @param zipCode the zipCode to set
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    @Field(name="contactPerson")
   private String contactPerson="";

    /**
     * @return the contactPerson
     */
    public String getContactPerson() {
        return contactPerson;
    }

    /**
     * @param contactPerson the contactPerson to set
     */
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }
    
    @Field(name="email")
   private String email="";

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    
    

}