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
import com.mongodb.DBObject;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
@Path("/activities")
public class ActivitesREST extends SMRTAbstractFacade<SMRTUser> {
    private static final Logger logger = LogManager.getLogger(CalendarREST.class);
    public String[] REQUIRED_PERMISSIONS = {SMRTRole.NONE};
    Map<String, String> hmap = new HashMap<String, String>();
    public ActivitesREST() {
        super(SMRTUser.class);
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getActivity(@QueryParam("token") String token)  throws Exception{
        SMRTUser u=this.validateToken(token);
        DB mongoDB = this.getDBInstance();
        DBCollection activity = mongoDB.getCollection("lastactivity");
        BasicDBObject condition = new BasicDBObject("customerId",u.getCustomerId());
        DBCursor cursor = activity.find(condition).sort(new BasicDBObject("createdOn",1));
        Gson json = new Gson();
        return json.toJson(cursor.toArray());
    }
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public String getActivity(@QueryParam("token") String token,@PathParam("id") String id)  throws Exception{
        SMRTUser u=this.validateToken(token);
        DB mongoDB = this.getDBInstance();
        DBCollection activity = mongoDB.getCollection("activities");
        BasicDBObject condition = new BasicDBObject("activityfor",id);
        DBCursor cursor = activity.find(condition).sort(new BasicDBObject("createdOn",-1)); 
        Gson json = new Gson();
        return json.toJson(cursor.toArray());
    }
    @GET
    @Path("status")
    @Produces({MediaType.APPLICATION_JSON})
   public String getStatus(@QueryParam("token") String token,@QueryParam("id") String id)  throws Exception{
    SMRTUser u = this.validateToken(token);
    DB mongoDB=this.getDBInstance();
   DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");
   BasicDBObject surveyFields = new BasicDBObject();
   
   surveyFields.put("st_st_30", 1);
   surveyFields.put("st_st_31", 1);
   surveyFields.put("st_st_33", 1);
   surveyFields.put("st_st_23", 1);
   surveyFields.put("st_st_24", 1);
   surveyFields.put("st_st_26", 1);
   surveyFields.put("st_st_16", 1);
   surveyFields.put("st_st_17", 1);
   surveyFields.put("st_st_19", 1);
   surveyFields.put("st_st_05", 1);
   surveyFields.put("st_st_11", 1);
   surveyFields.put("st_st_10", 1);
   surveyFields.put("st_st_06", 1);
   surveyFields.put("st_st_06a", 1);
   surveyFields.put("st_st_06b", 1);
   surveyFields.put("st_st_38", 1);
   surveyFields.put("st_st_37", 1);
   surveyFields.put("st_st_40", 1);
   surveyFields.put("st_st_44", 1);
   surveyFields.put("st_st_45", 1);
   surveyFields.put("st_st_47", 1);
   surveyFields.put("st_st_13", 1);
   surveyFields.put("oc_ins_8", 1);
   surveyFields.put("isimport", 1);
   BasicDBObject statusquery = new BasicDBObject();
   statusquery.put("participantId", id);

   hmap = surveyFields.toMap();
   hmapVal = surveyFields.toMap();
   
   DBCursor cursor = surveyResultCollection.find(statusquery, surveyFields);
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
      hmapVal.put("_id",((ObjectId)( obj.get("_id"))).toString());
   }else{
       for(Map.Entry<String, String> entry :hmap.entrySet()){
           entry.setValue("");
           hmapVal.put(entry.getKey(),"");
       }
   }
                   //Status

       BasicDBObject status1 = new BasicDBObject();
       BasicDBObject status2 = new BasicDBObject();
       BasicDBObject status3 = new BasicDBObject();
       BasicDBObject status4 = new BasicDBObject();
       BasicDBObject status5 = new BasicDBObject();
       BasicDBObject initialstatus = new BasicDBObject();
       
        //Status Change 5 - status
        if (!hmap.get("st_st_45").isEmpty() && !hmap.get("st_st_44").isEmpty()) {
            if(hmapVal.get("st_st_44").equals("1")){
                status5.append("status", "Stop Out").append("date",hmap.get("st_st_45"));
            }else if(hmapVal.get("st_st_44").equals("2")){
                if(!hmapVal.get("st_st_47").isEmpty()){
                    if(hmapVal.get("st_st_47").equals("1")){
                        status5.append("status", "Exited Complete").append("date",hmap.get("st_st_45"));
                    }else{
                        status5.append("status", "Exited Not Complete").append("date",hmap.get("st_st_45"));
                    }
                }
            }else if(hmapVal.get("st_st_44").equals("3")){
                status5.append("status", "Enrolled").append("date",hmap.get("st_st_45"));
            }
        }
        //Status Change 4 - status
        if (!hmap.get("st_st_38").isEmpty() && !hmap.get("st_st_37").isEmpty()) {
            if(hmapVal.get("st_st_37").equals("1")){
                status4.append("status", "Stop Out").append("date",hmap.get("st_st_38"));
            }else if(hmapVal.get("st_st_37").equals("2")){
                if(!hmapVal.get("st_st_40").isEmpty()){
                    if(hmapVal.get("st_st_40").equals("1")){
                        status4.append("status", "Exited Complete").append("date",hmap.get("st_st_38"));
                    }else{
                        status4.append("status", "Exited Not Complete").append("date",hmap.get("st_st_38"));
                    }
                }
            }else if(hmapVal.get("st_st_37").equals("3")){
                status4.append("status", "Enrolled").append("date",hmap.get("st_st_38"));
            }
        }
       //Status Change 3 - status
       if (!hmap.get("st_st_31").isEmpty() && !hmap.get("st_st_30").isEmpty()) {
           if(hmapVal.get("st_st_30").equals("1")){
               status3.append("status","Stop Out").append("date",hmap.get("st_st_31"));
           }else if(hmapVal.get("st_st_30").equals("2")){
               if(!hmapVal.get("st_st_33").isEmpty()){
                   if(hmapVal.get("st_st_33").equals("1")){
                       status3.append("status","Exited Complete").append("date",hmap.get("st_st_31"));
                   }else{
                       status3.append("status","Exited Not Complete").append("date",hmap.get("st_st_31"));
                   }
               }
           }else if(hmapVal.get("st_st_30").equals("3")){
               status3.append("status","Enrolled").append("date",hmap.get("st_st_31"));
           }
       }

//       if(status3.equals(null)){
           //Status Change 2 - status
           if (!hmap.get("st_st_24").isEmpty() && !hmap.get("st_st_23").isEmpty()) {
               if(hmapVal.get("st_st_23").equals("1")){
                   status2.append("status","Stop Out").append("date",hmap.get("st_st_24"));
               }else if(hmapVal.get("st_st_23").equals("2")){
                   if(!hmapVal.get("st_st_26").isEmpty()){
                       if(hmapVal.get("st_st_26").equals("1")){
                            status2.append("status","Exited Complete").append("date",hmap.get("st_st_24"));
                       }else{
                           status2.append("status","Exited Not Complete").append("date",hmap.get("st_st_24"));
                       }
                   }
               }else if(hmapVal.get("st_st_23").equals("3")){
                   status2.append("status","Enrolled").append("date",hmap.get("st_st_24"));
               }
           }
//       }

//       if(status3.equals(null) && status2.equals(null)){
           
           //Status Change 1 - status
           if (!hmap.get("st_st_17").isEmpty() && !hmap.get("st_st_16").isEmpty()) {
               if(hmapVal.get("st_st_16").equals("1")){
                   status1.append("status","Stop Out").append("date",hmap.get("st_st_17"));
               }else if(hmapVal.get("st_st_16").equals("2")){
                   if(!hmapVal.get("st_st_19").isEmpty()){
                       if(hmapVal.get("st_st_19").equals("1")){
                            status1.append("status","Exited Complete").append("date",hmap.get("st_st_17"));
                       }else{
                           status1.append("status","Exited Not Complete").append("date",hmap.get("st_st_17"));
                       }
                   }
               }

           }
//       }
//       if(status1.equals(null) && status2.equals(null) && status3.equals(null)){
               initialstatus.append("Enrolling",new BasicDBObject("status","Enrolling").append("date",hmap.get("st_st_10")));
               initialstatus.append("Enrolled",new BasicDBObject("status","Enrolled").append("date",hmap.get("st_st_11")));
               initialstatus.append("ExitedEnrollment",new BasicDBObject("status","Exited Enrollment").append("date",hmap.get("st_st_13")));
               initialstatus.append("Recruitment",new BasicDBObject("status","Recruitment").append("date",hmap.get("st_st_06a")));               
               initialstatus.append("Import",new BasicDBObject("status","Import").append("date",hmap.get("st_st_06b")));
               initialstatus.append("Intake",new BasicDBObject("status","Intake").append("date",hmap.get("st_st_06")));
               
           
               
           
           DBObject surveyId = new BasicDBObject();
           DBCollection surveyCollection = mongoDB.getCollection("survey");
           BasicDBObject cond = new BasicDBObject();
           cond.append("customerId",u.getCustomerId()).append("survey_type","status");
           DBCursor survey = surveyCollection.find(cond);
            if( survey.hasNext() ){
                DBObject obj = survey.next();
                surveyId = obj;
            }    

       BasicDBObject document= new BasicDBObject();
       document.append("status",new BasicDBObject("status1",status1)
        .append("status2",status2).append("status3",status3)
        .append("status4",status4).append("status5",status5)
        .append("initialstatus",initialstatus).append("surveyResultId",hmapVal.get("_id"))
         .append("surveyId",surveyId.get("_id")));
       return new Gson().toJson(document);
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

