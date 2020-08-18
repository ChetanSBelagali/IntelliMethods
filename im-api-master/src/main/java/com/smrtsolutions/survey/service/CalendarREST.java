/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.survey.service;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.util.GsonUTCDateAdapter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

/**
 *
 * @author macadmin
 */
@Path("/calendar")
public class CalendarREST extends SMRTAbstractFacade<SMRTUser> {
    private static final Logger logger = LogManager.getLogger(CalendarREST.class);
    public String[] REQUIRED_PERMISSIONS = {SMRTRole.NONE};
    Map<String, String> hmap = new HashMap<String, String>();
    public CalendarREST() {
        super(SMRTUser.class);
    }
    @GET
    @Path("getresource")
    @Produces({MediaType.APPLICATION_JSON})
    public String findresource(@QueryParam("token") String token)  throws Exception{
       try {
        SMRTUser user = this.validateToken(token, null); 
        DB mongoDB = this.getDBInstance();
        DBCollection userCollection = mongoDB.getCollection("group");
        //BasicDBObject condition = new BasicDBObject("content",user.getId());
        List<DBObject> aggQuery=Arrays.asList(
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$contents")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$match", 
                        new BasicDBObject("contents",user.getId())
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$groupOwner")
                        .append("preserveNullAndEmptyArrays", false)
                ),
                (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"SMRTUser")
                        .append("localField","groupOwner")
                        .append("foreignField","_id")
                        .append("as","casemanager")
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$casemanager")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$casemanager.roles")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$casemanager.location")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"Location")
                        .append("localField","casemanager.location")
                        .append("foreignField","_id")
                        .append("as","locations")
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$locations")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$project", 
                        new BasicDBObject()
                        .append("id","$casemanager._id")
                        .append("email","$casemanager.email")
                        .append("phone","$casemanager.phonenumber")
                        .append("firstname","$casemanager.firstname")
                        .append("lastname","$casemanager.lastname")
                        .append("role","$casemanager.roles")
                        .append("location","$locations.location")
                        .append("services","$casemanager.servicename")
                        .append("address","$locations.address")
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$services")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"ServiceName")
                        .append("localField","services")
                        .append("foreignField","_id")
                        .append("as","service")
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$service")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$match", 
                     new BasicDBObject()
                        .append("role", new BasicDBObject("$ne","admin"))
                ),
                (DBObject) new BasicDBObject("$group",
                        new BasicDBObject()
                        .append("_id",new BasicDBObject()
                                .append("id","$id")
                                .append("email", "$email")
                                .append("phone","$phone")
                                .append("firstname","$firstname")
                                .append("lastname","$lastname")
                                .append("service","$service.servicename")
                                .append("role","$role")
                                .append("location","$location")
                                .append("address","$address")
                )),
                (DBObject) new BasicDBObject("$project", 
                        new BasicDBObject()
                        .append("_id","$_id.id")
                        .append("email","$_id.email")
                        .append("phone","$_id.phone")
                        .append("firstname","$_id.firstname")
                        .append("lastname","$_id.lastname")
                        .append("service","$_id.service")
                        .append("role","$_id.role")
                        .append("location","$_id.location")
                        .append("address","$_id.address")
                )
            
        );
        Iterable<DBObject> resources = userCollection.aggregate(aggQuery).results();
        return new Gson().toJson(resources);
        
    } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
    } finally{
        //ToDO
    } 
    }
    @POST
    @Path("setavailability")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String setavailability(@FormParam("starttime") String start,
            @FormParam("endtime") String end,
            @FormParam("timeslot") String timeslot,
            @FormParam("repeats") String repeats,
            @FormParam("reserveid") String reserveid,
            @FormParam("status") String status,
            @FormParam("repeatuntil") String repeatuntil,
            @FormParam("eventfor") String eventfor,
            @FormParam("eventabout") String eventabout,
            @FormParam("eventtitle") String eventtitle,
            @QueryParam("token") String token)throws Exception{
    try {
    SMRTUser u=this.validateToken(token);
    DB mongoDB=this.getDBInstance();
    DBCollection appointmentCollection=mongoDB.getCollection("appointment");
    ISO8601DateFormat df = new ISO8601DateFormat();
    Date d1 = df.parse(start);
    Date d2 = df.parse(end);
    Calendar date2 = Calendar.getInstance();
    date2.setTime(d2);
    Calendar date = Calendar.getInstance();
    date.setTime(d1);
    String stats;
    List<Date> availability = new ArrayList<Date>();
    DBCollection userCollection = mongoDB.getCollection("SMRTUser");
    BasicDBObject search = new BasicDBObject();
    BasicDBObject update = new BasicDBObject();
    
    //String id=ObjectId.get().toString();
    BasicDBObject setavailability = new BasicDBObject().append("user_id", u.getId())
            .append("cancel_remarks", null)
            .append("cancel_user_id", null)
            .append("createdby",u.getUsertype())
            .append("created_on",new Date());
    if(repeats.equals("")&& (!timeslot.equals(""))||repeats.equals(null) && (!timeslot.equals(""))||repeats.equals(null)){
        if(reserveid!=null){
        DBCollection lastactivityCollection = mongoDB.getCollection("activities");
        update.put("activity","Client - Appointment");
        update.put("kind","n/a");
        update.put("createdBy",u.getId());
        update.put("createduname",u.getLastname()+", "+u.getFirstname());
        update.put("createdOn",new Date());
        update.put("customerId",u.getCustomerId());
        update.put("activityfor",reserveid);
        update.put("detail","n/a");
        update.put("information","n/a");
        lastactivityCollection.insert(update);
        }
    do {
        Date newdatetime=date.getTime();
        availability.add(newdatetime);
        if(timeslot.equals("15")){
        date.add(Calendar.MINUTE, 15);
        }
        else if(timeslot.equals("30")){
        date.add(Calendar.MINUTE, 30);
        }
        else if(timeslot.equals("45"))
        {
        date.add(Calendar.MINUTE, 45);
        }
        else
        {
        date.add(Calendar.MINUTE, 60);
        }
        String id = ObjectId.get().toString();
        setavailability.put("status",status);
        setavailability.put("timeslots",newdatetime);
        setavailability.put("eventtitle",eventtitle);
        setavailability.put("reserved_user_id",reserveid);
        setavailability.put("eventabout",eventabout);
        setavailability.put("start",newdatetime);
        //setavailability.put("eventfor",eventfor);
        setavailability.put("end",date.getTime());
        setavailability.put("_id",id);
        appointmentCollection.insert(setavailability);
        
    } while (date.getTime().before(date2.getTime()));

//    else 
//    {
//        if(u.getUsertype().equals("casemanager")&& (!reserveid.equals(null))){
//        update.append("$set",new BasicDBObject()
//                .append("lastactivity","Staff - Appointment"));
//    search.append("_id",reserveid);
//    WriteResult result = userCollection.update(search,update);
//     }
//    }
    }
    else if(repeats.equals("weekly")||repeats.equals("biweekly"))
    {
        int addDate;
        if(repeats.equals("weekly")){addDate=7;}else{addDate=14;}
        Date d3 = df.parse(repeatuntil);
        Calendar date3 = Calendar.getInstance();
        date3.setTime(d3);
        for (Date dates = date.getTime(); dates.before(date3.getTime()); date.add(Calendar.DATE, addDate), dates = date.getTime()) {
            
        do {
        Date newdatetime=date.getTime();
        availability.add(newdatetime);
        if(timeslot.equals("15")){
        date.add(Calendar.MINUTE, 15);
        }
        else if(timeslot.equals("30")){
        date.add(Calendar.MINUTE, 30);
        }
        else if(timeslot.equals("45"))
        {
        date.add(Calendar.MINUTE, 45);
        }
        else
        {
        date.add(Calendar.MINUTE, 60);
        }
        String id = ObjectId.get().toString();
        setavailability.put("status",status);
        setavailability.put("timeslots",newdatetime);
        setavailability.put("start",newdatetime);
        setavailability.put("eventtitle",eventtitle);
        setavailability.put("reserved_user_id",reserveid);
        setavailability.put("eventabout",eventabout);
        //setavailability.put("eventfor",eventfor);
        setavailability.put("end",date.getTime());
        setavailability.put("_id",id);
        appointmentCollection.insert(setavailability);
        } while (date.getTime().before(date2.getTime()));
        Calendar date4 = Calendar.getInstance();
        date4.setTime(d1);
        SimpleDateFormat date_format = new SimpleDateFormat("HH:mm:ss");
        String x =date_format.format(date4.getTime());
        String y =date_format.format(date2.getTime());
        Date d11 = date_format.parse(x);
        Date d22 = date_format.parse(y);
        long diff = d22.getTime() - d11.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        int minutes= (int) diffMinutes * -1;
        int hours= (int) diffHours * -1;
        date.add(Calendar.MINUTE,minutes);
        date.add(Calendar.HOUR, hours);
        date2.add(Calendar.DATE,addDate);
       }
    }
    else if(repeats.equals("monthly"))
    {
        Date d3 = df.parse(repeatuntil);
        Calendar date3 = Calendar.getInstance();
        date3.setTime(d3);
        for (Date dates = date.getTime(); dates.before(date3.getTime()); date.add(Calendar.MONTH,1), dates = date.getTime()) {
        do {
        Date newdatetime=date.getTime();
        availability.add(newdatetime);
        if(timeslot.equals("15")){
        date.add(Calendar.MINUTE, 15);
        }
        else if(timeslot.equals("30")){
        date.add(Calendar.MINUTE, 30);
        }
        else if(timeslot.equals("45"))
        {
        date.add(Calendar.MINUTE, 45);
        }
        else
        {
        date.add(Calendar.MINUTE, 60);
        }
        String id = ObjectId.get().toString();
        setavailability.put("status",status);
        setavailability.put("timeslots",newdatetime);
        setavailability.put("eventtitle",eventtitle);
        setavailability.put("reserved_user_id",reserveid);
        setavailability.put("eventabout",eventabout);
        //setavailability.put("eventfor",eventfor);
        setavailability.put("start",newdatetime);
        setavailability.put("end",date.getTime());
        setavailability.put("_id",id);
        appointmentCollection.insert(setavailability);
        } while (date.getTime().before(date2.getTime()));
        Calendar date4 = Calendar.getInstance();
        date4.setTime(d1);
        SimpleDateFormat date_format = new SimpleDateFormat("HH:mm:ss");
        String x =date_format.format(date4.getTime());
        String y =date_format.format(date2.getTime());
        Date d11 = date_format.parse(x);
        Date d22 = date_format.parse(y);
        long diff = d22.getTime() - d11.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        int minutes= (int) diffMinutes * -1;
        int hours= (int) diffHours * -1;
        date.add(Calendar.MINUTE,minutes);
        date.add(Calendar.HOUR, hours);
        date2.add(Calendar.MONTH, 1);
        }
    }
     
    }
    catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
    } finally{
        //ToDO
    } 
    return null;
    
    
    
    }
    
    @GET
    @Path("getavailability")
    @Produces({ MediaType.APPLICATION_JSON})
    public String availability(@QueryParam("token") String token,@QueryParam("id") String caseid)  throws Exception{
    try {
    SMRTUser u=this.validateToken(token);
    DB mongoDB=this.getDBInstance();
    String uid;
    if (caseid.isEmpty()||caseid.equals("")||caseid.equals(null))
    {
    uid=u.getId();
    }
    else
    {
    uid=caseid;
    }
    DBCollection availabilityCollection=mongoDB.getCollection("appointment");
    BasicDBObject condition;
    BasicDBList list;
    
    //if(u.getUsertype().equals("student")){
    if(u.getUsertype().equals("casemanager")){
       BasicDBObject matchStart = new BasicDBObject();
       list=new BasicDBList();
       list.add(new BasicDBObject("status","reserved"));
       list.add(new BasicDBObject("status","open"));
       list.add(new BasicDBObject("status","self"));
       matchStart=new BasicDBObject().append("$or", list)
                   .append("user_id",uid);
        List<DBObject> aggQuery=Arrays.asList(
                (DBObject) new BasicDBObject("$match", 
                        matchStart
                ),
                (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"SMRTUser")
                        .append("localField","reserved_user_id")
                        .append("foreignField","_id")
                        .append("as","availability")
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$availability")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$match", 
                        new BasicDBObject("start",new BasicDBObject("$gte" ,new Date()))
                ),
                (DBObject) new BasicDBObject("$project", 
                        new BasicDBObject()
                        .append("_id","$_id")
                        .append("loginName","$availability.loginName")
                        .append("firstname","$availability.firstname")
                        .append("lastname","$availability.lastname")
                        .append("email","$email")
                        .append("start","$start")
                        .append("reserved_user_id","$reserved_user_id")
                        .append("status","$status")            
                        .append("timeslots","$timeslots")
                        .append("end","$end")
                        .append("eventabout","$eventabout")
                        .append("eventtitle","$eventtitle")
                        .append("createdby","$createdby")
                        .append("status","$status")
                )
            
        );
        Iterable<DBObject> getavailability = availabilityCollection.aggregate(aggQuery).results();
//        TimeZone tz = TimeZone.getTimeZone("UTC");
//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
//        df.setTimeZone(tz);
//        String nowAsISO = df.format(new Date());
//        for(DBObject availability: getavailability){
//                ((BasicDBObject)availability).append("start", ((String)((BasicDBObject)availability).get("start")).compareTo(nowAsISO)>0);
//            }
        Gson json = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
        return json.toJson(getavailability); 
    //Gson json = new GsonBuilder().setDateFormat(new ISO8601DateFormat().toString()).create();
    
    }
    else
    {
       list=new BasicDBList();
       list.add(new BasicDBObject("status","open"));
       list.add(new BasicDBObject("reserved_user_id",u.getId()));
       condition=new BasicDBObject().append("$or", list)
                   .append("user_id",uid);
       DBCursor cursor = availabilityCollection.find(condition);
    //Gson json = new GsonBuilder().setDateFormat(new ISO8601DateFormat().toString()).create();
    Gson json = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
    return json.toJson(cursor.toArray()); 
    }
           
    
    
    
    }
    catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
    } finally{
        //ToDO
    } 

   }
    @PUT
    @Path("cancelavailability")
    public String cancelavailability(@QueryParam("id") String id,
            @QueryParam("token") String token,
            @QueryParam("remarks") String remarks,
            @QueryParam("status") String status
            //@FormParam("email") String email
    ) throws Exception{
    try{
    SMRTUser u=this.validateToken(token);
    DB mongoDB=this.getDBInstance();
    DBCollection availabilityCollection=mongoDB.getCollection("appointment");
    if(u.getUsertype().equals("student")){
    BasicDBObject fields = new BasicDBObject("reserved_user_id",id);
    BasicDBObject updateQuery = new BasicDBObject();
    updateQuery.append("$set",new BasicDBObject()
                .append("status", status)
                .append("eventabout", null)
                .append("createdby","casemanager")
            .append("reserved_user_id",null)
            );
    BasicDBObject searchQuery = new BasicDBObject();
    searchQuery.append("_id",id);
    availabilityCollection.update(searchQuery,updateQuery);
    }
    else
    {
    BasicDBObject fields = new BasicDBObject("_id",id);
    BasicDBObject updateQuery = new BasicDBObject();
    updateQuery.append("$set",new BasicDBObject()
            .append("cancel_remarks",remarks)
            .append("status", "Cancelled")
            .append("reserved_user_id",null)
            );
    BasicDBObject searchQuery = new BasicDBObject();
    searchQuery.append("_id",id);
    availabilityCollection.update(searchQuery,updateQuery);
    //String cc="";
    //List<String> ccs=new ArrayList<String>(Arrays.asList(cc.split(",")));
    //List<String> emailId=new ArrayList<String>(Arrays.asList(email.split(",")));
   // Emailer.sendEmail(emailId,ccs,"Cancelled Appointment", remarks);
    }
    }
    catch (Exception e){
    System.out.println(e.getMessage());
    throw new Exception("Error ");
    }
    finally{
          
    }
        return null;
  
    }
    @PUT
    @Path("setappointment")
    @Produces({MediaType.APPLICATION_JSON})
    public String setappointment(@FormParam("id") String id,@FormParam("userid") String userid,@FormParam("token") String token,@FormParam("eventabout") String eventabout) throws Exception{
    try{
    SMRTUser u=this.validateToken(token);
    DB mongoDB=this.getDBInstance();
    DBCollection availabilityCollection=mongoDB.getCollection("appointment");
    //BasicDBObject fields = new BasicDBObject("_id",id);
//    BasicDBObject updatecollectionQuery = new BasicDBObject();
//    updatecollectionQuery.append("$set",
//    new BasicDBObject().append("reserved_user_id",null)
//                       .append("status", "open"));
//    BasicDBObject searchcollectionQuery = new BasicDBObject();
//    searchcollectionQuery.append("reserved_user_id",u.getId())
//                          .append("user_id",userid);
//    availabilityCollection.update(searchcollectionQuery,updatecollectionQuery);
    if(u.getUsertype().equals("student")){
    DBCollection userCollection = mongoDB.getCollection("SMRTUser");
        BasicDBObject update = new BasicDBObject();
        update.append("$set",new BasicDBObject()
                    .append("lastactivity","Client-Appointment")
                    .append("last_login_date",new Date()));
        BasicDBObject search = new BasicDBObject();
        search.append("_id",u.getId());
        WriteResult result = userCollection.update(search,update);
    BasicDBObject updateQuery = new BasicDBObject();
    updateQuery.append("$set",new BasicDBObject()
            .append("reserved_user_id",u.getId())
            .append("status", "reserved")
            .append("createdby", u.getUsertype())
            .append("eventabout",eventabout)
            );
    BasicDBObject searchQuery = new BasicDBObject();
    searchQuery.append("_id",id);
    availabilityCollection.update(searchQuery,updateQuery);
    }
    else
    {
        DBCollection userCollection = mongoDB.getCollection("SMRTUser");
        BasicDBObject update = new BasicDBObject();
        update.append("$set",new BasicDBObject()
                    .append("lastactivity","Staff-Appointment")
                    .append("last_login_date",new Date()));
        BasicDBObject search = new BasicDBObject();
        search.append("_id",userid);
        WriteResult result = userCollection.update(search,update);
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.append("$set",new BasicDBObject()
                .append("user_id",u.getId())
                .append("reserved_user_id",userid)
                .append("status", "reserved")
                .append("createdby", u.getUsertype())
                .append("eventabout",eventabout)
                );
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.append("_id",id);
        availabilityCollection.update(searchQuery,updateQuery);
    }
    }
    catch (Exception e){
    System.out.println(e.getMessage());
    throw new Exception("Error ");
    }
    finally{
          
    }
    return null;
    }
    @GET
    @Path("makeappointment")
    @Consumes({ MediaType.APPLICATION_JSON})
    public String makeappointment(@QueryParam("token") String token,@QueryParam("id") String caseid)  throws Exception{
    try {
    SMRTUser u=this.validateToken(token);
    DB mongoDB=this.getDBInstance();
    DBCollection availabilityCollection=mongoDB.getCollection("appointment");
    BasicDBObject condition;
    List<DBObject> aggQuery=Arrays.asList(
                (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"SMRTUser")
                        .append("localField","reserved_user_id")
                        .append("foreignField","_id")
                        .append("as","student")
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$student")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$match", 
                        new BasicDBObject("student._id",caseid)
                ),
                (DBObject) new BasicDBObject("$project", 
                        new BasicDBObject()
                        .append("_id","$student._id")
                        .append("firstname","$student.firstname")
                        .append("lastname","$student.lastname")
                        .append("email","$student.email")
                        .append("start","$start")
                        .append("end","$end")
                )
    );
    Iterable<DBObject> appointment = availabilityCollection.aggregate(aggQuery).results();
    Gson json = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
    return json.toJson(appointment);
    }
    catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
    } finally{
        //ToDO
    } 
   }
    @GET
    @Path("getupcomingappointments")
    @Produces({ MediaType.APPLICATION_JSON})
    public String getupcomingappointments(@QueryParam("token") String token)  throws Exception{
    try {
    SMRTUser u=this.validateToken(token);
    DB mongoDB=this.getDBInstance();
    DBCollection availabilityCollection=mongoDB.getCollection("appointment");
    BasicDBObject condition;
    List<DBObject> aggQuery=Arrays.asList(
                (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"SMRTUser")
                        .append("localField","reserved_user_id")
                        .append("foreignField","_id")
                        .append("as","appointments")
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$appointments")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"SMRTUser")
                        .append("localField","user_id")
                        .append("foreignField","_id")
                        .append("as","address")
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$address")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$address.location")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                 (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"Location")
                        .append("localField","address.location")
                        .append("foreignField","_id")
                        .append("as","locations")
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$locations")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$match", 
                        new BasicDBObject("reserved_user_id",u.getId())
                ),
                (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"SMRTUser")
                        .append("localField","user_id")
                        .append("foreignField","_id")
                        .append("as","appointmentwith")
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$appointmentwith")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$appointmentwith.roles")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$address.servicename")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"ServiceName")
                        .append("localField","address.servicename")
                        .append("foreignField","_id")
                        .append("as","service")
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$service")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$lookup", 
                        new BasicDBObject("from" ,"Location")
                        .append("localField","address.location")
                        .append("foreignField","_id")
                        .append("as","locations")
                ),
                (DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$locations")
                        .append("preserveNullAndEmptyArrays", true)
                ),
                (DBObject) new BasicDBObject("$match", 
                        new BasicDBObject("start",new BasicDBObject("$gte" ,new Date()))
                ),
                (DBObject) new BasicDBObject("$project", 
                        new BasicDBObject()
                        .append("_id","$_id")
                        .append("firstname","$appointmentwith.firstname")
                        .append("lastname","$appointmentwith.lastname")
                        .append("role","$appointmentwith.roles")
                        .append("email","$appointmentwith.email")
                        .append("phone","$appointmentwith.phonenumber")
                        .append("start","$start")
                        .append("end","$end")
                        .append("services","$service.servicename")
                        .append("location","$locations.location")
                        .append("createdby","$createdby")
                        .append("address","$locations.address")
                ),
                (DBObject)new BasicDBObject("$sort", new BasicDBObject("start",1))
    );
    Iterable<DBObject> appointments = availabilityCollection.aggregate(aggQuery).results();
    Gson json = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
    return json.toJson(appointments);
    }
    catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
    } finally{
        //ToDO
    } 
   }
    @PUT
    @Path("updateevent")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String updateevent(@FormParam("starttime") String start,
            @FormParam("endtime") String end,
            @FormParam("timeslot") String timeslot,
            @FormParam("repeats") String repeats,
            @FormParam("reserveid") String reserveid,
            @FormParam("status") String status,
            @FormParam("eventid") String eventid,
            //@FormParam("eventfor") String eventfor,
            @FormParam("eventabout") String eventabout,
            @FormParam("eventtitle") String eventtitle,
            @QueryParam("token") String token)throws Exception{
    try {
    SMRTUser u=this.validateToken(token);
    DB mongoDB=this.getDBInstance();
    DBCollection appointmentCollection=mongoDB.getCollection("appointment");
    BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.append("$set",new BasicDBObject()
                .append("user_id",u.getId())
                .append("reserved_user_id",reserveid)
                .append("status", status)
                .append("createdby", u.getUsertype())
                .append("eventabout",eventabout)
                .append("reserved_user_id",reserveid)
                //.append("eventfor",eventfor)
                .append("eventtitle",eventtitle)
//                .append("start",start)
//                .append("end",end)
                );
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.append("_id",eventid);
        appointmentCollection.update(searchQuery,updateQuery);
    }
    catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
    } finally{
        //ToDO
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
