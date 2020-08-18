/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.util;

import com.smrtsolutions.survey.model.Permission;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author lenny
 */
@XmlRootElement
public class Token {
    private String accessToken = "";
    
    private String name = "";
    
    private String customerBranding = "";
    
    private String home = "";
    
    private int serviceLevel =0;
    
    private String[] roles = {};
    
    private String[] permissions = {};
    
    private String[] tabs = {};
        
    private String customerId = "";
    
    private String regCode ="";
    
    private List<Permission> userPermission;
    

    /**
     * @return the accessToken
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @param accessToken the accessToken to set
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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
     * @return the customerBranding
     */
    public String getCustomerBranding() {
        return customerBranding;
    }

    /**
     * @param customerBranding the customerBranding to set
     */
    public void setCustomerBranding(String customerBranding) {
        this.customerBranding = customerBranding;
    }

    /**
     * @return the home
     */
    public String getHome() {
        return home;
    }

    /**
     * @param home the home to set
     */
    public void setHome(String home) {
        this.home = home;
    }

    /**
     * @return the roles
     */
    public String[] getRoles() {
        return roles;
    }

    /**
     * @param roles the roles to set
     */
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    /**
     * @return the permissions
     */
    public String[] getPermissions() {
        return permissions;
    }

    /**
     * @param permissions the permissions to set
     */
    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    public String[] getTabs() {
        return tabs;
    }

    public void setTabs(String[] tabs) {
        this.tabs = tabs;
    }
    
    
    public int getServiceLevel() {
        return serviceLevel;
    }

    public void setServiceLevel(int serviceLevel) {
        this.serviceLevel = serviceLevel;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getRegCode() {
        return regCode;
    }

    public void setRegCode(String regCode) {
        this.regCode = regCode;
    }

    public List<Permission> getUserPermission() {
        return userPermission;
    }

    public void setUserPermission(List<Permission> userPermission) {
        this.userPermission = userPermission;
    }


    
    
}
