/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.service;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.smrtsolutions.exception.ForbiddenException;
import com.smrtsolutions.exception.InvalidParameterException;
import com.smrtsolutions.survey.model.Customer;
import com.smrtsolutions.survey.model.ParticipantSurveyStatus;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.survey.model.Survey;
import com.smrtsolutions.survey.model.SurveyResult;
import com.smrtsolutions.util.Token;
import com.smrtsolutions.util.TokenUtil;
import com.smrtsolutions.util.Util;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import javax.ejb.Stateless;
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
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.AggregationOptions;
import com.mongodb.Cursor;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import com.smrtsolutions.survey.model.Permission;
import com.smrtsolutions.survey.model.PermissionTemplates;
import com.smrtsolutions.util.SMSReply;
import com.smrtsolutions.survey.model.SurveyAllocate;
import com.smrtsolutions.util.GsonUTCDateAdapter;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import static org.apache.log4j.config.PropertyPrinter.capitalize;
import org.apache.logging.log4j.Level;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.persistence.jpa.jpql.parser.DateTime;

/**
 *
 * @author lenny
 */
//@Stateless
@Path("/user")
public class SMRTUserREST extends SMRTAbstractFacade<SMRTUser> {
            
    private static final Logger logger = LogManager.getLogger(SMRTUserREST.class);
    public String[] REQUIRED_PERMISSIONS = {SMRTRole.NONE};
    Map<String, String> hmap = new HashMap<String, String>();
    Map<String, String> hmapVal = new HashMap<String, String>();
    //@PersistenceProperty(name="test", value="")
    //@PersistenceContext(unitName = "SMRT_PU")
   // private EntityManager em;

    public SMRTUserREST() {
        super(SMRTUser.class);
    }

    /*
    @POST
    //@Override
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTUser create(@PathParam("customerId") String customerId, SMRTUser entity) throws Exception {
        log("*** User create ***");
        this.setCustomerId(customerId);
        log("tenant=" + this.getCustomerId());
        entity.setCustomer(this.findCustomer());
        log("cust=" + entity.getCustomer());
        return super.create(entity);
        
    }*/
    
    @POST
    //customerId is dummy here. get customerId from reg code
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Token register(SMRTUser entity, @HeaderParam("Referer") String referer) throws Exception {
        log("*** User register ***");
        
        System.out.println("Good Evening I m Here=======================================");
        System.out.println("Refered is: "+referer);
        
        String[] keys = referer.split("/"); 
        String key;
        if(keys.length >5)
            key = keys[5];
        else
            key = keys[2].split("\\.")[0];
        
        if(key.endsWith("admin"))
            key = key.replace("admin","");
        if(key.endsWith("student"))
            key = key.replace("student","");
        if(key.endsWith("test"))
            key  = key.replace("test","");
        Customer c = this.findCustomerByUrlKey(key);
        
        if ( this.isEmpty( entity.getFirstname())) {
            throw new InvalidParameterException("First Name is required");
        } 
         if ( this.isEmpty( entity.getLastname())) {
            throw new InvalidParameterException("Last Name is required");
        } 
                    
        if ( this.isEmpty( entity.getEmail()) ){
            throw new InvalidParameterException("Email is required");
        } 
//        else if(entity.getEmail().matches(emailReg)){
//            throw new InvalidParameterException("Invalid Email");
//        }
    
        entity.setEmail(entity.getEmail().toLowerCase());
        if ( this.isEmpty( entity.getPassword()) ){
            throw new InvalidParameterException("Password is required");
        }
        
        if ( this.isEmpty( entity.getPhonenumber())){
            throw new InvalidParameterException("Phonenumber is required");
        }
        entity.setPhonenumber(entity.getPhonenumber());
        this.setCustomerId(c.getId());
        log("tenant=" + this.getCustomerId());
        entity.setCustomer(c);
        
        //check if login name is available. login name must be unique across all customers
        if ( !this.checkLoginAvailability(entity.getEmail(),this.getCustomerId(),entity.getId())){
            throw new InvalidParameterException("Email ID already registered. Try another");
        }
        entity.setLoginName(entity.getEmail());
        entity.setPassword(Util.encrypt(entity.getPassword()));
        entity.setCustomerId(c.getId());
        
        // this is for capitalize
        entity.setFirstname(capitalize(entity.getFirstname()));
        entity.setLastname(capitalize(entity.getLastname()));
        //TODO more validations
        
        
        if ( entity.getRoles() == null || entity.getRoles().size() <= 0){
            String defrole = c.getSetting("default_role", "student");
            List<String> roles = new ArrayList<String>();
            roles.add(defrole);
            entity.setRoles(roles);
        }
        entity.setUsertype("student");
        log("cust=" + entity.getCustomer().getId());
        SMRTUser u = super.create(entity);
        u.setCustomer(c);
        
        //create demographic data to basic survey
        //Add data to Demographics Survey for students
        DB mongoDB = this.getDBInstance();
        DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");

        BasicDBObject query = new BasicDBObject();
        query.put("participantId", u.getId());
        query.put("customerId", u.getCustomerId());
        BasicDBObject data = new BasicDBObject();
        String fname= capitalize(u.getFirstname());
        String lname = capitalize(u.getLastname());
        data.put("s1_req_1", new BasicDBObject("val", u.getFirstname()).append("txt", fname));
        data.put("s1_req_2", new BasicDBObject("val", u.getLastname()).append("txt", lname));
        data.put("s1_req_9", new BasicDBObject("val", u.getEmail()).append("txt", u.getEmail()));
        data.put("s1_req_7", new BasicDBObject("val", u.getPhonenumber()).append("txt", u.getPhonenumber()));
        data.put("st_st_06a",new BasicDBObject("val", new SimpleDateFormat("MM/dd/yyyy").format(new Date())).append("txt", new SimpleDateFormat("MM/dd/yyyy").format(new Date())));
        data.put("surveyStatus",new BasicDBObject());

        data = new BasicDBObject(
                    "$set",
                    data
                    .append("resultTime", new Date())
                ).append(
                    "$setOnInsert",
                    new BasicDBObject("participantId", u.getId())
                    .append("customerId",u.getCustomerId())
                  //  .append("createdBy", user.getId()) todo set userID from token
                );
        surveyResultCollection.update(query, data, true, false);
        // set activity for user
            DBCollection lastactivityCollection = mongoDB.getCollection("activities");
            BasicDBObject updateActivity =  new BasicDBObject();
            String activity = null;
            if(entity.getUsertype().equals("student")){
                activity = "Student - Record; Create";
            }
            else{
                activity = "Staff - Record; Create";
            }
            updateActivity.put("activity",activity);
                updateActivity.put("kind","n/a");
                updateActivity.put("createdBy",u.getId());
                updateActivity.put("createduname",u.getLastname()+", "+u.getFirstname());
                updateActivity.put("createdOn",new Date());
                updateActivity.put("customerId",u.getCustomerId());
                updateActivity.put("activityfor",u.getId());
                updateActivity.put("detail","n/a");
                updateActivity.put("information","n/a");
                lastactivityCollection.insert(updateActivity);
                
        this.updatestatus(u.getId(),"",u.getId());
        return this.createToken(u);
        
        //TODO - should generate a token and send to email for email verification
        
    } 
    
 
    @POST
    @Path("/add")
    //customerId is dummy here. get customerId from reg code
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTUser addUser(@QueryParam("token") String token, SMRTUser entity) throws Exception {
        
         SMRTUser usr =this.validateToken(token);
         entity.setCustomerId(usr.getCustomerId());
        
        Customer c = this.findCustomer(entity.getCustomerId());
          
        if ( this.isEmpty( entity.getPassword())) {
                entity.setPassword("smrt"); 
        } 
        if ( this.isEmpty( entity.getFirstname())) {
            throw new InvalidParameterException("First Name is required");
        } 
         if ( this.isEmpty( entity.getLastname())) {
            throw new InvalidParameterException("Last Name is required");
        } 
       
                
        if ( this.isEmpty( entity.getEmail()) ){
            throw new InvalidParameterException("Email is required");
        } 
        
        if ( this.isEmpty( entity.getPhonenumber()) ){
            throw new InvalidParameterException("Phonenumber is required");
        } 
        
        entity.setEmail(entity.getEmail().toLowerCase());
        if ( this.isEmpty( entity.getPassword()) ){
            throw new InvalidParameterException("Password is required");
        }
//         if(this.isEmpty(entity.getPermission_type())) {
//            throw new InvalidParameterException("Please select an access right");
//        }
        entity.setCustomerId(c.getId());
        //TODO more validations
        this.setCustomerId(c.getId());
        log("tenant=" + this.getCustomerId());
        entity.setCustomer(c);
        
                //check if login name is available. login name must be unique across all customers
        if ( !this.checkLoginAvailability(entity.getEmail(),this.getCustomerId(),entity.getId())){
            throw new InvalidParameterException("Email ID already registered. Try another");
        }

        entity.setLoginName(entity.getEmail());      
        entity.setPassword(Util.encrypt(entity.getPassword()));
        entity.setOrganization(entity.getOrganization());
        entity.setPermission_template_id(entity.getPermission_template_id());
        entity.setPermission_type(entity.getPermission_type());
        entity.setCreatedOn(new Date());
        entity.setCreatedBy(usr.getId());
        entity.setVerify(new ArrayList());
        entity.setPhonenumber(entity.getPhonenumber());
        // for capitalize the names
        entity.setFirstname(capitalize(entity.getFirstname()));
        entity.setLastname(capitalize(entity.getLastname()));
        
        if ( entity.getRoles() == null || entity.getRoles().size() <= 0){
            String defrole = c.getSetting("default_role", "student");
            List<String> roles = new ArrayList<String>();
            roles.add(defrole);
            entity.setRoles(roles);
        }
        
        log("cust=" + entity.getCustomer().getId());
        SMRTUser u = super.create(entity);
        u.setCustomer(c);
        
        //Add data to Demographics Survey for students
        if(u.getUsertype().equals("student")){
             DB mongoDB = this.getDBInstance();
            DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");

            BasicDBObject query = new BasicDBObject();
            query.put("participantId", u.getId());
            query.put("customerId", u.getCustomerId());
            BasicDBObject data = new BasicDBObject();
            data.put("s1_req_1", new BasicDBObject("val", capitalize(u.getFirstname())).append("txt", capitalize(u.getFirstname())));
            data.put("s1_req_2", new BasicDBObject("val", capitalize(u.getLastname())).append("txt", capitalize(u.getLastname())));
            data.put("s1_req_9", new BasicDBObject("val", u.getEmail()).append("txt", u.getEmail()));
            data.put("s1_req_7", new BasicDBObject("val",u.getPhonenumber()).append("txt",u.getPhonenumber()));
            data.put("st_st_06a",new BasicDBObject("val", new SimpleDateFormat("MM/dd/yyyy").format(new Date())).append("txt", new SimpleDateFormat("MM/dd/yyyy").format(new Date())));
            data.put("surveyStatus",new BasicDBObject());
            data = new BasicDBObject(
                        "$set",
                        data
                        .append("resultTime", new Date())
                    ).append(
                        "$setOnInsert",
                        new BasicDBObject("participantId", u.getId())
                        .append("customerId",u.getCustomerId())
                      //  .append("createdBy", user.getId()) todo set userID from token
                    );
            surveyResultCollection.update(query, data, true, false);
            this.updatestatus(u.getId(),"",usr.getId());
            surveyResultCollection.update(query, data, true, false);
        // set activity for user
            DBCollection lastactivityCollection = mongoDB.getCollection("activities");
            BasicDBObject updateActivity =  new BasicDBObject();
            String activity = null;
//            if(entity.getUsertype().equals("student")){
//                activity = "Student - Record; Create";
//            }
//            else{
//                activity = "Staff - Record; Create";
//            }
            updateActivity.put("activity","Staff - Record; Create");
                updateActivity.put("kind","n/a");
                updateActivity.put("createdBy",usr.getId());
                updateActivity.put("createduname",usr.getLastname()+", "+usr.getFirstname());
                updateActivity.put("createdOn",new Date());
                updateActivity.put("customerId",usr.getCustomerId());
                updateActivity.put("activityfor",entity.getId());
                updateActivity.put("detail","n/a");
                updateActivity.put("information","n/a");
                lastactivityCollection.insert(updateActivity);
//            DBCollection groupCollection=mongoDB.getCollection("group");
//            
//            BasicDBObject document=new BasicDBObject();
//            
//            document.put("$addToSet",new BasicDBObject("contents",u.getId()));
//            groupCollection.update(new BasicDBObject("customerId",u.getCustomerId()).append("isDefault", true),document, false, false, WriteConcern.NONE);
            }
        
        
        return u;
        
        //TODO - should generate a token and send to email for email verification
        
    }
    
    
     @PUT
    //customerId is dummy here. get customerId from reg code
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTUser update(SMRTUser entity) throws Exception {

        
        if ( this.isEmpty( entity.getFirstname())) {
            throw new InvalidParameterException("First Name is required");
        } 
         if ( this.isEmpty( entity.getLastname())) {
            throw new InvalidParameterException("Last Name is required");
        }   
        if ( this.isEmpty( entity.getEmail()) ){
            throw new InvalidParameterException("Email is required");
        }
        if(this.isEmpty(entity.getPermission_type()) && !(entity.getUsertype().equals("student"))) {
            throw new InvalidParameterException("Please select an access right");
        }

        entity.setEmail(entity.getEmail().toLowerCase());
        SMRTUser u = this.find(entity.getId());
                
        if (!u.getEmail().equals(entity.getEmail()) && !this.checkLoginAvailability(entity.getEmail(),entity.getCustomerId(),entity.getId())){
            throw new InvalidParameterException("Email ID already registered. Try another");
        }
        
        u.setFirstname(entity.getFirstname());
        u.setLastname(entity.getLastname());
        u.setEmail(entity.getEmail());
        u.setLoginName(entity.getEmail());
        u.setOrganization(entity.getOrganization());
        u.setPartnerId(entity.getPartnerId());
        u.setPermission_template_id(entity.getPermission_template_id());
        u.setPermission_type(entity.getPermission_type());
        u.setUsertype(entity.getUsertype());
        u.setRoles(entity.getRoles());
        u.setPermissions(entity.getPermissions());
        u.setPhonenumber(entity.getPhonenumber());
        u.setLocation(entity.getLocation());
        u.setServicename(entity.getServicename());
        return  this.edit(u);
        
    }
    
    @POST
    @Path("sendtext")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response sendTextMessage(@FormParam("number") String number,@FormParam("userId") String userId,@FormParam("msg") String msg,@QueryParam("token") String token, @Context HttpServletRequest context) throws Exception{
        SMRTUser u=this.validateToken(token);
        if (number.isEmpty() || msg.isEmpty()) 
            return Response.ok("{\"error\":\"Number/Message is empty\"}").build();
        String[] numbers = number.split(",");
        String[] userIds =userId.split(",");
        List<String> users = new ArrayList<String>(Arrays.asList(userId.split(",")));
        int errorCount =0 ;
        int successCount = 0;
        for(int idx=0;idx<numbers.length;idx++) {   
            //context.
            ObjectId id = new ObjectId();
            String callBackurl = "https://"+context.getServerName()+ context.getContextPath()+"/services/user/textreply/"+id.toString()+"/"+users.get(idx);    
            //String callBackurl = context.getScheme()+ "://"+context.getServerName()+":"+ context.getServerPort()+ context.getContextPath()+"/services/user/textreply/"+id.toString()+"/"+userId ;
             //numbers[idx] = numbers[idx].replaceAll(" ", "").replaceAll("-", "").replaceAll("\\(", "").replaceAll("\\)", "");
        BasicDBObject message=new BasicDBObject();
                            
                    message.append("callback_url",callBackurl)
                            .append("content", msg)
                            .append("source_number", "18885039775")
                            .append("source_number_type","INTERNATIONAL")
                            .append("destination_number", "+1"+numbers[idx])
                            .append("format","SMS");
                      
                String req = "{\n" +
                            "  \"messages\": [\n" + new Gson().toJson(message)+
                            "  ]\n" +
                            "}";
//                
//        String req = "{\n" +
//                    "  \"messages\": [\n" +
//                    "    {\n" +
//                    "      \"callback_url\": \"" + callBackurl + "\",\n" +
//                    "      \"content\": \"" + msg + "\",\n" +
//                    "      \"source_number\": \"18885039775\",\n" +
//                    "      \"source_number_type\": \"INTERNATIONAL\",\n" +
//                    "      \"destination_number\": \"+1" + numbers[idx] + "\",\n" +
//                    "      \"format\": \"SMS\"\n" +
//                    "    }\n" +
//                    "  ]\n" +
//                    "}";
        try {
            String result = Util.sendPostRequest("http://api.messagemedia.com/v1/messages",req);
            BasicDBObject resultJson = new Gson().fromJson(result, BasicDBObject.class);
            DB mongoDB=this.getDBInstance();
            DBCollection communication= mongoDB.getCollection("communications");
            BasicDBObject document= new BasicDBObject();
            document.put("_id", id);
            document.put("type","SMS");
            document.put("customerId", u.getCustomerId());
            document.put("receiver",numbers[idx]);
            document.put("userId",userIds[idx]);
            document.put("message",msg);
            document.put("status",resultJson);
            document.put("errormsg","");
            document.put("senton",Calendar.getInstance().getTime());
            document.put("sentby",u.getId());
            communication.insert(document);
            if(u.getUsertype().equals("casemanager")){
            DBCollection txtreply= mongoDB.getCollection("sms_reply");
            BasicDBObject updateQuery = new BasicDBObject();
            updateQuery.append("$set",new BasicDBObject()
            .append("replystatus",true)
            );
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.append("userId",userIds[idx]);
            txtreply.updateMulti(searchQuery,updateQuery);
            }
            successCount++;
        } catch (Exception ex) {
             DB mongoDB=this.getDBInstance();
            DBCollection communication= mongoDB.getCollection("communications");
            BasicDBObject document=new BasicDBObject();
            document.put("_id", id);
            document.put("type","SMS");
            document.put("customerId", u.getCustomerId());
            document.put("receiver",numbers[idx]);
            document.put("userId",userIds[idx]);
            document.put("message",msg);
            document.put("status","failed");
            document.put("errormsg",ex.getMessage());
            document.put("senton",Calendar.getInstance().getTime());
            document.put("sentby",u.getId());
            communication.insert(document);
            errorCount++;
            //return Response.ok("{\"error\":\"" + ex.getMessage() + "\"}").build();
        }
        
        }
//        return Response.ok("{\"success\":\"OK\"}").build();
        return Response.ok("{\"success\":\"OK\"}").build();
    }
    
    @POST
    @Path("/sendemail")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String sendEmail(@FormParam("cc") String cc,@FormParam("subject") String subject,
    @FormParam("msg") String msg,@FormParam("emailId") String email,@FormParam("userId") String userid,@QueryParam("token") String token) throws Exception{
        List<String> ccs=new ArrayList<String>(Arrays.asList(cc.split(",")));
   //  List<String> emailId=new ArrayList<String>(Arrays.asList(email.split(",")));
        SMRTUser u=this.validateToken(token);
          
        DB mongodb = this.getDBInstance();
        DBCollection lastactivityCollection = mongodb.getCollection("activities");
        BasicDBObject update =  new BasicDBObject();
        
          String[] emails = email.split(",");
          String[] userIds = userid.split(",");
            int errorCount =0 ;
            int successCount = 0;
            List<String> to = new ArrayList<String>();
        for(int idx=0;idx<emails.length;idx++) {    
            to.clear();
            to.add(emails[idx]);
            try {
            Emailer.sendEmail(to, ccs, subject, msg);
            DB mongoDB=this.getDBInstance();
            DBCollection communication= mongoDB.getCollection("communications");
            BasicDBObject document=new BasicDBObject();
            document.put("type","E-mail");
            document.put("customerId", u.getCustomerId());
            document.put("receiver",emails[idx]);
            document.put("userId",userIds[idx]);
            document.put("cc",cc);
            document.put("message",msg);
            document.put("status","sent");
            document.put("errormsg","");
            document.put("senton",Calendar.getInstance().getTime());
            document.put("sentby",u.getId());
            communication.insert(document);
            successCount++;
   //        return Response.ok("{\"success\":\"OK\"}").build();
        } catch (Exception ex) {
        
            DB mongoDB=this.getDBInstance();
            DBCollection communication= mongoDB.getCollection("communications");
            BasicDBObject document=new BasicDBObject();
            document.put("type","E-mail");
            document.put("customerId", u.getCustomerId());
            document.put("receiver",emails[idx]);
            document.put("userId",userIds[idx]);
            document.put("cc",cc);
            document.put("message",msg);
            document.put("status","failed");
            document.put("errormsg",ex.getMessage());
            document.put("senton",Calendar.getInstance().getTime());
            document.put("sentby",u.getId());
            communication.insert(document);
            errorCount++;
            return "\"Error sending email. please try again later\"";
 //             return Response.ok("{\"error\":\"" + ex.getMessage() + "\"}").build();
        }
            
            // set activity for student 
                update.put("activity","Staff - Email");
                update.put("kind","n/a");
                update.put("createdBy",u.getId());
                update.put("createduname",u.getLastname()+", "+u.getFirstname());
                update.put("createdOn",new Date());
                update.put("customerId",u.getCustomerId());
                update.put("activityfor",userIds[idx]);
                update.put("detail","n/a");
                update.put("information","n/a");
                update.remove("_id");
                lastactivityCollection.insert(update);
            
       }
        return "\" Your email has been sent successfully \"";
    }
    
     @GET
    @Path("/communications")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String getCommunicationList(@QueryParam("userId") String userId,@QueryParam("token") String token,@QueryParam("type") String type, @QueryParam("from") int from,@QueryParam("limit") int limit) throws Exception{
       
        SMRTUser u = this.validateToken(token);
            
        DB mongoDB = this.getDBInstance();
        DBCollection communicationCollection = mongoDB.getCollection("communications");
//        List<DBObject> result = communicationCollection.find(new BasicDBObject("userId", userId).append("type",type))
//                .sort(new BasicDBObject("senton",-1))
//                .skip(from)
//                .limit(limit)
//                .toArray();
//        
        List<DBObject> arr = new ArrayList<>(Arrays.asList(
                (DBObject) new BasicDBObject("$match", 
                        new BasicDBObject("userId" ,userId).append("type", type)
                ),
                (DBObject) new BasicDBObject("$sort", 
                        new BasicDBObject("senton",-1)
                ),
                (DBObject) new BasicDBObject("$skip", from),
                (DBObject) new BasicDBObject("$limit", limit),
                (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"SMRTUser")
                        .append("localField", "sentby")
                        .append("foreignField", "_id")
                        .append("as", "sentby")
                ),
                (DBObject) new BasicDBObject("$unwind", "$sentby")
        ));
        
        if(type.equals("SMS")){
            arr.add((DBObject) new BasicDBObject("$lookup", 
                new BasicDBObject("from" ,"sms_reply")
                .append("localField", "_id")
                .append("foreignField", "msg_id")
                .append("as", "reply")
            ));
        }

        Iterable<DBObject> result = communicationCollection.aggregate(arr).results();
        Gson json = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
            return json.toJson(result);
      //  return new Gson().toJson(result);
    }
    @GET
    @Path("/textnotification")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String getTextNotificationList(@QueryParam("token") String token,
    @QueryParam("sort") String sort,
    @QueryParam("order") int order,
    @QueryParam("start") int start,
    @QueryParam("limit") int limit,
    @QueryParam("search") String search
            
    ) throws Exception{
        try{
        SMRTUser u = this.validateToken(token);
        if (sort.isEmpty()) sort="receivedon";
        BasicDBObject match = new BasicDBObject();
        if(!search.isEmpty()){
            BasicDBList nameList = new BasicDBList();
            nameList.add(new BasicDBObject("texts.studenttxt.firstname",new BasicDBObject("$regex", search).append("$options","i")));
            nameList.add(new BasicDBObject("texts.studenttxt.lastname",new BasicDBObject("$regex", search).append("$options","i")));
            match.put("$or", nameList);
        }
        DB mongoDB = this.getDBInstance();
        DBCollection groupCollection = mongoDB.getCollection("group");
        Long grpcount = groupCollection.count(new BasicDBObject("customerId", u.getCustomerId()).append("textnotification",this.getUser().getId()));
        BasicDBList userList = new BasicDBList();
        if(grpcount>0){ 
                            
            Iterable<DBObject>  userResult = groupCollection.aggregate(new ArrayList<DBObject>(Arrays.asList(
                (DBObject) new BasicDBObject("$match", new BasicDBObject("customerId", u.getCustomerId()).append("textnotification", u.getId()))
                  , (DBObject) new BasicDBObject("$project", new BasicDBObject("contents", 1))
                  , (DBObject) new BasicDBObject("$unwind", "$contents")
                  //, (DBObject) new BasicDBObject("$group", new BasicDBObject("_id", "$contents")
                  , (DBObject) new BasicDBObject("$group", new BasicDBObject("_id",null)
                  .append("user",new BasicDBObject("$addToSet","$contents")))
            ))).results();
            
            if(userResult.iterator().hasNext()){
                userList = (BasicDBList) userResult.iterator().next().get("user");
            }
        }

        DBCollection communicationCollection = mongoDB.getCollection("sms_reply");
        BasicDBObject matchCond = new BasicDBObject("replystatus",false);
        if(grpcount > 0){
            matchCond.append("userId", new BasicDBObject("$in", userList));
        }
        
        
        List<DBObject> arr = new ArrayList<DBObject>(Arrays.asList(
                (DBObject) new BasicDBObject("$match", 
                     matchCond
                ),
                (DBObject) new BasicDBObject("$lookup", 
                    new BasicDBObject("from" ,"SMRTUser")
                    .append("localField","userId")
                    .append("foreignField","_id")
                    .append("as","users")
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$users")
                     .append("preserveNullAndEmptyArrays", false)
                ),
                (DBObject) new BasicDBObject("$match",new BasicDBObject("users.customerId",u.getCustomerId())
                ),
                (DBObject) new BasicDBObject("$project", 
                new BasicDBObject()
                .append("userid","$users._id")
                .append("studenttext","$content")
                .append("receivedon","$date_received")
                .append("studentmsg_id","$msg_id")
                .append("studentfirstname","$users.firstname")
                .append("studentlastname","$users.lastname")
                .append("phonenumber","$source_number")
                )
               // ,(DBObject) new BasicDBObject("$sort",new BasicDBObject(sort,order))
                ,(DBObject) new BasicDBObject("$group",new BasicDBObject("_id" ,"$userid")
                .append("count",new BasicDBObject("$sum",1))
                .append("texts", new BasicDBObject("$first",new BasicDBObject("userid" ,"$userid")
                .append("studenttxt",new BasicDBObject("text","$studenttext")
                    .append("receivedon","$receivedon")
                    .append("msg_id","$studentmsg_id")
                    .append("firstname","$studentfirstname")
                    .append("phonenumber","$phonenumber")
                    .append("lastname","$studentlastname")       
                )))),
                (DBObject) new BasicDBObject("$match",match)
                
        ));
        Iterable<DBObject> count = communicationCollection.aggregate(arr).results();
        long size = count.spliterator().getExactSizeIfKnown();
            arr.add((DBObject) new BasicDBObject("$sort",new BasicDBObject(sort,order)));
            arr.add((DBObject) new BasicDBObject("$skip",start));
            arr.add((DBObject) new BasicDBObject("$limit",limit));
            
        Iterable<DBObject> result = communicationCollection.aggregate(arr).results();
            
            for(DBObject res: result){
                ((BasicDBObject)res).append("total", size);
            }
            
        //Iterable<DBObject> r = communicationCollection.aggregate(arr).results();
        Gson json = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
            return json.toJson(result);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
        } finally{
            //ToDO
        } 
      //  return new Gson().toJson(result);
    }
    
    @POST
    @Path("textreply/{id}/{userId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response textReply(@PathParam("id") String id,@PathParam("userId") String userId, SMSReply entity) throws Exception{
        if(entity.getReply_id()== null || entity.getReply_id().isEmpty()){
            //ignore, delivery callback
            return Response.ok().build();
        }
        DB mongoDB = this.getDBInstance();
        DBCollection communicationReply = mongoDB.getCollection("sms_reply");
        //List<String> users = new ArrayList<String>(Arrays.asList(userId.split(",")));
        entity.setReplystatus(Boolean.FALSE);
        entity.setUserId(userId);
        BasicDBObject data = (BasicDBObject)JSON.parse(new Gson().toJson(entity));
        data.append("msg_id", new ObjectId(id));
//        data.append("replystatus", false);
//        data.append("userId", userId);
        communicationReply.insert(data);
        return Response.ok().build();
    }
    
    @GET
    @Path("getusertexts")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String getusertexts(@QueryParam("token") String token,@QueryParam("userId") String userId)throws Exception{
    try{
    SMRTUser u=this.validateToken(token);
    DB mongoDB=this.getDBInstance();
    DBCollection communicationCollection = mongoDB.getCollection("communications");
    List<DBObject> arr = new ArrayList<>(Arrays.asList(
                (DBObject) new BasicDBObject("$match", 
                        new BasicDBObject("userId" ,userId).append("type", "SMS")
                ),
                (DBObject) new BasicDBObject("$sort", 
                        new BasicDBObject("senton",-1)
                ),
                (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"SMRTUser")
                        .append("localField", "sentby")
                        .append("foreignField", "_id")
                        .append("as", "sentby")
                ),
                (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"SMRTUser")
                        .append("localField", "userId")
                        .append("foreignField", "_id")
                        .append("as", "receiver")
                ),
                (DBObject) new BasicDBObject("$unwind", "$sentby"),
                (DBObject) new BasicDBObject("$unwind", "$receiver")
        ));
        arr.add((DBObject) new BasicDBObject("$lookup", 
                new BasicDBObject("from" ,"sms_reply")
                .append("localField", "_id")
                .append("foreignField", "msg_id")
                .append("as", "reply")
            ));
        arr.add((DBObject) new BasicDBObject("$lookup", 
                new BasicDBObject("from" ,"sms_reply")
                .append("localField", "userId")
                .append("foreignField", "userId")
                .append("as", "unseensms")
            ));
        

        Iterable<DBObject> result = communicationCollection.aggregate(arr).results();
        Gson json = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
            return json.toJson(result);
    }   
    catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
        } finally{
            //ToDO
        } 
    }
       
    
    private Token createToken(SMRTUser user) throws Exception{
        Token token = new Token();
        token.setAccessToken(TokenUtil.generateToken(user));
        token.setName(user.getName());
        token.setCustomerBranding(user.getCustomer().getBrandingHtml());
        token.setCustomerId(user.getCustomer().getId());
        token.setServiceLevel(user.getCustomer().getServiceLevel());
        token.setRoles(this.getRoleNames(user));
        token.setHome(this.getHomeUrl(user));
        token.setUserPermission(user.getPermissions());
        return token;
    }
    
    private String getHomeUrl (SMRTUser user) throws Exception{
        if ( this.hasPermission(user, SMRTRole.ALL)
                || this.hasPermission(user, SMRTRole.DASH_ADMIN) 
                || this.hasPermission(user, SMRTRole.USER_ADMIN)
                || this.hasPermission(user, SMRTRole.SURVEY_ADMIN)) {
            return "#/admin";
        } else if ( this.hasPermission(user, SMRTRole.CASE_DASH)
                || this.hasPermission(user, SMRTRole.OBSERVER_DASH)) {
            return "#/casedashboard";         
        } else {
            return "#/survey";
        }
    }
    
    private String[] getRoleNames(SMRTUser user) {
        /*String[] rs = new String[user.getRoles().size()];
        int i = 0;
        for ( SMRTRole r: user.getRoles() ){
            rs[i] = r.getName();
            i++;
        }
        return rs;*/
        if ( user.getRoles() == null) return new String[0];
        return user.getRoles().toArray(new String[user.getRoles().size()]);
    }
    private String[] getPermissions(SMRTUser user) throws Exception{
        Map<String,String> rs = new HashMap<String,String>();
        //get the distinct list of all the user's permissions
        List<SMRTRole> customerRoles = this.getCustomerRoles(user);
        for ( String rn: user.getRoles() ){
            SMRTRole r  = this.findRole(customerRoles, rn);
            String p = r.getPermissions();
            if ( !Util.isEmpty(p)) {
                String[] ps = p.split(",");
                for (String p1 : ps) {
                    String k = Util.getValue(p1);
                    if ( !Util.isEmpty(k)){
                        rs.put(k,k);
                    }
                }
            }
        }
        //convert the keys to an array
        Set<String> s = rs.keySet();
        String ret[] = s.toArray(new String[s.size()]);
        return ret;
    }   
    private String[] getTabs(SMRTUser user) throws Exception{
        Map<String,String> rs = new HashMap<String,String>();
        //get the distinct list of all the user's tabs
        List<SMRTRole> customerRoles = this.getCustomerRoles(user);
        for ( String rn: user.getRoles() ){
            SMRTRole r  = this.findRole(customerRoles, rn);
            String p = r.getTabs();
            if ( !Util.isEmpty(p)) {
                String[] ps = p.split(",");
                for (String p1 : ps) {
                    String k = Util.getValue(p1);
                    if ( !Util.isEmpty(k)){
                        rs.put(k,k);
                    }
                }
            }
        }
        //convert the keys to an array
        Set<String> s = rs.keySet();
        String ret[] = s.toArray(new String[s.size()]);
        return ret;
    }
    
    @POST
    @Path("authenticate")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public Token authenticate(@FormParam("loginName") String loginName
                , @FormParam("password") String password
                , @FormParam("customerRef") String customerRef
                , @HeaderParam("Referer") String referer
    
    ) throws Exception {
        if ( this.isEmpty( loginName) ){
            throw new InvalidParameterException("Login Name is required");
        }
        
        if ( this.isEmpty( password) ){
            throw new InvalidParameterException("Password is required");
        }
        String encpasswd = Util.encrypt(password);
        this.log("enc password=" + encpasswd);
        //find the customer from the URL key
        
        String[] keys = referer.split("/"); 
        String key;
        if(keys.length >5)
            key = keys[5];
        else
            key = keys[2].split("\\.")[0];
        
        if(key.endsWith("admin"))
            key = key.replace("admin","");
        if(key.endsWith("student"))
            key = key.replace("student","");
        if(key.equals("sspdtest"))
            key = "sspd";
        //return this.createToken(this.findUserByCredentials(Util.getValue(loginName).toLowerCase(), encpasswd));
        SMRTUser u = this.findUserByCredentials(Util.getValue(loginName),encpasswd,key);
           
            
        Customer uc = u.getCustomer();
        if ( uc == null ){
            throw new InvalidParameterException("System error, please contact support.");
        }
        
        if(u.getUsertype().equals("student")){
            //set permissions for demographics
            DB MongoDB =this.getDBInstance();
            DBCollection surveyallocateCollection = MongoDB.getCollection("surveyallocate");
            BasicDBObject query = new BasicDBObject("userId",u.getId());
            BasicDBObject fields =new BasicDBObject("survey_id",1);
            DBCursor cursor = surveyallocateCollection.find(query,fields);

             List<String> list=new ArrayList<>();

             while(cursor.hasNext()){
                BasicDBObject surveyResult = (BasicDBObject) cursor.next();
                list.add((String)surveyResult.get("survey_id"));
            }
             
            this.setCustomerId(u.getCustomerId());
            List<Survey> surveys = this.findDefaultSurveys(u.getCustomerId(),list);
            
            
            List<Permission> permissions = new ArrayList<Permission>();
            for(int i=0; i< surveys.size(); i++){
                Permission permission =  new Permission();
                permission.setCanview(true);
                permission.setCanadd(true);
                permission.setCanedit(true);
                permission.setModule_id(surveys.get(i).getId());
                permissions.add(permission);
            }
            u.setPermissions(permissions);
        }
//        else{ 
//        
//            String templateId= u.getPermission_template_id();
//            if(templateId !=null && !templateId.equals("")){
//            PermissionTemplates template =  this.getEntityManager().find(PermissionTemplates.class, templateId);
//            u.setPermissions(template.getPermissions());
//            }
        
        return this.createToken(u);

    }
    
    @PUT
    @Path("password")
    @Produces({MediaType.APPLICATION_JSON})
    public Response changePassword(
            @FormParam("userId") String userId,
            @FormParam("password") String password,
            @FormParam("token") String token
            ) throws Exception {
        SMRTUser u = this.validateToken(token, null);
        
        if (userId.isEmpty() || userId.equals("0")) userId = u.getId();
        SMRTUser us = this.findUser(userId);
        if ( this.isEmpty( password ) ){
            throw new InvalidParameterException("Invalid password");
        }
        
        String encpasswd = Util.encrypt(password);
        this.log("enc password=" + encpasswd);
        this.updateCredentials(us, encpasswd);
        return Response.ok().build();
    }
    
    @PUT
    @Path("ignoretxt")
    @Produces({MediaType.APPLICATION_JSON})
    public String ignoreTxtReply(
        @FormParam("userId") String userId,
        @FormParam("token") String token
        ) throws Exception {
        SMRTUser u = this.validateToken(token, null);
        DB mongoDB = this.getDBInstance();
        DBCollection txtreply= mongoDB.getCollection("sms_reply");
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.append("$set",new BasicDBObject()
        .append("replystatus",true)
        );
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.append("userId",userId);
        txtreply.updateMulti(searchQuery,updateQuery);
        return null;
        
    }

    /*
    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTUser edit(@PathParam("customerId") String customerId, @PathParam("id") String id, SMRTUser entity) throws Exception {
        this.setCustomerId(customerId);
        entity.setCustomer(this.findCustomer());
        return super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("customerId") String customerId, @PathParam("id") String id) throws Exception {
        this.setCustomerId(customerId);
        
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTUser find(@PathParam("customerId") String customerId, @PathParam("id") String id)  throws Exception{
        this.setCustomerId(customerId);
        return super.find(id);
    }

    @GET
    //@Override
    //@Path("/customer/{customerId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<SMRTUser> findAll(//@PathParam("customerId") String customerId
        ) throws Exception {
        
        this.setCustomerId(customerId);
        return super.findAll();
        
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<SMRTUser> findRange(@PathParam("customerId") String customerId, @PathParam("from") Integer from, @PathParam("to") Integer to) throws Exception {
        this.setCustomerId(customerId);
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST(@PathParam("customerId") String customerId) throws Exception {
        this.setCustomerId(customerId);
        return String.valueOf(super.count());
    }*/
    
    
    @GET
    @Path("{participantId}/survey/status")
    @Produces({MediaType.APPLICATION_JSON})
    public String getAllSurveyResultStatus(
              @PathParam("participantId") String participantId
            , @QueryParam("token") String token
            , @QueryParam("surveyType") String surveyType
            , @QueryParam("surveyId") String surveyId
            , @QueryParam("limit") int limit, @QueryParam("offset") int offset
    )  throws Exception{
        
        SMRTUser user = this.validateToken(token, null);
        
        //TODO check permissions
        // User should have search permissions to check view others data
        if ( this.hasPermission(user, SMRTRole.OTHERS_DATA_SEARCH)){
            //allow user to add data for other participants
            System.out.println("1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n 11111111111111add");
            user.setId(participantId);
            System.out.println("11111111111111add "+user.getId());
            System.out.println("11111111111111add "+participantId);
        } else{
            // For student login participant id is passed as value "0" so checking that condition
            if(!Util.isEmpty(participantId) && participantId.equals("0")){
                System.out.println("11111111111111pid");
            } else
            throw new Exception("You do not have required permissions to view others data");
        }
        this.setCustomerId(user.getCustomerId());
        //String customerId = "TODO";
        logger.debug("!1");
        //Query q = this.getEntityManager().createNativeQuery("select s.survey_id, sr.status, sr.customer_id, sr.user_id, sr.survey_result_id, sr.survey_id from survey_result sr left outer join survey s on s.survey_id = sr.survey_id where sr.customer_id = ?1 and sr.user_id = ?2", SurveyResult.class);
        //TODO if NOT useMaster then have alt sql
        Customer c = this.findCustomer(user);
        String sql = "";
        String masterDataId = c.getSetting("survey_master_id", "_1");
        boolean usemaster = false;
        
        String cid = c.getId();
        String scid = masterDataId.equalsIgnoreCase("_1")? cid : masterDataId;
        String uid = user.getId();
       
//          SMRTUser us = this.findUser(userId);


           
        //flow - controlled | sequential | immediate (none)
        //controlled_for : all_participants | participant
        String surveyFlow = c.getSetting("survey_flow", "none");
        String surveyFlowControl = c.getSetting("survey_flow_control", "participant");
//        System.out.println("1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n");
        List<Survey> surveys = null;
        if(surveyType.equals("all")){
            surveys = this.findAllSurveys(scid);
        } 
        else if(user.getUsertype().equals("student")){
            DB MongoDB =this.getDBInstance();
            DBCollection surveyallocateCollection = MongoDB.getCollection("surveyallocate");
            BasicDBObject query = new BasicDBObject("userId",uid);
            BasicDBObject fields =new BasicDBObject("survey_id",1);
            DBCursor cursor = surveyallocateCollection.find(query,fields);

             List<String> list=new ArrayList<>();

             while(cursor.hasNext()){
                BasicDBObject surveyResult = (BasicDBObject) cursor.next();
                list.add((String)surveyResult.get("survey_id"));
            }
             surveys = this.findDefaultSurveys(scid,list);
        }
        else {
            
            surveys = this.findSurveysByType(scid, surveyType);
        }        
//        System.out.println("1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n"+surveys);
        if ( "none".equalsIgnoreCase(surveyFlow)){
            List<ParticipantSurveyStatus> st = new ArrayList<ParticipantSurveyStatus>();
            for ( Survey s : surveys){
                ParticipantSurveyStatus pss = new ParticipantSurveyStatus();
                pss.setCustomerId(c.getId());
                pss.setSurveyId(s.getId());
                pss.setParticipantId(uid);
                pss.setStatus(SurveyResult.STATUS.PENDING.ordinal());
                pss.setSurveyName(s.getName());
                pss.setSurveyDescription(s.getDescription());
                pss.setSurveyLabel(s.getLabel());
                st.add(pss);
            }
            
            Gson json = new Gson();            
            return json.toJson(st);
        } else if ( "controlled".equalsIgnoreCase(surveyFlow)){
            if ( "participant".equalsIgnoreCase(surveyFlowControl)){
                //sql = "select sr from ParticipantSurveyStatus sr where sr.customerId = ?1 and sr.participantId = ?2";   
                sql = "select sr from SurveyResult sr where sr.customerId = ?1 and sr.participantId = ?2";   
            } else if ( "allparticipants".equalsIgnoreCase(surveyFlowControl)){
                //sql = "select sr from ParticipantSurveyStatus sr where sr.customerId = ?1 and sr.participantId = ?2"; 
                sql = "select sr from SurveyResult sr where sr.customerId = ?1 and sr.participantId = ?2";
                uid = "1"; //use a master
            } else {
                throw new Exception("Unhandled survey_flow_control=" + surveyFlowControl);
            }
        } else if (  "sequential".equalsIgnoreCase(surveyFlow)){
            //sql = "select sr from ParticipantSurveyStatus sr where sr.customerId = ?1 and sr.participantId = ?2";   
            sql = "select sr from SurveyResult sr where sr.customerId = ?1 and sr.participantId = ?2";
        } else {
            throw new Exception("Unhandled survey_flow=" + surveyFlow);
        }
//        //Query q = this.getEntityManager().createQuery(sql, ParticipantSurveyStatus.class);
//        Query q = this.getEntityManager().createQuery(sql, SurveyResult.class);
//        q.setHint("javax.persistence.cache.storeMode", "BYPASS");
//        q.setHint("javax.persistence.cache.retrieveMode", "BYPASS");
//        q.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
//        q.setHint(QueryHints.MAINTAIN_CACHE, HintValues.FALSE);
//        //q.setParameter("1", this.decodeId(customerId));
//        //q.setParameter("2", this.decodeId(userId));
//        q.setParameter("1", user.getCustomerId());
//        q.setParameter("2", uid);
//        q.setMaxResults(limit <= 0 ? 25 : limit);
//        q.setFirstResult(offset);
//        logger.debug("!1.1");
//        //List<ParticipantSurveyStatus> sr = q.getResultList();
//        List<SurveyResult> sr = q.getResultList();
//        if ( sr == null) {
//            logger.debug("!1.2");
//            //sr = new ArrayList<ParticipantSurveyStatus>();
//            sr = new ArrayList<SurveyResult>();
//        } else {
//            logger.debug("!1.3");
//            logger.debug(sr);
//        }
//        logger.debug("!2");
        // join in for all surveys
        List<ParticipantSurveyStatus> st = new ArrayList<ParticipantSurveyStatus>();
        
        //get Survey status from Survey result
        DB mongoDB = this.getDBInstance();
        DBCollection surveyResultsCollection = mongoDB.getCollection("survey_results");
        BasicDBObject query = new BasicDBObject("participantId",uid);
        BasicDBObject fields = new BasicDBObject("surveyStatus",1);
        BasicDBObject rec = (BasicDBObject) surveyResultsCollection.findOne(query, fields);
        
        if(rec != null){
            rec = (BasicDBObject) rec.get("surveyStatus"); //get survey status field if exists
        }
        
        for ( Survey s : surveys){
            
            if(!surveyType.equals("all") && !user.getUsertype().equals("student") && !s.getId().equals(surveyId)){
                continue;
            }
            
            ParticipantSurveyStatus pss = new ParticipantSurveyStatus();
            pss.setCustomerId(c.getId());
            pss.setSurveyId(s.getId());
            pss.setParticipantId(uid);
            pss.setSurveyName(s.getName());
            pss.setSurveyDescription(s.getDescription());
            pss.setSurveyLabel(s.getLabel());
            pss.setSurveyType(s.getSurveyType());
            
            if ( rec != null){
                Object res = rec.get(s.getId());
                if(res!= null){
                    pss.setStatus((int) res);
                }else{
                    pss.setStatus(SurveyResult.STATUS.PENDING.ordinal());
                }
                    
            } else {
                pss.setStatus(SurveyResult.STATUS.PENDING.ordinal());
            }
            pss.setSurveySettings(s.getSettings());
            if(surveyType.equals("all")){
                if(pss.getStatus() >= SurveyResult.STATUS.COMPLETE.ordinal() ){
                    st.add(pss);
                }
            } else {                
                st.add(pss);
            }
        }
        
        Gson json = new Gson();            
        return json.toJson(st);
    }
    
     @GET
    @Path("formlist")
    @Produces({MediaType.APPLICATION_JSON})
    public String getStudentFormList(@QueryParam("token") String token)  throws Exception{
        SMRTUser user = this.validateToken(token);
        if(!user.getUsertype().equals("student"))
            throw new ForbiddenException("Access Denied.");
        
        DB mongoDB = this.getDBInstance();
        DBCollection surveyCollection = mongoDB.getCollection("survey");
        
        //get default survey and survey allocated for student
        Iterable<DBObject> result = surveyCollection.aggregate(Arrays.asList(
            (DBObject) new BasicDBObject("$match", 
                new BasicDBObject("customerId" ,user.getCustomerId())
            ),
            (DBObject) new BasicDBObject("$lookup", 
                new BasicDBObject("from" ,"surveyallocate")
                    .append("localField","_id")
                    .append("foreignField","survey_id")
                    .append("as","allocate")
            ),
            (DBObject) new BasicDBObject("$unwind", 
                new BasicDBObject("path" ,"$allocate")
                    .append("preserveNullAndEmptyArrays", true)
            ),
            (DBObject) new BasicDBObject("$match", 
                new BasicDBObject("$or", (BasicDBList)com.mongodb.util.JSON.parse(
                    "[" +
                    "   {isDefault: \"Yes\"}," +
                    "   {allocationType: \"auto\"}," +
                    "   {\"allocate.userId\":\""+ user.getId() +"\"}" +
                    "]"
                ))
            ),
            (DBObject) new BasicDBObject("$sort", 
                new BasicDBObject("allocate.createdOn" ,1)
            ),
            (DBObject) new BasicDBObject("$group", 
                new BasicDBObject("_id" ,"$_id")
                    .append("label", new BasicDBObject("$first","$label"))
                    .append("isDefault", new BasicDBObject("$first","$isDefault"))
                    .append("url", new BasicDBObject("$first","$url"))
                    .append("surveyEntrySequence", new BasicDBObject("$first","$surveyEntrySequence"))
                    .append("allocate", new BasicDBObject("$first","$allocate"))
            ),
            (DBObject) new BasicDBObject("$lookup", 
                new BasicDBObject("from" ,"SMRTUser")
                    .append("localField","allocate.createdBy")
                    .append("foreignField","_id")
                    .append("as","assignedBy")
            ),
            (DBObject) new BasicDBObject("$unwind", 
                new BasicDBObject("path" ,"$assignedBy")
                    .append("preserveNullAndEmptyArrays", true)
            ),
            (DBObject) new BasicDBObject("$project", 
                new BasicDBObject("label" ,1)
                    .append("isDefault",1)
                    .append("url",1)    
                    .append("surveyEntrySequence",1)
                    .append("assignedBy", 
                        new BasicDBObject("id","$assignedBy._id")
                            .append("firstname","$assignedBy.firstname")
                            .append("lastname","$assignedBy.lastname")
                            .append("usertype","$assignedBy.usertype")
                    )
            ),
            (DBObject) new BasicDBObject("$sort", 
                new BasicDBObject("isDefault" ,-1)
                    .append("surveyEntrySequence",1)
            )
        )).results();
        
        List<BasicDBObject> list = IteratorUtils.toList(result.iterator());
        
        //get survey result of the participant
        DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");
        BasicDBObject surveyResult = (BasicDBObject) surveyResultCollection.findOne(new BasicDBObject("participantId", user.getId()).append("customerId", user.getCustomerId()));
        if(surveyResult != null){
            for(int i=0; i< list.size(); i++){
                String surveyId = list.get(i).getString("_id");
                BasicDBObject status = (BasicDBObject) surveyResult.get("surveyStatus");
                if(status!=null){
                    list.get(i).put("status", status.get(surveyId));
                }
                
                BasicDBObject updated = (BasicDBObject) surveyResult.get("surveyUpdated");
                if(updated!=null){
                    list.get(i).put("lastUpdated", updated.get(surveyId));
                }
            }
        }
        
        Gson json = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
        
        return json.toJson(list);
    }
    
    private ParticipantSurveyStatus findParticipantSurveyStatusRecord(List<ParticipantSurveyStatus> list, String custId, String uid, String surveyId) {
        for ( ParticipantSurveyStatus r : list){
            if ( r.getSurveyId().equalsIgnoreCase(surveyId)){
                return r;
            }
        }
        return null;
    }

    
    @GET
    @Path("/survey/results")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<SurveyResult> getSurveyResults(
            @QueryParam("token") String token
            , @QueryParam("limit") int limit, @QueryParam("offset") int offset
    )  throws Exception{
        
        SMRTUser user = this.validateToken(token, null);
        //TODO check permissions
        this.setCustomerId(user.getCustomerId());
        //String customerId = "TODO";
        logger.debug("!1");
        //Query q = this.getEntityManager().createNativeQuery("select s.survey_id, sr.status, sr.customer_id, sr.user_id, sr.survey_result_id, sr.survey_id from survey_result sr left outer join survey s on s.survey_id = sr.survey_id where sr.customer_id = ?1 and sr.user_id = ?2", SurveyResult.class);
        //TODO if NOT useMaster then have alt sql
        Customer c = this.findCustomer(user);
        String sql = "";
        long masterData = Util.toLong(c.getSetting("survey_master_id", "-1"), -1);
        boolean usemaster = false;
        sql = "select sr from SurveyResult sr where customer_id = ?1 and participant_id = ?2";
            
        if ( masterData > 0 ) { // get surveys from master 
            /*sql = "select s.survey_id" +
                " , COALESCE(sr.status,1) as status, COALESCE(sr.customer_id, " + this.getCustomerId() + ") as customer_id, COALESCE(sr.user_id, 0) as user_id, COALESCE(sr.survey_result_id,0) as survey_result_id" +
                " from survey s" +
                " left outer join survey_result sr" +
                " on s.survey_id = sr.survey_id and sr.customer_id = ?1 and sr.user_id = ?2" +
                " where s.customer_id = 1";
            */
            usemaster = true;
        } /*else {
            sql = "select s.survey_id" +
                " , COALESCE(sr.status,1) as status, COALESCE(sr.customer_id, " + this.getCustomerId() + ") as customer_id, COALESCE(sr.user_id, 0) as user_id, COALESCE(sr.survey_result_id,0) as survey_result_id" +
                " from survey s" +
                " left outer join survey_result sr" +
                " on s.survey_id = sr.survey_id and sr.customer_id = ?1 and sr.user_id = ?2" +
                " where s.customer_id = ?3";
            usemaster = false;
        }*/
        Query q = this.getEntityManager().createNativeQuery(sql, SurveyResult.class);
        q.setHint("javax.persistence.cache.storeMode", "BYPASS");
        q.setHint("javax.persistence.cache.retrieveMode", "BYPASS");
        q.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
        q.setHint(QueryHints.MAINTAIN_CACHE, HintValues.FALSE);
        //q.setParameter("1", this.decodeId(customerId));
        //q.setParameter("2", this.decodeId(userId));
        q.setParameter("1", user.getCustomerId());
        q.setParameter("2", user.getId());
        q.setMaxResults(limit <= 0 ? 25 : limit);
        q.setFirstResult(offset);
        logger.debug("!1.1");
        List<SurveyResult> sr = q.getResultList();
        if ( sr == null) {
            logger.debug("!1.2");
            sr = new ArrayList<SurveyResult>();
        } else {
            logger.debug("!1.3");
            logger.debug(sr);
        }
        logger.debug("!2");
        
        // TODO process pre-conditions
        /*hard coded for now
        for ( SurveyResult sri : sr) {
            if ( sri.getSurveyId().)
        }*/
        
        return sr;
    }
    
    /*@GET
    @Path("{userId}/home")
    public String getHome (@PathParam("customerId") String customerId, @PathParam("userId") String userId)  throws Exception{
        String home ="";
        this.setCustomerId(customerId);
        SMRTUser user = this.findUser(this.decodeId(userId));
        if ( Util.hasRole(user, SMRTRole.))
    }*/
    
    @GET
    @Path("12HrList")
    @Produces({ MediaType.APPLICATION_JSON})
    public String f12HrList(@QueryParam("token") String token,
        @QueryParam("sort") String sort,
        @QueryParam("order") int order,
        @QueryParam("start") int start,
        @QueryParam("limit") int limit,
        @QueryParam("search") String search
            ) throws Exception {
        SMRTUser user = this.validateToken(token, null);
        DB mongoDB = this.getDBInstance();
        
        if (sort.isEmpty()) sort="firstname";
        BasicDBObject match = new BasicDBObject();
        if(!search.isEmpty()){
            BasicDBList nameList = new BasicDBList();
            nameList.add(new BasicDBObject("firstname",new BasicDBObject("$regex", search).append("$options","i")));
            nameList.add(new BasicDBObject("lastname",new BasicDBObject("$regex", search).append("$options","i")));
            nameList.add(new BasicDBObject("email",new BasicDBObject("$regex",search).append("$options","i")));
            match.put("$or", nameList);
        }
        
        DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");
        
        BasicDBObject projectFields = new BasicDBObject("_id" ,0) //skip _id field
                        .append("id", "$participantId");
        
        BasicDBList fields = new BasicDBList();
        fields.add("$user.firstname");
        
        projectFields.append("firstname", fields);
        
        fields = new BasicDBList();
        fields.add("$user.email");

        projectFields.append("email", fields);

        fields = new BasicDBList();
        fields.add("$user.lastname");
      
        projectFields.append("lastname",fields);
        List<DBObject> userList = new ArrayList<DBObject>(Arrays.asList(
//        Iterable<DBObject> userList = surveyResultCollection.aggregate(Arrays.asList(
                (DBObject) new BasicDBObject("$match", 
                        new BasicDBObject("customerId" ,user.getCustomerId())
                        .append("e2_en_183.val",new BasicDBObject("$ne","1"))
                ),
                (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"SMRTUser")
                        .append("localField", "participantId")
                        .append("foreignField", "_id")
                        .append("as", "user")
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$user")
                        .append("preserveNullAndEmptyArrays", false)
                ),
                (DBObject) new BasicDBObject("$project", 
                        projectFields
                ),
                (DBObject) new BasicDBObject("$match",match)
                
        ));
        Iterable<DBObject> result = surveyResultCollection.aggregate(userList).results();
            long size = result.spliterator().getExactSizeIfKnown();
            userList.add((DBObject) new BasicDBObject("$sort",new BasicDBObject(sort,order)));
            userList.add((DBObject) new BasicDBObject("$skip",start));
            userList.add((DBObject) new BasicDBObject("$limit",limit));
        Iterable<DBObject> results = surveyResultCollection.aggregate(userList).results();
            
            for(DBObject res: results){
                ((BasicDBObject)res).append("total", size);
            }
        return new Gson().toJson(results);
        
    }
    
    @GET
    @Path("userlist")
    @Produces({ MediaType.APPLICATION_JSON})
    public String findRange(@QueryParam("token") String token,
            @QueryParam("limit") int limit,
            @QueryParam("start") int start,
            @QueryParam("sort") String sort,
            @QueryParam("order") int order,
            @QueryParam("search") String search
    ) throws Exception {
        SMRTUser user = this.validateToken(token, null);
        //this.checkPermissions(new String[]{ SMRTRole.USER_ADMIN});// 
        //this.setCustomerId(user.getCustomerId());
        DB mongoDB = this.getDBInstance();
        DBCollection userCollection = mongoDB.getCollection("SMRTUser");
        if (sort.isEmpty()) sort="lastname";
        BasicDBObject match=new BasicDBObject();
        if(!search.isEmpty()){
        BasicDBList name = new BasicDBList();
            name.add(new BasicDBObject("firstname",new BasicDBObject("$regex",search).append("$options","i")));
            name.add(new BasicDBObject("lastname",new BasicDBObject("$regex", search).append("$options","i")));
            name.add(new BasicDBObject("roles",new BasicDBObject("$regex", search).append("$options","i")));
            match.put("$or",name);
        }
        
        BasicDBObject dbQuery = new BasicDBObject();
        dbQuery.put("customerId", user.getCustomerId());
        dbQuery.put("roles",new BasicDBObject("$ne","student"));
        List<DBObject> aggQuery = new ArrayList<DBObject>(Arrays.asList(
        (DBObject) new BasicDBObject("$match",dbQuery)
        ,
        (DBObject) new BasicDBObject("$project",
                    new BasicDBObject().append("customerId", 1)
            .append("email", 1)
            .append("organization", 1)
            .append("firstname", 1)
            .append("lastname", 1)
            .append("permissions", 1)
            .append("roles", 1)
            .append("usertype", 1)
        .append("last_login_date",1))));
        Iterable<DBObject> count = userCollection.aggregate(aggQuery).results();
               long size = count.spliterator().getExactSizeIfKnown();
        aggQuery.add((DBObject) new BasicDBObject("$match",match));
        aggQuery.add((DBObject) new BasicDBObject("$skip",start));
        aggQuery.add((DBObject) new BasicDBObject("$limit",limit));
        aggQuery.add((DBObject) new BasicDBObject("$sort",new BasicDBObject(sort,order)));
        
        Iterable<DBObject> userresult = userCollection.aggregate(aggQuery).results();
        for(DBObject res:userresult){
                    ((BasicDBObject)res).append("total",size);
                }
        return new Gson().toJson(userresult);
    }
    ///Similar to findRange, just the URL is different
    @GET
    @Path("search")
    @Produces({MediaType.APPLICATION_JSON})
    public String searchUsers(@QueryParam("token") String token, @QueryParam("from") Integer from, 
            @QueryParam("to") Integer to, @QueryParam("query") String query, @QueryParam("role") String role
    ) throws Exception {
        SMRTUser user = this.validateToken(token, null);
        this.setUser(user);
        this.checkPermissions(new String[]{ SMRTRole.USER_ADMIN, SMRTRole.CASE_DASH, SMRTRole.OTHERS_DATA_SEARCH});// 
        this.setCustomerId(user.getCustomerId());
        System.out.println("@1");
        List<DBObject> res = this.searchForUsers(query, role, user.getCustomerId(), from, to);
        System.out.println("@2");
        if ( res == null) res = new ArrayList<DBObject>();
        return new Gson().toJson(res);
    }
    @GET
    @Path("searchall")
    @Produces({MediaType.APPLICATION_JSON})
    public String searchUsers(@QueryParam("token") String token, @QueryParam("query") String query, @QueryParam("usertype") String usertype, @QueryParam("partnerId") String partnerId,
            @QueryParam("listAll") String listAll
    ) throws Exception {
        SMRTUser user = this.validateToken(token, null);
       
       DB mongoDB = this.getDBInstance();
       DBCollection userViewCollection = mongoDB.getCollection("userView");
       
       BasicDBObject dbQuery = new BasicDBObject();
       dbQuery.put("customerId", user.getCustomerId());
       
       List<DBObject> aggQuery = new ArrayList<>();
       
       if(!usertype.equals("")){
           dbQuery.put("usertype", usertype);
           
           if(usertype.equals("partner") && !listAll.equals("true")){ //search only selected partner users
               dbQuery.put("partnerId", partnerId);
           }
           if(usertype.equals("partner") && listAll.equals("true")){
              List<String> partner = new ArrayList<String>(Arrays.asList(partnerId.split(",")));

                dbQuery.put("partnerId", new BasicDBObject("$in",partner));  
           }
           if(usertype.equals("admin")){
               dbQuery.append("usertype", "casemanager");
           }
       }
       
        
        
        if(query!= null && !query.isEmpty()){
            BasicDBList nameList = new BasicDBList();
            nameList.add(new BasicDBObject("firstname",new BasicDBObject("$regex", query).append("$options","i")));
            nameList.add(new BasicDBObject("lastname",new BasicDBObject("$regex", query).append("$options","i")));
            nameList.add(new BasicDBObject("email",new BasicDBObject("$regex", query).append("$options","i")));
            dbQuery.put("$or", nameList);
        }
        
        aggQuery.add((DBObject) new BasicDBObject("$match", dbQuery));
      
       if(!listAll.equals("true")){
            aggQuery.add((DBObject) new BasicDBObject("$limit", 5));
       }
       
       Cursor result = userViewCollection.aggregate(aggQuery, AggregationOptions.builder().outputMode(AggregationOptions.OutputMode.CURSOR).build());
        List<DBObject> res = new ArrayList<>();
        result.forEachRemaining(res::add);
        return new Gson().toJson(res);
    }
    
    @GET
    @Path("suggestassignedto")
    @Produces({MediaType.APPLICATION_JSON})
    public String suggestAssignedTo(@QueryParam("token") String token, @QueryParam("query") String query, @QueryParam("partnerId") String partnerId
    ) throws Exception {
        SMRTUser user = this.validateToken(token, null);
       
       DB mongoDB = this.getDBInstance();
       DBCollection userCollection = mongoDB.getCollection("SMRTUser");
       
       BasicDBObject dbQuery = new BasicDBObject();
       dbQuery.put("customerId", user.getCustomerId());
       
       List<DBObject> aggQuery = new ArrayList<>();
       
       if(partnerId!= null && !partnerId.isEmpty()){
           dbQuery.put("partnerId", partnerId);
       }else{
           dbQuery.put("usertype", new BasicDBObject("$nin", (BasicDBList)com.mongodb.util.JSON.parse("[\"student\",\"partner\"]")));
       }
       
        aggQuery.add((DBObject) new BasicDBObject("$match", dbQuery));
           
           aggQuery.add((DBObject) new BasicDBObject("$match",
                    new BasicDBObject("$or",
                            new BasicDBList(){
                                {
                                    add(new BasicDBObject("firstname",new BasicDBObject("$regex", query).append("$options","i")));
                                    add(new BasicDBObject("lastname",new BasicDBObject("$regex", query).append("$options","i")));
                                    add(new BasicDBObject("email",new BasicDBObject("$regex", query).append("$options","i")));
                                }
                            }
                    )
                ));
           
            aggQuery.add((DBObject) new BasicDBObject("$project", 
                    new BasicDBObject("firstname","$firstname")
                    .append("lastname","$lastname")
                    .append("email","$email")
            ));
       
       aggQuery.add((DBObject) new BasicDBObject("$project", 
                    new BasicDBObject("firstname","$firstname")
                    .append("lastname","$lastname")
                    .append("email","$email")
                    .append("name",new BasicDBObject("$concat", new BasicDBList(){
                        {
                            add("$firstname");
                            add(" ");
                            add("$lastname");
                        }
                    }))
            ));
       
       aggQuery.add((DBObject) new BasicDBObject("$sort", 
                new BasicDBObject("name", 1)
                .append("email", 1)
        ));
       
       aggQuery.add((DBObject) new BasicDBObject("$limit", 5));
       
       Iterable<DBObject> output = userCollection.aggregate(aggQuery).results();
            
        List<DBObject> res = new ArrayList<>();
        output.forEach(res::add);
        return new Gson().toJson(res);
    }

    @GET
    @Path("groupowner")
    @Produces({MediaType.APPLICATION_JSON})
    public String groupOwner(@QueryParam("token") String token) throws Exception {
        SMRTUser user = this.validateToken(token, null);
                
       DB mongoDB = this.getDBInstance();
       DBCollection userCollection = mongoDB.getCollection("SMRTUser");
       
       BasicDBObject dbQuery = new BasicDBObject();
       dbQuery.put("customerId", user.getCustomerId());       
       
        List<DBObject> aggQuery = new ArrayList<>();
       
       
        dbQuery.put("usertype", new BasicDBObject("$in", (BasicDBList)com.mongodb.util.JSON.parse("[\"admin\",\"casemanager\"]")));
        BasicDBObject result = new BasicDBObject("users",userCollection.find(dbQuery).sort(new BasicDBObject("lastname",1)).toArray());
        result.append("currentUser", user.getId());
        
        return new Gson().toJson(result);
    }
    @GET
    @Path("members")
    @Produces({MediaType.APPLICATION_JSON})
    public String members (@QueryParam("token") String token) throws Exception {
        SMRTUser user = this.validateToken(token, null);
       
       DB mongoDB = this.getDBInstance();
       DBCollection userCollection = mongoDB.getCollection("SMRTUser");
     
       BasicDBObject dbQuery = new BasicDBObject();
       dbQuery.put("customerId", user.getCustomerId());       
       
        List<DBObject> aggQuery = new ArrayList<>();
       
       
        dbQuery.put("usertype", new BasicDBObject("$in", (BasicDBList)com.mongodb.util.JSON.parse("[\"casemanager\",\"director\",\"instructor\",\"observer\",\"partner\",\"systemadmin\"]")));
        BasicDBObject result = new BasicDBObject("users",userCollection.find(dbQuery).sort(new BasicDBObject("lastname",1)).toArray());
        return new Gson().toJson(result);
    }
    //KriyaTec added method
    @GET
    @Path("data")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTUser getUserData(@QueryParam("token") String token, @QueryParam("id") String id
    ) throws Exception {
        SMRTUser user = this.validateToken(token, null);
        if(id.equals("")){id=user.getId();}
        this.checkPermissions(new String[]{ SMRTRole.USER_ADMIN, SMRTRole.CASE_DASH, SMRTRole.OTHERS_DATA_SEARCH});// 
        this.setCustomerId(user.getCustomerId());
        SMRTUser userData = this.findUser(id);
        return userData;
    }
    @GET
    @Path("/permheader/{pid}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String getPermheader(@PathParam("pid") String pid,@QueryParam("token") String token)
    {
        
        try
        {
            SMRTUser u=this.validateToken(token);
            DB mongodb=this.getDBInstance();
            DBCollection collection=mongodb.getCollection("SMRTUser");
            List<DBObject> aggQuery=Arrays.asList(
            (DBObject) new BasicDBObject("$match",new BasicDBObject("customerId",u.getCustomerId())
            .append("_id", pid)),
            (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"group")
                        .append("localField","_id")
                        .append("foreignField","contents")
                        .append("as","groups")
            ),
              (DBObject)new BasicDBObject("$unwind","$groups")
             ,(DBObject)new BasicDBObject("$unwind","$groups.contents")
             ,(DBObject)new BasicDBObject("$group",new BasicDBObject("_id",new BasicDBObject()
                     .append("groups","$groups.name")
                     .append("firstname","$firstname")
                     .append("lastname","$lastname")
                     .append("email","$email")
                     .append("phone","$phonenumber")
                     .append("lastactivity","$lastactivity")
                     .append("lastlogin","$last_login_date")
             ))   
            );
            Iterable<DBObject> resources = collection.aggregate(aggQuery).results();
        return new Gson().toJson(resources);
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        finally
        {
            //todo
        }
        return null;
    }
    @GET
    @Path("student/data")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String getClientUserData(@QueryParam("token") String token, @QueryParam("id") String id
    ) throws Exception {
        SMRTUser user = this.validateToken(token, null);
        if(id.equals("")){id=user.getId();}
        SMRTUser userData = this.findUser(id);
        DB mongoDB = this.getDBInstance();
        DBCollection userCollection = mongoDB.getCollection("survey_results");
        BasicDBObject fields = new BasicDBObject()
            .append("s1_req_1",1)
            .append("s1_req_2",1)
            .append("s1_req_9",1)
            .append("s1_req_7",1)
            .append("s1_req_4",1)
            .append("s1_req_5",1)
            .append("s1_req_6",1)
            .append("s1_req_3",1);
        BasicDBObject condition = new BasicDBObject("participantId",user.getId());
        DBCursor cursor = userCollection.find(condition,fields);
        Gson json = new Gson();
            return json.toJson(cursor.toArray());
    }
    
     @PUT
     @Path("student/data")
    //customerId is dummy here. get customerId from reg code
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTUser updateClientUserData(
    @QueryParam("token") String token,
    SMRTUser entity
    ) throws Exception {
        try{
        SMRTUser user=this.validateToken(token);
        SMRTUser u = this.find(user.getId());
        u.setFirstname(entity.getFirstname());
        u.setLastname(entity.getLastname());
        u.setEmail(entity.getEmail());
        u.setLoginName(entity.getEmail());
        u.setPhonenumber(entity.getPhonenumber());
        u.setAddress(entity.getAddress());
        u.setCity(entity.getCity());
        u.setState(entity.getState());
        u.setZip(entity.getZip());
        if(u.getUsertype().equals("student")){
            DB mongoDB = this.getDBInstance();
            DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");
            BasicDBObject query = new BasicDBObject();
            query.put("participantId", u.getId());
            BasicDBObject data = new BasicDBObject();
            data.put("s1_req_1", new BasicDBObject("val", u.getFirstname()).append("txt", u.getFirstname()));
            data.put("s1_req_2", new BasicDBObject("val", u.getLastname()).append("txt", u.getLastname()));
            data.put("s1_req_9", new BasicDBObject("val", u.getEmail()).append("txt", u.getEmail()));
            data.put("s1_req_7", new BasicDBObject("val", u.getPhonenumber()).append("txt", u.getPhonenumber()));
            data.put("s1_req_4", new BasicDBObject("val", u.getCity()).append("txt", u.getCity()));
            data.put("s1_req_5", new BasicDBObject("val", u.getState()).append("txt", u.getState()));
            data.put("s1_req_6", new BasicDBObject("val", u.getZip()).append("txt", u.getZip()));
            data.put("s1_req_3", new BasicDBObject("val", u.getAddress()).append("txt", u.getAddress()));
            data = new BasicDBObject(
                "$set",data.append("resultTime", new Date()))
                    .append("$setOnInsert",new BasicDBObject("participantId", u.getId())
                    .append("customerId",u.getCustomerId()));
            surveyResultCollection.update(query, data);
           }
        return  this.edit(u);
        }
    catch (Exception e){
    System.out.println(e.getMessage());
    throw new Exception("Error ");
    }
    finally{
          
    }
    }
    
    
    @GET
    @Path("data/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTUser getUser(@PathParam("id") String id
    ) throws Exception {
       SMRTUser userData = this.findUser(id);
       userData.setPermission_type(userData.getPermission_type());
       String type=userData.getPermission_type();
       String permissiontemplate=userData.getPermission_template_id();
        if(type!=null && type.equals("P") && null!=permissiontemplate){
        PermissionTemplates templates=new PermissionTemplates();
        PermissionTemplates data=this.findTemplate(permissiontemplate);
       userData.setPermissions(data.getPermissions());
       return userData;
       }
       else
       {
        return this.findUser(id);
       }
    }
    
    
    public void log(String message){
         logger.debug(message);
    }
    
    public void error(String message, Exception e){
         logger.error(message, e);
    }
    
    
    private  List<DBObject> searchForUsers(String query, String role, String customerId, int from, int to) throws Exception{
        
        DB mongoDB = this.getDBInstance();
        DBCollection userCollection = mongoDB.getCollection("SMRTUser");
        
        
        BasicDBObject dbQuery = new BasicDBObject();
        dbQuery.put("customerId", customerId);
        dbQuery.put("usertype",role);
        
        
        if(role!=null && role.equalsIgnoreCase("student")){
            
           // DBCollection userViewCollection = mongoDB.getCollection("userView");
            
            
            //search demographics data for users
            BasicDBList conds = new BasicDBList();
            conds.add(new BasicDBObject("firstname",new BasicDBObject("$regex", query).append("$options","i")));
            conds.add(new BasicDBObject("lastname",new BasicDBObject("$regex", query).append("$options","i")));
            conds.add(new BasicDBObject("email",new BasicDBObject("$regex", query).append("$options","i")));
            dbQuery.put("$or", conds);
            
            DBCollection groupCollection = mongoDB.getCollection("group");
            
            if(
                    !this.getUser().getUsertype().equals("admin") 
                    && 
                    !this.CheckUserPermission(this.getUser(), "M08", "canview")
                    &&
                    groupCollection.count(new BasicDBObject("customerId", customerId).append("groupType","All Clients").append("groupOwner",this.getUser().getId()))==0
            ){
                Iterable<DBObject> output = groupCollection.aggregate(Arrays.asList(
                        (DBObject) new BasicDBObject("$match", new BasicDBObject("customerId", this.getUser().getCustomerId()).append("groupOwner", this.getUser().getId()))
                       , (DBObject) new BasicDBObject("$project", new BasicDBObject("contents", 1))
                       , (DBObject) new BasicDBObject("$unwind", "$contents")
                       , (DBObject) new BasicDBObject("$group", new BasicDBObject("_id", null).append("contents", new BasicDBObject("$addToSet","$contents")))
                )).results();
                Gson json = new Gson();
                if(output.iterator().hasNext())
                    dbQuery.put("_id", new BasicDBObject("$in", ((BasicDBObject)output.iterator().next()).get("contents")));
                else
                    dbQuery.put("_id", new BasicDBObject("$in", new BasicDBList()));
            }
            
            Cursor result = userCollection.aggregate(Arrays.asList(
                (DBObject) new BasicDBObject("$match", dbQuery)
            ), AggregationOptions.builder().outputMode(AggregationOptions.OutputMode.CURSOR).build());
            
            List<DBObject> res = new ArrayList<>();
            result.forEachRemaining(res::add);
            return res;
        }
        
        if(role!=null && !role.equals("")){
            dbQuery.put("usertype", role);
        }
        
        BasicDBList conds = new BasicDBList();
        conds.add(new BasicDBObject("firstname",new BasicDBObject("$regex", query).append("$options","i")));
        conds.add(new BasicDBObject("lastname",new BasicDBObject("$regex", query).append("$options","i")));
        conds.add(new BasicDBObject("email",new BasicDBObject("$regex", query).append("$options","i")));
        dbQuery.put("$or", conds);
        
        return userCollection.find(dbQuery).sort(new BasicDBObject("firstname",1).append("lastname",1)).skip(from).limit(to).toArray();

    }
    
    private SMRTUser findUserByCredentials(String loginName, String password,String key) throws Exception{
        Customer customer = this.findCustomerByURLKey(key);
        
        Query q  = this.getEntityManager()
                .createQuery("select user from SMRTUser user WHERE user.loginName = :lname "
                        + "AND (user.password = :password OR user.resetpassword = :password)",SMRTUser.class);
        q.setParameter("lname", loginName);
        logger.log(Level.FATAL, loginName);
        logger.log(Level.FATAL, key);
        logger.log(Level.FATAL, password);
        q.setParameter("password", password);
        List<SMRTUser> users = null;
        SMRTUser user = null;
        try {
            users = (List<SMRTUser>)q.getResultList();
            for(SMRTUser obj : users) {
                if (obj.getCustomerId().equals(customer.getId())) {
                    obj.setCustomer(customer);
                    user = obj;
                    break;
                }
            }
        } catch (javax.persistence.NoResultException nodata) {
            user = null;
        }
        if ( user == null || Util.isEmpty(user.getId()) || user.getId().equals("0")) {
            throw new ForbiddenException("Invalid Email/Password " );
        }
            user.setLast_login_date(new Date());
            this.edit(user);
        return user;
    }
    
      private boolean checkLoginAvailability(String loginName,String customerId ,String id) throws Exception{
        Query q  = this.getEntityManager().createQuery("select user from SMRTUser user WHERE user.loginName = :lname and user.customerId = :customerId and user.id <> :id");

        q.setParameter("lname", loginName);
        q.setParameter("customerId", customerId);
        q.setParameter("id",id);

        SMRTUser c = null;
        try {
            c = (SMRTUser) q.getSingleResult();
        } catch (javax.persistence.NoResultException nodata) {
            c = null;
        }
        if ( c == null || Util.isEmpty(c.getId()) || c.getId().equals("0")) {
           // throw new ForbiddenException("Invalid Email/Password " );
           return true; // login is available
        } else {
            return false;
        }
        
    }
    
    
    private SMRTUser updateCredentials(SMRTUser u, String password) throws Exception{
        //Query q  = this.getEntityManager().createQuery("select user from SMRTUser user WHERE user.customer.id = :custId and user.email = :email and user.password = :password");
        try {
            this.getEntityManager().getTransaction().begin();
            u.setPassword(password);
            this.getEntityManager().merge(u);
            this.getEntityManager().getTransaction().commit();
            
        } catch (Exception e) {
            this.getEntityManager().getTransaction().rollback();
            throw e;
        }
        return u;
    }
    
    private Customer findCustomerByRegistrationCode(String code) throws Exception{
        
        Customer c = null;
        List<Customer> cl = null;
        if ( code == null || code.trim().isEmpty()){
            throw new InvalidParameterException("Registration code is required");
        }
        Query q  = this.getEntityManager().createQuery("select c FROM Customer c WHERE c.registrationCode = :code", Customer.class);
        q.setParameter("code", code);
        q.setMaxResults(1);
        cl = (List<Customer>) q.getResultList();
        if ( cl == null || cl.size() <= 0 ) {
            throw new InvalidParameterException("Invalid registration code. " + code);
        } 
        c = cl.get(0);
        return c;
    }
    private Customer findCustomerByUrlKey(String urlkey) throws Exception{
        
        Customer c = null;
        List<Customer> cl = null;
//        if ( urlkey == null || urlkey.trim().isEmpty()){
//            throw new InvalidParameterException("Registration code is required");
//        }
        Query q  = this.getEntityManager().createQuery("select c FROM Customer c WHERE c.urlKey = :urlkey", Customer.class);
        q.setParameter("urlkey", urlkey);
        q.setMaxResults(1);
        cl = (List<Customer>) q.getResultList();
        if ( cl == null || cl.size() <= 0 ) {
            throw new InvalidParameterException("Invalid urlKey. " + urlkey);
        } 
        c = cl.get(0);
        return c;
    }
    
    private SMRTUser findUserByLoginName(String loginName) throws Exception{
        
        SMRTUser c = null;
        List<SMRTUser> cl = null;
        if ( Util.isEmpty(loginName)){
            throw new InvalidParameterException("Login Name is required");
        }
        Query q  = this.getEntityManager().createQuery("select u FROM SMRTUser WHERE u.loginName = :lname", SMRTUser.class);
        q.setParameter("lname", loginName);
        q.setMaxResults(1);
        cl = (List<SMRTUser>) q.getResultList();
        if ( cl != null && cl.size() > 0 ) {
            c = cl.get(0);
            //throw new InvalidParameterException("Invalid registration code. " + code);
        } 

        return c;
    }
    
    @POST  
    @Path("{userId}/role/{roleName}")
    //@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTUser addRole(@PathParam("userId") String userId, @PathParam("roleName") String roleName, @FormParam("token") String token) throws Exception {
        System.out.println("AddRole");
        SMRTUser tu = this.validateToken(token, null);
        this.setCustomerId(tu.getCustomerId());
        this.checkPermissions(new String[] { SMRTRole.USER_ADMIN});
        SMRTUser u = null;
        Customer c = this.findCustomer(tu);
        try {
            //this.getEntityManager().getTransaction().begin();
             u = this.findUser(userId);
             if ( u == null) {
                throw new InvalidParameterException("Invalid user");

            }
            //SMRTRole r = this.findRole(this.decodeId(roleId));
            SMRTRole r = c.findRole(roleName);
            if ( r == null) {
                throw new InvalidParameterException("Invalid role");

            }
            List<String> roles = u.getRoles();
            if ( roles == null) {
                //TODO, should we try loading the roles again?
                roles = new ArrayList<String>();
            }
            //check if role exists
            boolean found = false;
            for ( String sr : roles){
                if ( sr.equalsIgnoreCase(roleName)){
                    //role already exists. set status to enabled
                    //sr.setStatus(1);
                    //this.getEntityManager().merge(sr);
                    found = true;
                }
            }
            if ( !found){
                //add role to user
                SMRTRole nr = new SMRTRole();
                nr.setStatus(1);
                nr.setIdentifier(r.getIdentifier());
                nr.setName(r.getName());
                ///nr.setCustomer(this.findCustomer());
                //roles.add(nr);
                //u.setRoles(roles);
                this.getEntityManager().getTransaction().begin();
                try {
                    u.getRoles().add(roleName);
                    this.getEntityManager().merge(u);
                    this.getEntityManager().getTransaction().commit();
                } catch(Exception e){
                    this.getEntityManager().getTransaction().rollback();
                    throw e;
                }
                
            }
            
        } catch(Exception e){
            //this.getEntityManager().getTransaction().rollback();
            throw e;
        }
        return u;
        
    }
    
    
    @DELETE  
    @Path("{userId}/role/{roleName}")
    //@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTUser removeRole(@PathParam("userId") String userId, @PathParam("roleName") String roleName, @QueryParam("token") String token) throws Exception {
        System.out.println("remRole");
        SMRTUser tu = this.validateToken(token, null);
        this.checkPermissions(new String[] { SMRTRole.USER_ADMIN});
        this.setCustomerId(tu.getCustomerId());
        Customer c = this.findCustomer(tu);
        SMRTUser u = null; //user that is being managed
        try {
            this.getEntityManager().getTransaction().begin();
             u = this.findUser(userId);
            //SMRTRole r = this.findRole(this.decodeId(roleId));
            if ( u == null) {
                throw new InvalidParameterException("Invalid user");

            }
            
            SMRTRole r = c.findRole(roleName);
            if ( r == null) {
                throw new InvalidParameterException("Invalid role");

            }
            List<String> roles = u.getRoles();
            if ( roles != null) {
                
                //check if role exists
                boolean found = false;
                for ( String sr : roles){
                    if ( sr.equalsIgnoreCase(roleName)){
                        //role already exists. set status to enabled
                        roles.remove(sr);
                        this.getEntityManager().merge(u);
                        break;
                    }
                }
            }
            
            this.getEntityManager().getTransaction().commit();
        } catch(Exception e){
            this.getEntityManager().getTransaction().rollback();
            throw e;
        }
        return u;
        
    }

    @POST  
    @Path("{roleName}/permission/{permission}")
    //@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void addPermission(@PathParam("permission") String permission, @PathParam("roleName") String roleName, @FormParam("token") String token) throws Exception {
        System.out.println("AddRole");
        SMRTUser tu = this.validateToken(token, null);
        this.setCustomerId(tu.getCustomerId());
        this.checkPermissions(new String[] { SMRTRole.USER_ADMIN});
        Customer c = this.findCustomer(tu);
        try {
            
            SMRTRole r = c.findRole(roleName);
            if ( r == null) {
                throw new InvalidParameterException("Invalid role");

            }
            String permissions = r.getPermissions();
            if ( permissions == null) {
                permissions = "";
            }
            //check if permission exists
            boolean found = false;
            String[] p = permissions.split(",");
            //String pu = Util.getValue(permission).toUpperCase();
            for (String p1 : p) {
              String pi = Util.getValue(p1);
              System.out.println("pi=" + pi);
              if (  pi.equalsIgnoreCase(permission)){
                  System.out.println("pi matched=" + pi);
                  found = true;
              }
            }
            if ( !found){
                //add permission to role
                String np = permission.toUpperCase();
                String perm = permissions+","+np;
                r.setPermissions(perm);
                ///nr.setCustomer(this.findCustomer());
                //roles.add(nr);
                //u.setRoles(roles);
                this.getEntityManager().getTransaction().begin();
                try {
                    c.findRole(roleName).setPermissions(perm);
                    this.getEntityManager().merge(c);
                    this.getEntityManager().getTransaction().commit();
                } catch(Exception e){
                    this.getEntityManager().getTransaction().rollback();
                    throw e;
                }
                
            }
            
        } catch(Exception e){
            //this.getEntityManager().getTransaction().rollback();
            throw e;
        }
//        return u;
        
    }
    
    
    @DELETE  
    @Path("{userId}/permission/{roleName}")
    //@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTUser removePermission(@PathParam("userId") String userId, @PathParam("roleName") String roleName, @QueryParam("token") String token) throws Exception {
        System.out.println("remRole");
        SMRTUser tu = this.validateToken(token, null);
        this.checkPermissions(new String[] { SMRTRole.USER_ADMIN});
        this.setCustomerId(tu.getCustomerId());
        Customer c = this.findCustomer(tu);
        SMRTUser u = null; //user that is being managed
        try {
            this.getEntityManager().getTransaction().begin();
             u = this.findUser(userId);
            //SMRTRole r = this.findRole(this.decodeId(roleId));
            if ( u == null) {
                throw new InvalidParameterException("Invalid user");

            }
            
            SMRTRole r = c.findRole(roleName);
            if ( r == null) {
                throw new InvalidParameterException("Invalid role");

            }
            List<String> roles = u.getRoles();
            if ( roles != null) {
                
                //check if role exists
                boolean found = false;
                for ( String sr : roles){
                    if ( sr.equalsIgnoreCase(roleName)){
                        //role already exists. set status to enabled
                        roles.remove(sr);
                        this.getEntityManager().merge(u);
                        break;
                    }
                }
            }
            
            this.getEntityManager().getTransaction().commit();
        } catch(Exception e){
            this.getEntityManager().getTransaction().rollback();
            throw e;
        }
        return u;
        
    }
    @GET
    @Path("searchRole")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<SMRTUser> searchUsersBasedOnRole() throws Exception {
        String sql = "select su from SMRTUser su where su.loginName = 'smrtdev4'";
//        Query q = this.getEntityManager().createNativeQuery("db.SMRTUser.find({\"roles\":\"professor\"})", SMRTUser.class);
        Query q = this.getEntityManager().createQuery(sql, SMRTUser.class);
        
        List<SMRTUser> c = null;
        c = q.getResultList();
        return c;
    }
    @Override
    public String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }
    
    
   
    @GET
    @Path("getuserphoto/{userId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public byte[] getUserPhoto(@PathParam("userId") String userId) throws Exception {

        SMRTUser us = this.findUser(userId);
        
        DB mongoDB = this.getDBInstance();
        DBCollection surveyResultsCollection = mongoDB.getCollection("survey_results");
        BasicDBObject query = new BasicDBObject("customerId", us.getCustomerId()).append("participantId", us.getId());
        BasicDBObject fields = new BasicDBObject("e2_en_103_file",1);
        
        String result = "";
        
        DBCursor cursor = surveyResultsCollection.find(query, fields);
        
        if(cursor.hasNext()){
            BasicDBObject surveyResult = (BasicDBObject) cursor.next();
            surveyResult = (BasicDBObject)surveyResult.get("e2_en_103_file");
            
            if(surveyResult!=null){
                result = (String) surveyResult.get("val");
            }
        }
        
        try {
            File file = null;
//            if(result!= null && !result.equals("")){
                String filePath = Paths.get(System.getenv("SMRT_DOCS"),"Uploads","SurveyDocs", us.getCustomerId(), us.getId(), result).toString();
                file = new File(filePath);

                 //file = new File(Paths.get(System.getenv("SMRT_DOCS"), "Uploads", "User",userId, "userpicture.png").toString());
            //}
             if (!file.exists()) {
                        file = new File(Paths.get(System.getenv("SMRT_DOCS"), "Uploads", "UserPhoto", "human.jpg").toString());
                   } 
                
           
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] fileContent = new byte[(int) file.length()];
            fileInputStream.read(fileContent);
            return fileContent;
        } catch (Exception ex) {
            return null;
        }
    }
    
    private Iterable<DBObject> getSurveyConfigDetails(DBCollection coll, String customerId,String keyNameFilters){
       BasicDBList matchCond = new BasicDBList();
       BasicDBList matchProject = new BasicDBList();
       BasicDBList matchKeys = new BasicDBList();
        try {
            matchCond.add(new BasicDBObject("sections.questions.type","radio"));
            matchCond.add(new BasicDBObject("sections.questions.type","select")); 

            matchProject.add(new BasicDBObject("questions.type","radio"));
            matchProject.add(new BasicDBObject("questions.type","select"));
            for(String keyName:keyNameFilters.split(",")) {
                matchKeys.add(new BasicDBObject("questions.name",keyName));
            }
            Iterable<DBObject> output = coll.aggregate(Arrays.asList(
                 (DBObject) new BasicDBObject("$match", new BasicDBObject("customerId", customerId)
                         .append("$or", matchCond))
                ,(DBObject) new BasicDBObject("$project", new BasicDBObject("questions", "$sections.questions"))
                ,(DBObject) new BasicDBObject("$unwind", "$questions")
                ,(DBObject) new BasicDBObject("$unwind", "$questions")
                    
                ,(DBObject) new BasicDBObject("$match", new BasicDBObject("$or", matchProject).append("$or", matchKeys))
                ,(DBObject) new BasicDBObject("$project", new BasicDBObject("key", "$questions.name")
                        .append("options", "$questions.options")
                        .append("optionLabels", "$questions.optionLabels"))
            )).results();
            return output; 
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            matchCond = null;
            matchProject = null;
            matchKeys = null;
        }
        return null;
    }
    
    private HashMap<String, String> getOptionValues(Iterable<DBObject> values,String searchKeyName) {
        HashMap<String, String> result = new HashMap<String,String>();
        BasicDBList options=null;
        BasicDBList labels = null;
        
        try {
        for(DBObject obj:values) {
            if (obj.get("key").equals(searchKeyName)) {
                options = (BasicDBList)obj.get("options");
                labels = (BasicDBList)obj.get("optionLabels");
                for(int idx=0;idx<options.size();idx++) {
                    result.put(options.get(idx).toString(),labels.get(idx).toString());
                }
                return result;
            }
        }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            result = null;
        }
    return null;
    }
    
    
    @GET
    @Path("getheader/{userId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String getHeader(@PathParam("userId") String userId,@QueryParam("token") String token) throws Exception{
        
        SMRTUser user = this.validateToken(token, null);
        
        Customer customer = this.findCustomer(user);
        
        BasicDBObject surveyFields = new BasicDBObject();
        
        surveyFields.put("s1_req_1", 1);
        surveyFields.put("s1_req_2", 1);
        surveyFields.put("s1_req_7", 1);
        surveyFields.put("s1_req_9", 1);
        surveyFields.put("uniq_imp_key", 1);
        
        if(customer.getServiceLevel() == 3){
            //Enrollments
            surveyFields.put("e2_en_74", 1);
            surveyFields.put("e2_en_80", 1);
            surveyFields.put("e2_en_86", 1);
            surveyFields.put("e2_en_92", 1);
            surveyFields.put("e2_en_98", 1);
            //status
            surveyFields.put("st_st_05", 1);
            surveyFields.put("st_st_16", 1);
            surveyFields.put("st_st_17", 1);
            surveyFields.put("st_st_19", 1);
            surveyFields.put("st_st_23", 1);
            surveyFields.put("st_st_24", 1);
            surveyFields.put("st_st_26", 1);
            surveyFields.put("st_st_30", 1);
            surveyFields.put("st_st_31", 1);
            surveyFields.put("st_st_33", 1);
            surveyFields.put("isimport", 1);
            
            //Education
            surveyFields.put("st_st_36", 1);
            surveyFields.put("st_st_37", 1);
            surveyFields.put("st_st_38", 1);
            surveyFields.put("st_st_41", 1);
            surveyFields.put("st_st_42", 1);
            surveyFields.put("st_st_43", 1);
            surveyFields.put("st_st_46", 1);
            surveyFields.put("st_st_47", 1);
            surveyFields.put("st_st_48", 1);
            //Social service
            surveyFields.put("st_st_51", 1);
            surveyFields.put("st_st_52", 1);
            surveyFields.put("st_st_53", 1);
            surveyFields.put("st_st_56", 1);
            surveyFields.put("st_st_57", 1);
            surveyFields.put("st_st_58", 1);
            surveyFields.put("st_st_61", 1);
            surveyFields.put("st_st_62", 1);
            surveyFields.put("st_st_63", 1);
            surveyFields.put("st_st_06", 1);
            surveyFields.put("st_st_10", 1);
            surveyFields.put("st_st_11", 1);
            surveyFields.put("st_st_13", 1);
            surveyFields.put("oc_ins_8", 1);
        }
        
        try{
            DB mongoDB = this.getDBInstance();
 
            DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");

            BasicDBObject query = new BasicDBObject();
            query.put("participantId", userId);
            
            hmap = surveyFields.toMap();
            
            hmapVal = surveyFields.toMap();
            
            DBCursor cursor = surveyResultCollection.find(query, surveyFields);
            if (cursor.hasNext()) { 
                BasicDBObject obj = (BasicDBObject) cursor.next();
                for(Map.Entry<String, String> entry :hmap.entrySet()){
                    BasicDBObject val = (BasicDBObject) obj.get(entry.getKey());
                    if(val!=null){
                        //check if txt exists, else set val
                        entry.setValue((String)(val.get("txt")!=null ? val.get("txt"): val.get("val")));
                        hmapVal.put(entry.getKey(),(String) val.get("val"));
                    }else{
                        entry.setValue("");
                        hmapVal.put(entry.getKey(),"");
                    }
                }
            }else{
                for(Map.Entry<String, String> entry :hmap.entrySet()){
                    entry.setValue("");
                    hmapVal.put(entry.getKey(),"");
                }
            }
            
            if(customer.getServiceLevel() == 3){
                // Process the data
                // Get most recent classes
                if (!hmap.get("e2_en_98").isEmpty()) {
                    hmap.put("cls_1",hmap.get("e2_en_86"));
                    hmap.put("cls_2",hmap.get("e2_en_92"));
                    hmap.put("cls_3",hmap.get("e2_en_98"));
                } else if(!hmap.get("e2_en_92").isEmpty()){
                    hmap.put("cls_1",hmap.get("e2_en_80"));
                    hmap.put("cls_2",hmap.get("e2_en_86"));
                    hmap.put("cls_3",hmap.get("e2_en_92"));
                } else {
                    hmap.put("cls_1",hmap.get("e2_en_74"));
                    hmap.put("cls_2",hmap.get("e2_en_80"));
                    hmap.put("cls_3",hmap.get("e2_en_86"));
                }

                //Most Recent Education
                if (!hmap.get("st_st_46").isEmpty()) {
                    hmap.put("edu_1",hmap.get("st_st_46"));
                    hmap.put("edu_2",hmap.get("st_st_47"));
                    hmap.put("edu_3",hmap.get("st_st_48"));
                } else if (!hmap.get("st_st_41").isEmpty()) {
                    hmap.put("edu_1",hmap.get("st_st_41"));
                    hmap.put("edu_2",hmap.get("st_st_42"));
                    hmap.put("edu_3",hmap.get("st_st_43"));
                } else {
                    hmap.put("edu_1",hmap.get("st_st_36"));
                    hmap.put("edu_2",hmap.get("st_st_37"));
                    hmap.put("edu_3",hmap.get("st_st_38"));
                }
                
                //Recent Social Service
                 if (!hmap.get("st_st_61").isEmpty()) {
                    hmap.put("sc_1",hmap.get("st_st_61"));
                    hmap.put("sc_2",hmap.get("st_st_62"));
                    hmap.put("sc_3",hmap.get("st_st_63"));
                } else if (!hmap.get("st_st_56").isEmpty()) {
                    hmap.put("sc_1",hmap.get("st_st_56"));
                    hmap.put("sc_2",hmap.get("st_st_57"));
                    hmap.put("sc_3",hmap.get("st_st_58"));
                } else {
                    hmap.put("sc_1",hmap.get("st_st_51"));
                    hmap.put("sc_2",hmap.get("st_st_52"));
                    hmap.put("sc_3",hmap.get("st_st_53"));
                }
             
                //Status
                hmap.put("status", "");
                
                //Status Change 3 - status
                if (!hmap.get("st_st_31").isEmpty() && !hmap.get("st_st_30").isEmpty()) {
                    if(hmapVal.get("st_st_30").equals("1")){
                        hmap.put("status", "Stop Out");
                    }else if(hmapVal.get("st_st_30").equals("2")){
                        if(!hmapVal.get("st_st_33").isEmpty()){
                            if(hmapVal.get("st_st_33").equals("1")){
                                hmap.put("status", "Exited Complete");
                            }else{
                                hmap.put("status", "Exited Not Complete");
                            }
                        }
                    }else if(hmapVal.get("st_st_30").equals("3")){
                        hmap.put("status", "Enrolled");
                    }
                }
                
                if(hmap.get("status").isEmpty()){
                    //Status Change 2 - status
                    if (!hmap.get("st_st_24").isEmpty() && !hmap.get("st_st_23").isEmpty()) {
                        if(hmapVal.get("st_st_23").equals("1")){
                            hmap.put("status", "Stop Out");
                        }else if(hmapVal.get("st_st_23").equals("2")){
                            if(!hmapVal.get("st_st_26").isEmpty()){
                                if(hmapVal.get("st_st_26").equals("1")){
                                    hmap.put("status", "Exited Complete");
                                }else{
                                    hmap.put("status", "Exited Not Complete");
                                }
                            }
                        }else if(hmapVal.get("st_st_23").equals("3")){
                            hmap.put("status", "Enrolled");
                        }
                    }
                }
                
                if(hmap.get("status").isEmpty()){
                    //Status Change 1 - status
                    if (!hmap.get("st_st_17").isEmpty() && !hmap.get("st_st_16").isEmpty()) {
                        if(hmapVal.get("st_st_16").equals("1")){
                            hmap.put("status", "Stop Out");
                        }else if(hmapVal.get("st_st_16").equals("2")){
                            if(!hmapVal.get("st_st_19").isEmpty()){
                                if(hmapVal.get("st_st_19").equals("1")){
                                    hmap.put("status", "Exited Complete");
                                }else{
                                    hmap.put("status", "Exited Not Complete");
                                }
                            }
                        }
                        
                    }
                }
                
                if(hmap.get("status").isEmpty()){
                    if(!hmap.get("st_st_11").isEmpty()){
                        hmap.replace("status","Enrolled");
                    }else if(!hmap.get("st_st_13").isEmpty()){
                        //exited Enrollment
                         hmap.replace("status","Exited Enrollment");
//                        if(!hmap.get("oc_ins_8").isEmpty() && hmap.get("oc_ins_8").equals("Yes")){
//                            hmap.replace("status","Exited Complete");
//                        }else if(!hmap.get("st_st_11").isEmpty()){
//                            hmap.replace("status","Exited Not Completed");
//                        }else{
//                            hmap.replace("status","Exited Enrollment");
//                        }
                    }else if(!hmap.get("st_st_10").isEmpty()){
                         hmap.replace("status","Enrolling");
                    }else{ // if(!hmap.get("st_st_06").isEmpty())
                        if (!hmap.get("isimport").isEmpty() && hmap.get("isimport").equals("true")) {
                            hmap.replace("status","Import"); 
                        }else{
                            hmap.replace("status","Intake");
                        }
                    }
                }
            }
            
            String jsonString = new Gson().toJson(hmap);
            
            return jsonString;
        } catch (Exception ex){
            System.out.println("Exception=====>"+ex);
            return ex.toString();
        }
        finally {
            hmap = null;
        }
    }
    
    @POST
    @Path ("/userpicupload/{id}")
    
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
    
    @PUT
    @Path("clientupdate")
    //customerId is dummy here. get customerId from reg code
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTUser clientupdate(SMRTUser entity) throws Exception {

        
        if ( this.isEmpty( entity.getFirstname())) {
            throw new InvalidParameterException("First Name is required");
        } 
         if ( this.isEmpty( entity.getLastname())) {
            throw new InvalidParameterException("Last Name is required");
        }   
        if ( this.isEmpty( entity.getEmail()) ){
            throw new InvalidParameterException("Email is required");
        }
//        if(this.isEmpty(entity.getPermission_type())) {
//            throw new InvalidParameterException("Please select an access right");
//        }

        entity.setEmail(entity.getEmail().toLowerCase());
        SMRTUser u = this.find(entity.getId());
                
        if (!u.getEmail().equals(entity.getEmail()) && !this.checkLoginAvailability(entity.getEmail(),entity.getCustomerId(),entity.getId())){
            throw new InvalidParameterException("Email ID already registered. Try another");
        }
        
        u.setFirstname(entity.getFirstname());
        u.setLastname(entity.getLastname());
        u.setEmail(entity.getEmail());
        u.setLoginName(entity.getEmail());
        u.setPhonenumber(entity.getPhonenumber());
        
        //to update phone number in survey
        DB mongoDB = this.getDBInstance();
        DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");

        BasicDBObject query = new BasicDBObject();
        query.put("participantId", u.getId());
        query.put("customerId", u.getCustomerId());
        BasicDBObject data = new BasicDBObject();

        data.put("s1_req_7", new BasicDBObject("val", u.getPhonenumber()).append("txt", u.getPhonenumber()));
        
        data = new BasicDBObject(
                    "$set",
                    data
                    .append("resultTime", new Date())
                ).append(
                    "$setOnInsert",
                    new BasicDBObject("participantId", u.getId())
                    .append("customerId",u.getCustomerId())
                  //  .append("createdBy", user.getId()) todo set userID from token
                );
        surveyResultCollection.update(query, data, true, false);

        return  this.edit(u);
        
    }
    
   @PUT
   @Path("approveverify/{id}")
 //  @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
   public SMRTUser verifyupdate(@QueryParam ("token") String token,@PathParam("id")String id)throws Exception{
       
       SMRTUser u=this.validateToken(token);

        SMRTUser user= this.find(id);
        String verifier = u.getId();
        if(u.getUsertype().equals("admin")){
            verifier = "admin";
        }

        List<String> verify  = user.getVerify();
        if(verify == null){
            verify = new ArrayList<String>();
        }
        verify.add(verifier);
        user.setVerify(verify);

        return  this.edit(user);
   }    
   @PUT
   @Path("approveverifyall")
   @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
   public String verifyall(@QueryParam("token") String token,@FormParam("data[]") List<String> users)throws Exception{
       
       SMRTUser u = this.validateToken(token);
       String verifier = u.getId();
       
       if(u.getUsertype().equals("admin")){
            verifier = "admin";
        }
       
       for(String id : users){
           SMRTUser user= this.find(id);

            List<String> verify  = user.getVerify();
            if(verify == null){
                verify = new ArrayList<String>();
            }
            verify.add(verifier);
            user.setVerify(verify);

             this.edit(user);
       }
       
       
       return "OK";
   } 
   
   //for userImport csv file not yet using
     @GET
        @Path("getuserImport")
        @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
        public byte[] getLogo(@QueryParam("token") String token) throws Exception {
        
            try {
            SMRTUser u=this.validateToken(token);
                File file = null;
           
                    String filePath = Paths.get(System.getenv("SMRT_DOCS"),"Uploads","UserImportDownld","userImport.csv").toString();
                    file = new File(filePath);
                 if (!file.exists()) {
                        file = new File(Paths.get(System.getenv("SMRT_DOCS"), "Uploads", "UserPhoto", "human.jpg").toString());
                    }  
                
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] fileContent = new byte[(int) file.length()];
                fileInputStream.read(fileContent);
                return fileContent;
            } catch (Exception ex) {
                return null;
            }
        }  
    
//   @POST
//    @Path ("saveAssignedTo")
//    @Consumes("application/x-www-form-urlencoded")
//    public String saveAllClients(@QueryParam("token") String token, 
//            @FormParam("id") String id
//            ,@FormParam("groupOwner[]") List<String> groupOwner
//            
//    ) throws Exception {
//        System.out.println("CREATE Group");
//        SMRTUser user = this.validateToken(token);
//        Group group = this.find(id);
//        group.setGroupOwner(groupOwner);
//        this.edit(group);
//        return "Group updated Successfully";
//    }
    private void setKeyValue(String key,HashMap<String, String> values) {
        try {
            hmap.replace(key,values.getOrDefault(hmap.get(key),""));
        } catch(Exception ex) { }
    }
    @POST
    @Path("resetpassword")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SMRTUser resetpassword(@QueryParam("email") String email) throws Exception {
    try{
        String newpassword=Util.randompassword();
        Query q  = this.getEntityManager().createQuery("select c FROM SMRTUser c WHERE c.email = :email", SMRTUser.class);
        q.setParameter("email", email);
        q.setMaxResults(1);
        List<SMRTUser> resultList = q.getResultList();
        String id=resultList.toString().replaceAll("[\\[\\]\\(\\)]", "");
        SMRTUser u=this.find(id);
        if(resultList.size()>0){
        u.setResetpassword(newpassword);
        this.update(u);
        List<String> ccs=new ArrayList<String>();
        String subject="Reset Password";
        List<String> to=new ArrayList<String>();
        to.add(u.getEmail());
        String msg="\n" 
                    +"Your new password is " +newpassword+ "\n " + 
                    
                    "";
        Emailer.sendEmail(to, ccs, subject, msg);
        }
        else
        {
         throw new InvalidParameterException("Please enter a valid Email-ID!");
        }
    } 
    catch(Exception ex)
    { 
        throw ex;
    }
        return null;
        
    }
    @GET
    @Path("getdashclientdata")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String getdashclientdata(@QueryParam("id") String id,@QueryParam("token") String token) throws Exception {
    try
    {
        if(id.isEmpty())return new Gson().toJson(false);
        SMRTUser u=this.validateToken(token);
        DB mongoDB = this.getDBInstance();
        DBCollection sentSms=mongoDB.getCollection("communications");
        DBCollection sms=mongoDB.getCollection("sms_reply");
        DBCollection activities=mongoDB.getCollection("activities");
        DBCollection notes=mongoDB.getCollection("activities");
        DBCollection tasks=mongoDB.getCollection("Task");
        DBCollection surveyresults=mongoDB.getCollection("survey_results");
        long usertextscount = sms.count(new BasicDBObject("userId",id));
        long textscount = sentSms.count(new BasicDBObject("userId",id));
            // get total sms count
            usertextscount = usertextscount + textscount;
        long activitiescount = activities.count(new BasicDBObject("customerId", u.getCustomerId()).append("activityfor", id));
        long notescount = notes.count(new BasicDBObject("customerId", u.getCustomerId()).append("activityfor", id));
        long taskscount = tasks.count(new BasicDBObject("customerId", u.getCustomerId()).append("clientId", id));
        long tasksopencount = tasks.count(new BasicDBObject("customerId", u.getCustomerId()).append("clientId", id).append("status", "Open"));
        BasicDBObject conditon=new BasicDBObject("participantId",id);
        DBObject user=surveyresults.findOne(conditon);
        String status;
        try
        {
            status=user.get("status").toString();
        }
        catch(Exception ex){
            status="Intake";
        }
        DBCollection survey_results=mongoDB.getCollection("survey_results");
        List<DBObject> aggQuery=new ArrayList<DBObject>(Arrays.asList(
                (DBObject) new BasicDBObject("$lookup",
                  new BasicDBObject("from","SMRTUser")      
                    .append("localField","participantId")
                    .append("foreignField","_id")
                    .append("as","user"))
                ,(DBObject) new BasicDBObject("$match",
                  new BasicDBObject("user._id",id))
                ,(DBObject) new BasicDBObject("$unwind",
                new BasicDBObject("path","$user")
                    .append("preserveNullAndEmptyArrays", true)
                    )));
//                  ,(DBObject) new BasicDBObject("$project",
//                         new BasicDBObject("firstname","$user.firstname")
//                            .append("lastname","$user.lastname")
//                            .append("email","$user.email")
//                            .append("createdOn","$user.createdOn")
//                            .append("lastactivity","$user.last_login_date")
//                            .append("lastaction","$user.lastactivity")
//                            .append("organization","$user.organization") 
//                     )  
//                ));
//                aggQuery.add((DBObject)new BasicDBObject("$lookup",
//                    new BasicDBObject("from","survey_results")
//                    .append("localField","_id" )
//                    .append("foreignField","participantId")
//                    .append("as","result")
//                ));
//                aggQuery.add((DBObject) new BasicDBObject("$unwind", 
//                     new BasicDBObject("path","$result" )
//                        .append("preserveNullAndEmptyArrays", true)
//                ));
                aggQuery.add((DBObject)new BasicDBObject("$lookup",
                    new BasicDBObject("from","group")
                    .append("localField","participantId" )
                    .append("foreignField","contents")
                    .append("as","groups")
                ));
                aggQuery.add((DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$groups")
                        .append("preserveNullAndEmptyArrays", true)
                ));
                aggQuery.add((DBObject)new BasicDBObject("$project",
                    new BasicDBObject("firstname","$user.firstname")
                        .append("lastname","$user.lastname")
                        .append("email","$user.email")
                        .append("groupname","$groups.name")
                        .append("phoneNumber","$s1_req_7.val")
                        .append("hphoneNumber","$s1_req_8.val")
                        //.append("createdOn","$result.st_st_06.val")
                        .append("studentId","$uniq_imp_key.val")      
                        .append("lastactivity","$user.lastactivity")
                        .append("lastaction","$user.lastaction")
                        .append("status",new Gson().fromJson("{$cond: { if:  { $or: [{$eq:[\"$status\", null ]},{$not:[\"$status\"]}]}, then: \"Intake\", else: $status }}",BasicDBObject.class))
                        .append("se_client_01","$se_client_01.val")
                        .append("prog_01_sec","$prog_01_sec.val")
                        .append("prog_02_sec","$prog_02_sec.val")
                        .append("prog_03_sec","$prog_03_sec.val")
                        .append("prog_04_sec","$prog_04_sec.val")
                        .append("prog_05_sec","$prog_05_sec.val")
                        .append("prog_06_sec","$prog_06_sec.val")
                ));
            
                aggQuery.add((DBObject) new BasicDBObject("$group",
                    new BasicDBObject("_id","$_id")
                    .append("firstname",new BasicDBObject("$first","$firstname"))
                    .append("lastname",new BasicDBObject("$first","$lastname"))
                    .append("email",new BasicDBObject("$first","$email"))
                    .append("phoneNumber",new BasicDBObject("$first","$phoneNumber"))
                    .append("hphoneNumber",new BasicDBObject("$first","$hphoneNumber"))
                    //.append("createdOn",new BasicDBObject("$first","$user.createdOn"))
                    .append("studentId",new BasicDBObject("$first","$studentId"))
                    .append("lastactivity",new BasicDBObject("$first","$lastactivity")) 
                    .append("lastaction",new BasicDBObject("$first","$lastaction"))
                    //.append("organization",new BasicDBObject("$first","$user.organization"))     
                    //.append("verify",new BasicDBObject("$first","$user.verify"))
                    .append("groups",new BasicDBObject("$push","$groupname"))
                    .append("program",new BasicDBObject("$first","$se_client_01"))
                    .append("prog_01_sec",new BasicDBObject("$first","$prog_01_sec"))
                    .append("prog_02_sec",new BasicDBObject("$first","$prog_02_sec"))
                    .append("prog_03_sec",new BasicDBObject("$first","$prog_03_sec"))
                    .append("prog_04_sec",new BasicDBObject("$first","$prog_04_sec"))
                    .append("prog_05_sec",new BasicDBObject("$first","$prog_05_sec"))
                    .append("prog_06_sec",new BasicDBObject("$first","$prog_06_sec"))
                    .append("status",new BasicDBObject("$first","$status"))    
                ));
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
                    .append("prog_01_sec","$prog_01_sec")
                    .append("prog_02_sec","$prog_02_sec")
                    .append("prog_03_sec","$prog_03_sec")
                    .append("prog_04_sec","$prog_04_sec")
                    .append("prog_05_sec","$prog_05_sec")
                    .append("prog_06_sec","$prog_06_sec")
                  ));
                Iterable<DBObject> result = survey_results.aggregate(aggQuery).results();
        SMRTUser userdata=this.find(id);
        BasicDBObject results=new BasicDBObject();
        results.append("usertextscount",usertextscount);
        results.append("activitiescount",activitiescount);
        results.append("notescount",notescount);
        results.append("taskscount",taskscount);
        results.append("clientstatus",status);
        results.append("clientdata",new Gson().toJson(result));
        results.append("tasksopencount",tasksopencount);
        results.append("userdetails",new BasicDBObject("name",userdata.getLastname()+", "+userdata.getFirstname())
                 .append("email", userdata.getEmail()).append("createdOn",userdata.getCreatedOn()));
        if(user != null){
         if(user.containsKey("s1_req_7"))
            {
                results.append("phoneNumber",((DBObject) user.get("s1_req_7")).get("val"));
            }
         if(user.containsKey("s1_req_8"))
            {
                results.append("hphoneNumber",((DBObject) user.get("s1_req_8")).get("val"));
            }
        }
       
        return new Gson().toJson(results);
       // new Gson().fromJson("{$cond: { if:  { $or: [{s1_req_7:{$exists: true }}]}, then: "+ ((DBObject)user.get("s1_req_7")).get("val")+", else: "+0+" }}",BasicDBObject.class))
    } 
    catch(Exception ex)
    { 
        throw ex;
    }
    }
}
