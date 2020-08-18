/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.service;

import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.WriteResult;
import com.smrtsolutions.exception.ForbiddenException;
import com.smrtsolutions.exception.InvalidParameterException;
import com.smrtsolutions.survey.model.Customer;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.survey.model.Survey;
import com.smrtsolutions.survey.model.SurveyResult;
import com.smrtsolutions.survey.model.SurveySection;
import com.smrtsolutions.survey.model.SurveySectionQuestion;

import com.smrtsolutions.util.Util;
import java.io.File;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import static org.apache.log4j.config.PropertyPrinter.capitalize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
/**
 *
 * @author lenny
 */
//@Stateless
@Path("/survey/{surveyId}/result")
public class SurveyResultREST extends SMRTAbstractFacade<SurveyResult>{
    
        
    private static final Logger logger = LogManager.getLogger(SurveyResultREST.class);
    public String[] REQUIRED_PERMISSIONS = {SMRTRole.SELF_DATA_ADD,SMRTRole.SELF_DATA_EDIT,SMRTRole.OTHERS_DATA_ADD,SMRTRole.OTHERS_DATA_EDIT};
    Map<String, String> hmap = new HashMap<String, String>();
    Map<String, String> hmapVal = new HashMap<String, String>();
    //@PersistenceProperty(name="test", value="")
    //@PersistenceContext(unitName = "SMRT_PU")
   // private EntityManager em;

    public SurveyResultREST() {
        super(SurveyResult.class);
        //super.setTenantId(customerId);
    }
    


    public void log(String message){
         logger.debug(message);
    }
    
    public void error(String message, Exception e){
         logger.error(message, e);
    }
    
    @Override
    public String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }
    
    @POST
    @Path("validateSection")
    @Consumes("application/x-www-form-urlencoded")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String validateSection( @PathParam("surveyId") String surveyId, @QueryParam("token") String token
            , @QueryParam("participant") String participantId
            , @QueryParam("type") String type
            , @FormParam("validateSectionName") String validateSectionName
            , @FormParam("validateSectionStatus") int validateSectionStatus
    )  throws Exception{
        try {
            SMRTUser user = this.validateToken(token, null);
            String customerId = user.getCustomerId();
            this.setCustomerId(customerId);
            
            this.log("validate section ***");
            //Customer c= this.findCustomer();
            Survey s = this.findSurvey(surveyId);
            String pt = s.getSetting(Survey.PARTICIPANT_TYPE_TAG, Survey.PARTICIPANT_TYPE_USER);
            String uid = "";
            this.getEntityManager().getTransaction().begin();
            if ( Survey.PARTICIPANT_TYPE_USER.equalsIgnoreCase(pt)){
                uid = user.getId();
                if ( !Util.isEmpty(participantId) && !uid.equals(participantId)) {
                    //check if the user has permission
                    if ( !this.hasPermission(user, SMRTRole.OTHERS_DATA_SEARCH) && !this.hasPermission(user, SMRTRole.CASE_DASH)){
                       throw new ForbiddenException("You do not have privilleges to view data of other participants");
                    }
                    System.out.println("I am here 1");
                    SMRTUser p = this.findUser(participantId);
                    uid = p.getId();
                }
            }
            
            DB mongoDB = this.getDBInstance();
            DBCollection surveyResultsCollection = mongoDB.getCollection("survey_results");
            
            BasicDBObject query = new BasicDBObject("participantId",participantId);
            BasicDBObject fields = new BasicDBObject("validationStatus",1);
            DBObject result = surveyResultsCollection.findOne(query, fields);
            
            BasicDBList sectionValidationStatus = null;
            
            if(result != null){
                sectionValidationStatus = (BasicDBList) result.get("validationStatus");
            }
            
            if(sectionValidationStatus == null)
                sectionValidationStatus = new BasicDBList();
            
            BasicDBObject validationStatus = null;
            
            for(int i=0; i< sectionValidationStatus.size(); i++){
              BasicDBObject data = ((BasicDBObject)sectionValidationStatus.get(i));
              if(data.get("sectionName").equals(validateSectionName)){
                  validationStatus = (BasicDBObject) data.clone();
                  sectionValidationStatus.remove(i);
                  break;
              }
            }
            
            if(validationStatus == null){
                validationStatus = new BasicDBObject();
            }
            
            validationStatus.put("validatedByUserId", user.getId());
            validationStatus.put("validatedByUserName", user.getName());
            validationStatus.put("sectionName", validateSectionName);
            validationStatus.put("validateTime", new Date());
            validationStatus.put("status", validateSectionStatus);
            
            sectionValidationStatus.add(validationStatus);
            WriteResult res = surveyResultsCollection.update(query, new BasicDBObject("$set", new BasicDBObject("validationStatus",sectionValidationStatus)), true, false);
            
            return this.latest(surveyId, token, participantId,type );

         } catch (Exception e){
            error("latest failed.", e);
            //throw new Exception("APP ERROR", e);
            throw e;
        }
    }
    @POST
    @Path("managementvalidateSection")
    @Consumes("application/x-www-form-urlencoded")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String managementvalidateSection( @PathParam("surveyId") String surveyId, @QueryParam("token") String token
            , @QueryParam("participant") String participantId
            , @FormParam("validateSectionName") String validateSectionName
            , @FormParam("validateSectionStatus") int validateSectionStatus
    )  throws Exception{
        try {
            SMRTUser user = this.validateToken(token, null);
            String customerId = user.getCustomerId();
            this.setCustomerId(customerId);
            
            this.log("management validate section ***");
            //Customer c= this.findCustomer();
            Survey s = this.findSurvey(surveyId);
            String pt = s.getSetting(Survey.PARTICIPANT_TYPE_TAG, Survey.PARTICIPANT_TYPE_USER);
            String uid = "";
            this.getEntityManager().getTransaction().begin();
//            if ( Survey.PARTICIPANT_TYPE_USER.equalsIgnoreCase(pt)){
//                uid = user.getId();
//                if ( !Util.isEmpty(participantId) && !uid.equals(participantId)) {
//                    //check if the user has permission
//                    if ( !this.hasPermission(user, SMRTRole.OTHERS_DATA_SEARCH) && !this.hasPermission(user, SMRTRole.CASE_DASH)){
//                       throw new ForbiddenException("You do not have privilleges to view data of other participants");
//                    }
//                    System.out.println("I am here 1");
//                    SMRTUser p = this.findUser(participantId);
//                    uid = p.getId();
//                }
//            }
            
            DB mongoDB = this.getDBInstance();
            DBCollection surveyResultsCollection = mongoDB.getCollection("survey_results");
            
            BasicDBObject query = new BasicDBObject("participantId","management");
            BasicDBObject fields = new BasicDBObject("validationStatus",1);
            DBObject result = surveyResultsCollection.findOne(query, fields);
            
            BasicDBList sectionValidationStatus = null;
            
            if(result != null){
                sectionValidationStatus = (BasicDBList) result.get("validationStatus");
            }
            
            if(sectionValidationStatus == null)
                sectionValidationStatus = new BasicDBList();
            
            BasicDBObject validationStatus = null;
            
            for(int i=0; i< sectionValidationStatus.size(); i++){
              BasicDBObject data = ((BasicDBObject)sectionValidationStatus.get(i));
              if(data.get("sectionName").equals(validateSectionName)){
                  validationStatus = (BasicDBObject) data.clone();
                  sectionValidationStatus.remove(i);
                  break;
              }
            }
            
            if(validationStatus == null){
                validationStatus = new BasicDBObject();
            }
            
            validationStatus.put("validatedByUserId", user.getId());
            validationStatus.put("validatedByUserName", user.getName());
            validationStatus.put("sectionName", validateSectionName);
            validationStatus.put("validateTime", new Date());
            validationStatus.put("status", validateSectionStatus);
            
            sectionValidationStatus.add(validationStatus);
            WriteResult res = surveyResultsCollection.update(query, new BasicDBObject("$set", new BasicDBObject("validationStatus",sectionValidationStatus)), true, false);
            
            return this.managementlatest(surveyId, token);

         } catch (Exception e){
            error("latest failed.", e);
            //throw new Exception("APP ERROR", e);
            throw e;
        }
    }
    @GET
    @Path("latest")
    @Produces({MediaType.APPLICATION_JSON})
    public String latest( @PathParam("surveyId") String surveyId, @QueryParam("token") String token
            , @QueryParam("participant") String participantId,@QueryParam("type") String type
    )  throws Exception{
        try {
            SMRTUser user = this.validateToken(token, null);
            String customerId = user.getCustomerId();
            this.setCustomerId(customerId);
            
            this.log("Latest ***");
            //Customer c= this.findCustomer();
            Survey s = this.findSurvey(surveyId);
            String pt = s.getSetting(Survey.PARTICIPANT_TYPE_TAG, Survey.PARTICIPANT_TYPE_USER);
            String uid = "";
            if ( Survey.PARTICIPANT_TYPE_USER.equalsIgnoreCase(pt)){
                uid = user.getId();
                if ( !Util.isEmpty(participantId) && !uid.equals(participantId)) {
                    //check if the user has permission
                    if ( !this.hasPermission(user, SMRTRole.OTHERS_DATA_SEARCH) && !this.hasPermission(user, SMRTRole.CASE_DASH)){
                       throw new ForbiddenException("You do not have privilleges to view data of other participants");
                    }
                    SMRTUser p = this.findUser(participantId);
                    uid = p.getId();
                }
            }
            
            //todo create collection to get survey keys
            //get current survey keys
            List<String> keys = new ArrayList<>();
            for(SurveySection section: s.getSections()){
                for(SurveySectionQuestion question: section.getQuestions()){
                    keys.add(question.getName());
                }
            }
            
            DB mongoDB = this.getDBInstance();
            DBCollection surveyResultsCollection = mongoDB.getCollection("survey_results");
            
            Iterable<DBObject> result = null;
            
            Gson json = new Gson();
            
            String keysString = json.toJson(keys);
            
            String map = "function () {" +
                    "var keys = "+ keysString + ";" +
                    "var surveyId= '"+ s.getId() +"';" +
                    "var content = [];" +
                    "keys.forEach(key=>{" +
                        "if(this[key]){" +
                            "content.push({name:key, value: this[key].val});" +
                        "}" +
                    "});" +
                    "emit(this._id, {id:this._id.str, customerId: this.customerId, participantId: this.participantId, content: content, validationStatus:this.validationStatus, status:NumberInt(this.surveyStatus[surveyId]||-1)});" +
                "}";
            
            //Not required since we are going to emit only one record
            String reduce = "function (key, values) {" +
                    "	return {values: values};" +
                    "}";
            
            BasicDBObject fields = new BasicDBObject();
            fields.put("customerId", user.getCustomerId());
            fields.put("participantId", uid);
            
            MapReduceCommand cmd = new MapReduceCommand(surveyResultsCollection, map, reduce,
                             null, MapReduceCommand.OutputType.INLINE, fields);
            result = surveyResultsCollection.mapReduce(cmd).results();
            
            // set activity for user
            DBCollection lastactivityCollection = mongoDB.getCollection("activities");
            BasicDBObject update =  new BasicDBObject();
            
            if(type != null){
            if(type.equals("view") || type.equals("Start")){
                if(user.getUsertype().equals("casemanager")){
                    if(type.equals("Start")){
                        update.put("activity","Staff - Form; Start");
                    }
                    else{
                        update.put("activity","Staff - Form; View");
                    }
                }
                if(user.getUsertype().equals("student")){
                    participantId = user.getId();
                    if(type.equals("Start")){
                        update.put("activity","Client - Form; Start");
                    }else{
                        update.put("activity","Client - Form; View");
                    }
                    
                }
                update.put("kind","n/a");
                update.put("createdBy",user.getId());
                update.put("createduname",user.getLastname()+", "+user.getFirstname());
                update.put("createdOn",new Date());
                update.put("surveyId",surveyId);
                update.put("customerId",user.getCustomerId());
                update.put("activityfor",participantId);
                update.put("detail",s.getLabel());
                update.put("information","n/a");
                lastactivityCollection.insert(update);
            
            }
            }
            if(result.iterator().hasNext()){
                BasicDBObject resultData = (BasicDBObject) result.iterator().next().get("value");
                if(((int)resultData.get("status")) == -1){ //To check if Survey already open, -1 if not open
                    resultData.put("status", 2);
                    
                    //To Set In Progress Status for Survey
                    BasicDBObject data = new BasicDBObject(
                        "$set",
                        new BasicDBObject("resultTime", new Date())
                        .append("surveyStatus."+s.getId(), SurveyResult.STATUS.IN_PROGRESS.ordinal())
                        .append("surveyUpdated."+s.getId(), new Date())
                    ).append(
                        "$setOnInsert",
                        new BasicDBObject("participantId", uid)
                        .append("customerId",user.getCustomerId())
                        .append("createdBy", user.getId())
                    );
                    
                    surveyResultsCollection.update(fields, data, true, false);
                }
                return resultData.toString();
            }
            //To Set Open Status for Survey
            BasicDBObject data = new BasicDBObject(
                "$set",
                new BasicDBObject("resultTime", new Date())
                .append("surveyStatus."+s.getId(), SurveyResult.STATUS.IN_PROGRESS.ordinal())
                .append("surveyUpdated."+s.getId(), new Date())
            ).append(
                "$setOnInsert",
                new BasicDBObject("participantId", uid)
                .append("customerId",user.getCustomerId())
                .append("createdBy", user.getId())
            );
            
            WriteResult res = surveyResultsCollection.update(fields, data, true, false);
            
            if(res.getN()>0){ //make recursive call to result from inserted data
                return this.latest(surveyId, token, participantId,type);
            }
            return "{}";
            
         } catch (Exception e){
            error("latest failed.", e);
            //throw new Exception("APP ERROR", e);
            throw e;
        }
    }
    
//    @GET
//    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
//    public List<SurveyResult> listResults( @PathParam("surveyId") String surveyId, @QueryParam("token") String token
//            , @QueryParam("participant") String participantId
//            , @QueryParam("from") int from, @QueryParam("to") int to 
//    )  throws Exception{
//        try {
//            SMRTUser user = this.validateToken(token, null);
//            String customerId = user.getCustomerId();
//            this.setCustomerId(customerId);
//            
//            this.log("Latest ***");
//            //Customer c= this.findCustomer();
//            Survey s = this.findSurvey(surveyId);
//            String pt = s.getSetting(Survey.PARTICIPANT_TYPE_TAG, Survey.PARTICIPANT_TYPE_NONE);
//            String uid = "";
//            if ( Survey.PARTICIPANT_TYPE_USER.equalsIgnoreCase(pt)){
//                uid = user.getId();
//                if ( !Util.isEmpty(participantId) && !uid.equals(participantId)) {
//                    //check if the user has permission
//                    if ( !this.hasPermission(user, SMRTRole.OTHERS_DATA_SEARCH)){
//                       throw new ForbiddenException("You do not have privilleges to view data of other participants");
//                    }
//                    SMRTUser p = this.findUser(participantId);
//                    uid = p.getId();
//                }
//            }
//            return this.getSurveyResultList(s, user, uid);
//         } catch (Exception e){
//            error("latest failed.", e);
//            //throw new Exception("APP ERROR", e);
//            throw e;
//        }
//    }
    /*
    @GET
    @Path("{id}/status")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public NameValuePair getStatus(@QueryParam("token") String token, @PathParam("id") String sid
            , @QueryParam("participantId") String participantId)  throws Exception{
        SMRTUser u = this.validateToken(token);
        this.checkPermissions(REQUIRED_PERMISSIONS);
        Customer c = this.findCustomer(u);
        this.setCustomerId(c.getId());
        System.out.println("surveyID=" + sid);
        if ( Util.isEmpty(sid)) throw new InvalidParameterException("Survey Id is required");
        Survey s = this.findSurvey(sid);
        //s = this.calculateAvailableStates(s, u, participantId);
        
        if ( Util.isEmpty(participantId)) throw new InvalidParameterException("ParticipantId is required");
        String flow = s.getSetting(Survey.FLOW_TAG, "");
        SMRTUser p = null;
        SurveyResult sr = null;
        if ( Survey.FLOW_CONTROLLED.equalsIgnoreCase(flow)){
            //TODO handle periodic controlled
            String pid = participantId;
            String controlflow = s.getSetting(Survey.FLOW_CONTROL_TAG, Survey.FLOW_CONTROLLED_ALL_PARTICIPANTS);
            if ( Survey.FLOW_CONTROLLED_ALL_PARTICIPANTS.equalsIgnoreCase(controlflow)){
                pid = "_ALL_";
                if ( !participantId.equals("_ALL_")){
                    pid = participantId; // looking for a specific particiant's status
                    p = this.findUser(pid); //validate the participant user
                }
                
            } else if (Survey.FLOW_CONTROLLED_PARTICIPANT.equalsIgnoreCase(controlflow)) {
                p = this.findUser(pid); //validate the participant user
                
            }
             sr = this.getLatestSurveyResult(s, u, pid);
            sr.getStatus();
        } else {
            sr = this.getLatestSurveyResult(s, u, participantId);
        }
        
        return new NameValuePair(sr.getStatus()+"",  s.getStatusDescription());
    }
    */
    @GET
    @Path("management/latest")
    @Produces({MediaType.APPLICATION_JSON})
    public String managementlatest( @PathParam("surveyId") String surveyId, @QueryParam("token") String token
    )  throws Exception{
        try {
            SMRTUser user = this.validateToken(token, null);
            String customerId = user.getCustomerId();
            this.setCustomerId(customerId);
            //if(isEmpty(participantId))participantId=user.getCustomerId();
            this.log("Latest ***");
            //Customer c= this.findCustomer();
            Survey s = this.findSurvey(surveyId);
            String pt = s.getSetting(Survey.PARTICIPANT_TYPE_TAG, Survey.PARTICIPANT_TYPE_USER);
            String uid = "";
//            if ( Survey.PARTICIPANT_TYPE_USER.equalsIgnoreCase(pt)){
//                uid = user.getId();
//                if ( !Util.isEmpty(participantId) && !uid.equals(participantId)) {
//                    //check if the user has permission
//                    if ( !this.hasPermission(user, SMRTRole.OTHERS_DATA_SEARCH) && !this.hasPermission(user, SMRTRole.CASE_DASH)){
//                       throw new ForbiddenException("You do not have privilleges to view data of other participants");
//                    }
//                    SMRTUser p = this.findUser(participantId);
//                    uid = p.getId();
//                }
//            }
            
            //todo create collection to get survey keys
            //get current survey keys
            List<String> keys = new ArrayList<>();
            for(SurveySection section: s.getSections()){
                for(SurveySectionQuestion question: section.getQuestions()){
                    keys.add(question.getName());
                }
            }
            
            DB mongoDB = this.getDBInstance();
            DBCollection surveyResultsCollection = mongoDB.getCollection("survey_results");
            
            Iterable<DBObject> result = null;
            
            Gson json = new Gson();
            
            String keysString = json.toJson(keys);
            
            String map = "function () {" +
                    "var keys = "+ keysString + ";" +
                    "var surveyId= '"+ s.getId() +"';" +
                    "var content = [];" +
                    "keys.forEach(key=>{" +
                        "if(this[key]){" +
                            "content.push({name:key, value: this[key].val});" +
                        "}" +
                    "});" +
                    "emit(this._id, {id:this._id.str, customerId: this.customerId, participantId: this.participantId, content: content, validationStatus:this.validationStatus, status:NumberInt(this.surveyStatus[surveyId]||-1)});" +
                "}";
            
            //Not required since we are going to emit only one record
            String reduce = "function (key, values) {" +
                    "	return {values: values};" +
                    "}";
            
            BasicDBObject fields = new BasicDBObject();
            fields.put("customerId", user.getCustomerId());
            fields.put("participantId", "management");
            
            MapReduceCommand cmd = new MapReduceCommand(surveyResultsCollection, map, reduce,
                             null, MapReduceCommand.OutputType.INLINE, fields);
            result = surveyResultsCollection.mapReduce(cmd).results();
            
            
            if(result.iterator().hasNext()){
                BasicDBObject resultData = (BasicDBObject) result.iterator().next().get("value");
                if(((int)resultData.get("status")) == -1){ //To check if Survey already open, -1 if not open
                    resultData.put("status", 2);
                    
                    //To Set In Progress Status for Survey
                    BasicDBObject data = new BasicDBObject(
                        "$set",
                        new BasicDBObject("resultTime", new Date())
                        .append("surveyStatus."+s.getId(), SurveyResult.STATUS.IN_PROGRESS.ordinal())
                        .append("surveyUpdated."+s.getId(), new Date())
                    ).append(
                        "$setOnInsert",
                        new BasicDBObject("participantId", "management")
                        .append("customerId",user.getCustomerId())
                        .append("createdBy", user.getId())
                    );
                    
                    surveyResultsCollection.update(fields, data, true, false);
                }
                return resultData.toString();
            }
            
            
            //To Set Open Status for Survey
            BasicDBObject data = new BasicDBObject(
                "$set",
                new BasicDBObject("resultTime", new Date())
                .append("surveyStatus."+s.getId(), SurveyResult.STATUS.IN_PROGRESS.ordinal())
                .append("surveyUpdated."+s.getId(), new Date())
            ).append(
                "$setOnInsert",
                new BasicDBObject("participantId", "management")
                .append("customerId",user.getCustomerId())
                .append("createdBy", user.getId())
            );
            
            WriteResult res = surveyResultsCollection.update(fields, data, true, false);
            
            if(res.getN()>0){ //make recursive call to result from inserted data
                return this.managementlatest(surveyId, token);
            }
            return "{}";
            
         } catch (Exception e){
            error("latest failed.", e);
            //throw new Exception("APP ERROR", e);
            throw e;
        }
    }
    @PUT
    @Consumes("application/x-www-form-urlencoded")
    @Path ("management/complete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    @Path("{id}/complete")
    public Survey managementcomplete(@QueryParam("token") String token, @PathParam("surveyId") String surveyId
            , @FormParam("participantId") String participantId
            , @FormParam("surveyResultStatus") String surveyResultStatus
    ) throws Exception {
        log("*** Submit Survey ***");
        SMRTUser user = this.validateToken(token, null);
        String customerId = user.getCustomerId();
        this.setCustomerId(customerId);
        //if(isEmpty(participantId))participantId=user.getCustomerId();
//        if ( !Util.isEmpty(participantId)) {
//            //check if user has permission to save participants data
//            
//            if ( this.hasPermission(user, SMRTRole.OTHERS_DATA_ADD ) || this.hasPermission(user, SMRTRole.OTHERS_DATA_EDIT )) { 
//                
//            } else if ( !participantId.equalsIgnoreCase(user.getId())) {
//                throw new InvalidParameterException("You do not have priviliges to add data for the participant");
//            }
//        } else {
            participantId = "management";
//        }
        
        //update status
        int status = SurveyResult.STATUS.COMPLETE.ordinal();
        if(!Util.isEmpty(surveyResultStatus)){
            status = Integer.parseInt(surveyResultStatus);
        }
        DB mongoDB = this.getDBInstance();
        DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");
        BasicDBObject query = new BasicDBObject();
        query.put("participantId", participantId);
        query.put("customerId", user.getCustomerId());
        
        DBCollection surveyCollection = mongoDB.getCollection("survey");
        BasicDBObject isDefaultSurvey = (BasicDBObject) surveyCollection.findOne(new BasicDBObject("_id", surveyId), new BasicDBObject("isDefault",1));
        
        
        BasicDBObject updateSet = new BasicDBObject("resultTime", new Date())
            .append("completedDate."+surveyId, new Date())
            .append("surveyStatus."+surveyId, status)
            .append("surveyUpdated."+surveyId, new Date());
                
                
        //check if survey is default
        if(isDefaultSurvey!=null && isDefaultSurvey.getString("isDefault").equals("Yes")){
            BasicDBObject surevyResult =  (BasicDBObject) surveyResultCollection.findOne(query, new BasicDBObject("st_st_06",1));
            if(surevyResult!=null && surevyResult.get("st_st_06")==null || ((BasicDBObject)surevyResult.get("st_st_06")).get("val")==null){
                
                String currDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
                updateSet.append("st_st_06",new BasicDBObject("val",currDate).append("txt",currDate));
            }
        }
        
        BasicDBObject data = new BasicDBObject(
            "$set",
            updateSet
        );
                    
        surveyResultCollection.update(query, data, true, false);
        
        return this.findSurvey(surveyId);
    }
    @PUT
    @Consumes("application/x-www-form-urlencoded")
    @Path ("{resultId}/complete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    @Path("{id}/complete")
    public Survey complete(@QueryParam("token") String token, @PathParam("surveyId") String surveyId
            , @PathParam("resultId") String resultId
            , @FormParam("participantId") String participantId
            , @FormParam("surveyResultStatus") String surveyResultStatus
            
    ) throws Exception {
        log("*** Submit Survey ***");
        SMRTUser user = this.validateToken(token, null);
        String customerId = user.getCustomerId();
        this.setCustomerId(customerId);
        
        if ( !Util.isEmpty(participantId)) {
            //check if user has permission to save participants data
            
            if ( this.hasPermission(user, SMRTRole.OTHERS_DATA_ADD ) || this.hasPermission(user, SMRTRole.OTHERS_DATA_EDIT )) { 
                
            } else if ( !participantId.equalsIgnoreCase(user.getId())) {
                throw new InvalidParameterException("You do not have priviliges to add data for the participant");
            }
        } 
            
        
        
        //update status
        int status = SurveyResult.STATUS.COMPLETE.ordinal();
        if(!Util.isEmpty(surveyResultStatus)){
            status = Integer.parseInt(surveyResultStatus);
        }
        DB mongoDB = this.getDBInstance();
        DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");
        DBCollection userCollection = mongoDB.getCollection("SMRTUser");
        BasicDBObject search = new BasicDBObject();
        BasicDBObject update = new BasicDBObject();
        if(user.getUsertype().equals("student")){
        update.append("$set",new BasicDBObject()
                    .append("lastactivity","Client-Form")
                    .append("last_login_date",new Date()));
        search.append("_id",user.getId());
        WriteResult result = userCollection.update(search,update);
        }
        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(resultId));
        query.put("customerId", user.getCustomerId());
        
        DBCollection surveyCollection = mongoDB.getCollection("survey");
        BasicDBObject isDefaultSurvey = (BasicDBObject) surveyCollection.findOne(new BasicDBObject("_id", surveyId), new BasicDBObject("isDefault",1));
        BasicDBObject recruitmentSurvey = (BasicDBObject) surveyCollection.findOne(new BasicDBObject("_id",surveyId),new BasicDBObject("recruitment",1));
        BasicDBObject intakeSurvey = (BasicDBObject) surveyCollection.findOne(new BasicDBObject("_id",surveyId),new BasicDBObject("intake",1));
        
        BasicDBObject updateSet = new BasicDBObject("resultTime", new Date())
            .append("completedDate."+surveyId, new Date())
            .append("surveyStatus."+surveyId, status)
            .append("surveyUpdated."+surveyId, new Date());
                
                
        //check if survey is default
        if(isDefaultSurvey!=null && isDefaultSurvey.getString("isDefault").equals("Yes")){
            BasicDBObject surevyResult =  (BasicDBObject) surveyResultCollection.findOne(query, new BasicDBObject("st_st_06",1));
            if(surevyResult!=null && surevyResult.get("st_st_06")==null || ((BasicDBObject)surevyResult.get("st_st_06")).get("val")==null){
                
                String currDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
                updateSet.append("st_st_06",new BasicDBObject("val",currDate).append("txt",currDate));
            }
        }
        //check if recruitment is true
        if(isDefaultSurvey!=null && recruitmentSurvey.getString("recruitment").equals("Yes")){
            BasicDBObject surevyResult =  (BasicDBObject) surveyResultCollection.findOne(query, new BasicDBObject("st_st_06a",1));
            if(surevyResult!=null && surevyResult.get("st_st_06a")==null || ((BasicDBObject)surevyResult.get("st_st_06a")).get("val")==null){
                
                String currDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
                updateSet.append("st_st_06a",new BasicDBObject("val",currDate).append("txt",currDate));
            }
        }
        //check if intake is default
        if(isDefaultSurvey!=null && intakeSurvey.getString("intake").equals("Yes")){
            BasicDBObject surevyResult =  (BasicDBObject) surveyResultCollection.findOne(query, new BasicDBObject("st_st_06",1));
            if(surevyResult!=null && surevyResult.get("st_st_06")==null || ((BasicDBObject)surevyResult.get("st_st_06")).get("val")==null){
                
                String currDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
                updateSet.append("st_st_06",new BasicDBObject("val",currDate).append("txt",currDate));
            }
        }
        BasicDBObject data = new BasicDBObject(
            "$set",
            updateSet
        );
                    
        surveyResultCollection.update(query, data, true, false);
        // get survey
        Survey surveys = this.findSurvey(surveyId);
        // set activity for user
            DBCollection lastactivityCollection = mongoDB.getCollection("activities");
            BasicDBObject updateActivity =  new BasicDBObject();
            String activity = null;
            if(user.getUsertype().equals("student")){
                activity = "Client - Form; Complete";
                participantId = user.getId();
            }
            else{
                activity = "Staff - Form; Complete";
            }
            updateActivity.put("activity",activity);
                updateActivity.put("kind","n/a");
                updateActivity.put("createdBy",user.getId());
                updateActivity.put("createduname",user.getLastname()+", "+user.getFirstname());
                updateActivity.put("createdOn",new Date());
                update.put("surveyId",surveyId);
                updateActivity.put("customerId",user.getCustomerId());
                updateActivity.put("activityfor",participantId);
                updateActivity.put("detail",surveys.getLabel());
                updateActivity.put("information","n/a");
                lastactivityCollection.insert(updateActivity);
            
        return this.findSurvey(surveyId);
    }
    
//    
//    @PUT
//    @Consumes("application/x-www-form-urlencoded")
//    @Path ("{resultId}/validate")
////    @Path("{id}/complete")
//    public SurveyResult validatonStatusUpdate(@QueryParam("token") String token, @PathParam("surveyId") String surveyId
//            , @PathParam("resultId") String resultId
//            , @FormParam("participantId") String participantId 
//    ) throws Exception {
//        log("*** Submit Survey ***");
//        SMRTUser user = this.validateToken(token, null);
//        String customerId = user.getCustomerId();
//        this.setCustomerId(customerId);
//        Customer c = this.findCustomer(user);
//        Survey s = this.findSurvey(surveyId);
//        
////        this.getEntityManager().getTransaction().begin();
//        //String participantId = user.getId(); //TODO get pid as a param
//        if ( !Util.isEmpty(participantId)) {
//            
//            //check if user has permission to save participants data
//            if ( this.hasPermission(user, SMRTRole.OTHERS_DATA_ADD ) || this.hasPermission(user, SMRTRole.OTHERS_DATA_EDIT )) { 
//                
//            } else if ( !participantId.equalsIgnoreCase(user.getId())) {
//                throw new InvalidParameterException("You do not have priviliges to add data for the participant");
//            }
//        } else {
//            participantId = user.getId();
//        }
//        
//        //update content
//        SurveyResult sr = this.findSurveyResult(resultId);
//        if ( sr == null){
//            throw new InvalidParameterException("Invalid result id="+ resultId);
//        }
////        sr.setStatus(SurveyResult.STATUS.IN_PROGRESS.ordinal());
//
//        
//        sr.setStatus(SurveyResult.STATUS.COMPLETE.ordinal());
//        System.out.println("Saving survey as " + SurveyResult.STATUS.COMPLETE.ordinal());
//        super.edit(sr);
//        return sr;
//    }
//    
//    
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON}) 
    @Path ("{resultId}/content/field/{keyName}")
    public String saveFieldData( 
             @PathParam("surveyId") String surveyId
            , @PathParam("keyName") String keyName
            , @FormParam("keyValue") String keyValue
            , @FormParam("participantId") String participantId 
            , @FormParam("token") String token
    ) throws Exception {
        log("*** Save FIELD value ***");
        System.out.println(keyName);
        SMRTUser user = this.validateToken(token, null);
        String customerId = user.getCustomerId();
        this.setCustomerId(customerId);
        Customer c = this.findCustomer(user);
        Survey s = this.findSurvey(surveyId);
        System.out.println("1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n"+participantId);
        this.getEntityManager().getTransaction().begin();
        //String participantId = user.getId(); //TODO get pid as a param
        if ( !Util.isEmpty(participantId)) {
            //check if user has permission to save participants data
            
            if ( this.hasPermission(user, SMRTRole.OTHERS_DATA_ADD ) || this.hasPermission(user, SMRTRole.OTHERS_DATA_EDIT )) { 
                
            } else if ( !participantId.equalsIgnoreCase(user.getId())) {
                throw new InvalidParameterException("You do not have priviliges to add data for the participant");
            }
        } else {
            participantId = user.getId();
        }
        
        //set PSS status to in progress
        /*
        ParticipantSurveyStatus pss = this.findParticipantSurveyStatus(customerId, surveyId, participantId);
        if ( pss == null) {
            pss = new ParticipantSurveyStatus();
            pss.setCustomerId(customerId);
            pss.setSurveyId(surveyId);
            pss.setParticipantId(participantId);
            pss.setStatus(SurveyResult.STATUS.IN_PROGRESS.ordinal());
            this.getEntityManager().persist(pss);
        } else {
            pss.setStatus(SurveyResult.STATUS.IN_PROGRESS.ordinal());
            this.getEntityManager().merge(pss);
        }*/
        
        
        SurveySectionQuestion q = this.findSurveyQuestion(s, keyName);
        if ( q == null ){ // invalid question
            throw new InvalidParameterException("Invalid question key="+ keyName);
        }
        //update content
        
        DB mongoDB = this.getDBInstance();
        DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");
        
        BasicDBObject query = new BasicDBObject();
        query.put("participantId", participantId);
        query.put("customerId", user.getCustomerId());
        BasicDBObject data = new BasicDBObject();
        data.put("val", keyValue);
        data.put("txt", keyValue);
        
        if(q.getOptions() != null && q.getOptions().size() > 0 && keyValue!= null && !keyValue.isEmpty()){
            int idx = q.getOptions().indexOf(keyValue);
            if(idx>=0){
                //here we assue Options and Lables size are equal, so let it throw error for array out of bounds
                data.put("txt", q.getOptionLabels().get(idx));
            }else if(q.getType().equalsIgnoreCase("checkbox")){
                
                //checkbox selected
                
                String[] keys = keyValue.split(",");
                List<String> values = new ArrayList<>();
                
                for(String key:keys){
                    idx = q.getOptions().indexOf(key);
                    if(idx>=0){
                        values.add(q.getOptionLabels().get(idx));
                    }
                }
                
                if(values.size()>0){
                    data.put("txt", String.join(",", values));
                }
                
            }
        }
        if(keyName.equals("s1_req_1") || keyName.equals("s1_req_2")){
            DBCollection userCollection = mongoDB.getCollection("SMRTUser");
            String key = keyName.equals("s1_req_1")? "firstname":"lastname";
            userCollection.update(new BasicDBObject("_id", participantId), new BasicDBObject("$set", new BasicDBObject(key, capitalize(keyValue))));
            
            data.put("txt", capitalize(keyValue));
            data.put("val", capitalize(keyValue));
        }
        
        if(keyName.equals("s1_req_9")){
            DBCollection userCollection = mongoDB.getCollection("SMRTUser");
            userCollection.update(new BasicDBObject("_id", participantId), new BasicDBObject("$set", new BasicDBObject("email", keyValue)
                    .append("loginName", keyValue)
            ));
        }
        if(keyName.equals("s1_req_3")){
            DBCollection userCollection = mongoDB.getCollection("SMRTUser");
            userCollection.update(new BasicDBObject("_id", participantId), new BasicDBObject("$set", new BasicDBObject("address", keyValue)));
        }
        if(keyName.equals("s1_req_7")){
            DBCollection userCollection = mongoDB.getCollection("SMRTUser");
            userCollection.update(new BasicDBObject("_id", participantId), new BasicDBObject("$set", new BasicDBObject("phonenumber", keyValue)));
        }
        BasicDBObject surveyStart=new BasicDBObject();
        Map<Integer,Set<String>> map = new HashMap<Integer,Set<String>>();
        Object d=new Object();
        if(s.getStatus()=='1'|| s.getStatus()==0)
        {
            d=new Date();
        }
        else
        {
           d="";
        }
        data = new BasicDBObject(
                    "$set",
                    new BasicDBObject(keyName, data)
                    .append("resultTime", new Date())
                    .append("surveyUpdated."+s.getId(), new Date())
                    .append("surveyStarted."+s.getId(),d)
                ).append(
                    "$setOnInsert",
                    new BasicDBObject("participantId", participantId)
                    .append("customerId",user.getCustomerId())
                    .append("createdBy", user.getId())
                );
        surveyResultCollection.update(query, data, true, false);
        System.out.println(keyName);
        
        //
        //Apply post save action
        //todo
        this.applyPostSaveAction(c, s, q, keyValue, user, participantId);
        this.updatestatus(participantId,surveyId,user.getId());
        return new Gson().toJson("OK "+participantId);
       //return "ok";
    }

    @POST
    @Path("update")
    @Consumes("application/x-www-form-urlencoded")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON}) 
    public String updateField(
            @FormParam("token") String token,
            @FormParam("participantId") String participantId,
            @FormParam("keyName") String keyName,
            @FormParam("keyValue") String keyValueName
            ) throws Exception
    {
        SMRTUser user = this.validateToken(token, null);
        
        DB mongoDB = this.getDBInstance();
        DBCollection surveyResultsCollection = mongoDB.getCollection("survey_results");
        BasicDBObject query = new BasicDBObject("participantId",participantId);
        BasicDBObject data = new BasicDBObject();
        data.put("val", keyValueName);
        data.put("txt", keyValueName.equals("1")?"Yes":"No");
        WriteResult res = surveyResultsCollection.update(query, new BasicDBObject("$set", new BasicDBObject(keyName,data)).append("$setOnInsert",
            new BasicDBObject("participantId", participantId)
                .append("customerId",user.getCustomerId())
                .append("createdBy", user.getId())
        ), true, false);
        
        return "true";    
    }
    
    @POST
    @Path ("{resultId}/file/field/{keyName}")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public String uploadFile(
            @PathParam("resultId") String resultId,
            @PathParam("surveyId") String surveyId,
            @PathParam("keyName") String keyName,
            @Context HttpServletRequest req) 
            throws Exception {
        
        
        HashMap<String, String> formData = new HashMap<>();
        
        
     //   SMRTUser user = this.validateToken(token, null);
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
             String participantId = formData.get("participantId");
             
             SMRTUser user = this.validateToken(token, null);
             
             if(user.getUsertype().equals("student")){
                 participantId = user.getId();
             }
                          
             //set file path
             String filePath = Paths.get(System.getenv("SMRT_DOCS"),"Uploads","SurveyDocs", user.getCustomerId(), participantId).toString();
             File dir = new File(filePath);
             
             //check for directory exists or not and create 
             if(!dir.exists()){
                 dir.mkdirs();
             }
             
             if(fileItem != null){ //check if uploaded file was received
                String fileName = fileItem.getName();
                String fullPath = Paths.get(filePath, keyName+"__"+fileName).toString();
                File file = new File(fullPath);
                
                //save file to path
                fileItem.write(file);
                
                //get survey by surveyId
                Survey survey = this.findSurvey(surveyId);
                
                //get survey question
                SurveySectionQuestion question = this.findSurveyQuestion(survey, keyName);
                if ( question == null ){ // invalid question
                    throw new InvalidParameterException("Invalid question key="+ keyName);
                }
                
                DB mongoDB = this.getDBInstance();
                DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");

                BasicDBObject query = new BasicDBObject();
                query.put("participantId", participantId);
                query.put("customerId", user.getCustomerId());
                BasicDBObject data = new BasicDBObject();
                
                data.put(keyName, new BasicDBObject("val",keyName+"__"+fileName).append("txt",keyName+"__"+fileName));
                data.put(keyName.replaceFirst("_file", "_link"),new BasicDBObject("val",fileName).append("txt",fileName));

                data = new BasicDBObject(
                            "$set",
                            data
                            .append("resultTime", new Date())
                            .append("surveyUpdated."+survey.getId(), new Date())
                        ).append(
                            "$setOnInsert",
                            new BasicDBObject("participantId", participantId)
                            .append("customerId",user.getCustomerId())
                            .append("createdBy", user.getId())
                        );
                surveyResultCollection.update(query, data, true, false);
                
                return fileName;
             }else{
                 throw new InvalidParameterException( "Error: Uploaded file not received");
             }
        }
        throw new InvalidParameterException("Only Multipart Upload Supported");
    }
    
    @POST
    @Path ("{resultId}/file/remove/{keyName}")
    @Consumes("application/x-www-form-urlencoded")
    public String removeFile(
            @PathParam("resultId") String resultId,
            @PathParam("surveyId") String surveyId,
            @PathParam("keyName") String keyName,
            @FormParam("token") String token,
            @FormParam("participantId") String participantId,
            @Context HttpServletRequest req) 
            throws Exception {
        SMRTUser user = this.validateToken(token, null);
        
        //get survey by surveyId
        Survey survey = this.findSurvey(surveyId);
        
        //get survey question
        SurveySectionQuestion question = this.findSurveyQuestion(survey, keyName);
        if ( question == null ){ // invalid question
            throw new InvalidParameterException("Invalid question key="+ keyName);
        }
        
        DB mongoDB = this.getDBInstance();
        DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");

        BasicDBObject query = new BasicDBObject();
        query.put("participantId", participantId);
        query.put("customerId", user.getCustomerId());
        BasicDBObject data = new BasicDBObject();
        
         //clear upload file
        data.put(keyName+"_file", "");
        //clear upload file link
        data.put(keyName+"_link", "");

        data = new BasicDBObject(
                    "$set",
                    new BasicDBObject("resultTime", new Date())
                    .append("surveyUpdated."+survey.getId(), new Date())
                ).append(
                    "$unset",
                    data
                );
        
        //update to DB
        surveyResultCollection.update(query, data, true, false);

        return "OK";
    }
    
    @GET
    @Path ("{resultId}/file/download/{keyName}")
    @Produces("application/octet-stream")
    public Response downloadFile(
            @PathParam("resultId") String resultId,
            @PathParam("surveyId") String surveyId,
            @PathParam("keyName") String keyName,
            @QueryParam("token") String token,
            @QueryParam("participantId") String participantId,
            @Context HttpServletRequest req) 
            throws Exception {
        
        SMRTUser user = this.validateToken(token, null);
        
        if(user.getUsertype().equals("student")){
            participantId = user.getId();
        }
        
        //get survey by surveyId
        Survey survey = this.findSurvey(surveyId);
        
        //get survey question
        SurveySectionQuestion question = this.findSurveyQuestion(survey, keyName);
        if ( question == null ){ // invalid question
            throw new InvalidParameterException("Invalid question key="+ keyName);
        }
        
        //get survey result
        DB mongoDB = this.getDBInstance();
        DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");

        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(resultId));
        query.put("participantId", participantId);
        query.put("customerId", user.getCustomerId());
        
        BasicDBObject fields = new BasicDBObject(keyName, 1);
        
        BasicDBObject surveyResult = (BasicDBObject) surveyResultCollection.findOne(query, fields);
        
        //check if survey result exists
        if(surveyResult == null){
            throw new InvalidParameterException("File not found, no content for survey - "+ resultId);
        }
        //get key value from survey result
        surveyResult = (BasicDBObject) surveyResult.get(keyName);
        
        //check if survey key value exists
        if(surveyResult == null){
            throw new InvalidParameterException("File not found, no content for survey - "+ resultId);
        }
        
        String fileName = (String) surveyResult.get("val");
        String filePath = Paths.get(System.getenv("SMRT_DOCS"),"Uploads","SurveyDocuments", user.getCustomerId(), participantId, fileName).toString();

         File file = new File(filePath);

         //check if file exists
         if(!file.exists()){
             throw new InvalidParameterException("File not found - "+ keyName);
         }

         ResponseBuilder response = Response.ok((Object) file);
         response.header("Content-Disposition","attachment; filename=\""+ fileName.replace(keyName+"__", "") +"\"");
         return response.build();
    }
    
    
    protected void applyPostSaveAction(Customer c, Survey s, SurveySectionQuestion q, String value, SMRTUser u, String participantId) throws Exception{
        
        if ( !Util.isEmpty(q.getPostSaveAction())){
            
            try {
                this.getEntityManager().getTransaction().begin();
           
            //parse the action
            //METHOD:ARG1:ARG2:ARG3
            //method=disable_survey | enable_survey
            //SURVEY_NAME:TARGET_FIELD_VALUE
            String[] args = q.getPostSaveAction().split(":");
            if ( args.length < 3) throw new Exception("Invalid PostSaveAction on question "  + q.getName() + " format=METHOD:ARG1:ARG2");
            String action = Util.getValue(args[0]).trim();
            String sname = Util.getValue(args[1]).trim();
            String tval = Util.getValue(args[2]).trim();
            
            
            //find target survey
            
            Survey ts = this.findSurveyByName(c.getId(), sname);
            if ( ts == null) {
                throw new Exception("Invalid PostSaveAction on question "  + q.getName() + " survey=" + sname + " is not valid");
            }
            if ( "enable_survey".equalsIgnoreCase(action) || "disable_survey".equalsIgnoreCase(action) ) {
                
                int tstat = 0;
                if ( tval.equals(value)) {
                    if ( "disable_survey".equalsIgnoreCase(action) ) {
                        tstat = 0; //disable
                    } else {
                        tstat = 1;
                    }
                } else {
                    if ( "disable_survey".equalsIgnoreCase(action) ) {
                        tstat = 1; //enable
                    } else {
                        tstat = 0; //disable
                    }
                }
                
                DB mongoDB = this.getDBInstance();
                DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");

                BasicDBObject query = new BasicDBObject();
                query.put("participantId", participantId);
                BasicDBObject data = new BasicDBObject();
                data.put("resultTime", new Date());
                data.put("surveyUpdated."+s.getId(), new Date());
                data.put("surveyStatus."+s.getId(), tstat);
                data.put("participantId", participantId);
                surveyResultCollection.update(query,  new BasicDBObject("$set",data), true, false);
                
            } else {
                throw new Exception("Invalid PostSaveAction on question "  + q.getName() + " unknown action=" + action);
            }
            
          } catch (Exception e){
              if ( this.getEntityManager().isJoinedToTransaction()){
                  try {
                        this.getEntityManager().getTransaction().rollback();
                  } catch (Exception ig){
                      this.logger.error("Cannot roll back transaction in applyPostSaveAction", ig);
                  }
              }
                throw e;
         }  finally {
                
            }
        }
        
    }

    
}
