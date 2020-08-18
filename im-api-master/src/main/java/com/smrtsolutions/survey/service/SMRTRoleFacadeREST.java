/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.survey.service;

import com.smrtsolutions.exception.InvalidParameterException;
import com.smrtsolutions.survey.model.Customer;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
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
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author lenny
 */
@Stateless
@Path("customer/{customerId}/role")
public class SMRTRoleFacadeREST extends SMRTAbstractFacade<SMRTRole> {
    
     private static final Logger logger = LogManager.getLogger(SMRTRoleFacadeREST.class);
     
     public String[] REQUIRED_PERMISSIONS = {SMRTRole.ALL};
    
    //@PersistenceContext(unitName = "SMRT_PU")
    private EntityManager em;

    public SMRTRoleFacadeREST() {
        super(SMRTRole.class);
    }

    /*
    @GET
    public List<SMRTRole> getRoles(@PathParam("customerId") String customerId, @QueryParam("token") String token) throws Exception {
        log("Listing roles");
        SMRTUser u = this.validateToken(token);
        //this.checkPermissions(new String[]{SMRTRole.NONE});
        this.setCustomerId(customerId);
        return super.findAll();
    }*/
    
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTRole addRole(@PathParam("customerId") String customerId, @QueryParam("token") String token, SMRTRole role) throws Exception {
        log("Adding role=" + role.getName());
        try {
            SMRTUser u = this.validateToken(token);
            this.checkPermissions(REQUIRED_PERMISSIONS);
            this.getEntityManager().getTransaction().begin();
            //if ( 1 == 1) throw new Exception("how to set customer on role");
            Customer c = null;
            if ( this.isSystemToken(token)){ //use the customer id in path. This is for the setup script
                ///role.setCustomer(this.findCustomer(customerId));
                System.out.println("find customer " + customerId );
                c = this.findCustomer(customerId);
            } else { //ignore the customer id in path, use the user's customer id
                ///role.setCustomer(u.getCustomer());
                System.out.println("find customer " + u.getCustomerId() );
               
                c = this.findCustomer(u.getCustomerId());
            }
             System.out.println("found customer " + c.getName());
            if ( c.getRoles() == null) c.setRoles(new ArrayList<SMRTRole>());
            boolean found = false;
            for ( SMRTRole r : c.getRoles()){
                if ( r.getName().equals(role.getName())){
                    found = true;
                }
            }
            if ( !found ){
                c.getRoles().add(role);
            }
            this.getEntityManager().merge(c);
            this.getEntityManager().getTransaction().commit();
        } catch (Exception e){
            this.getEntityManager().getTransaction().rollback();
            throw e;
        }
        return role;
    }
    
    

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTRole edit(@PathParam("id") Long id, SMRTRole entity, @QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token);
        this.setCustomerId(u.getCustomer().getId());
        this.checkPermissions(REQUIRED_PERMISSIONS);
        return super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id, @QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token);
        this.setCustomerId(u.getCustomer().getId());
        this.checkPermissions(REQUIRED_PERMISSIONS);
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTRole find(@PathParam("id") Long id, @QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token);
        this.setCustomerId(u.getCustomer().getId());
        this.checkPermissions(REQUIRED_PERMISSIONS);
        return super.find(id);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<SMRTRole> findAll(@QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token);
        this.setCustomerId(u.getCustomer().getId());
        //this.checkPermissions(NONE);
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<SMRTRole> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to,@QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token);
        //this.checkPermissions(NONE);
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() throws Exception{
        return String.valueOf(super.count());
    }


    
    public void log(String message) {
        logger.debug(message); 
    }
    
    public Customer findCustomer(String id) throws Exception{
        Customer c = this.getEntityManager().find(Customer.class, id);
        if ( c == null) {
            throw new InvalidParameterException("Invalid Customer key " + id);
        }
        return c;
    }
@Override
    public String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }

    
}
