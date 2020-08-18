/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.JoinField;
import org.eclipse.persistence.nosql.annotations.NoSql;
//import org.eclipse.persistence.annotations.Multitenant;
//import static org.eclipse.persistence.annotations.MultitenantType.SINGLE_TABLE;
//import org.eclipse.persistence.annotations.TenantDiscriminatorColumn;

/**
 *
 * @author lenny
 */
//@Entity
/*@Multitenant(SINGLE_TABLE)
@TenantDiscriminatorColumn(name = "customer_id", contextProperty = "participant-tenant.id")*/
//@XmlRootElement
@Embeddable
@NoSql(dataType="participant", dataFormat=DataFormatType.MAPPED)
public class Participant implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    //@GeneratedValue(generator="system-uuid")
    @GeneratedValue(strategy=GenerationType.TABLE)
    @Field(name="participant_id")
    private long id;
    
    @ManyToOne(optional=false)
    @JoinField(name="customer_id")
    private Customer customer;
    
    /*
    @ManyToOne(optional=false)
    @JoinColumn(name="participant_type_id",referencedColumnName="participant_type_id")
    private ParticipantType participantType;
    */
    
    @Field(name="name")
    private String name;
    
    @Field(name="status")
    private int status;

        public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += id;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Participant)) {
            return false;
        }
        Participant other = (Participant) object;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        //return "com.smrtsolutions.survey.Customer[ id=" + id + " ]";
        return id+"" ;
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
     * @return the customer
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * @param customer the customer to set
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    /**
     * @return the participantType
     */
    /*public ParticipantType getParticipantType() {
        return participantType;
    }*/

    /**
     * @param participantType the participantType to set
     */
    /*public void setParticipantType(ParticipantType participantType) {
        this.participantType = participantType;
    }*/
    
}
