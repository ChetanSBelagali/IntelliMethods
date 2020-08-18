/*
 * SMRT Solutions
 * Data Collection Platform
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
import com.smrtsolutions.exception.InvalidParameterException;
import com.smrtsolutions.survey.model.NameValuePair;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.survey.model.Survey;
import com.smrtsolutions.survey.model.SurveyResult;
import com.smrtsolutions.survey.model.SurveySectionQuestion;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
 * @author Kriyatec - Santhosh
 */
@Path("/survey/documentLibrary")
public class SMRTDocLibraryREST  extends SMRTAbstractFacade<Survey>{
    
    private static final Logger logger = LogManager.getLogger(SMRTDocLibraryREST.class);
    
    public SMRTDocLibraryREST() {
         super(Survey.class);
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
    @Path ("{participantId}")
    @Produces({MediaType.APPLICATION_JSON})
    public String getDocumentLibrary(
            @QueryParam("token") String token,
            @PathParam("participantId") String participantId,
            @Context HttpServletRequest req) 
            throws Exception {
            
            SMRTUser user = this.validateToken(token, null);
            
            try{
                DB mongoDB = this.getDBInstance();
                
                Iterable<DBObject> surveyDocResults = null;
                
                DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");
                
                Iterable<DBObject> surveyDocs = mongoDB.getCollection("survey").aggregate(Arrays.asList(
                        (DBObject) new BasicDBObject("$match", new BasicDBObject("customerId" ,user.getCustomerId()).append("sections.questions.type", "file")
                         .append("sections.questions.widget", Pattern.compile("^DOC", Pattern.CASE_INSENSITIVE)))
                        ,(DBObject) new BasicDBObject("$project", new BasicDBObject("_id", "$_id")
                        .append("settings","$settings")
                        .append("questions", "$sections.questions"))
                        ,(DBObject) new BasicDBObject("$unwind", "$questions")
                        ,(DBObject) new BasicDBObject("$unwind", "$questions")
                        ,(DBObject) new BasicDBObject("$match", new BasicDBObject("questions.type","file"))
                         ,(DBObject) new BasicDBObject("$group", new BasicDBObject("_id", new BasicDBObject("id","$_id").append("type", "$questions.widget"))
                         .append("questions", new BasicDBObject("$push", new BasicDBObject("key","$questions.name").append("label", "$questions.placeholder"))))
                        ,(DBObject) new BasicDBObject("$project", new BasicDBObject("_id","$_id.id").append("group","$_id.type").append("setting","$settings.value").append("questions", "$questions"))
                        ,(DBObject) new BasicDBObject("$sort", new BasicDBObject("group", 1))
                )).results();
                
                //add to List
                List<DBObject> docs = new ArrayList<>();
                surveyDocs.forEach(docs::add);
                
                BasicDBObject docFields = new BasicDBObject();
                
                for(DBObject doc: docs){
                    BasicDBList questions = (BasicDBList) doc.get("questions");
                    for(Object q: questions.toArray()){
                        String key = (String)((DBObject)q).get("key");
                        docFields.put(key, 1);
                        docFields.put(key.replace("_file","_name"), 1);
                    }
                }
                
                BasicDBObject query = new BasicDBObject();
                query.put("customerId", user.getCustomerId());
                query.put("participantId", participantId);
                
                DBCursor docResult = surveyResultCollection.find(query, docFields);
                
                String results = "{}";
                Gson json = new Gson();
                if (docResult.hasNext()) {
                    DBObject obj = docResult.next();
                    obj.put("id", obj.get("_id").toString());
                    results = json.toJson(obj);
                }
            
                
                 return "{\"surveys\":"+json.toJson(surveyDocs)+",\"results\":"+results+"}";
                
            }catch(Exception ex){
                logger.error("getDocumentLibrary", ex);
                throw ex;
            }
            
       
    }
    
    @POST
    @Path ("{surveyId}/upload/{keyName}")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public String upload(
            @PathParam("surveyId") String surveyId,
            @PathParam("keyName") String keyName,
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
             String participantId = formData.get("participantId");
             String documentName = formData.get("name");
             
             SMRTUser user = this.validateToken(token, null);
                          
             //set file path
                String filePath = null;
             if(keyName.equals("e2_en_103_file")){
                 filePath = Paths.get(System.getenv("SMRT_DOCS"),"Uploads","SurveyDocs", user.getCustomerId(), participantId).toString();
             }else{
                 filePath = Paths.get(System.getenv("SMRT_DOCS"),"Uploads","SurveyDocuments", user.getCustomerId(), participantId).toString();
             }
             File dir = new File(filePath);
             
             //check for directory exists or not and create 
             if(!dir.exists()){
                 dir.mkdirs();
             }
             String fullPath;
             if(fileItem != null){ //check if uploaded file was received
                 String fileName = null;
                 
                if(keyName.equals("e2_en_103_file")){
                     fileName = "userpicture.png";
                     fullPath= Paths.get(filePath,fileName).toString();
                }else{
                     fileName = fileItem.getName();
                     fullPath = Paths.get(filePath,keyName+"__"+fileName).toString();
                }    
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
                
                //Check if document is User PIC Widget
                if(!question.getWidget().equals("USERPIC")){
                    //set document radio value as Yes
                    data.put(keyName.replaceFirst("_file", ""), new BasicDBObject("val","1").append("txt","1"));
                }
                
                //set upload file
                data.put(keyName, new BasicDBObject("val",keyName+"__"+fileName).append("txt", keyName+"__"+fileName));
                
                //set upload file link
                data.put(keyName.replaceFirst("_file", "_link"), new BasicDBObject("val",fileName).append("txt", fileName));
                
                if(documentName != null){
                    //set document name
                    data.put(keyName.replaceFirst("_file", "_name"), new BasicDBObject("val",documentName).append("txt", documentName));
                }

                data = new BasicDBObject(
                            "$set",
                            data
                            .append("resultTime", new Date())
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
    
    
}

