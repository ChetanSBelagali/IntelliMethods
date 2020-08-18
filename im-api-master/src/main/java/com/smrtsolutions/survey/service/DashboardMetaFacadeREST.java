/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.survey.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.smrtsolutions.exception.InvalidParameterException;
import com.smrtsolutions.survey.model.Content;
import com.smrtsolutions.survey.model.Customer;
import com.smrtsolutions.survey.model.Dashboard;
import com.smrtsolutions.survey.model.NameValuePair;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.util.Util;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author lenny
 */
@Stateless
@Path("asset/customer/{customerId}/dashboard")
public class DashboardMetaFacadeREST extends SMRTAbstractFacade<Dashboard> {
    
     private static final Logger logger = LogManager.getLogger(DashboardMetaFacadeREST.class);
    //@PersistenceContext(unitName = "SMRT_PU")
    private EntityManager em;
    
    public String[] REQUIRED_PERMISSIONS = {SMRTRole.CUSTOMER_ADMIN, SMRTRole.ALL};
        

    public DashboardMetaFacadeREST() {
        super(Dashboard.class);
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Dashboard create(@QueryParam("token") String token, @PathParam("customerId") String customerId, Dashboard entity) throws Exception {
        System.out.println("CREATE CUST DASHBOARD");
        SMRTUser u = this.validateToken(token, customerId);
        System.out.println("User id=" + u.getId());
        this.checkPermissions(REQUIRED_PERMISSIONS);
        System.out.println("HAS PERMISSIONS cust content name=" + entity.getName());
        logger.debug("customer content obj is " + ( entity==null?"NULL": entity.getName()));
        if ( Util.isEmpty(entity.getCustomerId())) {
            entity.setCustomerId(u.getCustomerId());
        }
        return super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Dashboard edit(@QueryParam("token") String token, @PathParam("customerId") String customerId, @PathParam("id") Long id, Dashboard entity) throws Exception {
        SMRTUser u = this.validateToken(token, customerId);
        this.checkPermissions(REQUIRED_PERMISSIONS);
        if ( Util.isEmpty(entity.getCustomerId())) {
            entity.setCustomerId(u.getCustomerId());
        }
        return super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("customerId") String customerId, @PathParam("id") Long id, @QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token, customerId);
        this.checkPermissions(REQUIRED_PERMISSIONS);
        super.remove(super.find(id));
    }

    @GET
    @Path("{name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Dashboard find(@PathParam("name") String name, @QueryParam("token") String token, @QueryParam("surveyId") String surveyId, @Context UriInfo uriInfo) throws Exception {
        String custId = "";
        if ( Util.isEmpty(token)){
            //get customer from URL
            String subdomain = this.getSubDomain(uriInfo);
            if ( Util.isEmpty(subdomain)){
                //no token and no subdomain, use default domain?
                if (uriInfo.getAbsolutePath().getHost().equals("localhost")){
                    custId = "97"; //"smrtdev";
                } else {
                    throw new InvalidParameterException("No customer found");
                }
            } else {
                //find customer based on subdomain
                Customer c = this.findCustomerBySubDomain(subdomain);
                if ( c == null || Util.isEmpty(c.getId())){
                    throw new InvalidParameterException("No customer found");
                }
                custId = c.getId();
            }
        } else {
            
            SMRTUser u = this.validateToken(token);
            custId = u.getCustomerId();
        }
        this.setCustomerId(custId);
        //dont need any permissions
        //this.checkPermissions(REQUIRED_PERMISSIONS);
        
        return this.findDashboard(custId, name, surveyId);
    }
    
    @Path("customersettings")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<NameValuePair> getCustomerSettings(@QueryParam("token") String token, @Context UriInfo uriInfo) throws Exception {
        Customer c= null;
        if ( !Util.isEmpty(token)) {
            SMRTUser u = this.validateToken(token);
            //this.checkPermissions(REQUIRED_PERMISSIONS);
            c = this.findCustomer(u);
        } else {
            //get customer from subdomain
            c = this.findCustomerBySubDomain(token);
        }
        if ( c == null) {
            throw new InvalidParameterException("Invalid customer");
        }
        
        return c.getSettings();
    }
    




    
    private Dashboard findDashboard(String customerId, String name, String surveyId) throws Exception{
        Dashboard c = null;
        try {
            if(name.equals("demographics") && surveyId.equals("")){
                 DB mongoDB = this.getDBInstance();
                DBCollection surveyCollection = mongoDB.getCollection("survey");
                BasicDBObject result =  (BasicDBObject) surveyCollection.findOne(new BasicDBObject("customerId", customerId).append("survey_type", "student").append("isDefault","Yes"));
                if(result!=null){
                    surveyId = result.getString("_id");
                }
            }
            
            String sql = "SELECT c FROM Dashboard c WHERE c.customerId = :customerId and c.name = :name and c.surveyId=:surveyId";
            Query q = this.getEntityManager().createQuery(sql, Dashboard.class);
                q.setParameter("customerId", customerId);
                q.setParameter("name", name);
                q.setParameter("surveyId", surveyId);
            
            List<Dashboard> cl = q.getResultList();
            if ( cl == null || cl.size()<= 0){
                throw new InvalidParameterException("Invalid content name=" + name);
            }
            c= cl.get(0);
            //set the menu
            Customer cr = this.findCustomer(customerId);
            /*
            List<String> menu = new ArrayList<String>();
            menu.add(cr.getSetting("menu_asset1_label", "Clients"));
            menu.add(cr.getSetting("menu_asset2_label", "Local"));
            menu.add(cr.getSetting("menu_asset3_label", "Resource"));
            c.setCustomerSettings(menu);*/
            c.setCustomerSettings(cr.getSettings());
            return c;
        } catch (Exception e){
            logger.error("findDashboard customerId=" + customerId + " name=" + name, e);
            throw e;
        } finally {
            
        }
    }

    

    
    public void log(String message) {
        logger.debug(message); 
    }
    

    @Override
    public String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }
    


    
}
