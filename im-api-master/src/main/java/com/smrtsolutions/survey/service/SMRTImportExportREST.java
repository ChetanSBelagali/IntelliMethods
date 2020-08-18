/*
 * SMRT Solutions
 * Data Collection Platform
 */
package com.smrtsolutions.survey.service;


import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.util.JSON;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.smrtsolutions.exception.InvalidParameterException;
import com.smrtsolutions.survey.model.NameValuePair;
import com.smrtsolutions.survey.model.PermissionTemplates;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.survey.model.Survey;
import com.smrtsolutions.survey.model.SurveyResult;
import com.smrtsolutions.survey.model.SurveySectionQuestion;
import com.smrtsolutions.util.Token;
import com.smrtsolutions.util.Util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.sf.jasperreports.engine.util.FileBufferedWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import static org.apache.log4j.config.PropertyPrinter.capitalize;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONStringer;
import org.jfree.util.StringUtils;

/**
 *
 * @author Kriyatec - Santhosh
 */
@Path("/customer/importexport")
public class SMRTImportExportREST  extends SMRTAbstractFacade<Survey>{
    
    private static final Logger logger = LogManager.getLogger(SMRTImportExportREST.class);
    
    public SMRTImportExportREST() {
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
    
    @POST
    @Path("upload")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public String upload(
            @PathParam("surveyId") String surveyId,
            @Context HttpServletRequest req)
            throws Exception {

        try {
            HashMap<String, String> formData = new HashMap<>();
            if (ServletFileUpload.isMultipartContent(req)) {

                FileItemFactory fiFactory = new DiskFileItemFactory();
                ServletFileUpload fileUpload = new ServletFileUpload(fiFactory);

                List<FileItem> listItems = fileUpload.parseRequest(req);
                FileItem fileItem = null;

                for (FileItem f : listItems) {
                    if (f.isFormField()) {
                        formData.put(f.getFieldName(), f.getString());
                    } else {
                        fileItem = f;
                    }
                }

                String token = formData.get("token");
                String participantId = formData.get("participantId");
                String documentName = formData.get("name");

                SMRTUser user = this.validateToken(token, null);

                //set file path
                String filePath = Paths.get(System.getenv("SMRT_DOCS"), "Uploads", "ImportExport", user.getCustomerId()).toString();
                File dir = new File(filePath);

                //check for directory exists or not and create 
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                if (fileItem != null) { //check if uploaded file was received
                    String fileName = fileItem.getName();
                    DateFormat df = new SimpleDateFormat("MM_dd_yyyy_hh_mm_ss_");
                    fileName = df.format(new Date()) + fileName;
                    String fullPath = Paths.get(filePath, fileName).toString();
                    File file = new File(fullPath);

                    //save file to path
                    fileItem.write(file);

                    CSVFormat format = CSVFormat.newFormat(',').withHeader();

                    
                    Reader reader = new InputStreamReader(new BOMInputStream(new FileInputStream(file)), "UTF-8");

                    CSVParser parser = new CSVParser(reader, format);

                    DB mongoDB = this.getDBInstance();
                    DBCollection userCollection = mongoDB.getCollection("SMRTUser");
                    DBCollection surveyCollection = mongoDB.getCollection("survey");
                    BulkWriteResult result = null;
                    //get csv headers
                    Map<String, Integer> headers = parser.getHeaderMap();
                    
                    for(Map.Entry<String, Integer> entry: headers.entrySet()){
                        if(entry.getKey().equals("")){
                            headers.remove("");
                        }
                    }
                    
                    Gson json = new Gson();

                    String headerKeys = json.toJson(headers.keySet());

                    String map = "function () {"
                            + "	var keys = " + headerKeys + ";"
                            + "	this.sections.forEach(section=>{"
                            + "	   section.questions.forEach(question=>{"
                            + "	     if(keys.indexOf(question.name)>=0){"
                            + "	       	var options = {};"
                            + "	       	if(question.options!= null && question.optionLabels!= null){"
                            + "	       	  	"
                            + "				question.optionLabels.forEach((label, idx)=>{"
                            + "					options[label] = question.options[idx];"
                            + "				});"
                            + "	       	}"
                            + "	       "
                            + "	      	 emit(\"\",{name: question.name , type: question.type, options: options});"
                            + "	     }"
                            + "	   });"
                            + "	});"
                            + "}";

                    String reduce = "function (key, values) {\n"
                            + "\n"
                            + "    var result = {};\n"
                            + "    \n"
                            + "    values.forEach(value=>{\n"
                            + "      result[value.name] = {type: value.type, options: value.options};\n"
                            + "    });\n"
                            + "    \n"
                            + "    return result;\n"
                            + "}";

                    BasicDBObject fields = new BasicDBObject();
                    fields.put("customerId", user.getCustomerId());

                    MapReduceCommand cmd = new MapReduceCommand(surveyCollection, map, reduce,
                            null, MapReduceCommand.OutputType.INLINE, fields);
                    Iterable<DBObject> mapResult = surveyCollection.mapReduce(cmd).results();

                    BasicDBObject optionsKey = (BasicDBObject) mapResult.iterator().next();
                    optionsKey = (BasicDBObject) optionsKey.get("value");

                    
                    int emailIdx = headers.get("s1_req_9");
                    int dobIdx = headers.get("s1_req_14");
                    int pwd = headers.get("password_key");
                    int phone = headers.get("s1_req_7");
                    int fNameIdx = headers.get("s1_req_1");
                    int lNameIdx = headers.get("s1_req_2");
             
                    DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");
                    BulkWriteOperation bulkWriteOperation = surveyResultCollection.initializeUnorderedBulkOperation();
                    List<ArrayList<String>> errorRows = new ArrayList<>();
                    
                    errorRows.add(new ArrayList<String>(headers.keySet()));
                    errorRows.get(0).add("Error Description");
                    
                    int rowCount = 0;
                    int errorCount = 0;
                    
                    String errorMsg;
                    
                    for (CSVRecord csvRecord : parser) {
                        BasicDBObject data = new BasicDBObject();
                        String email = csvRecord.get(emailIdx);
                        //ignore empty record
                        if (email.equals("")) {
                            continue;
                        }
                        rowCount++;

                        String dob = csvRecord.get(dobIdx);
                        String fName = csvRecord.get(fNameIdx);
                        String lName = csvRecord.get(lNameIdx);
                        String password = csvRecord.get(pwd);
                        String phonenumber= csvRecord.get(phone);
                        Boolean isDataError = false;
                        ArrayList<String> errorRow = new ArrayList<String>();
                        errorMsg = "";
                        if(dob.equals("")){
                            if(!isDataError)
                                errorCount++;
                            isDataError = true;
                            errorMsg+= "DOB value cannot be empty\r\n";
                        }
                        if(fName.equals("")){
                            if(!isDataError)
                                errorCount++;
                            isDataError = true;
                            errorMsg+= "Firstname value cannot be empty\r\n";
                        }
                        if(lName.equals("")){
                            isDataError = true;
                            if(!isDataError)
                                errorCount++;
                            isDataError = true;
                            errorMsg+= "Lastname value cannot be empty\r\n";
                        }
                        if(email.equals("")){
                            isDataError = true;
                            if(!isDataError)
                                errorCount++;
                            isDataError = true;
                            errorMsg+= "Email value cannot be empty\r\n";
                        }
                        if(password.equals("")){
                            isDataError = true;
                            if(!isDataError)
                                errorCount++;
                            isDataError = true;
                            errorMsg+= "Password value cannot be empty\r\n";
                        }
                        
                            BasicDBList u = new BasicDBList();
                            u.add(new BasicDBObject ("email",email));
                            u.add(new BasicDBObject ("dob",dob).append("fName",fName).append("lName",lName).append("lName",lName));
                        BasicDBObject userRec = (BasicDBObject) userCollection.findOne(new BasicDBObject("$or", u).append("customerId", user.getCustomerId()).append("usertype","student"),new BasicDBObject("_id", 1));
                        
                        //skip user add, if data error exists and user doesn't exists already
                        if (userRec == null && !isDataError) {
                            BasicDBList roles = new BasicDBList();
                            roles.add("student");

                            this.getEntityManager().getTransaction().begin();
                            try {
                                SMRTUser userEntity = new SMRTUser();
                                userEntity.setRoles(new ArrayList<String>() {
                                    {
                                        add("student");
                                    }
                                });
                                userEntity.setUsertype("student");
                                userEntity.setLoginName(email);
                               // String fName =capitalize(fNameIdx);
                                 fName=capitalize(fName);
                                 lName=capitalize(lName);
                                userEntity.setFirstname(fName);
                                userEntity.setLastname(lName);
                                userEntity.setCreatedOn(new Date());
                                userEntity.setName(fName + " " + lName);
                                userEntity.setEmail(email);
                                userEntity.setCustomerId(user.getCustomerId());
                                userEntity.setPassword(Util.encrypt(password));
                                userEntity.setPhonenumber(phonenumber);
                                userEntity.setCreatedOn(new Date());
                                getEntityManager().persist(userEntity);
                                this.getEntityManager().getTransaction().commit();
                                
                                //set imported status for user
                                data.put("isimport", new BasicDBObject("val", "true").append("txt", "true"));
                                userRec = new BasicDBObject("_id", userEntity.getId());
                            } catch (Exception e) {

                                this.getEntityManager().getTransaction().rollback();
                                throw e;
                            }
                        }else if(!isDataError){
                            //update user record
                            
                            fName=capitalize(fName);
                            lName=capitalize(lName);
                            userRec.put("firstname", fName);
                            userRec.put("lastname", lName);
                            userRec.put("name", fName + " " + lName);
                            userRec.put("phonenumber", phonenumber);
                            
                            userCollection.update(new BasicDBObject("_id", userRec.get("_id")), new BasicDBObject("$set",new BasicDBObject(userRec)));
                            
                        }
                        
                        

                        for (Map.Entry<String, Integer> entry : headers.entrySet()) {
                            System.out.println(entry.getKey() + "/" + entry.getValue());
                            
                            BasicDBObject val = new BasicDBObject();
                            String getVal = csvRecord.get(entry.getValue()).trim();
                            String strVal = getVal.replaceAll("^\"|\"$", "");
                            val.put("txt", strVal);
                            val.put("val", strVal);
                            
                            if(strVal.equals(null) || strVal.equals("")){
                                //don't insert data if value is empty
                                continue;
                            }

                            BasicDBObject option = (BasicDBObject) optionsKey.get(entry.getKey());
                            // checking the option key available in survey
                            if(option == null){
                                throw new InvalidParameterException("Please add a valid key's");
                            }
                            if (option.getString("type").equals("select") || option.getString("type").equals("radio")) {
                                val.put("val", (String) ((BasicDBObject) option.get("options")).get(strVal));
                            } else if (option.getString("type").equals("checkbox")) {
                                String[] strSplit = strVal.split(",");

                                String[] valStr = new String[strSplit.length];
                                for (int i = 0; i < strSplit.length; i++) {
                                    valStr[i] = (String) ((BasicDBObject) option.get("options")).get(strSplit[i]);
                                }
                                String optValue = String.join(",",valStr);
                                if (optValue.equals(null)) {
                                    String x = "error";
                                } 
                                val.put("val",optValue );
                            }
                            
                            if(val.get("val") == null){
                                
                                if(!isDataError)
                                    errorCount++;
                                
                                isDataError = true;
                                
                                errorMsg+= "Invalid value at "+ entry.getKey()+ " \r\n";
                            }
                            
                            data.put(entry.getKey(), val);
                            
                            
                        }
                        
                        //don't insert with error
                        if(isDataError){
                            
                            for (Map.Entry<String, Integer> item : headers.entrySet()) {
                                errorRow.add(csvRecord.get(item.getValue()));
                            }
                            
                            errorRow.add(errorMsg);
                            errorRows.add(errorRow);
                            continue;
                        }
                        
                        data.put("s1_req_1", new BasicDBObject("val",fName).append("txt",fName));
                        data.put("s1_req_2", new BasicDBObject("val",lName).append("txt",lName));
                        data.put("st_st_06b",new BasicDBObject("val", new SimpleDateFormat("MM/dd/yyyy").format(new Date())).append("txt", new SimpleDateFormat("MM/dd/yyyy").format(new Date())));
                        if(data.containsKey("uniq_imp_key") && data.get("uniq_imp_key")!=null){
                            String val = (String)((BasicDBObject)data.get("uniq_imp_key")).get("val");
                            if(val!=null && !val.isEmpty() && val.toUpperCase().startsWith("ID")){
                               val = val.substring(2);
                               ((BasicDBObject)data.get("uniq_imp_key")).put("val", val);
                               ((BasicDBObject)data.get("uniq_imp_key")).put("txt", val);
                            }
                                    
                        }
                        
                         if(data.containsKey("password_key") && data.get("password_key")!=null){
                            String val = (String)((BasicDBObject)data.get("password_key")).get("val");
                            if(val!=null && !val.isEmpty() && val.toUpperCase().startsWith("ID")){
                               val = val.substring(2);
                               ((BasicDBObject)data.get("password_key")).put("val", val);
                               ((BasicDBObject)data.get("password_key")).put("txt", val);
                            }    
                        }
                //status
                data.put("status","");
                
                    if(data.containsKey("isimport")){
                        String isimport = (String)((BasicDBObject)data.get("isimport")).get("val");
                        data.replace("status","Import");
                    }
                    if(data.containsKey("st_st_06a") && data.get("st_st_06a")!=null){
                         data.replace("status","Recruitment");
                    }
                    
                    if(data.containsKey("st_st_06") && data.get("st_st_06")!=null){
                        data.replace("status","Intake");
                    }
                    if(data.containsKey("st_st_10") && data.get("st_st_10")!=null){
                         data.replace("status","Enrolling");
                    }
                    if(data.containsKey("st_st_11") && data.get("st_st_11")!=null){
                         data.replace("status","Enrolled");
                    }
                    if(data.containsKey("st_st_13") && data.get("st_st_13")!=null) {
                            data.replace("status","Exited Enrollment"); 
                    }
                    if(data.get("status").equals("")){
                        data.replace("status","Intake");
                    }
                    
                if(data.get("status").equals("")){
                    //Status Change 1 - status
                    if (data.containsKey("st_st_17") && data.get("st_st_17")!=null && data.containsKey("st_st_16") && data.get("st_st_16")!=null) {
                        if(data.get("st_st_16").equals("1")){
                            data.replace("status", "Stop Out");
                        }else if(data.get("st_st_16").equals("2")){
                            if(data.containsKey("st_st_19") && data.get("st_st_19")!=null){
                                if(data.get("st_st_19").equals("1")){
                                    data.replace("status", "Exited Complete");
                                }else{
                                    data.replace("status", "Exited Not Complete");
                                }
                            }
                        }
                        
                    }
                } 
                 if(data.get("status").equals("")){
                    //Status Change 2 - status
                    if (data.containsKey("st_st_24") && data.get("st_st_24")!=null && data.containsKey("st_st_23") && data.get("st_st_23")!=null) {
                        if(data.get("st_st_23").equals("1")){
                            data.replace("status", "Stop Out");
                        }else if(data.get("st_st_23").equals("2")){
                            if(data.containsKey("st_st_26") && data.get("st_st_26")!=null){
                                if(data.get("st_st_26").equals("1")){
                                    data.replace("status", "Exited Complete");
                                }else{
                                    data.replace("status", "Exited Not Complete");
                                }
                            }
                        }else if(data.get("st_st_23").equals("3")){
                            data.replace("status", "Enrolled");
                        }
                    }
                }
                //Status Change 3 - status
                if (data.containsKey("st_st_31") && data.get("st_st_31")!=null && data.containsKey("st_st_30") && data.get("st_st_30")!=null) {
                    if(data.get("st_st_30").equals("1")){
                        data.replace("status", "Stop Out");
                    }else if(data.get("st_st_30").equals("2")){
                        if(data.containsKey("st_st_33") && data.get("st_st_33")!=null){
                            if(data.get("st_st_33").equals("1")){
                                data.replace("status", "Exited Complete");
                            }else{
                                data.replace("status", "Exited Not Complete");
                            }
                        }
                    }else if(data.get("st_st_30").equals("3")){
                        data.replace("status", "Enrolled");
                    }
                }
                
                //Status Change 4 - status
                if(data.get("status").equals("")){
                    if (data.containsKey("st_st_38") && data.get("st_st_38")!=null && data.containsKey("st_st_37") && data.get("st_st_16")!=null) {
                        if(data.get("st_st_37").equals("1")){
                            data.replace("status", "Stop Out");
                        }else if(data.get("st_st_37").equals("2")){
                            if(data.containsKey("st_st_40") && data.get("st_st_40")!=null){
                                if(data.get("st_st_40").equals("1")){
                                    data.replace("status", "Exited Complete");
                                }else{
                                    data.replace("status", "Exited Not Complete");
                                }
                            }
                        }
                    }
                }  
                
                //Status Change 5 - status
                if(data.get("status").equals("")){
                    if (data.containsKey("st_st_45") && data.get("st_st_45")!=null && data.containsKey("st_st_44") && data.get("st_st_16")!=null) {
                        if(data.get("st_st_44").equals("1")){
                            data.replace("status", "Stop Out");
                        }else if(data.get("st_st_44").equals("2")){
                            if(data.containsKey("st_st_47") && data.get("st_st_47")!=null){
                                if(data.get("st_st_47").equals("1")){
                                    data.replace("status", "Exited Complete");
                                }else{
                                    data.replace("status", "Exited Not Complete");
                                }
                            }
                        }
                        
                    }
                }

              
                    
               
//                if(data.get("status").equals("")){
//                    if(data.containsKey("st_st_11") && data.get("st_st_11")!=null){
//                        data.replace("status","Enrolled");
//                    }else if(data.containsKey("st_st_13") && data.get("st_st_13")!=null){
//                         data.replace("status","Exited Enrollment");
//                    }else if(data.containsKey("st_st_10") && data.get("st_st_10")!=null){
//                         data.replace("status","Enrolling");
//                    }else{ 
//                        
//                        if (data.containsKey("isimport")) {
//                            String isimport = (String)((BasicDBObject)data.get("isimport")).get("val");
//                            if(isimport!=null && isimport.equals("true"))
//                            {
//                                data.replace("status","Import"); 
//                            }
//                            else
//                            {
//                                data.replace("status","Intake");
//                            }
//                            
//                        }else{
//                            data.replace("status","Intake");
//                        }
//                    }
//                }

                        
                        BasicDBObject updateFields = new BasicDBObject("$set", data)
                                .append(
                                        "$setOnInsert",
                                        new BasicDBObject("participantId", (String) userRec.get("_id"))
                                                .append("customerId", user.getCustomerId())
                                                .append("createdBy", user.getId())
                                                .append("surveyStatus", new BasicDBObject())
                                );

                        bulkWriteOperation.find(new BasicDBObject("customerId", user.getCustomerId())
                                .append("participantId", (String) userRec.get("_id"))).upsert().updateOne(updateFields);

                    }
                    if(rowCount - errorCount > 0){
                        try {
                            result = bulkWriteOperation.execute();
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                            result = null;
                        }
                    }
                    
                    if(errorCount>0){
                        //save error data to file
                        FileWriter fileWriter = null;
                        CSVPrinter csvFilePrinter = null;
                        
                        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
                                                
                        fullPath = Paths.get(filePath, "errorcsv").toString();
                        
                        dir = new File(fullPath);
                        
                        //check for directory exists or not and create 
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        
                        fullPath = Paths.get(fullPath, fileName).toString();
                        //initialize FileWriter object
                        fileWriter = new FileWriter(fullPath);
                        //initialize CSVPrinter object 
                        csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
                        for(ArrayList item : errorRows){
                            csvFilePrinter.printRecord(item);
                        }
                        fileWriter.flush();
	                fileWriter.close();
                    }else{
                        fileName = "";
                    }
                    int insertCount=0;
                    if (result!=null)
                       insertCount = result.getModifiedCount() + result.getUpserts().size();
                    
                    BasicDBObject outResult = new BasicDBObject();
                    outResult.append("rowCount", rowCount);
                    outResult.append("insertCount", insertCount);
                    outResult.append("errorCount", errorCount);
                    outResult.append("skipCount", rowCount - insertCount - errorCount);
                    outResult.append("fileName", fileName);
                    outResult.append("uploadedBy", user.getId());
                    
                    DBCollection importCollection = mongoDB.getCollection("import_history");
                    importCollection.insert(outResult);
                    
                    
                    return new Gson().toJson(outResult);
                } else {
                    throw new InvalidParameterException("Error: Uploaded file not received");
                }
            }
            throw new InvalidParameterException("Only Multipart Upload Supported");

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println(ex.getStackTrace());
            throw new Exception("Error Importing csv. Check whether the csv is valid and try again");
        }
    }
    
    @GET
    @Path("errorcsv")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,MediaType.APPLICATION_OCTET_STREAM})
    public Response getMetadataFile(@QueryParam("token") String token,@QueryParam("filename") String filename) throws Exception {
        SMRTUser user = this.validateToken(token, null);
        try {
            File file = null;
            String filePath = Paths.get(System.getenv("SMRT_DOCS"),"Uploads","ImportExport", user.getCustomerId(), "errorcsv", filename).toString();
            file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            return Response
                    .ok(file, MediaType.APPLICATION_OCTET_STREAM)
                    .header("content-disposition","attachment;filename=\"" + filename+"\"")
                    .build();
        } catch (Exception ex) {
            return null;
        }
    }
    
    
    @POST
    @Path("template/config")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public Response saveTemplate(@FormParam("templateName") String templateName
                , @FormParam("id") String id
                , @FormParam("fields") String fields
                , @FormParam("token") String token) throws Exception {
    try {
        //validate user token and accordingly get the customer id     
        SMRTUser user = this.validateToken(token, null); 
        DB mongoDB = this.getDBInstance();
        DBCollection userCollection = mongoDB.getCollection("export_config");
        BasicDBObject document = new BasicDBObject();
        if(id==null || id.equals("")){
            id = ObjectId.get().toString();
        }
        document.put("$set", 
                new BasicDBObject("customerId",user.getCustomerId())
                .append("name",templateName)
                .append("fields",(DBObject) JSON.parse(fields))
        );
        document.put("$setOnInsert", new BasicDBObject("_id", id ));
        
        userCollection.update(new BasicDBObject("_id",id),document, true, true, WriteConcern.ACKNOWLEDGED);

        return Response.ok().build();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error adding");
        } 
    }
    
    
  
    
    @GET
    @Path("template/config/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public String getTemplate(
            @PathParam("id") String id,
            @QueryParam("token") String token) throws Exception {
     this.validateToken(token, null); 
     return new Gson().toJson(getConfigFields(id));       
    }
    
    private DBObject getConfigFields(String templateId) throws Exception  {
    DB mongoDB;
        try {
            mongoDB = this.getDBInstance();
            DBCollection exportCollection = mongoDB.getCollection("export_config");
            BasicDBObject condition = new BasicDBObject("_id",templateId);
            DBCursor cursor = exportCollection.find(condition);
            if (cursor.hasNext()) return cursor.next();
        } catch (Exception ex) {  

        }
    return null;
    }
    
    
    
    @GET
    @Path("template/list")
    @Produces({MediaType.APPLICATION_JSON})
    public String listTemplate(@QueryParam("token") String token) throws Exception {
    try {
        //validate user token and accordingly get the customer id     
        SMRTUser user = this.validateToken(token, null); 
        DB mongoDB = this.getDBInstance();
        DBCollection userCollection = mongoDB.getCollection("export_config");
        BasicDBObject fields = new BasicDBObject("_id",1).append("name",1);
        BasicDBObject condition = new BasicDBObject("customerId",user.getCustomerId());
        DBCursor cursor = userCollection.find(condition,fields);
        Gson json = new Gson();
            return json.toJson(cursor.toArray());
    } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
    } finally{
        //ToDO
    }
    }
    
    
    @DELETE
    @Path("template/config/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public String deleteTemplate(
            @PathParam("id") String id,
            @QueryParam("token") String token) throws Exception{
        try{
        SMRTUser user = this.validateToken(token, null); 
        DB mongoDB = this.getDBInstance();
        DBCollection userCollection = mongoDB.getCollection("export_config");
        BasicDBObject fields = new BasicDBObject("_id",id);
        WriteResult result = userCollection.remove(fields);
    }catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
    } finally{
        //ToDO
    }
        return null;
        
 }
    
    
    @GET
    @Path("survey/download/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response surveyDownload(
            @PathParam("id") String templateId,
            @QueryParam("eo") String exportOption,
            @QueryParam("token") String token) throws Exception {
    DB mongoDB = null;
    DBCollection surveyCollection =null;
    BasicDBObject condition = null;
    DBObject configResult =null;
    DBCursor cursor = null;
    List<DBObject> result;
    CSVPrinter csvFilePrinter = null;
    FileWriter fileWriter;
    File file = null;
    try {
        //validate user token and accordingly get the customer id     
        SMRTUser user = this.validateToken(token, null); 
        configResult = getConfigFields(templateId);
        if (configResult==null) {
            throw new Exception("Template Id not found");
        }
        BasicDBObject fields = (BasicDBObject)configResult.get("fields");
        mongoDB = this.getDBInstance();
        surveyCollection = mongoDB.getCollection("survey_results");
        condition = new BasicDBObject("customerId",user.getCustomerId());
        cursor = surveyCollection.find(condition,fields);
        file = File.createTempFile("output.", ".tmp");
        fileWriter = new FileWriter(file);
        csvFilePrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withRecordSeparator("\n"));
        ArrayList<String> s = new ArrayList<String>();
        fields = this.getSurveyQuestionLabels(user.getCustomerId(), fields);
        for(String k:fields.keySet()) {
            s.add(fields.getString(k));
        }
        csvFilePrinter.printRecord(s);
        while(cursor.hasNext()) {
            DBObject r = cursor.next();
            s = new ArrayList<String>();
            for(String k:fields.keySet()) {
                 if (r.containsField(k))
                    if(exportOption.equalsIgnoreCase("c"))
                        if(((BasicDBObject)r.get(k)).get("txt")==null){
                        s.add("");
                        }
                        else
                        {
                            s.add(((BasicDBObject)r.get(k)).get("txt").toString());
                        }
                    else
                        if(((BasicDBObject)r.get(k)).get("val")==null){
                        s.add("");
                        }
                        else
                        {
                           s.add(((BasicDBObject)r.get(k)).get("val").toString());
                        }
                        
                else
                    s.add("");
            }
            csvFilePrinter.printRecord(s);
        }
        fileWriter.flush();
	fileWriter.close();
        return Response
              .ok(file, MediaType.APPLICATION_OCTET_STREAM)
              .header("content-disposition",".csv" + "attachment; filename = data-" + new SimpleDateFormat("yyyy-MM-dd-hhmmss").format(new Date()) +".csv")
              .build();
    } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
    } finally{
        mongoDB = null;
        surveyCollection =null;
        condition = null;
        configResult =null;
        cursor = null;
        file.deleteOnExit();
    }
    
    }
    @POST
    @Path("uploadresource")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public String uploadresource(
            @Context HttpServletRequest req)
            throws Exception {

        try {
            HashMap<String, String> formData = new HashMap<>();
            if (ServletFileUpload.isMultipartContent(req)) {

                FileItemFactory fiFactory = new DiskFileItemFactory();
                ServletFileUpload fileUpload = new ServletFileUpload(fiFactory);

                List<FileItem> listItems = fileUpload.parseRequest(req);
                FileItem fileItem = null;

                for (FileItem f : listItems) {
                    if (f.isFormField()) {
                        formData.put(f.getFieldName(), f.getString());
                    } else {
                        fileItem = f;
                    }
                }

                String token = formData.get("token");
                String participantId = formData.get("participantId");
                String documentName = formData.get("name");

                SMRTUser user = this.validateToken(token, null);

                //set file path
                String filePath = Paths.get(System.getenv("SMRT_DOCS"), "Uploads", "ImportExport", user.getCustomerId()).toString();
                File dir = new File(filePath);

                //check for directory exists or not and create 
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                if (fileItem != null) { //check if uploaded file was received
                    String fileName = fileItem.getName();
                    DateFormat df = new SimpleDateFormat("MM_dd_yyyy_hh_mm_ss_");
                    fileName = df.format(new Date()) + fileName;
                    String fullPath = Paths.get(filePath, fileName).toString();
                    File file = new File(fullPath);

                    //save file to path
                    fileItem.write(file);

                    CSVFormat format = CSVFormat.newFormat(',').withHeader();
                    format.withHeader();
                    
                    Reader reader = new InputStreamReader(new BOMInputStream(new FileInputStream(file)), "UTF-8");

                    CSVParser parser = new CSVParser(reader, format);

                    DB mongoDB = this.getDBInstance();
                    DBCollection userCollection = mongoDB.getCollection("SMRTUser");
                    BulkWriteResult result = null;
                    //get csv headers
                    Map<String, Integer> headers = parser.getHeaderMap();
                    
                    for(Map.Entry<String, Integer> entry: headers.entrySet()){
                        if(entry.getKey().equals("")){
                            headers.remove("");
                        }
                    }
                    
                    Gson json = new Gson();
                    BasicDBObject fields = new BasicDBObject();
                    fields.put("customerId", user.getCustomerId());
                    
                    int firstname = headers.get("firstname");
                    int lastname = headers.get("lastname");
                    int emailid = headers.get("email");
                    int phonenumber = headers.get("phonenumber");
                    int servicename = headers.get("servicename");
                    int location=headers.get("location");
                    int organization=headers.get("organization");
                    int usertypes=headers.get("usertype");
                    int permission_template=headers.get("permission_template");
                    
                    List<ArrayList<String>> errorRows = new ArrayList<>();
                    errorRows.add(new ArrayList<String>(headers.keySet()));
                    errorRows.get(0).add("Error Description");
                    
                    int rowCount = 0;
                    int errorCount = 0;
                    
                    String errorMsg;
                    
                    for (CSVRecord csvRecord : parser) {
                        BasicDBObject data = new BasicDBObject();
                        String email = csvRecord.get(emailid);
                        //ignore empty record
                        if (email.equals("")) {
                            continue;
                        }
                        rowCount++;
                        String fName = csvRecord.get(firstname);
                        String lName = csvRecord.get(lastname);
                        String role  = csvRecord.get(usertypes);
                        String usertype = csvRecord.get(usertypes);
                        String phone = csvRecord.get(phonenumber);
                        String services = csvRecord.get(servicename);
                        List<String> servicesprovided = Arrays.asList(services.split(","));
                        String locations = csvRecord.get(location);
                        List<String> locationsprovided = Arrays.asList(locations.split(","));
                        String organizations = csvRecord.get(organization);
                        String permissiontemplate = csvRecord.get(permission_template);
                        
                        Boolean isDataError = false;
                        ArrayList<String> errorRow = new ArrayList<String>();
                        errorMsg = "";
                        if(fName.equals("")){
                            if(!isDataError)
                                errorCount++;
                            isDataError = true;
                            errorMsg+= "Firstname value cannot be empty\r\n";
                        }
                        if(lName.equals("")){
                            isDataError = true;
                            if(!isDataError)
                                errorCount++;
                            isDataError = true;
                            errorMsg+= "Lastname value cannot be empty\r\n";
                        }
                        if(email.equals("")){
                            isDataError = true;
                            if(!isDataError)
                                errorCount++;
                            isDataError = true;
                            errorMsg+= "Email value cannot be empty\r\n";
                        }
                        //||!(role.equals("casemanager"))||!(role.equals("director"))||!(role.equals("instructor"))||!(role.equals("observer"))||!(role.equals("partner"))||!(role.equals("student"))||!(role.equals("admin"))||!(role.equals("dataadmin"))
                        if(role.equals("")){
                            isDataError = true;
                            if(!isDataError)
                                errorCount++;
                            isDataError = true;
                            errorMsg+= "Provide a valid Role\r\n";
                        }
                        if(usertype.equals("")){
                            isDataError = true;
                            if(!isDataError)
                                errorCount++;
                            isDataError = true;
                            errorMsg+= "Usertype value cannot be empty\r\n";
                        }
                        if(phone.equals("")){
                            isDataError = true;
                            if(!isDataError)
                                errorCount++;
                            isDataError = true;
                            errorMsg+= "Phone Number value cannot be empty\r\n";
                        }
                        if(permissiontemplate.equals("")){
                            isDataError = true;
                            if(!isDataError)
                                errorCount++;
                            isDataError = true;
                            errorMsg+= "Permission Template value cannot be empty\r\n";
                        }
                        PermissionTemplates templates=new PermissionTemplates();
                        PermissionTemplates predefinedpermission=this.findTemplateName(permissiontemplate);
                       
                        
                        BasicDBList u = new BasicDBList();
                        u.add(new BasicDBObject ("email",email)
                        );
                        BasicDBObject userRec = (BasicDBObject) userCollection.findOne(new BasicDBObject("$or", u).append("customerId", user.getCustomerId()),new BasicDBObject("_id", 1));
                        
                        //skip user add, if data error exists and user doesn't exists already
                        if (userRec == null && !isDataError) {
                            BasicDBList roles = new BasicDBList();
                            roles.add(role);

                            this.getEntityManager().getTransaction().begin();
                            try {
                                SMRTUser userEntity = new SMRTUser();
                                userEntity.setRoles(new ArrayList<String>() {
                                    {
                                        add(role);
                                    }
                                });
                                userEntity.setUsertype(role);
                                userEntity.setLoginName(email);
                               // String fName =capitalize(fNameIdx);
                                 fName=capitalize(fName);
                                 lName=capitalize(lName);
                                userEntity.setFirstname(fName);
                                userEntity.setLastname(lName);
                                userEntity.setName(fName + " " + lName);
                                userEntity.setEmail(email);
                                userEntity.setOrganization(organizations);
                                userEntity.setPhonenumber(phone);
                                userEntity.setServicename(servicesprovided);
                                userEntity.setCustomerId(user.getCustomerId());
                                userEntity.setPassword(Util.encrypt("smrt"));
                                userEntity.setPermissions(predefinedpermission.getPermissions());
                                userEntity.setPermission_template_id(predefinedpermission.getId());
                                userEntity.setLocation(locationsprovided);
                                userEntity.setCreatedOn(new Date());
                                userEntity.setPermission_type("P");
                                getEntityManager().persist(userEntity);
                                this.getEntityManager().getTransaction().commit();

                                userRec = new BasicDBObject("_id", userEntity.getId());
                            } catch (Exception e) {

                                this.getEntityManager().getTransaction().rollback();
                                throw e;
                            }
                        }
                        for (Map.Entry<String, Integer> entry : headers.entrySet()) {
                            System.out.println(entry.getKey() + "/" + entry.getValue());

                            BasicDBObject val = new BasicDBObject();
                            String strVal = csvRecord.get(entry.getValue());
                            val.put("txt", strVal);
                            val.put("val", strVal);
                            
                            if(strVal.equals(null) || strVal.equals("")){
                                //don't insert data if value is empty
                                continue;
                            }
                           
                            if(val.get("val") == null){
                                
                                if(!isDataError)
                                    errorCount++;
                                
                                isDataError = true;
                                
                                errorMsg+= "Invalid value at "+ entry.getKey()+ " \r\n";
                            }
                            
                            data.put(entry.getKey(), val);
                            
                            
                        }
                        
                        //don't insert with error
                        if(isDataError){
                            
                            for (Map.Entry<String, Integer> item : headers.entrySet()) {
                                errorRow.add(csvRecord.get(item.getValue()));
                            }
                            
                            errorRow.add(errorMsg);
                            errorRows.add(errorRow);
                            continue;
                        }
                        
                        data.put("firstname",fName);
                        data.put("lastname", lName);

//                        BasicDBObject updateFields = new BasicDBObject("$set", data)
//                                .append(
//                                        "$setOnInsert",
//                                        new BasicDBObject("participantId", (String) userRec.get("_id"))
//                                                .append("customerId", user.getCustomerId())
//                                                .append("createdBy", user.getId())
//                                                .append("surveyStatus", new BasicDBObject())
//                                );
//
//                        bulkWriteOperation.find(new BasicDBObject("customerId", user.getCustomerId())
//                                .append("participantId", (String) userRec.get("_id"))).upsert().updateOne(updateFields);

                    }
//                    if(rowCount - errorCount > 0){
//                        try {
//                            result = bulkWriteOperation.execute();
//                        } catch (Exception ex) {
//                            System.out.println(ex.getMessage());
//                            result = null;
//                        }
//                    }
                    
                    if(errorCount>0){
                        //save error data to file
                        FileWriter fileWriter = null;
                        CSVPrinter csvFilePrinter = null;
                        
                        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
                                                
                        fullPath = Paths.get(filePath, "errorcsv").toString();
                        
                        dir = new File(fullPath);
                        
                        //check for directory exists or not and create 
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        
                        fullPath = Paths.get(fullPath, fileName).toString();
                        //initialize FileWriter object
                        fileWriter = new FileWriter(fullPath);
                        //initialize CSVPrinter object 
                        csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
                        for(ArrayList item : errorRows){
                            csvFilePrinter.printRecord(item);
                        }
                        fileWriter.flush();
	                fileWriter.close();
                    }else{
                        fileName = "";
                    }
                    int insertCount=0;
                    if (result!=null)
                       insertCount = result.getModifiedCount() + result.getUpserts().size();
                    
                    BasicDBObject outResult = new BasicDBObject();
                    outResult.append("rowCount", rowCount);
                    outResult.append("insertCount", insertCount);
                    outResult.append("errorCount", errorCount);
                    outResult.append("skipCount", rowCount - insertCount - errorCount);
                    outResult.append("fileName", fileName);
                    outResult.append("uploadedBy", user.getId());
                    
                    DBCollection importCollection = mongoDB.getCollection("import_history");
                    importCollection.insert(outResult);
                    
                    
                    return new Gson().toJson(outResult);
                } else {
                    throw new InvalidParameterException("Error: Uploaded file not received");
                }
            }
            throw new InvalidParameterException("Only Multipart Upload Supported");

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw new Exception("Error Importing csv. Check whether the csv is valid and try again");
        }
    }
    
}

