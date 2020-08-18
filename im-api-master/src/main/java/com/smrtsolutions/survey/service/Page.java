package com.smrtsolutions.survey.service;


import com.smrtsolutions.exception.InvalidParameterException;
import com.smrtsolutions.survey.model.Content;
import com.smrtsolutions.survey.model.Customer;
import com.smrtsolutions.util.Util;
import java.net.URI;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author lenny
 */
@Path("/page")
public class Page {
    
    private EntityManager em = null;
    /*
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path ("/index")
    public Response getIndexPage(@PathParam("customerKey") String customerKey){
        //Response r = new WebResponse();
        
        return null;
    }*/
    
    private String getCustomerKey(UriInfo uri) throws Exception{
        URI abs = uri.getAbsolutePath();
        String host = abs.getHost();
        int e = host.indexOf(".");
        String c = "";
        if ( e > 0 ){
            c = host.substring(0, e);
        } else {
            throw new InvalidParameterException("URL must use subdomain for customer");
        }
        if ( Util.isEmpty(c)){
            throw new InvalidParameterException("URL must use subdomain for customer");
        }
        return c;
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path ("/{pageName}")
    public Content getPage(@PathParam("pageName") String pageName, @Context UriInfo uri ) throws Exception{
        //Response r = new WebResponse();
        
        String customerKey = this.getCustomerKey(uri);
        
        Customer c = this.getCustomer(customerKey);
        
        //find the content template and variables
        Content con = this.getContent(c.getCustomerId(), pageName);
        
        
        return con;
    }
    
    private Customer getCustomer(String urlKey) throws Exception{
        Query q = this.getEntityManager().createQuery("select c from Customer c where c.urlKey = :urlKey", Customer.class);
        q.setParameter("urlKey", urlKey);
        Customer c = (Customer) q.getSingleResult();
        if ( c == null || Util.isEmpty(c.getId())){
            throw new InvalidParameterException("Invalid customer");
        }
        return c;
   
    }
    
    private Content getContent(String customerId, String contentName) throws Exception{
         Query q = this.getEntityManager().createQuery("select c from Content c where c.customerId = :customerId and c.name = :pageName", Content.class);
         q.setParameter("customerId", customerId);
         q.setParameter("contentName", contentName);
         
         Content c = (Content) q.getSingleResult();
         return c;
    }
    
    private EntityManager getEntityManager() {
        if ( em == null) {
            em = Persistence.createEntityManagerFactory("SMRT_PU").createEntityManager();
        }
        return em;
    }
    
    public void closeEntityManager() {
        if ( em != null) {
            em.close();
        }
    }
    
    
}
