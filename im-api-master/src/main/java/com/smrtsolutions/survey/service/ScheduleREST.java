package com.smrtsolutions.survey.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import com.smrtsolutions.exception.InvalidParameterException;
import com.smrtsolutions.survey.model.Content;
import com.smrtsolutions.survey.model.Customer;
import com.smrtsolutions.survey.model.NameValuePair;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.survey.model.Task;
import com.smrtsolutions.survey.model.TaskAssignment;
import com.smrtsolutions.survey.model.TaskNote;
import com.smrtsolutions.util.GsonUTCDateAdapter;
import com.smrtsolutions.util.Util;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import static javafx.scene.Cursor.cursor;
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
import javax.ws.rs.HeaderParam;
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
import org.bson.types.ObjectId;
import org.eclipse.persistence.jpa.jpql.parser.DateTime;
import org.jose4j.json.internal.json_simple.JSONArray;

@Stateless
@Path("schedule")
public class ScheduleREST extends SMRTAbstractFacade<Content>{
    
    private static final Logger logger = LogManager.getLogger();
    private EntityManager em;

    public ScheduleREST() {
        super(Content.class);
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
@Path("appointment")
public String get()throws Exception{
    
    DB mongodb=this.getDBInstance();
    DBCollection apptCollection=mongodb.getCollection("appointment");
    
        
        Calendar cal = Calendar.getInstance();
        Date start = cal.getTime();
        cal.add(Calendar.DATE, 1);
        Date end = cal.getTime();
        
        Iterator<DBObject> result = apptCollection.aggregate(Arrays.asList( 
                
            (DBObject) new BasicDBObject("$match",new BasicDBObject("status","reserved")
                    .append("start", new BasicDBObject("$gte", start))
                    .append("end", new BasicDBObject("$lte", end))
                    .append("reminded", new BasicDBObject("$ne", true)))
            ,(DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"SMRTUser")
                        .append("localField","user_id")
                        .append("foreignField","_id")
                        .append("as","sender")
                    )
            ,(DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$sender")
                        .append("preserveNullAndEmptyArrays", true)
                    ) 
            ,(DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"survey_results")
                        .append("localField","reserved_user_id")
                        .append("foreignField","participantId")
                        .append("as","user")
                    )
            ,(DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$user")
                        .append("preserveNullAndEmptyArrays", true)
                    )
            ,(DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"customer")
                        .append("localField","sender.customerId")
                        .append("foreignField","_id")
                        .append("as","customer")
                    )
            ,(DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$customer")
                        .append("preserveNullAndEmptyArrays", true)
                    )
            ,(DBObject) new BasicDBObject("$project",
                    new BasicDBObject("userfirstname","$user.s1_req_1.txt")
                    .append("userlastname","$user.s1_req_2.txt")
                    .append("userphonenumber","$user.s1_req_7.txt")
                    .append("userId","$user.participantId")        
                    .append("senderfirstname","$sender.firstname")        
                    .append("senderlastname","$sender.lastname")
                    .append("customerKey","$customer.urlKey")
                    .append("customerId","$customer._id")
                    .append("timezone","$customer.timezone")
                    .append("sentby","$sender._id")
                    .append("datetime","$start")
                    )   
            )).results().iterator();
         
        
        while(result.hasNext()){
            BasicDBObject record = (BasicDBObject) result.next();
            String ufname = (String) record.get("userfirstname");
            String ulname = (String) record.get("userlastname");
            String uphone = (String) record.get("userphonenumber");
            String userId = (String) record.get("userId");
            String sfname = (String) record.get("senderfirstname");
            String slname = (String) record.get("senderlastname");
            String scustomerKey = (String) record.get("customerKey");
            String scustomerID = (String) record.get("customerId");
            String sentby = (String) record.get("sentby");
            Date date = (Date) record.get("datetime");
            String timezone = (String)record.get("timezone");
            
            if(timezone == null || timezone.isEmpty()){
                timezone = "US/Central";
            }
            
            DateFormat timeformatter = new SimpleDateFormat("hh:mm a");
            timeformatter.setTimeZone(TimeZone.getTimeZone(timezone));
            String time = timeformatter.format(date);
            
            DateFormat dateformatter = new SimpleDateFormat("MM/dd/yyyy");
            dateformatter.setTimeZone(TimeZone.getTimeZone(timezone));
            String dateString = dateformatter.format(date);
                    
            if (ufname ==null){ ufname =""; }
            String msg="Hi " + ufname + ",\r\nYou have an appointment scheduled, please log in to your " + scustomerKey.toUpperCase() + " student portal to see details or to cancel" + " https://"+scustomerKey.toLowerCase()+"student.smrtdata.info"
                    + "\r\nWith: " + slname +", " +sfname+  "\r\nTime: "+ time +"\r\nDate: "+dateString ;
            
            if(uphone !=null && !uphone.isEmpty()){ 
             try{      
            uphone = uphone.replaceAll(" ", "").replaceAll("-", "").replaceAll("\\(", "").replaceAll("\\)", "");
        BasicDBObject message=new BasicDBObject();
                            
                    message.append("content", msg)
                            .append("source_number", "18885039775")
                            .append("source_number_type","INTERNATIONAL")
                            .append("destination_number", "+1"+uphone)
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
            document.put("customerId",scustomerID);
            document.put("receiver",uphone);
            document.put("userId",userId);
            document.put("message",msg);
            document.put("status",resultJson);
            document.put("errormsg","");
            document.put("senton",Calendar.getInstance().getTime());
            document.put("sentby",sentby);
            communication.insert(document);
            DBCollection txtreply= mongodb.getCollection("sms_reply");
            BasicDBObject updateQuery = new BasicDBObject();
            updateQuery.append("$set",new BasicDBObject()
            .append("replystatus",true)
            );
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.append("userId",sentby);
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
            document.put("customerId",scustomerID);
            document.put("receiver",uphone);
            document.put("userId",userId);
            document.put("message",msg);
            document.put("status","failed");
            document.put("errormsg",ex.getMessage());
            document.put("senton",Calendar.getInstance().getTime());
            document.put("sentby",sentby);
            communication.insert(document);
        }
        }
                    
        apptCollection.update(new BasicDBObject("_id", record.get("_id")), new BasicDBObject(
            "$set",
            new BasicDBObject("reminded", true)
        ), false, false);
    
        }
        return "Ok";  
    }

    @GET
    @Path("reminder")

    public String getreminder()throws Exception{

        DB mongodb = this.getDBInstance();
        DBCollection formCollection = mongodb.getCollection("surveyallocate");
        DBCollection taskCollection = mongodb.getCollection("Task");
        DBCollection appointmentCollection = mongodb.getCollection("appointment");
        DBCollection surveyCollection = mongodb.getCollection("survey_results");
        
        //get pending form result list
        Iterator<DBObject> formResult = formCollection.aggregate(Arrays.asList( 
            (DBObject) new BasicDBObject("$unwind","$userId")
            ,(DBObject) new BasicDBObject("$group",
                new BasicDBObject("_id",new BasicDBObject("userId","$userId")
                .append("surveyId","$survey_id"))
            )
            ,(DBObject) new BasicDBObject("$lookup", 
                new BasicDBObject("from" ,"survey_results")
                .append("localField","_id.userId")
                .append("foreignField","participantId")
                .append("as","result")
            )
            ,(DBObject) new BasicDBObject("$unwind","$result")
            ,(DBObject) new BasicDBObject("$project",
                new BasicDBObject("_id",1)
               .append("status",new BasicDBObject("$filter",
                    new BasicDBObject("input",new BasicDBObject("$objectToArray","$result.surveyStatus"))
                    .append("as","item")
                    .append("cond",new BasicDBObject("$eq", (BasicDBList)com.mongodb.util.JSON.parse("[\"$$item.k\",\"$_id.surveyId\"]"))
                )))
            ),
            (DBObject) new BasicDBObject("$unwind",
                new BasicDBObject("path", "$status")
               .append("preserveNullAndEmptyArrays", true)
            ),
            (DBObject) new BasicDBObject("$match",
                new BasicDBObject("status.v", new BasicDBObject("$ne",3))
            ),
            (DBObject) new BasicDBObject("$group",
                new BasicDBObject("_id","$_id.userId")
               .append("count",new BasicDBObject("$sum",1))
            )
        )).results().iterator();
        
        //get pending task list
        Iterator<DBObject> taskResult = taskCollection.aggregate(Arrays.asList( 
            (DBObject) new BasicDBObject("$match",
                new BasicDBObject("status","Open")
            ),
            (DBObject) new BasicDBObject("$lookup", 
                new BasicDBObject("from" ,"SMRTUser")
                .append("localField","clientId")
                .append("foreignField","_id")
                .append("as","user")
            ),
            (DBObject) new BasicDBObject("$unwind",
                new BasicDBObject("path","$user")
            ),
            (DBObject) new BasicDBObject("$match",
                new BasicDBObject("user.usertype","student")
            )
            ,(DBObject) new BasicDBObject("$group",
                new BasicDBObject("_id","$clientId")
                .append("count",new BasicDBObject("$sum",1))
            )
        )).results().iterator();
        
        //get pending appointment list
        Iterator<DBObject> appointmentResult = appointmentCollection.aggregate(Arrays.asList( 
            (DBObject) new BasicDBObject("$match",
                new BasicDBObject("status","reserved")
               .append("start",new BasicDBObject("$gt",new Date())))
            ,(DBObject) new BasicDBObject("$group",
                new BasicDBObject("_id","$reserved_user_id")
               .append("count",new BasicDBObject("$sum",1))
            )
        )).results().iterator();

        HashMap<String, HashMap<String, Integer>> reminders = new HashMap<>();
        
        //add all form result to hash map
        while(formResult.hasNext()){
            BasicDBObject record = (BasicDBObject) formResult.next();
            HashMap<String, Integer> data = new HashMap<>();
            data.put("forms", (Integer) record.get("count"));
            reminders.put((String)record.get("_id"), data);
        }
        
        //add all task result to hash map
        while(taskResult.hasNext()){
            BasicDBObject record = (BasicDBObject) taskResult.next();
            HashMap<String, Integer> data = reminders.get((String)record.get("_id"));
            if(data == null){
                data = new HashMap<>();
            }
            data.put("tasks", (Integer) record.get("count"));
            reminders.put((String)record.get("_id"), data);
        }
        
        //add all appointment result to hash map
        while(appointmentResult.hasNext()){
            BasicDBObject record = (BasicDBObject) appointmentResult.next();
            HashMap<String, Integer> data = reminders.get((String)record.get("_id"));
            if(data == null){
                data = new HashMap<>();
            }
            data.put("appointments", (Integer) record.get("count"));
            reminders.put((String)record.get("_id"), data);
        }
        
        List<String> users = new ArrayList<>(reminders.keySet());
        if(users.isEmpty())
            return "0k";
        //get user Infos to send text reminders
        Iterator<DBObject> userResult = surveyCollection.aggregate(Arrays.asList( 
            (DBObject) new BasicDBObject("$match",
                new BasicDBObject("participantId",new BasicDBObject("$in", users))
                        .append("$and", (BasicDBList)com.mongodb.util.JSON.parse("[{\"s1_req_7.txt\" :{$ne: null}},{\"s1_req_7.txt\" :{$ne: \"\"}}]"))
                        .append("s1_req_7.txt",new BasicDBObject("$ne", null))
            )
            , (DBObject) new BasicDBObject("$lookup",
                new BasicDBObject("from","customer")
                    .append("localField","customerId")
                    .append("foreignField","_id")
                    .append("as","customer")
            )
            , (DBObject) new BasicDBObject("$unwind","$customer")
            , (DBObject) new BasicDBObject("$project",
                new BasicDBObject("firstname","$s1_req_1.txt")
                    .append("lastname","$s1_req_2.txt")
                    .append("phonenumber","$s1_req_7.txt")
                    .append("userId","$participantId")        
                    .append("customerKey","$customer.urlKey")
                    .append("customerId","$customer._id")
            )
        )).results().iterator();
        
        //send reminder text notification
        while(userResult.hasNext()){ 
            BasicDBObject record = (BasicDBObject) userResult.next();
            String fname = (String) record.get("firstname");
              if (fname ==null){ fname =""; }      
            String msg = "Good Morning "+ fname+",\r\n";
            msg += "This is your friendly reminder of items that require attention in your "+ ((String)record.get("customerKey")).toUpperCase()+" student portal.\r\n";
            msg += "https://" + ((String)record.get("customerKey")).toLowerCase() + "student.smrtdata.info\r\n";
            
            HashMap<String, Integer> reminder = reminders.get((String)record.get("userId"));
            if(reminder.containsKey("forms")){
                msg += "\r\n" + reminder.get("forms") + " Forms [pending/new]";
            }
            
            if(reminder.containsKey("tasks")){
                msg += "\r\n" + reminder.get("tasks") + " Tasks";
            }
            
            if(reminder.containsKey("appointments")){
                msg += "\r\n" + reminder.get("appointments") + " Appointments";
            }
            
            String phone = (String) record.get("phonenumber");
            //todo send message
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
            document.put("customerId", record.get("customerId"));
            document.put("receiver",phone);
            document.put("userId",(String)record.get("userId"));
            document.put("message",msg);
            document.put("status",resultJson);
            document.put("errormsg","");
            document.put("senton",Calendar.getInstance().getTime());
            document.put("sentby",null);
            communication.insert(document);
            
            }
          catch(Exception ex){
           this.logger.error("Cannot send text message - survey allocate", ex);
           DB mongoDB=this.getDBInstance();
            DBCollection communication= mongoDB.getCollection("communications");
            BasicDBObject document=new BasicDBObject();
            ObjectId id = new ObjectId();
            document.put("_id", id);
            document.put("type","SMS");
            document.put("customerId", record.get("customerId"));
            document.put("receiver",phone);
            document.put("userId",(String)record.get("userId"));
            document.put("message",msg);
            document.put("status","failed");
            document.put("errormsg",ex.getMessage());
            document.put("senton",Calendar.getInstance().getTime());
            document.put("sentby",null);
            communication.insert(document);
        }
        }

        }
        
        return "0k";
    }
}