package com.smrtsolutions.survey.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 *
 * @author Baskar
 */
@Entity
@NoSql(dataType="permission_templates", dataFormat=DataFormatType.MAPPED)
@XmlRootElement
public class PermissionTemplates extends EntityBase implements Serializable {
    
    @Field(name="name")
    private String name;  

    @ElementCollection
    @Field(name="permissions")
    private List<Permission> permissions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    
    
}

