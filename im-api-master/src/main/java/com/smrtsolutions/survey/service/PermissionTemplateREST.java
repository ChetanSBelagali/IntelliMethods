/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.service;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.smrtsolutions.survey.model.SMRTRole;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.smrtsolutions.survey.model.PermissionTemplates;
import com.smrtsolutions.survey.model.SMRTUser;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 *
 * @author lenny
 */
//@Stateless
@Path("/permission")
public class PermissionTemplateREST extends SMRTAbstractFacade<PermissionTemplates> {
            
    private static final Logger logger = LogManager.getLogger(PermissionTemplateREST.class);
    public String[] REQUIRED_PERMISSIONS = {SMRTRole.NONE};
    Map<String, String> hmap = new HashMap<String, String>();
    //@PersistenceProperty(name="test", value="")
    //@PersistenceContext(unitName = "SMRT_PU")
   // private EntityManager em;

    public PermissionTemplateREST() {
        super(PermissionTemplates.class);
    }

    @POST
    @Path("/add")
    //customerId is dummy here. get customerId from reg code
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public PermissionTemplates addPermission(@QueryParam("token") String token,PermissionTemplates templates) throws Exception {
        SMRTUser u = this.validateToken(token, null);
        templates.setCreatedBy(u.getId());
        templates.setCustomerId(u.getCustomerId());
        return super.create(templates);
    }
    
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public PermissionTemplates updatePermission(
            @QueryParam("token") String token,
            PermissionTemplates templates) throws Exception {
    SMRTUser u = this.validateToken(token, null);
    PermissionTemplates p = this.find(templates.getId());
    p.setName(templates.getName());
    p.setPermissions(templates.getPermissions());
    return super.edit(p);
    }
    
    @GET
    @Path("/view")
    //customerId is dummy here. get customerId from reg code
    @Produces({MediaType.APPLICATION_JSON})
    public String getTemplateList(@QueryParam("token") String token) throws Exception {
    SMRTUser u = this.validateToken(token, null);
    DBCollection templateCollection=null;
    BasicDBObject condition=null;
    DBCursor cursor=null;
    try{
        DB mongoDB = this.getDBInstance();
        templateCollection = mongoDB.getCollection("permission_templates");
        condition = new BasicDBObject("createdBy",u.getId());
        cursor = templateCollection.find(condition);
        Gson json = new Gson();
        return json.toJson(cursor.toArray());
    }
    catch(Exception e)
    {
      System.out.println(e.getMessage());
      throw new Exception("Error ");
    }
    finally
    {
        templateCollection=null;
        condition=null;
        cursor=null;
    }
    
    }
    
    @DELETE
    @Path ("delete/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public String deleteTemplate(
        @QueryParam("token") String token,
        @PathParam("id") String id,
        @Context HttpServletRequest req) 
        throws Exception {
        SMRTUser user = this.validateToken(token, null);
        DBCollection templateCollection=null;
        BasicDBObject fields=null;
        WriteResult result=null;
            try{
                DB mongoDB = this.getDBInstance();
                templateCollection = mongoDB.getCollection("permission_templates");
                DBCollection usercollecion=mongoDB.getCollection("SMRTUser");
                fields = new BasicDBObject("_id",id);
                if(usercollecion.count(new BasicDBObject("customerId",user.getCustomerId()).append("permission_template_id",id))==0)
                {  
                result = templateCollection.remove(fields);
                return "true";
                }
                return "false";
             }
            catch (Exception e) 
            {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
            } 
            finally{
            templateCollection=null;
            fields=null;
            result=null;
    }
    }
    
    @GET
    @Path("/getpermission/{id}")
    //customerId is dummy here. get customerId from reg code
    @Produces({MediaType.APPLICATION_JSON})
    public String getPermission(@QueryParam("token") String token,@PathParam("id") String id) throws Exception {
    SMRTUser u = this.validateToken(token, null);
    DBCollection templateCollection=null;
    BasicDBObject condition=null;
    DBCursor cursor=null;
    try{
        DB mongoDB = this.getDBInstance();
        templateCollection = mongoDB.getCollection("permission_templates");
        condition = new BasicDBObject("_id",id);
        cursor = templateCollection.find(condition);
        Gson json = new Gson();
        return json.toJson(cursor.toArray());
    }
    catch(Exception e)
    {
      System.out.println(e.getMessage());
      throw new Exception("Error ");
    }
    finally
    {
        templateCollection=null;
        condition=null;
        cursor=null;
    }
    
    }
    
    @Override
    public void log(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getRequiredPermissions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
     
}
