

/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import javax.xml.bind.annotation.XmlRootElement;
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
@NoSql(dataType = "Location", dataFormat = DataFormatType.MAPPED)
public class Location extends EntityBase implements Serializable {
    //private static final long serialVersionUID = 1L;

    //public static String SYSTEM_MASTER_CUSTOMER_ID = "1";

    /**
     * Key is used for linking a URL/subdomain to a customer
     */
    
    @Field(name = "customerId")
    private String customerId;

    @Field(name = "location")
    private String location;
    
    @Field(name = "address")
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Field(name="createdBy")
    private String createdBy;
    
    @Field(name="createdOn")
    @Temporal(TemporalType.DATE) 
    private Date createdOn;

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    


}
