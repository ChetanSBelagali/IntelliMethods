/*
 * SMRT Solutions
 * Data Collection Platform
 */
package com.smrtsolutions.survey.service;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.smrtsolutions.survey.model.Group;
import com.smrtsolutions.survey.model.SMRTUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.json.internal.json_simple.JSONArray;

/**
 *
 * @author Kriyatec - Santhosh
 */
@Path("/group")
public class GroupREST  extends SMRTAbstractFacade<Group>{
    
    private static final Logger logger = LogManager.getLogger(GroupREST.class);
    
    public GroupREST() {
         super(Group.class);
    }

    @Override
    public void log(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getRequiredPermissions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @GET
    @Path ("contents")
    @Produces({MediaType.APPLICATION_JSON})
    public String getGroup(
        @QueryParam("token") String token,
        @Context HttpServletRequest req) 
        throws Exception {
            
            SMRTUser user = this.validateToken(token, null);           
            try{
                DB mongoDB = this.getDBInstance();
                DBCollection userCollection = mongoDB.getCollection("SMRTUser");
                DBCollection surveyCollection = mongoDB.getCollection("survey");
                DBCollection groupCollection = mongoDB.getCollection("group");
                Gson json = new Gson();
                
                JsonElement usersJson, classesJson, groupsJson;
                
                Iterable<DBObject> output;
                if(user.getUsertype().equals("admin")|| this.CheckUserPermission(user, "M08", "canview")){
                    output = userCollection.aggregate(Arrays.asList(
                        (DBObject) new BasicDBObject("$match", new BasicDBObject("customerId", user.getCustomerId()).append("roles", "student"))
                       
                       , (DBObject) new BasicDBObject("$project", 
                                new BasicDBObject("lastname","$lastname").append("firstname","$firstname")
                        )
                       ,(DBObject) new BasicDBObject("$sort",new BasicDBObject("lastname",1).append("firstname",1))
                       ,(DBObject) new BasicDBObject("$group", new BasicDBObject("_id", null)
                                    .append("key",new BasicDBObject("$push","$_id"))
                                    .append("value",new BasicDBObject("$push", new BasicDBObject("lastname","$lastname").append("firstname","$firstname"))
                                    ))
                       ,(DBObject) new BasicDBObject("$project", new BasicDBObject("_id", 0)
                               .append("keys", "$key")
                               .append("values", "$value"))
                   )).results();
                }else{
                    output = groupCollection.aggregate(Arrays.asList(
                        (DBObject) new BasicDBObject("$match", new BasicDBObject("customerId", user.getCustomerId()).append("groupOwner", user.getId()))
                       , (DBObject) new BasicDBObject("$project", new BasicDBObject("contents", 1))
                       , (DBObject) new BasicDBObject("$unwind", "$contents")
                       , (DBObject) new BasicDBObject("$group", new BasicDBObject("_id", "$contents"))
                       ,(DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from","SMRTUser")
                        .append("localField","_id")
                        .append("foreignField","_id")
                        .append("as","user"))
                       , (DBObject) new BasicDBObject("$unwind", 
                            new BasicDBObject("path","$user")
                            .append("preserveNullAndEmptyArrays",true)
                        )
                       , (DBObject) new BasicDBObject("$project", 
                                new BasicDBObject("lastname","$user.lastname"
                                ).append("firstname","$user.firstname"
                                )
                        )
                       ,(DBObject) new BasicDBObject("$sort",new BasicDBObject("lastname",1).append("firstname",1))
                       ,(DBObject) new BasicDBObject("$group", new BasicDBObject("_id", null)
                                    .append("key",new BasicDBObject("$push","$_id"))
                                    .append("value",new BasicDBObject("$push", new BasicDBObject("lastname","$lastname").append("firstname","$firstname"))
                                    ))
                       ,(DBObject) new BasicDBObject("$project", new BasicDBObject("_id", 0)
                               .append("keys", "$key")
                               .append("values", "$value"))
                   )).results();
                }
                
                if(output.iterator().hasNext())
                    usersJson = json.toJsonTree(output.iterator().next());
                else
                    usersJson = new JsonObject();
                
                
                
                BasicDBObject matchCond = new BasicDBObject("customerId", user.getCustomerId());
                
                if(!user.getUsertype().equals("admin")){
                    
                    //Check User has Super Group Permission
                    if(this.CheckUserPermission(user, "M08", "canview")){
                         matchCond.append("$or", new Gson().fromJson("[{\"groupType\":\"MyGroup\", \"groupOwner\":\""+ user.getId() +"\"}, {\"groupType\":{\"$ne\":\"MyGroup\"}}]", BasicDBList.class));
                    }else{
                        matchCond.append("groupOwner", user.getId());
                    }
                   
                }else{
                     matchCond.append("groupType", new BasicDBObject("$ne","MyGroup"));
                }
                
                groupsJson = json.toJsonTree(groupCollection.find(matchCond, new BasicDBObject("groupType",1).append("name",1).append("contents",1)).sort(new BasicDBObject("name",1)).toArray());
                
                JsonObject jsonObject = new JsonObject();
                
                jsonObject.add("users", usersJson);
                jsonObject.add("groups", groupsJson);
                return jsonObject.toString();
                
            }catch(Exception ex){
                throw ex;
            }
    }
    
    @GET
    @Path ("get/{groupId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Group getGroupList(
        @QueryParam("token") String token,
        @PathParam("groupId") String groupId,
        @Context HttpServletRequest req) 
        throws Exception {
            SMRTUser user = this.validateToken(token, null);
            return this.find(groupId);
    }
    
      @GET
    @Path ("contentForOther/{groupId}")
    @Produces({MediaType.APPLICATION_JSON})
    public String getGroups(
        @QueryParam("token") String token,
        @PathParam("groupId") String groupId,    
        @Context HttpServletRequest req) 
        throws Exception {
            
            SMRTUser user = this.validateToken(token, null);           
            try{
                DB mongoDB = this.getDBInstance();
                DBCollection groupCollection = mongoDB.getCollection("group");
                    Gson json = new Gson();

                Iterable<DBObject> output;
                    output = groupCollection.aggregate(Arrays.asList(
                        (DBObject) new BasicDBObject("$match", new BasicDBObject("customerId", user.getCustomerId()).append("_id", groupId))
                        
                        ,(DBObject) new BasicDBObject("$unwind", new BasicDBObject("path", "$contents")
                            .append("preserveNullAndEmptyArrays",true))    
                            
                            ,(DBObject) new BasicDBObject("$lookup", 
                            new BasicDBObject("from","SMRTUser")
                            .append("localField","contents")
                            .append("foreignField","_id")
                            .append("as","user"))
                            
                        ,(DBObject) new BasicDBObject("$unwind", new BasicDBObject("path", "$user")
                            .append("preserveNullAndEmptyArrays",true))  
                            
                       , (DBObject) new BasicDBObject("$group",
                               
                                new BasicDBObject("_id","$contents").append("lastname",new BasicDBObject("$first","$user.lastname")).append("firstname",new BasicDBObject("$first","$user.firstname"))
           
                       )
                       ,(DBObject) new BasicDBObject("$sort",new BasicDBObject("lastname",1))
                    )).results();
                 return new Gson().toJson(output);   

            }catch(Exception ex){
                throw ex;
            }
            
    }
    
    @GET
    @Path ("list")
    @Produces({MediaType.APPLICATION_JSON})
    public String getGroupList(
        @QueryParam("token") String token,
        @Context HttpServletRequest req) 
        throws Exception {
            
            SMRTUser user = this.validateToken(token, null);
      
            
            try{
            	System.out.println("============================================================");
            	System.out.println("The Token is: "+token);
            	System.out.println("============================================================");
                DB mongoDB = this.getDBInstance();
                DBCollection groupCollection = mongoDB.getCollection("group");
                Gson json = new Gson();
        
                
                BasicDBObject matchCond = new BasicDBObject("customerId", user.getCustomerId());
                if(!user.getUsertype().equals("admin")){
                    
                    //Check User has Super Group Permission
                    if(this.CheckUserPermission(user, "M08", "canview")){
                         matchCond.append("$or", new Gson().fromJson("[{\"groupType\":\"MyGroup\", \"groupOwner\":\""+ user.getId() +"\"}, {\"groupType\":{\"$ne\":\"MyGroup\"}}]", BasicDBList.class));
                    }else{
                        matchCond.append("groupOwner", user.getId());
                    }
                   
                }else{
                	System.out.println("I m inside admin user");
                     matchCond.append("groupType", new BasicDBObject("$ne","MyGroup"));
                }
                
                System.out.println("DB Collection1 i am here");
                DBCollection userCollection = mongoDB.getCollection("SMRTUser");
                long usercount = userCollection.count(new BasicDBObject("customerId", user.getCustomerId()).append("usertype","student"));
                
                
                
                System.out.println("DB Collection2 i am here");
                Iterable<DBObject> output = groupCollection.aggregate(Arrays.asList(
                    (DBObject) new BasicDBObject("$match", matchCond)   
                        ,(DBObject) new BasicDBObject("$unwind", 
                       new BasicDBObject("path", "$groupOwner")
                        .append("preserveNullAndEmptyArrays", true)
                       )
                    ,(DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"SMRTUser")
                        .append("localField","groupOwner")
                        .append("foreignField","_id")
                        .append("as","assignedTo")
                    ),    
                   (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$assignedTo")
                        .append("preserveNullAndEmptyArrays", true)
                    ),  
                   (DBObject) new BasicDBObject("$project", new BasicDBObject("name", 1)
                           .append("assignedTo",new BasicDBObject("_id","$assignedTo._id")
                           .append("firstname","$assignedTo.firstname")
                           .append("lastname","$assignedTo.lastname"))
                           .append("description", 1)
                           .append("isDefault",1)
                           .append("groupType",1) 
                           .append("count", new Gson().fromJson("{$cond: { if: "
                                   + "{ $and: [{$eq:[\"$isDefault\", true ]},"
                                   + "{$eq:[\"$contents\",null]}]}, then: "+ usercount +","
                                           + "else:{$cond:{if: { $and: [{$eq:[\"$isDefault\", false ]},"
                                           + "{$eq:[\"$contents\",null]}]},"
                                           + " then: "+ 0 +","
                                                   + "else: {\"$size\":\"$contents\"}"
                                                   + "}"
                                                   + "}"
                                                   + "}"
                                                   + "}", BasicDBObject.class)))
                         ,(DBObject) new BasicDBObject("$group", new BasicDBObject("_id","$_id")
                                  .append("assignedTo",new BasicDBObject("$push","$assignedTo"))
                                  .append("description",new BasicDBObject("$first","$description"))
                                  .append("isDefault",new BasicDBObject("$first","$isDefault"))
                                  .append("groupType",new BasicDBObject("$first","$groupType"))
                                  .append("count",new BasicDBObject("$first","$count"))
                                  .append("name",new BasicDBObject("$first","$name"))
                                    )
                         ,(DBObject) new BasicDBObject("$sort",new BasicDBObject("groupType",1).append("name",1))              
               )).results();
                
                System.out.println("Before Returning Json I am here");
                return json.toJson(output);
            }catch(Exception ex){
            	System.out.println("I got an exception: "+ex.getMessage());
                throw ex;
            }
       
    
    }            
      
    @POST
    @Path("assigngrp")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public String saveassignGroup(@QueryParam("token") String token
            ,@FormParam("groups[]") List<String> group
            ,@FormParam("users[]") List<String> user)throws Exception{
        
        SMRTUser u=this.validateToken(token);
        
            DB mongoDB = this.getDBInstance();
            DBCollection groupCollection = mongoDB.getCollection("group");
//            groupCollection.update(new BasicDBObject("_id",new BasicDBObject("$in",group))
//                    .append("contents",new BasicDBObject("$ne", null)), new BasicDBObject("$addToSet",new BasicDBObject("contents",new BasicDBObject("$each", user))), false, true);
//            groupCollection.update(new BasicDBObject("_id",new BasicDBObject("$in",group))
//                    .append("contents",new BasicDBObject("$eq", null)), new BasicDBObject("$set",new BasicDBObject("contents",user)), false, true);
        groupCollection.update(
                new BasicDBObject("_id", new BasicDBObject("$in", group))
                 .append("contents",new BasicDBObject("$ne", null)),
                new BasicDBObject("$addToSet",new BasicDBObject("contents",new BasicDBObject("$each", user))),
                false,
                true
        );
        groupCollection.update(
                new BasicDBObject("_id", new BasicDBObject("$in", group))
                 .append("contents",new BasicDBObject("$eq", null)),
                new BasicDBObject("$set", new BasicDBObject("contents",user)),
                false,
                true
        );
            return  "true";

                
    }
   
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String save(@QueryParam("token") String token,Group entity 
    ) throws Exception {
        System.out.println("CREATE Group");
        SMRTUser user = this.validateToken(token);
        Group group = new Group();
        group.setCustomerId(user.getCustomerId());
        group.setName(entity.getName());
        group.setDescription(entity.getDescription());
        if(entity.getContents().get(0).equals(""))
        {
        group.setContents(null);
        }
        else
        {
            group.setContents(entity.getContents());
        }
        group.setisDefault(entity.getisDefault());
        group.setGroupOwner(entity.getGroupOwner());
        group.setCreatedBy(user.getId());
        group.setGroupType(entity.getGroupType());
        group.setTextnotification(entity.getTextnotification());
        if(entity.getId().isEmpty()||entity.getId().equals("")||entity.getId().equals(null)||entity.getId().equals("undefined")){ //Add group
            super.create(group);
            return new Gson().toJson("Group created Successfully");
        }
        group.setId(entity.getId());
        this.edit(group);
        return new Gson().toJson("Group updated Successfully");
    }
    @POST
    @Path ("saveAssignedTo")
    @Consumes("application/x-www-form-urlencoded")
    public String saveAllClients(@QueryParam("token") String token, 
            @FormParam("id") String id
            ,@FormParam("groupOwner[]") List<String> groupOwner
            ,@FormParam("textnotification[]") List<String> textnotification
    ) throws Exception {
        System.out.println("CREATE Group");
        SMRTUser user = this.validateToken(token);
        Group group = this.find(id);
        group.setGroupOwner(groupOwner);
        group.setTextnotification(textnotification);
        this.edit(group);
        return "Group updated Successfully";
    }
    
    @POST
    @Path ("saveClientAssign")
    @Consumes("application/x-www-form-urlencoded")
    public String saveClientAssign(@QueryParam("token") String token, 
            @FormParam("id") String id
            ,@FormParam("assigned[]") List<String> assigned
            ,@FormParam("unAssigned[]") List<String> unAssigned
            
    ) throws Exception {
        DB mongoDB = this.getDBInstance();
        DBCollection groupCollection = mongoDB.getCollection("group");
        List<String> ids = new ArrayList<String>();
        ids.add(id);
        groupCollection.update(
                new BasicDBObject("_id", new BasicDBObject("$in", assigned))
                 .append("contents",new BasicDBObject("$ne", null)),
                new BasicDBObject("$addToSet", new BasicDBObject("contents", id)),
                false,
                true
        );
        groupCollection.update(
                new BasicDBObject("_id", new BasicDBObject("$in", assigned))
                 .append("contents",new BasicDBObject("$eq", null)),
                new BasicDBObject("$set", new BasicDBObject("contents",ids)),
                false,
                true
        );
        groupCollection.update(
                new BasicDBObject("_id", new BasicDBObject("$in", unAssigned))
                    .append("contents",new BasicDBObject("$ne", null)),
                new BasicDBObject("$pull", new BasicDBObject("contents", id))
                        ,
                false,
                true
        );
        
        return "true";
    }

    @DELETE
    @Path ("deleteGroup/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public String deleteGroup(
        @QueryParam("token") String token,
        @PathParam("id") String id,
        @Context HttpServletRequest req) 
        throws Exception {
        SMRTUser user = this.validateToken(token, null);
            try{
                DB mongoDB = this.getDBInstance();
                DBCollection groupCollection = mongoDB.getCollection("group");
                BasicDBObject fields = new BasicDBObject("_id",id);
                WriteResult result = groupCollection.remove(fields);
            }catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
    } finally{
        //ToDO
    }
        return null;
    }
   
//    @GET
//    @Path("viewClients/{id}")
//    @Produces({MediaType.APPLICATION_JSON})
//    public String viewClients(
//    @QueryParam("token") String token,
//        @PathParam("id") String id
//    )
//     throws Exception {
//        SMRTUser user = this.validateToken(token, null);
//                try{
//                DB mongoDB = this.getDBInstance();
//                DBCollection groupCollection = mongoDB.getCollection("group");
//                BasicDBObject condition = new BasicDBObject("_id",id);
//                DBCursor cursor = groupCollection.find(condition);
//                if (cursor.hasNext()) return new Gson().toJson(cursor.next());
//            }
//            catch (Exception e) {
//            System.out.println(e.getMessage());
//            throw new Exception("Error ");
//    } finally{
//        //ToDO
//    }
//        return null;
//    }

    private void results() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

