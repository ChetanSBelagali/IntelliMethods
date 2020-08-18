/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import javax.xml.bind.annotation.XmlRootElement;
//import org.eclipse.persistence.annotations.Multitenant;
//import static org.eclipse.persistence.annotations.MultitenantType.SINGLE_TABLE;
//import org.eclipse.persistence.annotations.TenantDiscriminatorColumn;

/**
 *
 * @author lenny
 */
///@Entity
/*@Multitenant(SINGLE_TABLE)
@TenantDiscriminatorColumn(component = "customer_id", contextProperty = "participant-tenant.id")*/
///@XmlRootElement
public class ErrorLog implements Serializable {
    private static final long serialVersionUID = 1L;
    ///@Id
    ///@GeneratedValue(strategy=GenerationType.TABLE)
    ///@Column(name="error_id", insertable=true, updatable=true, unique=true, nullable=false)
    private long id;
    
    private long customerid;
    
    private String moduleName;
    
    private String component = "";
    
    private String message = "";
    
    private String ipaddress;
    
    private long userid ;
    
    
    
    

    public long  getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    
    /**
     * @return the component
     */
    public String getComponent() {
        return component;
    }

    /**
     * @param component the component to set
     */
    public void setComponent(String component) {
        this.component = component;
    }

    /**
     * @return the customerid
     */
    public long getCustomerid() {
        return customerid;
    }

    /**
     * @param customerid the customerid to set
     */
    public void setCustomerid(long customerid) {
        this.customerid = customerid;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the ipaddress
     */
    public String getIpaddress() {
        return ipaddress;
    }

    /**
     * @param ipaddress the ipaddress to set
     */
    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    /**
     * @return the userid
     */
    public long getUserid() {
        return userid;
    }

    /**
     * @param userid the userid to set
     */
    public void setUserid(long userid) {
        this.userid = userid;
    }

    /**
     * @return the moduleName
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * @param moduleName the moduleName to set
     */
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    

    



    
    
}
