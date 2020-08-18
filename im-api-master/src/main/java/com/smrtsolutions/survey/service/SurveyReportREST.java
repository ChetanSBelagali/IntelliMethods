/*
 * SMRT Solutions
 * Data Collection Platform
 */
package com.smrtsolutions.survey.service;


import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.smrtsolutions.exception.ForbiddenException;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.survey.model.SurveyResult;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JsonDataSource;

/**
 *
 * @author lenny
 */
@Path("/report")
public class SurveyReportREST  extends SMRTAbstractFacade<SurveyResult>{

    public SurveyReportREST() {
         super(SurveyResult.class);
    }

    @Override
    public void log(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getRequiredPermissions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private String getReportFolderPath() {
        return Paths.get(System.getenv("SMRT_DOCS"),"Reports").toString();
    }
    
    @GET
    @Path("client/{participantId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,MediaType.APPLICATION_OCTET_STREAM})
    public Response getClientServiceReport(
            @PathParam("participantId") String participantId, 
            @QueryParam("f") String format,
            @QueryParam("token") String token) throws Exception{
        
        SMRTUser user = this.validateToken(token, null);
        try{
            DB mongoDB = this.getDBInstance();
            DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");
            BasicDBObject query = new BasicDBObject();
            query.put("participantId", participantId);
            DBCursor cursor = surveyResultCollection.find(query);
            DBObject result = new BasicDBObject();
            if(cursor.hasNext()){
                result = cursor.next();
            }
            Gson json = new Gson();
            InputStream io = new ByteArrayInputStream(json.toJson(result).getBytes());
            JRDataSource jsonDataSource = new JsonDataSource(io); 
            return this.exportReport("clientService.jasper", io, "pdf", "ClientService.pdf");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
             throw ex;
        }
    }
    
    @GET
    @Path("program")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,MediaType.APPLICATION_OCTET_STREAM,MediaType.TEXT_HTML})
    public Response programReport(
            @QueryParam("token") String token,
            @QueryParam("f") String format,
            @QueryParam("financialyear") String financialyear,
            @QueryParam("s") String sex,
            @QueryParam("a") String age,
            @QueryParam("r") String race,
            @QueryParam("e") String ethnicity,
            @QueryParam("v") String viewoption
    )  throws Exception{
       
        DB mongoDB = null;
        DBCollection surveyResultsCollection  = null;
        Gson json = null;
        Iterable<DBObject> result = null;
        BasicDBObject fields=null;
        BasicDBObject results=null;
        MapReduceCommand cmd = null;
        InputStream io=null;
        String map=null;
        String reduce=null;
        try {
            SMRTUser user = this.validateToken(token, null);
            String customerId = user.getCustomerId();
            this.setCustomerId(customerId);   
            mongoDB = this.getDBInstance();
            surveyResultsCollection = mongoDB.getCollection("survey_results");
            json = new Gson();
                      
            map = "function () {\n" +
                " var startDate = new Date("+financialyear+", 06, 01); //Jul (7-1), month starts from 0\n" +
                " var endDate = new Date(startDate);\n" +
                " endDate.setFullYear(endDate.getFullYear()+1);\n" +
                "var days;\n"+
                "	if(this.st_st_11){//enrollment date and program exists\n" +
                "	   var date = new Date(this.st_st_11.txt);\n" +
                "	   var month = (date.getFullYear())+'-'+(date.getMonth()+1)+'-01'; \n" +
                "	   if (this.s1_req_11) sex=this.s1_req_11.txt; else sex= \"Prefer not to disclose\";\n" +
                "	   if (this.s1_req_13) race=this.s1_req_13.txt; else race = \"Not applicable\";\n" +
                "	   if (this.s1_req_12) hispanic = this.s1_req_12.txt; else hispanic = \"Prefer not to disclose\";\n" +
                "	   if (this.s1_req_14) days = (date - new Date(this.s1_req_14.txt))/31536000000; else 0;\n" +
                "	   if (days >=16 && days < 19) age = \"16-18\";\n" +
                "	   else if (days >=19 && days < 25) age = \"19-25\";\n" +
                "	   else if (days >=25 && days < 44) age = \"26-44\";\n" +
                "	   else if (days >=44 && days < 54) age = \"45-54\";\n" +
                "	   else if (days >=54 && days < 59) age = \"55-59\";\n" +
                "	   else age = \"60+\";\n";
                if (!age.isEmpty()) map+=" if('" + age + "'!=age){return;}\n";
                map+= "	   if(date >= startDate && date< endDate){ //enrolled prev Jul will be ignored\n" +
                "	  		if(this.e2_en_160){ \n" +
                "	  		    var program = this.e2_en_160; \n" +
                "	    		emit(new ObjectId(),{month: month, pid: this.participantId, prgm: program.txt, age:age, sex: sex, race: race, hispanic: hispanic}); //emit as 1 count for key\n" +
                "	  		}\n" +
                "  		if(this.e2_en_161){ \n" +
                "	  		    var program = this.e2_en_161; \n" +
                "	    		emit(new ObjectId(),{month: month, pid: this.participantId, prgm: program.txt, age:age,sex: sex, race: race, hispanic: hispanic});  //emit as 1 count for key\n" +
                "		}\n" +
                "  		if(this.e2_en_162){ \n" +
                "	  		    var program = this.e2_en_162; \n" +
                "	    		emit(new ObjectId(),{month: month, pid: this.participantId, prgm: program.txt, age:age,sex: sex, race: race, hispanic: hispanic});  //emit as 1 count for key\n" +
                "		}\n" +
                "		if(this.e2_en_163){ \n" +
                "	  		    var program = this.e2_en_163; \n" +
                "	    		emit(new ObjectId(),{month: month, pid: this.participantId, prgm: program.txt, age:age,sex: sex, race: race, hispanic: hispanic});  //emit as 1 count for key\n" +
                "		}\n" +
                "	  	if(this.e2_en_164){ \n" +
                "	  		   	var program = this.e2_en_164; \n" +
                "	    		emit(new ObjectId(),{month: month, pid: this.participantId, prgm: program.txt, age:age,sex: sex, race: race, hispanic: hispanic}); //emit as 1 count for key\n" +
                "		}\n" +
                "	}\n" +
                "	}\n" +
                "}";

            //Not required since we are going to emit only one record
            reduce = "function (key, values) { return;}";
            
            fields = new BasicDBObject();
            fields.put("customerId", user.getCustomerId());
            if (!sex.isEmpty()) fields.put("s1_req_11.val", sex);
            if (!ethnicity.isEmpty()) fields.put("s1_req_12.val", ethnicity);
            if (!viewoption.isEmpty()) fields.put("e2_en_183", viewoption);
            if (!race.isEmpty()) fields.put("s1_req_13.val", race);

            cmd = new MapReduceCommand(surveyResultsCollection, map, reduce,
                             null, MapReduceCommand.OutputType.INLINE, fields);
            result = surveyResultsCollection.mapReduce(cmd).results();
            
            results = new BasicDBObject("program", result);           
            io = new ByteArrayInputStream(json.toJson(results).getBytes());
            return this.exportReport("overall.jasper",io, format, "Program-Enrollments-Report.pdf");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
             throw ex; 
        } finally {
            mongoDB = null;
            surveyResultsCollection  = null;
            json = null;
            result = null;
            fields=null;
            results=null;
            cmd = null;
            io=null;
            map=null;
            reduce=null;
        }
    }
    
    @GET
    @Path("wages-report")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,MediaType.APPLICATION_OCTET_STREAM,MediaType.TEXT_HTML})
    public Response wagesReport(
            @QueryParam("token") String token,
            @QueryParam("date") String date,
            @QueryParam("f") String format
    )  throws Exception{
        DB mongoDB = null;
        DBCollection surveyResultsCollection  = null;
        Gson json = null;  
        BasicDBObject results = null; 
        InputStream io=null; 
        Iterable<DBObject> output = null;
        try {
            SMRTUser user = this.validateToken(token, null);
            Boolean permission = this.CheckUserPermission(user, "M21", "canview");
            if(!permission){
               throw new ForbiddenException("NO PERMISSION");
            }
            mongoDB = this.getDBInstance();
            surveyResultsCollection = mongoDB.getCollection("survey_results");
            BasicDBObject match = new BasicDBObject("customerId" ,user.getCustomerId());
            if(!date.isEmpty())match.append("emplt_01a.txt", new BasicDBObject("$lte",date)); 
            output = surveyResultsCollection.aggregate(Arrays.asList(
                (DBObject) new BasicDBObject("$match", 
                        match
                ),
                    (DBObject)new BasicDBObject("$project",
                    new BasicDBObject("startdate","$emplt_01a.txt")
                    .append("lastname", "$s1_req_2.txt")           
                    .append("firstname","$s1_req_1.txt")   
                    .append("jobtitle","$emplt_02.txt")
                    .append("company","$emplt_01.txt")
                    .append("hourlywage","$emplt_04.txt")
                    .append("weeklyhours","$emplt_05.txt"))   
            )).results();
            
            List<DBObject> res = new ArrayList<>();
            output.forEach(res::add);  
            Map<String,Object> map = new HashMap();
            map.put("report", res);
            io = new ByteArrayInputStream(new Gson().toJson(map).getBytes());
            return this.exportReport("Wages-Report.jasper", io, format, "Wages-Report.pdf");            
        }
        catch(Exception ex){
             System.out.println(ex.getMessage());
             throw ex;
        }  
    }
    
    @GET
    @Path("student-enrollment-report")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,MediaType.APPLICATION_OCTET_STREAM,MediaType.TEXT_HTML})
    public Response studentEnrollment(
            @QueryParam("token") String token,
            @QueryParam("date") String date,
            @QueryParam("f") String format,
            @QueryParam("status") String status,
            @QueryParam("group") String group
    )  throws Exception{
        DB mongoDB = null;
        DBCollection surveyResultsCollection  = null;
        DBCollection groupCollection  = null;
        Gson json = null;  
        BasicDBObject results = null; 
        InputStream io=null; 
        Iterable<DBObject> output = null;
        try { 
            SMRTUser user = this.validateToken(token, null);
            Boolean permission = this.CheckUserPermission(user, "M22", "canview");
            if(!permission){
               throw new ForbiddenException("NO PERMISSION");
            }
            mongoDB = this.getDBInstance();
            surveyResultsCollection = mongoDB.getCollection("survey_results");
            BasicDBObject match = new BasicDBObject();
            if(!status.isEmpty())match.append("status", status); 
            if(group.isEmpty()){
            if(!date.isEmpty())match.append("emplt_01a.txt", new BasicDBObject("$lte",date));
            match.append("customerId" ,user.getCustomerId());
            output = surveyResultsCollection.aggregate(Arrays.asList(
                (DBObject) new BasicDBObject("$match", match), 
                    (DBObject)new BasicDBObject("$project", new BasicDBObject("startdate","$emplt_01a.txt")
                    .append("lastname", "$s1_req_2.txt")           
                    .append("firstname","$s1_req_1.txt")   
                    .append("campus","$prog_04b_sec.txt")
                    .append("status","$status")
                    .append("craft",new Gson().fromJson("{$cond: { if:  { $or: [{$eq:[\"$prog_04_sec.txt\", null ]}]}, then: \"$prog_04_sec.txt\", else: $prog_05_sec.txt}}",BasicDBObject.class)) 
                    )
               
            )).results();
            } else {
                if(!date.isEmpty())match.append("startdate", new BasicDBObject("$lte",date)); 
                groupCollection = mongoDB.getCollection("group");
                output = groupCollection.aggregate(Arrays.asList(
                    (DBObject) new BasicDBObject("$match", new BasicDBObject("name",group).append("customerId" ,user.getCustomerId())),
                    (DBObject) new BasicDBObject("$unwind", "$contents"),
                    (DBObject) new BasicDBObject("$group", new BasicDBObject("_id", "$contents")),
                    (DBObject) new BasicDBObject("$lookup",
                        new BasicDBObject("from","survey_results")      
                          .append("localField","_id")
                          .append("foreignField","participantId")
                          .append("as","user"))
                      ,
                    (DBObject) new BasicDBObject("$unwind", "$user"),
                    (DBObject)new BasicDBObject("$project", new BasicDBObject("startdate","$user.emplt_01a.txt")    
                        .append("lastname", "$user.s1_req_2.txt")           
                        .append("firstname","$user.s1_req_1.txt")   
                        .append("campus","$user.prog_04b_sec.txt")
                        .append("status","$user.status")
                        .append("craft",new Gson().fromJson("{$cond: { if:  { $or: [{$eq:[\"$user.prog_04_sec.txt\", null ]}]}, then: \"$user.prog_04_sec.txt\", else: $user.prog_05_sec.txt}}",BasicDBObject.class)) 
                    ),
                     (DBObject) new BasicDBObject("$match", match)
               
            )).results();
            }
            List<DBObject> res = new ArrayList<>();
            output.forEach(res::add);  
            Map<String,Object> map = new HashMap();
            map.put("report", res);
            io = new ByteArrayInputStream(new Gson().toJson(map).getBytes());
            return this.exportReport("Student - Enrollment.jasper", io, format, "Student-Enrollment-Report.pdf");            
        }
        catch(Exception ex){
             System.out.println(ex.getMessage());
             throw ex;
        }  
    }
    
    @GET
    @Path("nrs-table-2")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,MediaType.APPLICATION_OCTET_STREAM,MediaType.TEXT_HTML})
    public Response nrsTable2Report(
            @QueryParam("token") String token,
            @QueryParam("f") String format,
            @QueryParam("financialyear") String financialyear
    )  throws Exception{
        DB mongoDB = null;
        DBCollection surveyResultsCollection  = null;
        Gson json = null;
        Iterable<DBObject> result = null;
        BasicDBObject fields=null;
        MapReduceCommand cmd = null;
        InputStream io=null;
        String map=null;
        String reduce=null;
        
        try {
            SMRTUser user = this.validateToken(token, null);
            String customerId = user.getCustomerId();
            this.setCustomerId(customerId);

            mongoDB = this.getDBInstance();
            surveyResultsCollection = mongoDB.getCollection("survey_results");
            json = new Gson();
            map = "function () {\n" +
                        " var startDate = new Date("+financialyear+", 06, 01); //Jul (7-1), month starts from 0\n" +
                        " var endDate = new Date(startDate);\n" +
                        " endDate.setFullYear(endDate.getFullYear()+1);\n" +
                        "	if(this.st_st_11){//enrollment date and program exists\n" +
                        "	   var date = new Date(this.st_st_11.txt);  \n" +
                        "	   if(date >= startDate && date< endDate){ //enrolled prev Jul will be ignored\n" +
                        "	        if (this.s1_req_11) sex=this.s1_req_11.val; else return;\n" +
                        "	        if (this.s1_req_11.val>2) return;\n" +
                        "	   		if (this.s1_req_13) race=this.s1_req_13.val; else return;\n" +
                        "	   		if (this.s1_req_14) days = (date - new Date(this.s1_req_14.txt))/31536000000; else 0;\n" +
                        "	   		if (days >=16 && days < 19) age = \"16-18\";\n" +
                        "	   		else if (days >=19 && days < 25) age = \"19-25\";\n" +
                        "	   		else if (days >=25 && days < 44) age = \"26-44\";\n" +
                        "	   		else if (days >=44 && days < 54) age = \"45-54\";\n" +
                        "	   		else if (days >=54 && days < 59) age = \"55-59\";\n" +
                        "	   		else age = \"60+\";\n" +
                        "	   		emit(this.participantId,{age:age, sex: sex, race: race,count:1}); \n" +
                        "	}\n" +
                        "	}\n" +
                        "}";

            //Not required since we are going to emit only one record
            reduce = "function (key, values) { return;}";
            
            fields = new BasicDBObject();
            fields.put("customerId", user.getCustomerId());
         
            cmd = new MapReduceCommand(surveyResultsCollection, map, reduce,
                             null, MapReduceCommand.OutputType.INLINE, fields);
            result = surveyResultsCollection.mapReduce(cmd).results();
            BasicDBObject results = new BasicDBObject("enrollment", result);
            io = new ByteArrayInputStream(json.toJson(result).getBytes());
            return this.exportReport("NRS-Table-2.jasper", io, format, "NRS-Table-2.pdf");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
             throw ex;
        } finally {
            mongoDB = null;
            surveyResultsCollection  = null;
            json = null;
            result = null;
            fields=null;
            cmd = null;
            io=null;
            map=null;
            reduce=null;
        }
    }
    @GET
    @Path("status")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,MediaType.APPLICATION_OCTET_STREAM,MediaType.TEXT_HTML})
    public Response statusReport(
            @QueryParam("token") String token,
            @QueryParam("f") String format,
            @QueryParam("s") String sex,
            @QueryParam("a") String age,
            @QueryParam("r") String race,
            @QueryParam("e") String ethnicity,
            @QueryParam("financialyear") String financialyear
    )  throws Exception{
       
        DB mongoDB = null;
        DBCollection surveyResultsCollection  = null;
        Gson json = null;
        List<DBObject> result = null;
        BasicDBObject fields=null;
        BasicDBObject results=null;
        MapReduceCommand cmd = null;
        InputStream io=null;
        String map=null;
        String reduce=null;
        try {
            SMRTUser user = this.validateToken(token, null);
            String customerId = user.getCustomerId();
            this.setCustomerId(customerId);   
            mongoDB = this.getDBInstance();
            surveyResultsCollection = mongoDB.getCollection("survey_results");
            json = new Gson();

            String ageCondition="";
            if (!age.isEmpty()) 
            ageCondition = "days = (new Date() - new Date(this.s1_req_14.txt))/31536000000;\n" +
                " if (days >=16 && days < 19) age = \"16-18\";\n" +
                " else if (days >=19 && days < 25) age = \"19-25\";\n" +
                " else if (days >=25 && days < 44) age = \"26-44\";\n" +
                " else if (days >=44 && days < 54) age = \"45-54\";\n" +
                " else if (days >=54 && days < 59) age = \"55-59\";\n" +
                " else if(days >=60)age = \"60+\";\n" +
                " if('" + age + "'!=age){return;}\n";    
                
            
            Integer year = Calendar.getInstance().get(Calendar.YEAR);
            
            if(Calendar.getInstance().get(Calendar.MONTH)<6){
                year = year -1;
            }
            
            map = "function () {\n" +
            "	var startDate = new Date("+financialyear+", 06, 01); //Jul (7-1), month starts from 0\n" +
            "	var endDate = new Date(startDate);\n" +
            "	endDate.setFullYear(endDate.getFullYear()+1);\n" +
            "	if(isInit){\n" +
            "	   for(var date = new Date(startDate); date< endDate; date.setMonth(date.getMonth()+1)){\n" +
            "			emit((date.getFullYear())+'-'+(date.getMonth()+1)+'-01',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 0,status:'01-Intake'}); \n" +
            "			emit((date.getFullYear())+'-'+(date.getMonth()+1)+'-02',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 0,status:'02-Enrolling'}); \n" +
            "			emit((date.getFullYear())+'-'+(date.getMonth()+1)+'-03',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 0,status:'03-Enrolled'}); \n" +
            "			emit((date.getFullYear())+'-'+(date.getMonth()+1)+'-04',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 0,status:'04-Exited Enrollment'}); \n" +
            "			emit((date.getFullYear())+'-'+(date.getMonth()+1)+'-05',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 0,status:'05-Stop Out'}); \n" +
            "			emit((date.getFullYear())+'-'+(date.getMonth()+1)+'-06',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 0,status:'06-Exited Not Complete'});\n" +
            "			emit((date.getFullYear())+'-'+(date.getMonth()+1)+'-07',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 0,status:'07-Exited Complete'});\n" +
            "	   }\n" +
            "		isInit = false;  \n" +
            "	}\n" +
            "\n" +
            ageCondition+
            "	  \n" +
            "	if(this.st_st_06 && this.st_st_06.txt) { //Intake\n" +
            "		var date = new Date(this.st_st_06.txt);\n" +
            "		if(date >= startDate && date< endDate){\n" +
            "			emit(this.participantId+'01',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 1,status:'01-Intake',pid:this.participantId}); \n" +
            "		}\n" +
            "	}\n" +
            "\n" +
            "	if(this.st_st_10 && this.st_st_10.txt) { //Enrolling\n" +
            "		var date = new Date(this.st_st_10.txt);\n" +
            "		if(date >= startDate && date< endDate){\n" +
            "			emit(this.participantId+'02',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 1,status:'02-Enrolling',pid:this.participantId}); \n" +
            "		}\n" +
            "	}\n" +
            "\n" +
            "	if(this.st_st_11 && this.st_st_11.txt) { //Enrolled\n" +
            "		var date = new Date(this.st_st_11.txt);\n" +
            "		if(date >= startDate && date< endDate){\n" +
            "			emit(this.participantId+'03',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 1,status:'03-Enrolled',pid:this.participantId}); \n" +
            "		}\n" +
            "	}\n" +
            "\n" +
            "	if(this.st_st_13 && this.st_st_13.txt) { //Exited Enrollment\n" +
            "		var date = new Date(this.st_st_13.txt);\n" +
            "		if(date >= startDate && date< endDate){\n" +
            "			emit(this.participantId+'04',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 1,status:'04-Exited Enrollment',pid:this.participantId}); \n" +
            "		}\n" +
            "	}\n" +
            "\n" +
            "	if(this.st_st_17 && this.st_st_17.txt) { //status change 1\n" +
            "		var date = new Date(this.st_st_17.txt);\n" +
            "		if(date >= startDate && date< endDate){\n" +
            "			if(this.st_st_16 && this.st_st_16.val) {\n" +
            "				if(this.st_st_16.val == 1){\n" +
            "					emit(this.participantId+'05',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 1,status:'05-Stop Out',pid:this.participantId});\n" +
            "				}else if(this.st_st_16.val == 2){\n" +
            "					if(this.st_st_19 && this.st_st_19.val) {\n" +
            "						if(this.st_st_19.val == 1){\n" +
            "							 emit(this.participantId+'07',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 1,status:'07-Exited Complete',pid:this.participantId});\n" +
            "						}else if(this.st_st_19.val > 1){\n" +
            "							 emit(this.participantId+'06',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 1,status:'06-Exited Not Complete',pid:this.participantId});\n" +
            "						}\n" +
            "					}\n" +
            "				}\n" +
            "			}\n" +
            "		}\n" +
            "	}\n" +
            "\n" +
            "	if(this.st_st_24 && this.st_st_24.txt) { //status change 2\n" +
            "		var date = new Date(this.st_st_24.txt);\n" +
            "		if(date >= startDate && date< endDate){\n" +
            "			if(this.st_st_23 && this.st_st_23.val) {\n" +
            "				if(this.st_st_23.val == 1){\n" +
            "					emit(this.participantId+'05',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 1,status:'05-Stop Out',pid:this.participantId});\n" +
            "				}else if(this.st_st_23.val == 2){\n" +
            "					if(this.st_st_26 && this.st_st_26.val) {\n" +
            "						if(this.st_st_26.val == 1){\n" +
            "							 emit(this.participantId+'07',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 1,status:'07-Exited Complete',pid:this.participantId});\n" +
            "						}else if(this.st_st_26.val > 1){\n" +
            "							 emit(this.participantId+'06',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 1,status:'06-Exited Not Complete',pid:this.participantId});\n" +
            "						}\n" +
            "					}\n" +
            "				}else if(this.st_st_23.val == 3){\n" +
            "					 emit(this.participantId+'03',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 1,status:'03-Enrolled',pid:this.participantId});\n" +
            "				}\n" +
            "			}\n" +
            "		}\n" +
            "	}\n" +
            "\n" +
            "	if(this.st_st_31 && this.st_st_31.txt) { //status change 3\n" +
            "		var date = new Date(this.st_st_31.txt);\n" +
            "		if(date >= startDate && date< endDate){\n" +
            "			if(this.st_st_30 && this.st_st_30.val) {\n" +
            "				if(this.st_st_30.val == 1){\n" +
            "					emit(this.participantId+'05',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 1,status:'05-Stop Out',pid:this.participantId});\n" +
            "				}else if(this.st_st_30.val == 2){\n" +
            "					if(this.st_st_33 && this.st_st_33.val) {\n" +
            "						if(this.st_st_33.val == 1){\n" +
            "							 emit(this.participantId+'07',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 1,status:'07-Exited Complete',pid:this.participantId});\n" +
            "						}else if(this.st_st_33.val > 1){\n" +
            "							 emit(this.participantId+'06',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 1,status:'06-Exited Not Complete',pid:this.participantId});\n" +
            "						}\n" +
            "					}\n" +
            "				}else if(this.st_st_30.val == 3){\n" +
            "					 emit(this.participantId+'03',{month: (date.getFullYear())+'-'+(date.getMonth()+1)+'-01', count: 1,status:'03-Enrolled',pid:this.participantId});\n" +
            "				}\n" +
            "			}\n" +
            "		}\n" +
            "	}\n" +
            "}";

            //Not required since we are going to emit only one record
            reduce = "function (key, values) { return;}";
            
            fields = new BasicDBObject();
            fields.put("customerId", user.getCustomerId());
            if (!sex.isEmpty()) fields.put("s1_req_11.val", sex);
            if (!ethnicity.isEmpty()) fields.put("s1_req_12.val", ethnicity);
            if (!race.isEmpty()) fields.put("s1_req_13.val", race);
            
            cmd = new MapReduceCommand(surveyResultsCollection, map, reduce,
                             null, MapReduceCommand.OutputType.INLINE, fields);
            

            Map <String, Object> hm = new HashMap<>();
            hm.put("isInit", true);
            cmd.setScope(hm);
            result = (List<DBObject>) surveyResultsCollection.mapReduce(cmd).results();
            
            if(!result.iterator().hasNext()){
                //no result populate empty result
               List<DBObject> list = new ArrayList<DBObject>();
               
                for(int i=0; i< 12; i++){
                    int month = 7 + i;
                    int yr = Integer.parseInt(financialyear);
                    if(month >= 13){
                        month-=12;
                        yr++;
                    }
                    
                    list.add(new BasicDBObject("_id",yr+"-"+ month +"-01")
                        .append("value", 
                                new BasicDBObject("month",yr+"-"+ month +"-01")
                                .append("count", 0.0)
                                .append("status", "01-Intake")
                        ));
                    
                    list.add(new BasicDBObject("_id",yr+"-"+ month +"-02")
                        .append("value", 
                                new BasicDBObject("month",yr+"-"+ month +"-01")
                                .append("count", 0.0)
                                .append("status","02-Enrolling")
                        ));
                    
                    list.add(new BasicDBObject("_id",yr+"-"+ month +"-03")
                        .append("value", 
                                new BasicDBObject("month",yr+"-"+ month +"-01")
                                .append("count", 0.0)
                                .append("status", "03-Enrolled")
                        ));
                    
                    list.add(new BasicDBObject("_id",yr+"-"+ month +"-04")
                        .append("value", 
                                new BasicDBObject("month",yr+"-"+ month +"-01")
                                .append("count", 0.0)
                                .append("status", "04-Exited Enrollment")
                        ));
                    
                    list.add(new BasicDBObject("_id",yr+"-"+ month +"-05")
                        .append("value", 
                                new BasicDBObject("month",yr+"-"+ month +"-01")
                                .append("count", 0.0)
                                .append("status", "05-Stop Out")
                        ));
                    
                    list.add(new BasicDBObject("_id",yr+"-"+ month +"-05")
                        .append("value", 
                                new BasicDBObject("month",yr+"-"+ month +"-01")
                                .append("count", 0.0)
                                .append("status", "06-Exited Not Complete")
                        ));
                    
                    list.add(new BasicDBObject("_id",yr+"-"+ month +"-07")
                        .append("value", 
                                new BasicDBObject("month",yr+"-"+ month +"-01")
                                .append("count", 0.0)
                                .append("status", "07-Exited Complete")
                        ));
                }
               
                
                result = (List<DBObject>) list;
            }
           // results = new BasicDBObject("status", result);           
            io = new ByteArrayInputStream(json.toJson(result).getBytes());
            return this.exportReport("PA-Status.jasper",io, format, "Program-Enrollments-Report.pdf");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
             throw ex;
        } finally {
            mongoDB = null;
            surveyResultsCollection  = null;
            json = null;
            result = null;
            fields=null;
            results=null;
            cmd = null;
            io=null;
            map=null;
            reduce=null;
        }
    }
    @GET
    @Path("getFinancialYear")
    @Produces({MediaType.APPLICATION_JSON})
    public String getAllSurveyResultStatus(
            @QueryParam("token") String token
    )  throws Exception{
       try{
        SMRTUser user = this.validateToken(token, null); 
        DB mongoDB = this.getDBInstance();
        DBCollection customerCollection = mongoDB.getCollection("customer");
        BasicDBObject fields = new BasicDBObject();
        fields.put("createdOn",1);
        BasicDBObject condition = new BasicDBObject("_id",user.getCustomerId());
        List<DBObject> results = customerCollection.find(condition,fields).toArray();
        return new Gson().toJson(results);
           
       } catch (Exception ex) {
            System.out.println(ex.getMessage());
             throw ex;
        } finally {
           
        }
    }
}
