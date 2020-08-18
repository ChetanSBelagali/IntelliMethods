/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;
/**
 *
 * @author 
 */
@Entity
@NoSql(dataType="SMRTUser", dataFormat=DataFormatType.MAPPED)
@XmlRootElement
public class SMRTUser extends EntityBase implements Serializable {
    //private static final long serialVersionUID = 1L;

    @Transient
    private boolean systemTokenUser;
    
    @Field(name="loginName")
    private String loginName;
    
    @Field(name="lastactivity")
    private String lastactivity;

    public String getLastactivity() {
        return lastactivity;
    }

    public void setLastactivity(String lastactivity) {
        this.lastactivity = lastactivity;
    }
    
    @Field(name="name")
    private String name;
    
    @Field(name="firstname")
    private String firstname;
    
    @Field(name="lastname")
    private String lastname;
    
       
    @Field(name="organization")
    private String organization;
    
    @Field(name="resetpassword")
    private String resetpassword;

    public String getResetpassword() {
        return resetpassword;
    }

    public void setResetpassword(String resetpassword) {
        this.resetpassword = resetpassword;
    }
    
    @Field(name="userpermission")
    private String userpermission;

    public String getUserpermission() {
        return userpermission;
    }

    public void setUserpermission(String userpermission) {
        this.userpermission = userpermission;
    }
    
    @Field(name="email")
    private String email;
    
    @Field(name="address")
    private String address;
    
    @Field(name="city")
    private String city;
    
    @Field(name="state")
    private String state;
    
    @Field(name="zip")
    private String zip;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
    
    @Field(name="password")
    private String password;
    
    @Field(name="usertype")
    private String usertype;
    
    @Field(name="status")
    private int status;
    
    // Kriyatec - Start 
    @Field(name="partnerId")
    private String partnerId;
    
    @Field(name="permission_template_id")
    private String permission_template_id;
    
    @Field(name="permission_type")
    private String permission_type;

    @Field(name="createdOn")
    @Temporal(TemporalType.DATE) 
    private Date createdOn;
    
    @ElementCollection
    @Field(name="servicename")
    private List<String>servicename;
    
    @ElementCollection
    @Field(name="location")
    private List<String>location;
    
    
    
    @Field(name="phonenumber")
    private String phonenumber;

    @Field(name="last_login_date")
    @Temporal(TemporalType.DATE) 
    private Date last_login_date;

    @ElementCollection
    @Field (name="verify")
    private List<String> verify;

    public List<String> getVerify() {
        return verify;
    }

    public void setVerify(List<String> verify) {
        this.verify = verify;
    }
  
    
    public Date getLast_login_date() {
        return last_login_date;
    }

    public void setLast_login_date(Date last_login_date) {
        this.last_login_date = last_login_date;
    }
     
    
   

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
    
    
    
    public List<String> getServicename() {
        return servicename;
    }

    public void setServicename(List<String> servicename) {
        this.servicename = servicename;
    }

    public List<String> getLocation() {
        return location;
    }

    public void setLocation(List<String> location) {
        this.location = location;
    }

    public static String getSYSTEM_USER_ID() {
        return SYSTEM_USER_ID;
    }

    public static void setSYSTEM_USER_ID(String SYSTEM_USER_ID) {
        SMRTUser.SYSTEM_USER_ID = SYSTEM_USER_ID;
    }
    
  
    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
    
    
    
    public String getPermission_type() {
        return permission_type;
    }

    public void setPermission_type(String permission_type) {
        this.permission_type = permission_type;
    }

    public String getPermission_template_id() {
        return permission_template_id;
    }

    public void setPermission_template_id(String permission_template_id) {
        this.permission_template_id = permission_template_id;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }
    
    @ElementCollection
    @Field(name="permissions")
    private List<Permission> permissions;
    

    // Kriyatec - End
    
    
    @Transient
    //@XmlTransient
    private String code;
    
    @Transient
    private Customer customer;
    
    @ElementCollection
    @Field(name="roles")
    //private List<SMRTRole> roles;
    private List<String> roles;
    
    public static String SYSTEM_USER_ID = "1";

    /*@Transient */
    public Customer getCustomer(){
        /*Customer c = new  Customer();
        c.setId(this.getId());
        //c.setId(0);
        return c;
        */
        return this.customer;
    }
    
    //@Transient 
    public void setCustomer(Customer c){
        //this.setId( c.getId()+"");
        this.customer = c;
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

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    
    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
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

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the code
     */
    /*public String getCode() {
        return code;
    }*/

    /**
     * @param code the code to set
     */
    /*public void setCode(String code) {
        this.code = code;
    }*/

    /**
     * @return the roles
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * @param roles the roles to set
     */
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    /**
     * @return the loginName
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * @param loginName the loginName to set
     */
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the systemTokenUser
     */
    public boolean isSystemTokenUser() {
        return systemTokenUser;
    }

    /**
     * @param systemTokenUser the systemTokenUser to set
     */
    public void setSystemTokenUser(boolean systemTokenUser) {
        this.systemTokenUser = systemTokenUser;
    }

    public String getUsertype() {
        return usertype;
    }

//    Kriyatec - Start
    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }
}
