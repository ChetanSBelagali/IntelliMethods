package com.smrtsolutions.survey.model;

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 *
 * @author Baskar
 */
@Embeddable
@XmlRootElement
@NoSql(dataType="permission", dataFormat=DataFormatType.MAPPED)
public class Permission implements Serializable {
    
    @Field(name="module_id")
    private String module_id; 
    
    @Field(name="canview")
    private Boolean canview;
    
    @Field(name="canadd")
    private Boolean canadd;

    @Field(name="canedit")
    private Boolean canedit;
    
    @Field(name="caneditval")
    private Boolean caneditval;
    
    @Field(name="deploy")
    private Boolean deploy;

    public Boolean getDeploy() {
        return deploy;
    }

    public void setDeploy(Boolean deploy) {
        this.deploy = deploy;
    }

    public String getModule_id() {
        return module_id;
    }

    public void setModule_id(String module_id) {
        this.module_id = module_id;
    }
    
    public Boolean getCanview() {
        return canview;
    }

    public void setCanview(Boolean canview) {
        this.canview = canview;
    }

    public Boolean getCanadd() {
        return canadd;
    }

    public void setCanadd(Boolean canadd) {
        this.canadd = canadd;
    }

    public Boolean getCanedit() {
        return canedit;
    }

    public void setCanedit(Boolean canedit) {
        this.canedit = canedit;
    }
    
    public Boolean getCaneditval() {
        return caneditval;
    }
    
    public void setCaneditval(Boolean caneditval) {
        this.caneditval = caneditval;
    }

}
