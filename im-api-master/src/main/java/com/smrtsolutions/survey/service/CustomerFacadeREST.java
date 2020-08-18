/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.survey.service;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.smrtsolutions.exception.InvalidParameterException;
import com.smrtsolutions.survey.model.Content;
import com.smrtsolutions.survey.model.Customer;
import com.smrtsolutions.survey.model.Group;
import com.smrtsolutions.survey.model.NameValuePair;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
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
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author lenny
 */
@Stateless
@Path("customer")
public class CustomerFacadeREST extends SMRTAbstractFacade<Customer> {
    
     private static final Logger logger = LogManager.getLogger(CustomerFacadeREST.class);
    //@PersistenceContext(unitName = "SMRT_PU")
    private EntityManager em;
    
    public String[] REQUIRED_PERMISSIONS = {SMRTRole.ALL};
        

    public CustomerFacadeREST() {
        super(Customer.class);
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Customer create(@QueryParam("token") String token, Customer entity) throws Exception {
        
        if ( this.isEmpty( entity.getId())) {
            throw new InvalidParameterException("Customer Code is required");
        } 
        entity.setCustomerCode(entity.getId());
         if ( this.isEmpty( entity.getCustomerName())) {
            throw new InvalidParameterException("Customer Name is required");
        } 
                    
        if ( this.isEmpty( entity.getEmail()) ){
            throw new InvalidParameterException("Email is required");
        } 
         if ( this.isEmpty( entity.getRegistrationCode())) {
            throw new InvalidParameterException("Customer Registration Code is required");
        } 
                    
        if ( this.isEmpty( entity.getUrlKey())){
            throw new InvalidParameterException("URL Key is required");
        } 
        
        System.out.println("CREATE CUST");
        SMRTUser u = this.validateToken(token);
        
        entity.setStatus(1);
        entity.setCreatedOn(new Date());
        entity.setSuperadminpass(entity.getUrlKey().concat("@123"));
        Customer customer = super.create(entity);
        //Create default user for Admin
        List<String> defaultRole = new ArrayList<String>();
        defaultRole.add("admin");
        SMRTUser user = new SMRTUser();
        user.setFirstname(customer.getCustomerName());
        user.setLastname("Admin User");
        user.setEmail(customer.getEmail());
        user.setLoginName(customer.getEmail());
        user.setCustomerId(customer.getId());
        user.setName(customer.getCustomerName() + " Admin User");
        user.setOrganization(customer.getCustomerName());
        user.setUsertype("admin");
        user.setRoles(defaultRole);
        user.setPassword(Util.encrypt("smrt"));
        user.setCreatedOn(new Date());
        this.getEntityManager().getTransaction().begin();
        try {
            getEntityManager().persist(user);
            this.getEntityManager().getTransaction().commit();
        } catch ( Exception e){
            this.getEntityManager().getTransaction().rollback();
            throw e;
        }
        Group usergroup=new Group();
        ArrayList<String> v = new ArrayList<>();
        v.add(user.getId());
        usergroup.setContents(new ArrayList<String>());
        usergroup.setCustomerId(customer.getId());
        usergroup.setName("All"); 
        usergroup.setDescription("All Clients");
        usergroup.setGroupType("All Clients");
        usergroup.setisDefault(true);     
        usergroup.setGroupOwner(v);
        
        this.getEntityManager().getTransaction().begin();
        try {
            getEntityManager().persist(usergroup);
            this.getEntityManager().getTransaction().commit();
        } catch ( Exception e){
            this.getEntityManager().getTransaction().rollback();
            throw e;
        }
        
        
        return customer;
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Customer edit(@QueryParam("token") String token, @PathParam("id") String id, Customer entity) throws Exception {
        SMRTUser u = this.validateToken(token);
   //     this.checkPermissions(REQUIRED_PERMISSIONS);
        
        return super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id, @QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token);
        this.checkPermissions(REQUIRED_PERMISSIONS);
        
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Customer find(@PathParam("id") String id, @QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token);
        return super.find(id);
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Customer findCustomerFromToken(@QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token);
        this.checkPermissions(REQUIRED_PERMISSIONS);
        
        return this.findCustomer(u);
    }
    
    @Path("all")
    @GET
    @Produces({ MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public List<Customer> findAll(@QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token);
        return super.findAll();
        
    }
    
    @Path("settings")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<NameValuePair> getCustomerSettings(@QueryParam("token") String token) throws Exception {
        
        SMRTUser u = this.validateToken(token);
        this.checkPermissions(REQUIRED_PERMISSIONS);
        Customer c = this.findCustomer(u);
        
        return c.getSettings();
    }
    
    
    @Path("roles")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<SMRTRole> getCustomerRoles(@QueryParam("token") String token) throws Exception {
        
        SMRTUser u = this.validateToken(token);
        this.checkPermissions(REQUIRED_PERMISSIONS);
        Customer c = this.findCustomer(u);
        return c.getRoles();
    }
    @GET
    @Path("/settings/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public String findcustomer(@PathParam("id") String id) throws Exception {
    try {
        DB mongoDB = this.getDBInstance();
        DBCollection userCollection = mongoDB.getCollection("customer");
        BasicDBObject condition = new BasicDBObject("urlKey",id);
        DBCursor cursor = userCollection.find(condition);
        Gson json = new Gson();
            return json.toJson(cursor.toArray());
    } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
    } finally{
        //ToDO
    }
    }

/*
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Customer> findAll(@QueryParam("token") String token) throws Exception {
        
        SMRTUser u = this.validateToken(token);
        this.checkPermissions(REQUIRED_PERMISSIONS);
        
        return super.findAll();
    }*/

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Customer> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to, @QueryParam("token") String token) throws Exception {
        
        SMRTUser u = this.validateToken(token);
        this.checkPermissions(REQUIRED_PERMISSIONS);
        
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST(@QueryParam("token") String token) throws Exception{
        SMRTUser u = this.validateToken(token);
        this.checkPermissions(REQUIRED_PERMISSIONS);
        
        return String.valueOf(super.count());
    }

    @POST
    @Path ("/logoupload/{id}")
    
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public String upload(
            @PathParam ("id") String id,
            @Context HttpServletRequest req)
          
            throws Exception {
        
        
        HashMap<String, String> formData = new HashMap<>();
        if (ServletFileUpload.isMultipartContent(req)) {
            
            FileItemFactory fiFactory = new DiskFileItemFactory();
            ServletFileUpload fileUpload = new ServletFileUpload(fiFactory);
            
             List<FileItem> listItems = fileUpload.parseRequest(req);
             FileItem fileItem = null;
             
             for (FileItem f: listItems){
                if(f.isFormField()){
                     formData.put(f.getFieldName(), f.getString());
                }else{
                   fileItem = f;
                }
             }
             
             
             String token = formData.get("token");
//             String participantId = formData.get("participantId");
  
             SMRTUser user = this.validateToken(token, null);
                 
             
             
             //set file path
             String filePath = Paths.get(System.getenv("SMRT_DOCS"),"Uploads","CustomerLogo",id).toString();
             File dir = new File(filePath);
             
             //check for directory exists or not and create 
             if(!dir.exists()){
                 dir.mkdirs();
             }
             
             if(fileItem != null){ //check if uploaded file was received
                String fileName = "logo.png";
                String fullPath = Paths.get(filePath,fileName).toString();
                File file = new File(fullPath);
                
                //save file to path
                fileItem.write(file);
                
                return fileName;
             }else{
                 throw new InvalidParameterException( "Error: Uploaded file not received");
             }
        }
        throw new InvalidParameterException("Only Multipart Upload Supported");
        
    }

     @GET
        @Path("getLogo/{id}")
        @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
        public byte[] getLogo(@PathParam("id") String id) throws Exception {
        
            try {
                File file = null;
           
                    String filePath = Paths.get(System.getenv("SMRT_DOCS"),"Uploads","CustomerLogo",id,"logo.png").toString();
                    file = new File(filePath);
                    if (!file.exists()) {
                        file = new File(Paths.get(System.getenv("SMRT_DOCS"), "Uploads", "CustomerLogo", "smrt-logo.png").toString());
                    }
                
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] fileContent = new byte[(int) file.length()];
                fileInputStream.read(fileContent);
                return fileContent;
            } catch (Exception ex) {
                return null;
            }
        }  
  
     @POST
    @Path("superAdminLogin")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String checkSuperAdminPassword(@FormParam("password") String password, @QueryParam("token") String token) throws Exception {
       SMRTUser user = this.validateToken(token);
       DB mongoDB = this.getDBInstance();
       DBCollection customerCollection = mongoDB.getCollection("customer");
       return String.valueOf(customerCollection.count(new BasicDBObject("_id", user.getCustomerId()).append("superAdminPassword", password)) == 1);
   } 
      
     

    /*@Override
    protected EntityManager getEntityManager() {
        if ( em == null) {
            em = Persistence.createEntityManagerFactory("SMRT_PU").createEntityManager();
        }
        return em;
    }*/
    
    

    
    public void log(String message) {
        logger.debug(message); 
    }
  
    public Customer findCustomer(String id) throws Exception{
       return super.findCustomer(id);
    }
    
    
    /*
    public long decodeId(String id){
        //TODO DECODE
        return Long.parseUnsignedLong(id);
    }*/

    @Override
    public String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }
    
    
    

    
}
