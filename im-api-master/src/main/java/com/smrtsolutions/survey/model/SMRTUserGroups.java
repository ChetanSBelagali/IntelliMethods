/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.survey.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 *
 * @author joy
 */
@Entity
@NoSql(dataType="SMRTUserGroups", dataFormat=DataFormatType.MAPPED)
@XmlRootElement
public class SMRTUserGroups extends EntityBase implements Serializable {
    //private static final long serialVersionUID = 1L;
    
}
