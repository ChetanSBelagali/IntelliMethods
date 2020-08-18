/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import javax.xml.bind.annotation.XmlRootElement;
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
//@Entity
/*@Multitenant(SINGLE_TABLE)
@TenantDiscriminatorColumn(name = "customer_id", contextProperty = "participant-tenant.id")*/
//@XmlRootElement
/*@Table(name="smrt_role"
, uniqueConstraints=@UniqueConstraint(columnNames={"customer_id", "name"})
)*/
@Embeddable
@XmlRootElement
@NoSql(dataType="smrt_role", dataFormat=DataFormatType.INDEXED)
public class SMRTRole implements Serializable {
    private static final long serialVersionUID = 1L;


    @Field(name="identifier")
    private long identifier;
    

    @Field(name="name")
    private String name;
    
    @Field(name="description")
    private String description;

    /**
     * @return the permissions
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * @param permissions the permissions to set
     */
    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }
    public static final String ALL = "ALL";
    public static final String SELF_DATA_ADD = "SELF_DATA_ADD";
    public static final String SELF_DATA_EDIT = "SELF_DATA_EDIT";
    public static final String SELF_DATA_SEARCH = "SELF_DATA_SEARCH";
    public static final String OTHERS_DATA_ADD = "OTHERS_DATA_ADD";
    public static final String OTHERS_DATA_EDIT = "OTHERS_DATA_EDIT";
    public static final String OTHERS_DATA_SEARCH = "OTHERS_DATA_SEARCH";
    public static final String DASH_ADD = "DASH_ADD";
    public static final String CASE_DASH = "CASE_DASH";
    public static final String PARTICIPANT_DASH = "PARTICIPANT_DASH";
    public static final String EXEC_DASH = "EXEC_DASH";
    
    public static final String NONE = "NONE";

    public static final String CUSTOMER_ADMIN = "CUSTOMER_ADMIN";
    public static final String USER_ADMIN = "USER_ADMIN";
    public static final String SURVEY_ADMIN = "SURVEY_ADMIN";
    public static final String DASH_ADMIN = "DASH_ADMIN";
    public static final String INSTRUCTOR_DASH = "INSTRUCTOR_DASH";
    public static final String OBSERVER_DASH = "OBSERVER_DASH";
    
    public static NameValuePair[] PERMISSIONS =  {
          new NameValuePair(NONE, "None"), new NameValuePair(ALL, "All")
        , new NameValuePair(SELF_DATA_ADD, "Add your data")
        , new NameValuePair(SELF_DATA_EDIT, "Edit your data"), new NameValuePair("SELF_DATA_DEL", "Delete your data")
        , new NameValuePair(SELF_DATA_SEARCH, "Search your data"), new NameValuePair("SELF_DATA_VIEW", "View your data")
        , new NameValuePair(OTHERS_DATA_ADD, "Add data for others")
        , new NameValuePair(OTHERS_DATA_EDIT, "Edit other's data"), new NameValuePair("OTHERS_DATA_DEL", "Delete other's data")
        , new NameValuePair(OTHERS_DATA_SEARCH, "Search all data"), new NameValuePair("OTHERS_DATA_VIEW", "View all data")
        , new NameValuePair(DASH_ADD, "Add Dashboard"), new NameValuePair("DASH_VIEW", "View Dashboards")
        , new NameValuePair(CASE_DASH, "Case Manager Dashboard"), new NameValuePair("TEACHER_DASH", "Teacher Dashboard")
        , new NameValuePair(PARTICIPANT_DASH, "Participant Dashboard"), new NameValuePair("EXEC_DASH", "Executive Dashboard")
        , new NameValuePair(USER_ADMIN, "User Administration"), new NameValuePair(SURVEY_ADMIN, "Survey Administration")
        , new NameValuePair(DASH_ADMIN, "Dashboard Administration"), new NameValuePair(CUSTOMER_ADMIN, "Customer Administration")
        , new NameValuePair(INSTRUCTOR_DASH, "Instructor Dashboard"), new NameValuePair(OBSERVER_DASH, "Observer Dashboard")
    
    };
    
    public static NameValuePair[] TAB_PERMISSIONS =  {
        new NameValuePair("DEMOGRAPHICS", "Demographics")
        , new NameValuePair("ENROLLMENT", "Enrollment")
        , new NameValuePair("ASSESSMENTS", "Sssessments")
        , new NameValuePair("STUDENT_SURVEYS", "Student Surveys")
        , new NameValuePair("OUTCOMES", "Outcomes")
        , new NameValuePair("TASKS", "Tasks")
        , new NameValuePair("ADMIN", "Admin") 
        , new NameValuePair("AUDIT", "Audit")
        , new NameValuePair("SNAP_TANF", "Snap Tanf")
        , new NameValuePair("TRANSITIONS", "Transitions") 
        , new NameValuePair("GRANT_MGMT", "Grant Management")
        , new NameValuePair("REPORTS", "Reports")        
    };
    
    public static NameValuePair getPermission(String name) throws Exception{
        for ( int i =0; i< PERMISSIONS.length; i++){
            if ( (PERMISSIONS[i].getName().equalsIgnoreCase(name)))
            {
                return PERMISSIONS[i];
            }
        }
        throw new Exception("INVALID PERMISSION " + name );
    }
    
    public static List<NameValuePair> getAllPermissions() throws Exception{

        List<NameValuePair> ap = new ArrayList<>();
        for(NameValuePair nv: PERMISSIONS){
            ap.add(nv);
        }
        return ap;
//        throw new Exception("ERROR RETRIEVING ALL PERMISSIONS " + name );
    }
    
    public static List<NameValuePair> getAllTabPermissions() throws Exception{

        List<NameValuePair> ap = new ArrayList<>();
        for(NameValuePair nv: TAB_PERMISSIONS){
            ap.add(nv);
        }
        return ap;
//        throw new Exception("ERROR RETRIEVING ALL PERMISSIONS " + name );
    }
    
    /*
     * Perissions as a CSV string
     *
     */
    @Field(name="permissions")
    private String permissions;
    
    @Field(name="tabs")
    private String tabs;
    
    @Field(name="status")
    private int status;
    




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
     * @return the identifier
     */
    public long getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }

    public String getTabs() {
        return tabs;
    }

    public void setTabs(String tabs) {
        this.tabs = tabs;
    }

}
