/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.survey.model;

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

/**
 *
 * @author joy
 */
@Embeddable
@XmlRootElement
@NoSql(dataType="taskAssignedTo", dataFormat=DataFormatType.INDEXED)
public class ParticipantAssignedTo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Field(name="name")
    private String name;
    
    @Field(name="id")
    private String id;
    
    @Field(name="type")
    private String type;
}
