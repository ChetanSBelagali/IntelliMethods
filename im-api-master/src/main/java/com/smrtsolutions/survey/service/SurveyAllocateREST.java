

package com.smrtsolutions.survey.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.survey.model.Survey;
import com.smrtsolutions.survey.model.SurveyAllocate;
import com.smrtsolutions.util.GsonUTCDateAdapter;
import com.smrtsolutions.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

@Stateless
@Path("surveyAllocate")
public class SurveyAllocateREST extends SMRTAbstractFacade<SurveyAllocate> {
    
     private static final Logger logger = LogManager.getLogger(SurveyAllocateREST.class);
    //@PersistenceContext(unitName = "SMRT_PU")
    private EntityManager em;
    
    //public String[] REQUIRED_PERMISSIONS = {SMRTRole.ALL};
    Map<String, String> hmap = new HashMap<String, String>();
    public SurveyAllocateREST() {
        super(SurveyAllocate.class);
    }

   

@POST
@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
public SurveyAllocate create(@QueryParam("token") String token,SurveyAllocate entity,@HeaderParam("Referer") String referer)throws Exception{
    try{
    	System.out.println("=================================================================================");
    	System.out.println("Token is: ");
    	System.out.println(token);
    	System.out.println("Survey Id is: "+entity.getSurvey_id());
    	System.out.println("Allocated to is: "+entity.getAllocatedTo());
    	System.out.println("Customer Id is: "+entity.getCustomerId());
    	System.out.println("Created by is: "+entity.getCreatedBy());
    	System.out.println("Created on is: "+entity.getCreatedOn());
    	System.out.println("=================================================================================");
    	
    SMRTUser u=this.validateToken(token);
    
        entity.setCustomerId(u.getCustomerId());
        entity.setCreatedBy(u.getId());
        entity.setCreatedOn(new Date());
        
        if(entity.getAllocatedTo().equals("groups")){
            DB mongoDB=this.getDBInstance();
            DBCollection groupCollection = mongoDB.getCollection("group");
            Iterable<DBObject> result = groupCollection.aggregate(Arrays.asList(
                    (DBObject) new BasicDBObject("$match",
                        new BasicDBObject("customerId",u.getCustomerId())
                        .append("_id", new BasicDBObject("$in", entity.getUserId()))
                    ),
                    (DBObject) new BasicDBObject("$unwind", 
                        new BasicDBObject("path", "$contents")
                        .append("preserveNullAndEmptyArrays", true)
                    ),
                    (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from", "SMRTUser")
                        .append("localField", "contents")
                        .append("foreignField", "_id")
                        .append("as", "user")
                    ),
                    (DBObject) new BasicDBObject("$match",
                        new BasicDBObject("user.usertype", "student")
                    ),
                    (DBObject) new BasicDBObject("$group",
                        new BasicDBObject("_id", "$user._id")
                    ),
                    (DBObject) new BasicDBObject("$unwind", 
                        new BasicDBObject("path", "$_id")
                        .append("preserveNullAndEmptyArrays", true)
                    ),
                    (DBObject) new BasicDBObject("$group",
                        new BasicDBObject("_id", null)
                            .append("id",new BasicDBObject("$addToSet","$_id"))
                    )
            )).results();

            BasicDBObject res = (BasicDBObject)result.iterator().next();
            entity.setUserId((List<String>)res.get("id"));
        }
        
        //todo
        
        DB mongodb=this.getDBInstance();
        DBCollection lastactivityCollection = mongodb.getCollection("activities");
        BasicDBObject update =  new BasicDBObject();
        DBCollection surveyAllocateCollection=mongodb.getCollection("surveyallocate");
        
        Iterable <DBObject> res=surveyAllocateCollection.aggregate(Arrays.asList(
        
              (DBObject) new BasicDBObject("$match",new BasicDBObject("survey_id",entity.getSurvey_id())
                      .append("customerId",entity.getCustomerId()))
                
              ,(DBObject) new BasicDBObject("$project",
                        new BasicDBObject("userId","$userId"))
              
              ,(DBObject)  new BasicDBObject("$unwind","$userId")
              ,(DBObject) new BasicDBObject("$group",new BasicDBObject("_id",null).append("user",new BasicDBObject("$addToSet","$userId")))  

        )).results();
                
        Set<String> existing = new HashSet<String>();
         
         if(res.iterator().hasNext()){
             BasicDBList list = (BasicDBList)(((BasicDBObject)(res.iterator().next())).get("user"));

            for(Object el: list) {
                 existing.add((String) el);
            }
         }
         
         Set<String> users = new HashSet<String>(entity.getUserId());
         
         users.removeAll(existing);
         
        if(users.size()>0){
        
            DBCollection surveyCollection=mongodb.getCollection("survey_results");
            Iterable<DBObject> output;
            output = surveyCollection.aggregate(Arrays.asList(
            (DBObject) new BasicDBObject("$match",new BasicDBObject("participantId",new BasicDBObject("$in",users))
                    .append("customerId",entity.getCustomerId()))
            
            ,(DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"customer")
                        .append("localField","customerId")
                        .append("foreignField","_id")
                        .append("as","customer")
                    )
            ,(DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$customer")
                        .append("preserveNullAndEmptyArrays", true)
                    )        
            ,(DBObject)new BasicDBObject("$project",
                    new BasicDBObject("phonenumber","$s1_req_7.txt")
                    .append("lastname", "$s1_req_2.txt")
                    .append("firstname","$s1_req_1.txt")
                    .append("customerId","$customer.urlKey")        
                    .append("userid","$participantId"))        
            )).results();
            Iterator<DBObject> iter =output.iterator();
            while(iter.hasNext()){
            BasicDBObject result = (BasicDBObject)iter.next();
            String phone = (String)(result).get("phonenumber");
            String fname = (String) (result).get("firstname");
            String lname = (String) (result).get("lastname");
            String userid = (String) (result).get("userid");
            String customerId = (String) (result).get("customerId");
            Survey surveyname = this.findSurvey(entity.getSurvey_id());
            // set activity for student 
                update.put("activity","Staff - Form; Deploy");
                update.put("kind","n/a");
                update.put("createdBy",u.getId());
                update.put("createduname",u.getLastname()+", "+u.getFirstname());
                update.put("createdOn",new Date());
                update.put("customerId",u.getCustomerId());
                update.put("activityfor",userid);
                update.put("detail",surveyname.getLabel());
                update.put("information","n/a");
                update.remove("_id");
                lastactivityCollection.insert(update);
                
            if (fname ==null){ fname =""; }
            String msg=" Hi " + fname + ",\r\n New Form - Please Sign in to your "+ customerId.toUpperCase() +" student portal. "+ " https://"+customerId.toLowerCase()+"student.smrtdata.info";
            
            if(phone !=null && !phone.isEmpty()){ 
             try{      
            phone = phone.replaceAll(" ", "").replaceAll("-", "").replaceAll("\\(", "").replaceAll("\\)", "");
        BasicDBObject message=new BasicDBObject();
                            
                    message.append("content", msg)
                            .append("source_number", "18885039775")
                            .append("source_number_type","INTERNATIONAL")
                            .append("destination_number", "+1"+phone)
                            .append("format","SMS");
                      
                String req = "{\n" +
                            "  \"messages\": [\n" + new Gson().toJson(message)+
                            "  ]\n" +
                            "}";

        //   this.log(req);
        String results = Util.sendPostRequest("http://api.messagemedia.com/v1/messages",req);
        BasicDBObject resultJson = new Gson().fromJson(results, BasicDBObject.class);
         DBCollection communication= mongodb.getCollection("communications");
            BasicDBObject document= new BasicDBObject();
            ObjectId id = new ObjectId();
            document.put("_id", id);
            document.put("type","SMS");
            document.put("customerId",u.getCustomerId());
            document.put("receiver",phone);
            document.put("userId",userid);
            document.put("message",msg);
            document.put("status",resultJson);
            document.put("errormsg","");
            document.put("senton",Calendar.getInstance().getTime());
            document.put("sentby",u.getId());
            communication.insert(document);
            DBCollection txtreply= mongodb.getCollection("sms_reply");
            BasicDBObject updateQuery = new BasicDBObject();
            updateQuery.append("$set",new BasicDBObject()
            .append("replystatus",true)
            );
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.append("userId",u.getId());
            txtreply.updateMulti(searchQuery,updateQuery);
            
            
           
            }
          catch(Exception ex){
           this.logger.error("Cannot send text message - survey allocate", ex);
           DB mongoDB=this.getDBInstance();
            DBCollection communication= mongoDB.getCollection("communications");
            BasicDBObject document=new BasicDBObject();
            ObjectId id = new ObjectId();
            document.put("_id", id);
            document.put("type","SMS");
            document.put("customerId",u.getCustomerId());
            document.put("receiver",phone);
            document.put("userId",userid);
            document.put("message",msg);
            document.put("status","failed");
            document.put("errormsg",ex.getMessage());
            document.put("senton",Calendar.getInstance().getTime());
            document.put("sentby",u.getId());
            communication.insert(document);
        }
            }
            }
        }
        return super.create(entity);
     }

     catch(Exception ex)
     {
         System.out.println(ex.getMessage());
         throw new Exception("Error ");
     }
     finally{
         //todo
         }
     }
    @GET
    @Path("subprograms")
    @Produces({MediaType.APPLICATION_JSON})
    public String getSubPrograms(@QueryParam("token") String token
            )throws Exception{
             try {
            SMRTUser user = this.validateToken(token); 
            DB mongoDB = this.getDBInstance();
            DBCollection programCollection = mongoDB.getCollection("subprograms");
            BasicDBObject condition = new BasicDBObject("customerId",user.getCustomerId());
            DBCursor cursor = programCollection.find(condition);
            Gson json = new Gson();
                return json.toJson(cursor.toArray());
        }
         catch (Exception e) {
                System.out.println(e.getMessage());
                throw new Exception("Error ");
        } finally{
            //ToDO 
        }
    }
    @POST
    @Path("createsubprogram")
    public String createSubPrograms(
        @FormParam("id") String id,
        @FormParam("token") String token,
        @FormParam("program") String program,
        @FormParam("key") String key,
        @FormParam("subprogram") String subprogram
        )throws Exception{
        SMRTUser user = this.validateToken(token); 
        DB mongoDB = this.getDBInstance();
        DBCollection programCollection = mongoDB.getCollection("subprograms");
        if(id==null || id.equals("")){
        id = ObjectId.get().toString();
        }
        System.out.println(id);
        
        BasicDBObject document=new BasicDBObject();
        document.put("$set", 
                new BasicDBObject("customerId",user.getCustomerId())
                .append("customerId",user.getCustomerId())
                .append("createdBy",user.getId())
                .append("program",program)
                .append("key",key)
                .append("subprogram",subprogram)
                .append("createdOn",Calendar.getInstance().getTime())
        );
        document.put("$setOnInsert", new BasicDBObject("_id", id ));
        programCollection.update(new BasicDBObject("_id",id),document, true, true, WriteConcern.ACKNOWLEDGED);
        return null;
    
}
    @DELETE
    @Path ("subprogram/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public String deleteSubProgram(
        @QueryParam("token") String token,
        @PathParam("id") String id) 
        throws Exception {
        SMRTUser user = this.validateToken(token, null);
        try{
            DB mongoDB = this.getDBInstance();
            DBCollection programCollection = mongoDB.getCollection("subprograms");
            BasicDBObject fields = new BasicDBObject("_id",id);
            WriteResult result = programCollection.remove(fields);
        }catch (Exception e) {
        System.out.println(e.getMessage());
        throw new Exception("Error ");
    } finally{
        //ToDO
    }
        return null;
    }
    
    @GET
    @Path("programs")
    @Produces({MediaType.APPLICATION_JSON})
    public String getPrograms(@QueryParam("token") String token
            )throws Exception{
             try {
            SMRTUser user = this.validateToken(token); 
            DB mongoDB = this.getDBInstance();
            DBCollection programCollection = mongoDB.getCollection("programs");
            BasicDBObject condition = new BasicDBObject("customerId",user.getCustomerId());
            DBCursor cursor = programCollection.find(condition);
            Gson json = new Gson();
                return json.toJson(cursor.toArray());
        }
         catch (Exception e) {
                System.out.println(e.getMessage());
                throw new Exception("Error ");
        } finally{
            //ToDO 
        }
    }
    
    @POST
    @Path("programs")
    public String createPrograms(
            @FormParam("id") String id,
            @FormParam("token") String token,
            @FormParam("program") String program,
            @FormParam("key") String key
            )throws Exception{
            SMRTUser user = this.validateToken(token); 
            DB mongoDB = this.getDBInstance();
            DBCollection programCollection = mongoDB.getCollection("programs");
            if(id==null || id.equals("")){
            id = ObjectId.get().toString();
            }
            System.out.println(id);

            BasicDBObject document=new BasicDBObject();
            document.put("$set", 
                    new BasicDBObject("customerId",user.getCustomerId())
                    .append("customerId",user.getCustomerId())
                    .append("createdBy",user.getId())
                    .append("program",program)
                    .append("key",key)
                    .append("createdOn",Calendar.getInstance().getTime())
            );
            document.put("$setOnInsert", new BasicDBObject("_id", id ));
            programCollection.update(new BasicDBObject("_id",id),document, true, true, WriteConcern.ACKNOWLEDGED);
            return null;

    }
    
    @DELETE
    @Path ("programs/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public String deleteProgram(
        @QueryParam("token") String token,
        @PathParam("id") String id) 
        throws Exception {
        SMRTUser user = this.validateToken(token, null);
        try{
            DB mongoDB = this.getDBInstance();
            DBCollection programCollection = mongoDB.getCollection("programs");
            BasicDBObject fields = new BasicDBObject("_id",id);
            WriteResult result = programCollection.remove(fields);
        }catch (Exception e) {
        System.out.println(e.getMessage());
        throw new Exception("Error ");
    } finally{
        //ToDO
    }
        return null;
    }
@GET
@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
public String getList(@QueryParam("token")String Token,
        @QueryParam("order") int order,
        @QueryParam("start") int start,
        @QueryParam("limit") int limit,
        @QueryParam("client") String client,
        @QueryParam("sort") String sort
        )throws Exception{
    
    SMRTUser u=this.validateToken(Token);
    
    
    DB mongoDB=this.getDBInstance();
    BasicDBObject match=new BasicDBObject();
    if(!client.isEmpty()){
    BasicDBList name = new BasicDBList();
        name.add(new BasicDBObject("createdBy.firstname",new BasicDBObject("$regex",client).append("$options","i")));
        name.add(new BasicDBObject("createdBy.lastname",new BasicDBObject("$regex", client).append("$options","i")));
        name.add(new BasicDBObject("survey.name",new BasicDBObject("$regex", client).append("$options","i")));
        match.put("$or",name);
    }
    
    DBCollection surveyCollection = mongoDB.getCollection("surveyallocate");

    List<DBObject> aggQuery = new ArrayList<DBObject>(Arrays.asList(
    
        (DBObject) new BasicDBObject("$match",
                new BasicDBObject("customerId",u.getCustomerId()))
            
        ,(DBObject) new BasicDBObject("$lookup",
                new BasicDBObject("from","survey")
                .append("localField","survey_id")
                .append("foreignField","_id")
                .append("as","survey")) 
            
        ,(DBObject) new BasicDBObject("$lookup",
                new BasicDBObject("from","SMRTUser")
                .append("localField","createdBy")
                .append("foreignField","_id")
                .append("as","createdBy"))

        ,(DBObject) new BasicDBObject("$lookup",
                new BasicDBObject("from","SMRTUser")
                .append("localField","userId")
                .append("foreignField","_id")
                .append("as","userid"))

        ,(DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$survey")
                        .append("preserveNullAndEmptyArrays", true)
                )    
        ,(DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$createdBy")
                        .append("preserveNullAndEmptyArrays", true)
                )
        ,(DBObject) new BasicDBObject("$project",
                    new BasicDBObject("_id","$_id")
                    .append("survey",new BasicDBObject("name","$survey.label").append("id","$survey._id"))
                    .append("createdBy",new BasicDBObject("firstname","$createdBy.firstname").append("lastname","$createdBy.lastname").append("id","$createdBy._id"))
                    .append("assignedTo","$userid")
                    .append("assignedOn","$createdOn")
        )));
        aggQuery.add((DBObject) new BasicDBObject("$match",match));
//        ,(DBObject) new BasicDBObject("$group",new BasicDBObject("_id","$_id")
//                    .append("allocatedTo","$allocatedTo")
//                    .append("survey","$survey")
//                    .append("createdBy","$createdBy")
//                    .append("assignedTo","$userid")
//                    .append("assignedToGroup",new BasicDBObject("$push","$assignedToGroup"))
//                        ));  
               Iterable<DBObject> count = surveyCollection.aggregate(aggQuery).results();
               long size = count.spliterator().getExactSizeIfKnown();

               aggQuery.add((DBObject) new BasicDBObject("$skip",start));
               aggQuery.add((DBObject) new BasicDBObject("$limit",limit));
               aggQuery.add((DBObject) new BasicDBObject("$sort",new BasicDBObject(sort,order)));
               Iterable<DBObject> tas = surveyCollection.aggregate(aggQuery).results();
               
               Gson json = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
                for(DBObject res:tas){
                    ((BasicDBObject)res).append("total",size);
                }
                    return json.toJson(tas);
    
    }

@GET
@Path("surveyFormStatus")
@Produces({MediaType.APPLICATION_JSON})
public String getSurveyForm(@QueryParam("token")String token,
        @QueryParam("isAll")String isAll,
        @QueryParam("start") int start,
        @QueryParam("limit") int limit,
        @QueryParam("form") String id,
        @QueryParam("group") String group,
        @QueryParam("status") String status,
        @QueryParam("search") String search,
        @QueryParam("sort") String sort,
        @QueryParam("order") int order
    )throws Exception{
    
    SMRTUser u=this.validateToken(token);
    
        try{
            DB mongoDB = this.getDBInstance();
            DBCollection groupCollection = mongoDB.getCollection("group");
             DBCollection collection;
             if(id.isEmpty()){
                 BasicDBObject res = new BasicDBObject("total",0);
                    return new Gson().toJson(res);
             }
            // this is for filter queries
            BasicDBObject matchStart = new BasicDBObject();
                if (!group.isEmpty()) matchStart.append("groupid",group);
                if (!status.isEmpty()) matchStart.append("status",Integer.parseInt(status));
                if (sort.isEmpty())sort = "lastname";
            BasicDBObject matches = new BasicDBObject();
            if(!search.isEmpty()){
                BasicDBList nameList = new BasicDBList();
                nameList.add(new BasicDBObject("firstname",new BasicDBObject("$regex", search).append("$options","i")));
                nameList.add(new BasicDBObject("lastname",new BasicDBObject("$regex", search).append("$options","i")));
                nameList.add(new BasicDBObject("email",new BasicDBObject("$regex", search).append("$options","i")));
                nameList.add(new BasicDBObject("groupname",new BasicDBObject("$regex", search).append("$options","i")));
                matches.put("$or", nameList);
                }    
             
             List<DBObject> aggQuery;
             
             if(this.CheckUserPermission(u, "M08", "canview")){ //check user has super group permission
                 collection  = mongoDB.getCollection("SMRTUser");
                aggQuery = new ArrayList<DBObject>(Arrays.asList(
                    (DBObject) new BasicDBObject("$match", new BasicDBObject("customerId", u.getCustomerId()).append("roles", "student"))
                    , (DBObject) new BasicDBObject("$project", 
                            new BasicDBObject("firstname","$firstname")
                            .append("lastname","$lastname")
                            .append("email","$email")
                    )
                ));
             }else{
                collection  = mongoDB.getCollection("group");
                 
                aggQuery = new ArrayList<DBObject>(Arrays.asList(
                    (DBObject) new BasicDBObject("$match", new BasicDBObject("customerId", u.getCustomerId()).append("groupOwner", u.getId()))
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
                            new BasicDBObject("firstname","$user.firstname")
                            .append("lastname","$user.lastname")
                            .append("email","$user.email")     
                    )
                ));
             }
             
             aggQuery.add((DBObject) new BasicDBObject("$lookup", 
                new BasicDBObject("from","surveyallocate")
                .append("localField","_id")
                .append("foreignField","userId")
                .append("as","survey")     
            ));
    
            aggQuery.add((DBObject)new BasicDBObject("$unwind",
                    new BasicDBObject("path","$survey")
                    .append("preserveNullAndEmptyArrays",true)
            ));
         
            aggQuery.add((DBObject)new BasicDBObject("$lookup",
                    new BasicDBObject("from","survey_results")
                    .append("localField","_id" )
                    .append("foreignField","participantId")
                    .append("as","result")
            ));
//            aggQuery.add((DBObject)new BasicDBObject("$unwind",
//                    new BasicDBObject("path","$result")
//                    .append("preserveNullAndEmptyArrays",true)
//            ));
            aggQuery.add((DBObject)new BasicDBObject("$lookup",
                    new BasicDBObject("from","group")
                    .append("localField","_id" )
                    .append("foreignField","contents")
                    .append("as","groups")
                ));
           
            aggQuery.add((DBObject) new BasicDBObject("$unwind", 
                 new BasicDBObject("path", "$groups")
                    .append("preserveNullAndEmptyArrays", true)
            ));
             aggQuery.add((DBObject)new BasicDBObject("$match",
                    new BasicDBObject("groupType",new BasicDBObject("$ne","All Clients"))
                    ));
            aggQuery.add((DBObject)new BasicDBObject("$project",
                new BasicDBObject("firstname","$firstname")
                .append("lastname","$lastname")
                .append("email","$email")     
                .append("groupname","$groups.name")    
                .append("groupid","$groups._id")        
                .append("createdOn","$survey.createdOn")
                .append("completedDate",(BasicDBObject)com.mongodb.util.JSON.parse("{ $arrayElemAt: [\"$result.completedDate."+ id +"\", 0 ] }"))
                .append("phoneNumber",(BasicDBObject)com.mongodb.util.JSON.parse("{ $arrayElemAt: [\"$result.s1_req_7.val\", 0 ] }"))
                .append("surveyId",new BasicDBObject("$ifNull",(BasicDBList)com.mongodb.util.JSON.parse("[\"$survey.survey_id\",\""+ id +"\"]")))
                .append("status",new BasicDBObject("$ifNull",(BasicDBList)com.mongodb.util.JSON.parse("[ { $arrayElemAt: [\"$result.surveyStatus."+ id +"\", 0 ] }, 1]")))
            ));
           
                BasicDBObject match = new BasicDBObject("surveyId",id);
                if(isAll==null || !isAll.equals("true")){
                    match = new BasicDBObject("surveyId",id).append("createdOn", new BasicDBObject("$ne", null));
                }
                aggQuery.add((DBObject)new BasicDBObject("$match", match));
     
            aggQuery.add((DBObject)new BasicDBObject("$match", matches));
            aggQuery.add((DBObject)new BasicDBObject("$match", matchStart));   
            aggQuery.add((DBObject)new BasicDBObject("$group",
                new BasicDBObject("_id","$_id")
                .append("firstname", new BasicDBObject("$first","$firstname"))
                .append("lastname", new BasicDBObject("$first","$lastname"))
                .append("email", new BasicDBObject("$first","$email"))  
                .append("groupname",new BasicDBObject("$addToSet","$groupname"))        
                .append("createdOn", new BasicDBObject("$first","$createdOn"))
                .append("phoneNumber", new BasicDBObject("$first","$phoneNumber")) 
                .append("completedDate", new BasicDBObject("$first","$completedDate"))   
                .append("surveyId", new BasicDBObject("$first","$surveyId"))
                .append("status", new BasicDBObject("$first","$status"))
            )); 
            
            Iterable<DBObject> count = collection.aggregate(aggQuery).results();
            long size = count.spliterator().getExactSizeIfKnown();
            aggQuery.add((DBObject) new BasicDBObject("$sort",new BasicDBObject(sort,order)));
            aggQuery.add((DBObject) new BasicDBObject("$skip",start));
            aggQuery.add((DBObject) new BasicDBObject("$limit",limit));
            // aggQuery.add((DBObject)new BasicDBObject("$sort", new BasicDBObject("status",1)));

                Iterable<DBObject> result = collection.aggregate(aggQuery).results();
            // tasks = collection.aggregate(aggQuery).results();
            
            for(DBObject task: result){
                ((BasicDBObject)task).append("total", size);
            }
                
                Gson json = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
                return json.toJson(result);
                        
        }
        catch(Exception ex){
            throw ex;
        }
        
    }




    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id")String id,@QueryParam("token") String token)throws Exception{
        
        SMRTUser u=this.validateToken(token);
        super.remove(super.find(id));
    
    }

   @GET
   @Path("cmuserlist")
   @Produces({MediaType.APPLICATION_JSON})
    public String getcmUserList(@QueryParam("token")String token,@QueryParam("type")String type,
            @QueryParam("start") int start,
            @QueryParam("limit") int limit,
            @QueryParam("group") String group,
            @QueryParam("status") String status,
            @QueryParam("program") String program,
            @QueryParam("subprogram") String subprogram,
            @QueryParam("search") String search,
            @QueryParam("sort") String sort,
            @QueryParam("order") int order
    )throws Exception{
        
     SMRTUser u=this.validateToken(token);
        
        try{
            DB mongoDB=this.getDBInstance();
            DBCollection collection;
            DBCollection groupCollection = mongoDB.getCollection("group");
            BasicDBObject matchStart=new BasicDBObject();
            if (!group.isEmpty()) matchStart.append("groups",group);
            if (!status.isEmpty()) matchStart.append("status",status);
            if (!program.isEmpty()) matchStart.append("program.key",program);
            if (!subprogram.isEmpty()) matchStart.append("subprogram.key",subprogram);
            if (sort.isEmpty()) sort="lastname";
            
//            if (!search.isEmpty()) matchStart.append("search",new BasicDBObject("$regex", search)).append("$options","i");
            if(!search.isEmpty()){
            BasicDBList nameList = new BasicDBList();
            nameList.add(new BasicDBObject("firstname",new BasicDBObject("$regex", search).append("$options","i")));
            nameList.add(new BasicDBObject("lastname",new BasicDBObject("$regex", search).append("$options","i")));
//          nameList.add(new BasicDBObject("email",new BasicDBObject("$regex", search).append("$options","i")));
            matchStart.put("$or", nameList);
            }
            List<DBObject> typeQuery;
            List<String> ids = new ArrayList<>();
            if(type!=null && type.equals("newClient")){
               if(u.getUsertype().equals("admin")){
                    collection = mongoDB.getCollection("SMRTUser");
                   Iterable<DBObject>  unAssignedClientsResult = collection.aggregate(new ArrayList<DBObject>(Arrays.asList(
                    (DBObject) new BasicDBObject("$match",new BasicDBObject("customerId",u.getCustomerId())
                            .append("usertype","student")
                            .append("verify",new BasicDBObject("$ne","admin")))
                    ,(DBObject) new BasicDBObject("$group",
                            new BasicDBObject("_id","")
                            .append("ids",new BasicDBObject("$addToSet","$_id"))
                     )))).results();
                        if(unAssignedClientsResult.iterator().hasNext()){
                            ids = (List<String>)unAssignedClientsResult.iterator().next().get("ids");
                        }
               }
               else if(this.CheckUserPermission(u, "M08", "canview") || groupCollection.count(new BasicDBObject("customerId", u.getCustomerId()).append("groupType","All Clients").append("groupOwner",this.getUser().getId()))>0){
                   collection = mongoDB.getCollection("SMRTUser");
                   Iterable<DBObject>  unAssignedClientsResult = collection.aggregate(new ArrayList<DBObject>(Arrays.asList(
                    (DBObject) new BasicDBObject("$match",new BasicDBObject("customerId",u.getCustomerId()).append("usertype","student")
                            .append("verify",new BasicDBObject("$ne",u.getId()))
                            .append("$or", new Gson().fromJson("[{\"groupType\":\"MyGroup\", \"groupOwner\":\""+ u.getId() +"\"}, {\"groupType\":{\"$ne\":\"MyGroup\"}}]", BasicDBList.class)))
                     ,(DBObject) new BasicDBObject("$lookup",
                          new BasicDBObject("from","group")      
                            .append("localField","_id")
                            .append("foreignField","contents")
                            .append("as","groups"))
                     ,(DBObject) new BasicDBObject("$group",
                            new BasicDBObject("_id","")
                            .append("ids",new BasicDBObject("$addToSet","$_id"))
                     )))).results();
                        if(unAssignedClientsResult.iterator().hasNext()){
                            ids = (List<String>)unAssignedClientsResult.iterator().next().get("ids");
                        }
               }
               else { 
                   collection = mongoDB.getCollection("group");
                   Iterable<DBObject>  unAssignedClientsResult = collection.aggregate(new ArrayList<DBObject>(Arrays.asList(
                       (DBObject) new BasicDBObject("$match", new BasicDBObject("customerId", u.getCustomerId())
                               .append("groupOwner", u.getId()))
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
                    ,(DBObject) new BasicDBObject("$match",new BasicDBObject("user.verify",new BasicDBObject("$ne",u.getId())))     
                     ,(DBObject) new BasicDBObject("$group",
                            new BasicDBObject("_id","")
                            .append("ids",new BasicDBObject("$addToSet","$_id"))
                     )))).results();
                        if(unAssignedClientsResult.iterator().hasNext()){
                            ids = (List<String>)unAssignedClientsResult.iterator().next().get("ids");
                        }
               }
               
            }
            String verifier = u.getId();
            if(u.getUsertype().equals("admin")){
                verifier = "admin";
            }
            List<DBObject> aggQuery;
            collection = mongoDB.getCollection("group");
            if(u.getUsertype().equals("admin") ||
                this.CheckUserPermission(u, "M08", "canview") ||
                 groupCollection.count(new BasicDBObject("customerId", u.getCustomerId()).append("groupType","All Clients").append("groupOwner",this.getUser().getId()))>0
            ){
                collection = mongoDB.getCollection("SMRTUser");
                aggQuery = new ArrayList<DBObject>(Arrays.asList(
                     (DBObject)new BasicDBObject("$match",new BasicDBObject("customerId",u.getCustomerId()).append("usertype","student"))       
                    ,(DBObject) new BasicDBObject("$project",
                        new BasicDBObject("firstname","$firstname")
                        .append("lastname","$lastname")
                        .append("email","$email")
                        .append("createdOn","$createdOn")   
                        .append("lastactivity","$last_login_date")
                        .append("lastaction","$lastactivity")
                        .append("organization","$organization")
                        .append("verify",new BasicDBObject("$eq",(BasicDBList)com.mongodb.util.JSON.parse("[\"$verify\",\""+ verifier +"\"]")))    
                    )  
                ));
                
                
                
            }
            else{
                aggQuery =new ArrayList<DBObject>(Arrays.asList(
                (DBObject) new BasicDBObject("$match",new BasicDBObject("customerId",u.getCustomerId()).append("groupOwner",u.getId()))
                ,(DBObject) new BasicDBObject("$project",new BasicDBObject("contents",1))
                ,(DBObject) new BasicDBObject("$unwind","$contents")
                ,(DBObject) new BasicDBObject("$group",new BasicDBObject("_id","$contents"))
                ,(DBObject) new BasicDBObject("$lookup",
                  new BasicDBObject("from","SMRTUser")      
                    .append("localField","_id")
                    .append("foreignField","_id")
                    .append("as","user"))
                ,(DBObject) new BasicDBObject("$match",
                  new BasicDBObject("user.usertype","student"))
                ,(DBObject) new BasicDBObject("$unwind",
                new BasicDBObject("path","$user")
                    .append("preserveNullAndEmptyArrays", true)
                    )
                  ,(DBObject) new BasicDBObject("$project",
                         new BasicDBObject("firstname","$user.firstname")
                            .append("lastname","$user.lastname")
                            .append("email","$user.email")
                            .append("createdOn","$user.createdOn")
                            .append("lastactivity","$user.last_login_date")
                            .append("lastaction","$user.lastactivity")
                            .append("organization","$user.organization") 
                            .append("verify",new BasicDBObject("$eq",(BasicDBList)com.mongodb.util.JSON.parse("[\"$verify\",\""+ verifier +"\"]")))       

                    )  
                ));
            }
            
            if(type!=null && type.equals("newClient")){
                 aggQuery.add((DBObject)new BasicDBObject("$match",
                    new BasicDBObject("_id", new BasicDBObject("$in", ids))
                ));
            }
            
                aggQuery.add((DBObject)new BasicDBObject("$lookup",
                    new BasicDBObject("from","survey_results")
                    .append("localField","_id" )
                    .append("foreignField","participantId")
                    .append("as","result")
                ));
                aggQuery.add((DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path","$result" )
                        .append("preserveNullAndEmptyArrays", true)
                ));
                aggQuery.add((DBObject)new BasicDBObject("$lookup",
                    new BasicDBObject("from","group")
                    .append("localField","_id" )
                    .append("foreignField","contents")
                    .append("as","groups")
                ));
                aggQuery.add((DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$groups")
                        .append("preserveNullAndEmptyArrays", true)
                ));
                aggQuery.add((DBObject)new BasicDBObject("$lookup",
                    new BasicDBObject("from","programs")
                    .append("localField","result.se_client_01.val" )
                    .append("foreignField","key")
                    .append("as","program")
                ));
                aggQuery.add((DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$program")
                        .append("preserveNullAndEmptyArrays", true)
                ));
                aggQuery.add((DBObject)new BasicDBObject("$project",
                    new BasicDBObject("firstname","$firstname")
                        .append("lastname","$lastname")
                        .append("email","$email")
                        .append("groupname","$groups.name")
                        .append("phoneNumber","$result.s1_req_7.val")
                        .append("hphoneNumber","$result.s1_req_8.val")
                        .append("createdOn","$result.st_st_06.val")
                        .append("studentId","$result.uniq_imp_key.val")      
                        .append("lastactivity","$lastactivity")
                        .append("lastaction","$lastaction")
                        .append("organization","$organization") 
                        .append("verify","$verify")
                        .append("status",new Gson().fromJson("{$cond: { if:  { $or: [{$eq:[\"$result.status\", null ]},{$not:[\"$result.status\"]}]}, then: \"Intake\", else: $result.status }}",BasicDBObject.class))
//                        .append("prog_01",new Gson().fromJson(
//                        "{ $switch : { branches: [ { case : { $eq : [ $result.se_client_01.val,\"1\" ] },\n" +
//                        "  then : \"ABE\" },\n" +
//                        "  { case : { $eq: [$result.se_client_01.val,\"2\"] },\n" +
//                        "  then : \"ESL\" },\n" +
//                        "  { case: { \"$eq\" : [$result.se_client_01.val,\"3\"] },\n" +
//                        "  then : \"GED\" },\n" +
//                        "  { case : { $eq : [ $result.se_client_01.val,\"4\"] },\n" +
//                        "  then : \"Craft Trade Professional\" },\n" +
//                        "  { case : { $eq : [ $result.se_client_01.val,\"5\"] },\n" +
//                        "  then : \"Health Care\" },\n" +
//                        "  { case : { $eq : [ $result.se_client_01.val,\"6\"] },\n" +
//                        "  then : \"OTHER\" } ],\n" +
//                        "  default  : \"n/a\" } }"
//                        ,BasicDBObject.class)) 
                        .append("prog_01",new BasicDBObject("key","$program.key").append("value", "$program.program"))
                        .append("subprogram",new BasicDBObject("key",new Gson().fromJson(
                        "{ $switch : { branches: [ { case : { $eq : [ $result.se_client_01.val,\"1\" ] },\n" +
                        "  then : $result.prog_01_sec.val },\n" +
                        "  { case : { $eq: [$result.se_client_01.val,\"2\"] },\n" +
                        "  then : $result.prog_02_sec.val },\n" +
                        "  { case: { \"$eq\" : [$result.se_client_01.val,\"3\"] },\n" +
                        "  then : $result.prog_03_sec.val },\n" +
                        "  { case : { $eq : [ $result.se_client_01.val,\"4\"] },\n" +
                        "  then : $result.prog_04_sec.val},\n" +
                        "  { case : { $eq : [ $result.se_client_01.val,\"5\"] },\n" +
                        "  then : $result.prog_05_sec.val },\n" +
                        "  { case : { $eq : [ $result.se_client_01.val,\"6\"] },\n" +
                        "  then : $result.prog_06_sec.val } ],\n" +
                        "  default  : \"n/a\" } }"
                        ,BasicDBObject.class)).append("value",new Gson().fromJson(
                        "{ $switch : { branches: [ { case : { $eq : [ $result.se_client_01.val,\"1\" ] },\n" +
                        "  then : $result.prog_01_sec.txt },\n" +
                        "  { case : { $eq: [$result.se_client_01.val,\"2\"] },\n" +
                        "  then : $result.prog_02_sec.txt },\n" +
                        "  { case: { \"$eq\" : [$result.se_client_01.val,\"3\"] },\n" +
                        "  then : $result.prog_03_sec.txt },\n" +
                        "  { case : { $eq : [ $result.se_client_01.val,\"4\"] },\n" +
                        "  then : $result.prog_04_sec.txt},\n" +
                        "  { case : { $eq : [ $result.se_client_01.val,\"5\"] },\n" +
                        "  then : $result.prog_05_sec.txt },\n" +
                        "  { case : { $eq : [ $result.se_client_01.val,\"6\"] },\n" +
                        "  then : $result.prog_06_sec.txt } ],\n" +
                        "  default  : \"n/a\" } }"
                        ,BasicDBObject.class)))));
                aggQuery.add((DBObject)new BasicDBObject("$lookup",
                    new BasicDBObject("from","activities")
                    .append("localField","_id" )
                    .append("foreignField","activityfor")
                    .append("as","lastactivitydata")
                ));
                aggQuery.add((DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$lastactivitydata")
                        .append("preserveNullAndEmptyArrays", true)
                ));
                aggQuery.add((DBObject) new BasicDBObject("$group",
                    new BasicDBObject("_id","$_id")
                    .append("firstname",new BasicDBObject("$first","$firstname"))
                    .append("lastname",new BasicDBObject("$first","$lastname"))
                    .append("email",new BasicDBObject("$first","$email"))
                    .append("phoneNumber",new BasicDBObject("$first","$phoneNumber"))
                    .append("hphoneNumber",new BasicDBObject("$first","$hphoneNumber"))
                    .append("createdOn",new BasicDBObject("$first","$createdOn"))
                    .append("studentId",new BasicDBObject("$first","$studentId"))
                    .append("lastactivity",new BasicDBObject("$last","$lastactivitydata.createdOn")) 
                    .append("lastaction",new BasicDBObject("$last","$lastactivitydata.activity"))
                    .append("organization",new BasicDBObject("$first","$organization"))     
                    .append("verify",new BasicDBObject("$first","$verify"))
                    .append("groups",new BasicDBObject("$addToSet","$groupname"))
                    .append("program",new BasicDBObject("$first","$prog_01"))
                    .append("subprogram",new BasicDBObject("$first","$subprogram"))
                    .append("status",new BasicDBObject("$first","$status"))    
                ));
                
                aggQuery.add((DBObject)new BasicDBObject("$match",matchStart));
                
                        
//                Iterable<DBObject> count = collection.aggregate(aggQuery).results();
//                long size = count.spliterator().getExactSizeIfKnown();
                aggQuery.add((DBObject) new BasicDBObject("$project",
                    new BasicDBObject("firstname","$firstname")
                    .append("lastname","$lastname")
                    .append("email","$email")
                    .append("phoneNumber","$phoneNumber")
                    .append("hphoneNumber","$hphoneNumber") 
                    .append("createdOn","$createdOn")
                    .append("studentId","$studentId")
                    .append("lastactivity","$lastactivity") 
                    .append("lastaction","$lastaction")
                    .append("organization","$organization")     
                    .append("verify","$verify")
                    .append("groups","$groups")
                    .append("status","$status")
                    .append("program","$program") 
                    .append("subprogram","$subprogram")
                  ));
                
                
                aggQuery.add(((DBObject)new BasicDBObject("$facet", new BasicDBObject().append("data", (BasicDBList)com.mongodb.util.JSON.parse("[ {" +
                    "     $sort: {" +
                    "      "+sort+": "+order+
                    "     }}," +
                    "     {$skip: "+start+"}," +
                    "     {$limit:"+limit+"}" +
                    "    ]"))
                        .append("total",(BasicDBList)com.mongodb.util.JSON.parse("[ {$group: {_id: 1, count: {\"$sum\": 1}}} ]")))));
                
                
                

                    Iterable<DBObject> result = collection.aggregate(aggQuery).results();
                    Gson json = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
                    if(result.iterator().hasNext()) {
                        return json.toJson(result.iterator().next());
                    }
                    
                     return "{ \"data\": [], \"total\": [{\"count\":0}]}}";
                    
                       
        }
        catch(Exception ex){
         throw ex;   
        }
        
    } 
 @PUT
 @Path("clientcreatefilter/{id}")
   @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createOrUpdateFilter(
        @PathParam("id") String id,    
        @QueryParam("token") String token,
        @QueryParam("myfname") String myfiltername,
        @QueryParam("fn") String filtername,
        @QueryParam("gn") String groupname,
        @QueryParam("s") String status,
//        @QueryParam("sort") String sortby, 
        @QueryParam("isDflt") Boolean isDefault,    
        @QueryParam("enroll") String enrollment    
            
        
        ) throws Exception {
    try {
        SMRTUser user = this.validateToken(token, null); 
        DB mongoDB = this.getDBInstance();
        DBCollection filterCollection = mongoDB.getCollection("clientfilters");
        BasicDBObject condition = new BasicDBObject("isDefault",true);
        //if(filterCollection.find(condition).length()>1){}
        BasicDBObject document = new BasicDBObject();
        if(id.isEmpty()||id.equals("null") || id.equals("")){id =ObjectId.get().toString();}
        if(isDefault.equals(true))
        {
           BasicDBObject updateQuery = new BasicDBObject();
           updateQuery.append("$set",
           new BasicDBObject().append("isDefault", false));
           BasicDBObject searchQuery = new BasicDBObject("customerId", user.getCustomerId());
           searchQuery.append("isDefault",true);
           WriteResult result = filterCollection.updateMulti(searchQuery,updateQuery);
           result.getClass();
        }
        
        document.put("$set", 
                new BasicDBObject("createdBy",user.getId())
                .append("myfiltername",myfiltername)
                .append("filtername",filtername)
                .append("groupname",groupname)
                .append("status",status)
//                .append("sortby",sortby)        
                .append("isDefault",isDefault)
                .append("enrollment",enrollment)
                
                
        );
        document.put("$setOnInsert", new BasicDBObject("_id", id ).append("customerId", user.getCustomerId()));
        filterCollection.update(new BasicDBObject("_id",id),document, true, true, WriteConcern.ACKNOWLEDGED);
        return Response.ok().build();
        
    } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
    } finally{
        //ToDO 
    }
    
    }      
   @GET
    @Path("getClientFilter")
    @Produces({MediaType.APPLICATION_JSON})
    public String getFilters(@QueryParam("token") String token)throws Exception 
    {
    try {
        SMRTUser user = this.validateToken(token); 
        DB mongoDB = this.getDBInstance();
        DBCollection clientFilterCollection = mongoDB.getCollection("clientfilters");
        BasicDBObject condition = new BasicDBObject("createdBy",user.getId());
        DBCursor cursor = clientFilterCollection.find(condition);
        Gson json = new Gson();
            return json.toJson(cursor.toArray());
    }
     catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
    } finally{
        //ToDO 
    }

    }
    @DELETE
    @Path("deleteFilter/{id}")
    public String removefilter(@PathParam("id") String id,@QueryParam("token") String token)throws Exception{
        SMRTUser u=this.validateToken(token);
        try{
        DB mongoDB=this.getDBInstance();
        DBCollection filterCollection=mongoDB.getCollection("clientfilters");
        BasicDBObject fields=new BasicDBObject("_id",id);
        WriteResult result = filterCollection.remove(fields);
        }
        catch(Exception e){
            throw new Exception("Error");
        }
        return null;
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