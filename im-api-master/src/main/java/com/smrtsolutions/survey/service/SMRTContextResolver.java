/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.survey.service;

import com.smrtsolutions.survey.model.*;
import com.smrtsolutions.util.Token;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

/**
 *
 * @author lenny
 */
@Provider
public class SMRTContextResolver implements ContextResolver<JAXBContext>{
    
      
   

    private JAXBContext context;
    private Class<?>[] types = { Token.class, SMRTUser.class, Survey.class, 
        Customer.class, SMRTRole.class, SurveyResult.class
        //, SurveyResultItem.class
            , SurveySection.class, SurveySectionQuestion.class 
            , ParticipantSurveyStatus.class
            , NameValuePair.class
            , Content.class
            , ParticipantNote.class
            , Receipient.class
            , Content.class
            , Dashboard.class
            , ParticipantTask.class
            , Partner.class
    };

    public SMRTContextResolver() throws Exception {
        this.context = new JSONJAXBContext(
                JSONConfiguration.natural().build(), types);
    }

    @Override
    public JAXBContext getContext(Class<?> objectType) {
        for (int i = 0; i < this.types.length; i++)
            if (this.types[i].equals(objectType)) return context;
        //System.out.println(objectType.getPackage().getName());
        //return context;
        System.out.println("SMRTContextResolver not handling " + objectType.getName());
        return null;
    }
}
    

