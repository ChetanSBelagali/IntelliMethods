package com.smrtsolutions.survey.service;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.smrtsolutions.survey.model.Content;
import com.smrtsolutions.survey.model.Location;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.util.Util;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author SRINATH
 */
@Stateless
@Path("location")
public class LocationREST  extends SMRTAbstractFacade<Location> {
    
    private static final Logger logger = LogManager.getLogger(LocationREST.class);
    //@PersistenceContext(unitName = "SMRT_PU")
    private EntityManager em;
    
   // public String[] REQUIRED_PERMISSIONS = {SMRTRole.CUSTOMER_ADMIN, SMRTRole.ALL};
        

    public LocationREST() {
        super(Location.class);
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Location create(@QueryParam("token") String token,Location entity,@QueryParam("id") String id) throws Exception {
        try{
        System.out.println("CREATE CUST");
        SMRTUser u = this.validateToken(token);
        entity.setCustomerId(u.getCustomerId());
        entity.setCreatedBy(u.getId());
        entity.setCreatedOn(new Date());
//        System.out.println("User id=" + u.getId());
//        this.checkPermissions(REQUIRED_PERMISSIONS);
//        System.out.println("HAS PERMISSIONS cust content name=" + entity.getName());
//        logger.debug("customer content obj is " + ( entity==null?"NULL": entity.getName()));
//        if ( Util.isEmpty(entity.getCustomerId())) {
//            entity.setCustomerId(u.getCustomerId());
//        }
       if(!id.isEmpty()){
        DB mongoDB = this.getDBInstance();
        DBCollection locationCollection = mongoDB.getCollection("Location");
        BasicDBObject fields = new BasicDBObject();
        fields.put("location", entity.getLocation());
        fields.put("address", entity.getAddress());
        fields.put("createdOn", entity.getCreatedOn());
        fields.put("createdBy", entity.getCreatedBy());
        fields.put("customerId", entity.getCustomerId());        
        BasicDBObject condition = new BasicDBObject("_id",id);
        WriteResult result = locationCollection.update(condition,fields);
        
       }else
       {
           return super.create(entity);
        
       }
        return null;
    }
     catch(Exception ex)
    {
       throw ex;
    }
    }

   @GET
    @Path("list")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON}) 
    public String list(@QueryParam("token")String token)throws Exception{
        
        SMRTUser u=this.validateToken(token);
        
        try{
            DB mongoDB=this.getDBInstance();
            DBCollection locationcollection=mongoDB.getCollection("Location");
            
            Iterable<DBObject> output=locationcollection.aggregate(Arrays.asList(
              (DBObject)new BasicDBObject("$match",new BasicDBObject("customerId",u.getCustomerId()))   
              ,(DBObject) new BasicDBObject("$lookup",
              new BasicDBObject("from","SMRTUser")
              .append("localField","createdBy")
              .append("foreignField","_id")
              .append("as","createdBy"))      
                    
             ,(DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$createdBy")
                        .append("preserveNullAndEmptyArrays", true)
                    )
             ,(DBObject)new BasicDBObject("$project",
                     new BasicDBObject("createdBy",new BasicDBObject("firstname","$createdBy.firstname").append("lastname","$createdBy.lastname"))
                    .append("location","$location")
                    .append("address","$address")
                    .append("createdOn","$createdOn")
                    
                )
            ,(DBObject) new BasicDBObject("$sort",new BasicDBObject("location",1))

            )).results();  
                return new Gson().toJson(output);
        }
        catch(Exception ex){
            
            throw ex;
        }
    
    }  
    @DELETE
    @Path("delete/{id}")
    public String remove(@QueryParam("token")String token,@PathParam("id")String id)throws Exception{
        SMRTUser u=this.validateToken(token);
       
        DB mongoDB=this.getDBInstance();
        DBCollection usercollecion=mongoDB.getCollection("SMRTUser");
         if(usercollecion.count(new BasicDBObject("customerId",u.getCustomerId()).append("location",id))==0)
        
         {  
                
        super.remove(super.find(id));
        return "true";
         }
         return "false";
    }
    @GET
    @Path("edit/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON}) 
    public String edit(@QueryParam("token")String token,@PathParam("id")String id)throws Exception{
    SMRTUser u=this.validateToken(token);
    DB mongoDB=this.getDBInstance();
    DBCollection locationcollection=mongoDB.getCollection("Location");
    BasicDBObject fields = new BasicDBObject("_id",1)
            .append("location",1)
            .append("address",1);
    BasicDBObject searchQuery=new BasicDBObject();
    searchQuery.put("_id", id);
    List<DBObject> results = locationcollection.find(searchQuery,fields).toArray();
    return new Gson().toJson(results);
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
