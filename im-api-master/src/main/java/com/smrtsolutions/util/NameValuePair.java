/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.util;

import java.io.Serializable;

/**
 *
 * @author lenny
 */
public class NameValuePair implements Serializable {
    

    private String name = "";
    

    private String value = "";
    
    public NameValuePair(){
        
    }
    
    public NameValuePair(String name, String value){
        this.setName(name);
        this.setValue(value);
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
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    
}
