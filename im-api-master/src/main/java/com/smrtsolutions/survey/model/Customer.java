/*
 * SMRT Solutions
 * Data Collection Portal
 */
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;
//import org.eclipse.persistence.annotations.Multitenant;
//import static org.eclipse.persistence.annotations.MultitenantType.SINGLE_TABLE;

/**
 *
 * @author lenny
 */
@Entity
@XmlRootElement
@NoSql(dataType = "customer", dataFormat = DataFormatType.MAPPED)
public class Customer extends EntityBase implements Serializable {
    //private static final long serialVersionUID = 1L;

    public static String SYSTEM_MASTER_CUSTOMER_ID = "1";

    /**
     * Key is used for linking a URL/subdomain to a customer
     */
    @Field(name = "urlKey")
    private String urlKey;

    @Field(name = "name")
    private String name;

    @Field(name = "status")
    private int status;

    @Field(name = "serviceLevel")
    private int serviceLevel;

    @XmlTransient
    @Field(name = "registrationCode")
    private String registrationCode = "";

    @Field(name = "brandingHtml")
    private String brandingHtml = "";

    @Field(name = "customerCode")
    private String customerCode = "";
    
    @Field(name="studentViewDesc")
    private String studentViewDesc="";
    
    @Field(name="createdOn")
    @Temporal(TemporalType.DATE)
    private Date createdOn;
    
    @Field(name="superAdminPassword")
    private String superadminpass;

    public String getSuperadminpass() {
        return superadminpass;
    }

    public void setSuperadminpass(String superadminpass) {
        this.superadminpass = superadminpass;
    }
    
    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
    

    public String getStudentViewDesc() {
        return studentViewDesc;
    }

    public void setStudentViewDesc(String studentViewDesc) {
        this.studentViewDesc = studentViewDesc;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode.substring(0, 1).toUpperCase() + customerCode.substring(1);
    }

    @Field(name = "customerName")
    private String customerName = "";

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName.substring(0, 1).toUpperCase() + customerName.substring(1);
    }

    @Field(name = "serviceStartDate")
    private String serviceStartDate = "";

    public String getServiceStartDate() {
        return serviceStartDate;
    }

    public void setServiceStartDate(String serviceStartDate) {
        this.serviceStartDate = serviceStartDate;
    }

    @Field(name = "serviceEndDate")
    private String serviceEndDate = "";

    public String getServiceEndDate() {
        return serviceEndDate;
    }

    public void setServiceEndDate(String serviceEndDate) {
        this.serviceEndDate = serviceEndDate;
    }

    @Field(name = "address")
    private String address = "";

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Field(name = "country")
    private String country = "";

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Field(name = "state")
    private String state = "";

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Field(name = "zipCode")
    private String zipCode = "";

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @Field(name = "contactPerson")
    private String contactPerson = "";

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    @Field(name = "email")
    private String email = "";

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Field(name = "demoClient")
    private String demoClient = "";

    @ElementCollection
    @Field(name = "acl")
    private List<NameValuePair> acl = new ArrayList<NameValuePair>();

    @ElementCollection
    @Field(name = "settings")
    private List<NameValuePair> settings = new ArrayList<NameValuePair>();

    @ElementCollection
    @Field(name = "permissions")
    private List<NameValuePair> permissions = new ArrayList<NameValuePair>();

    @ElementCollection
    @Field(name = "tab_permissions")
    private List<NameValuePair> tabPermissions = new ArrayList<NameValuePair>();

    @ElementCollection
    @Field(name = "roles")
    private List<SMRTRole> roles = new ArrayList<SMRTRole>();

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
     * @return the registrationCode
     */
    //@XmlTransient
    public String getRegistrationCode() {
        return registrationCode;
    }

    /**
     * @param registrationCode the registrationCode to set
     */
    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode;
    }

    /**
     * @return the brandingHtml
     */
    public String getBrandingHtml() {
        return brandingHtml;
    }

    /**
     * @param brandingHtml the brandingHtml to set
     */
    public void setBrandingHtml(String brandingHtml) {
        this.brandingHtml = brandingHtml;
    }

    /**
     * @return the roles
     */
    public List<SMRTRole> getRoles() {
        return roles;
    }

    /**
     * @param roles the roles to set
     */
    public void setRoles(List<SMRTRole> roles) {
        this.roles = roles;
    }

    /**
     * @param settings the settings to set
     */
    public void setSettings(List<NameValuePair> settings) {
        this.settings = settings;
    }

    public String getDemoClient() {
      return demoClient;
    }

    public void setDemoClient(String demoClient) {
        this.demoClient = demoClient;
    }

    /**
     *
     * @return the settings list
     */
    public List<NameValuePair> getSettings() {
        return this.settings;
    }

    public List<NameValuePair> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<NameValuePair> permissions) {
        this.permissions = permissions;
    }

    public List<NameValuePair> getTabPermissions() {
        return tabPermissions;
    }

    public void setTabPermissions(List<NameValuePair> tabPermissions) {
        this.tabPermissions = tabPermissions;
    }

    @Transient
    public String getSetting(String name, String def) {
        if (this.settings == null) {
            return def;
        }
        for (NameValuePair nvp : this.settings) {
            if (nvp.getName().equalsIgnoreCase(name)) {
                return Util.getValue(nvp.getValue(), def);
            }
        }
        return def;
    }

    @Transient
    public SMRTRole findRole(String name) {
        for (SMRTRole r : this.getRoles()) {
            if (r.getName().equalsIgnoreCase(name)) {
                return r;
            }
        }
        return null;
    }

    /**
     * @return the urlKey
     */
    public String getUrlKey() {
        return urlKey;
    }

    /**
     * @param urlKey the urlKey to set
     */
    public void setUrlKey(String urlKey) {
        this.urlKey = urlKey;
    }

    public int getServiceLevel() {
        return serviceLevel;
    }

    public void setServiceLevel(int serviceLevel) {
        this.serviceLevel = serviceLevel;
    }

    public List<NameValuePair> getAcl() {
        return acl;
    }

    public void setAcl(List<NameValuePair> acl) {
        this.acl = acl;
    }


}
