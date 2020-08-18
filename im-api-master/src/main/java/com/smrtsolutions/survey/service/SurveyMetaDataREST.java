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
import com.mongodb.DBObject;
import com.smrtsolutions.exception.InvalidParameterException;
import com.smrtsolutions.survey.model.Customer;
import com.smrtsolutions.survey.model.Dashboard;
import com.smrtsolutions.survey.model.NameValuePair;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.survey.model.Survey;
import com.smrtsolutions.survey.model.SurveyResult;
import com.smrtsolutions.survey.model.SurveySection;
import com.smrtsolutions.survey.model.SurveySectionQuestion;
import com.smrtsolutions.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
//import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
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
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author lenny
 */
//@Stateless
@Path("/customer/{customerId}/survey")
public class SurveyMetaDataREST extends SMRTAbstractFacade<Survey> {
    
        
    private static final Logger logger = LogManager.getLogger(SurveyMetaDataREST.class);
    public String[] REQUIRED_PERMISSIONS = {SMRTRole.ALL}; //admin
    
    //@PersistenceProperty(name="test", value="")
    //@PersistenceContext(unitName = "SMRT_PU")
   // private EntityManager em;

    public SurveyMetaDataREST() {
        super(Survey.class);
        //super.setTenantId(customerId);
    }

      
    @PUT
    @Path("/metadatafile")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    public String saveMetaData(@PathParam("customerId") String customerId,@Context HttpServletRequest req) 
            throws UnknownHostException {
        try{
            System.out.println("==============================================================================");
        	System.out.println("Hi I am here, Uploading a photo");
            if (ServletFileUpload.isMultipartContent(req)) {
              FileItemFactory fiFactory = new DiskFileItemFactory();
              ServletFileUpload fileUpload = new ServletFileUpload(fiFactory);

              List<FileItem> listItems = fileUpload.parseRequest(req);
              Iterator<FileItem> iter = listItems.iterator();
              
              String basePath = Paths.get(System.getenv("SMRT_DOCS"), "Uploads","metadata", customerId).toString();
              System.out.println("I am here Base path is: ============================"+basePath);
              while (iter.hasNext()) {
                FileItem item = iter.next();
                if (!item.isFormField()) {  //for file object
                    String fileName = item.getName();
                    //String filePath = System.getenv("USER_PHOTO");
                    //System.out.println("file path="+filePath);
                    
                    File dir = new File(basePath);
                    if(!dir.exists()){
                        dir.mkdirs();
                    }
                    String fullPath = Paths.get(basePath,fileName).toString();
                    File metadataFile = new File(fullPath);
                 
                    item.write(metadataFile);
                    
                } 
              }
            }
            return new Gson().toJson("Upload has been successful");
        } 
        catch (Exception e) {
            System.out.println(e);
            return "Error Uploading user photo";
        }
        
    }        
    
    
    @GET
    @Path("metadatafile/{filename}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,MediaType.APPLICATION_OCTET_STREAM})
    public Response getMetadataFile(@PathParam("customerId") String customerId,@PathParam("filename") String filename) throws Exception {
 
        try {
        	System.out.println("===============================================================================");
        	System.out.println("I am here inside getMetaDataFile");
            File file = null;
            String filePath = Paths.get(System.getenv("SMRT_DOCS"),"Uploads","metadata", customerId,filename).toString();
            file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
//            byte[] fileContent = new byte[(int) file.length()];
//            fileInputStream.read(fileContent);
            return Response
                    .ok(file, MediaType.APPLICATION_OCTET_STREAM)
                    .header("content-disposition","attachment;filename=\"" + filename+"\"")
                    .build();
        } catch (Exception ex) {
            return null;
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    @POST
    //@Override
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Survey create(@PathParam("customerId") String customerId, @QueryParam("token") String token, @QueryParam("password") String password
    , Survey entity) throws Exception {
        log("*** Survey create ***");
        //EntityTransaction t = this.getEntityManager().getTransaction();
        //t.begin(); 
        try {
            SMRTUser u = this.validateToken(token, customerId);
            DB mongoDB = this.getDBInstance();
            DBCollection customerCollection = mongoDB.getCollection("customer");
        
            if(customerCollection.count(new BasicDBObject("_id", u.getCustomerId()).append("superAdminPassword", password)) != 1){
               throw new InvalidParameterException("Invalid password. Access Denied");
            }
        System.out.println("test");
            this.getEntityManager().getTransaction().begin();
            entity.setCustomerId(customerId);
             DBCollection surveyCollection = mongoDB.getCollection("survey");
            if(entity.getIsDefault().equals("Yes")){
               
                surveyCollection.updateMulti(new BasicDBObject("customerId", u.getCustomerId()), 
                        new BasicDBObject("$set", new BasicDBObject("isDefault","No"))
                );
            }
            if(entity.getRecruitment().equals("Yes")){
                surveyCollection.updateMulti(new BasicDBObject("customerId",u.getCustomerId()),
                        new BasicDBObject("$set",new BasicDBObject("recruitment","No"))
                );
            }
            if(entity.getIntake().equals("Yes")){
                surveyCollection.updateMulti(new BasicDBObject("customerId",u.getCustomerId()),
                        new BasicDBObject("$set",new BasicDBObject("intake","No"))
                );
            }
            entity.setCreatedOn(new Date());
            if(entity.getId()!=null && !entity.getId().equals("")){
                this.getEntityManager().merge(entity); //edit Survey
            }else{
                this.getEntityManager().persist(entity); //add Survey
            }
            
            String surveyType = entity.getSurveyType();
            
            if(surveyType.equalsIgnoreCase("student")){
                surveyType = "demographics";
            }
            
            Dashboard record;
            
            record = new Dashboard();
            record.setCustomerId(customerId);
            record.setName(surveyType);
            record.setDescription(entity.getLabel()+" Dashboard");
            record.setTemplate("templates/casedashboard/"+ surveyType +"-dashboard.ejs");
            record.setSurveyId(entity.getId());
            
            
            //Set dashboard sections
            BasicDBList dashSections = new BasicDBList();
            
            //add Notes section to all dashboard
            dashSections.add(
                new BasicDBObject("label","Notes")
                .append("name","Notes")
                .append("section_order","1a")
                .append("template", new BasicDBObject("element","#notes_wrapper"))
                .append("isPrintableSection",true)
            );
            
            for(SurveySection section : entity.getSections()){
                
                BasicDBObject sectionData = new BasicDBObject("label",section.getLabel())
                        .append("name", section.getName())
                        .append("isValidatable", section.getIsValidatable())
                        .append("isPrintableSection", section.getIsPrintableSection())
                        .append("section_order", section.getSection_order())
                        .append("more_pos", section.getMore_pos())
                        .append("toggle_section", section.getToggle_section());
                
                 BasicDBList fields = new BasicDBList();
                 
                 Boolean sectionEditable = false;
                 
                 for(SurveySectionQuestion question : section.getQuestions()){
                     fields.add(question.getName());
                     
                     if(question.getIsEditable()){
                         sectionEditable = true;
                     }
                 }
                 
                 if(sectionEditable){
                     sectionData.append("isEditableSection", true);
                 }
                 sectionData.append("fields", fields);
                 
                
                dashSections.add(sectionData);
            }
            
            BasicDBList survey = new BasicDBList();
            survey.add(entity.getId());
            
            BasicDBObject content = new BasicDBObject("dashboard", 
                    new BasicDBObject("label", entity.getLabel())
                    .append("survey", survey)
                    .append("sections", dashSections)
            );
            //Gson json = new GsonBuilder().setPrettyPrinting().create();
            Gson json = new Gson();
            
            record.setContent(json.toJson(content));
            
            //Create or update dashboard
            Query q = this.getEntityManager().createQuery("select d.id from Dashboard d WHERE d.customerId = :customerId AND d.name = :name AND d.surveyId=:surveyId", String.class);
            q.setParameter("customerId", customerId);
            q.setParameter("name", surveyType);
            q.setParameter("surveyId", entity.getId());
            List<String> dashboardIds = q.getResultList();
            
            if(dashboardIds!=null && dashboardIds.size()>0){ //update dashboard
                record.setId(dashboardIds.get(0));
                this.getEntityManager().merge(record);
            }else{
                this.getEntityManager().persist(record);
            }
            
            
            
            /*
            //parent ids are not set to child table, so hack. TODO FIX THIS
            String surveyId = entity.getId();
            for ( SurveySection ss :entity.getSections()){
                //ss.setCustomer(c);
                
                //ss.setSurvey(entity);
                this.getEntityManager().persist(ss);
                    for ( SurveySectionQuestion ssq: ss.getQuestions()){
                    ssq.setCustomer(c);
                    ssq.setSurveySection(ss);
                    this.getEntityManager().persist(ssq);
                }
            }
            //*/
            this.getEntityManager().getTransaction().commit();
        } catch (Exception e){
            try { 
                this.getEntityManager().getTransaction().rollback();
            } catch (Exception ig){}
            throw e;
        }
        
        return entity;
        //return super.create(entity);casedashboard/demographics
        
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Survey edit(@PathParam("customerId") String customerId, @PathParam("id") String id, @QueryParam("token") String token, Survey entity) throws Exception {
        this.setCustomerId(customerId);
        entity.setId(id);
        SMRTUser u = this.validateToken(token, customerId);
        this.checkPermissions(REQUIRED_PERMISSIONS);
        Customer c = this.findCustomer(u);
        entity.setCustomerId(this.findCustomer(u).getId());
        return super.edit(entity);
    }

    @GET
    @Path("{id}/delete")
    public void remove(@PathParam("customerId") String customerId, @QueryParam("password") String password, @QueryParam("token") String token, @PathParam("id") String id) throws Exception {
        SMRTUser u = this.validateToken(token, customerId);
            //this.checkPermissions(REQUIRED_PERMISSIONS);
            Customer c = this.findCustomer(u);
            
             DB mongoDB = this.getDBInstance();
       DBCollection customerCollection = mongoDB.getCollection("customer");
       if(customerCollection.count(new BasicDBObject("_id", c.getId()).append("superAdminPassword", password)) != 1){
           throw new InvalidParameterException("Invalid password. Access Denied");
       }
        
        this.setCustomerId(c.getId());
        
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Survey find(@PathParam("customerId") String customerId, @QueryParam("token") String token, @PathParam("id") String id)  throws Exception{
        SMRTUser u = this.validateToken(token, customerId);
        Customer c = this.findCustomer(u);
        this.setCustomerId(c.getId());
        System.out.println("surveyID=" + id);
        Survey s = super.find(id);
        s = this.calculateAvailableStates(s, u, u.getId());
        return s;
    }
    
    
    @GET
    @Path("{id}/status")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public NameValuePair getStatus(@PathParam("customerId") String customerId, @QueryParam("token") String token, @PathParam("id") String id
            , @QueryParam("participantId") String participantId)  throws Exception{
        SMRTUser u = this.validateToken(token, customerId);
        this.checkPermissions(REQUIRED_PERMISSIONS);
        Customer c = this.findCustomer(u);
        this.setCustomerId(c.getId());
        System.out.println("surveyID=" + id);
        Survey s = super.find(id);
        s = this.calculateAvailableStates(s, u, participantId);
        return new NameValuePair(s.getStatus()+"",  s.getStatusDescription());
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<Survey> findAll(@PathParam("customerId") String customerId, @QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token, customerId);
        this.setCustomerId(u.getCustomerId());
        //return super.findAll();
        Customer c = this.findCustomer(u);
        
        String masterData = c.getSetting("survey_master_id", "");
        if ( !Util.isEmpty(masterData)  ) { // get surveys from master 
            return this.findAllSurveys(masterData);
        } else { // get surveys from customer
            //todo validate for admin token, else set customerId from token
            return this.findAllSurveys(u.getCustomerId());
        }
        
    }
    @GET
    @Path("surveystatus/{id}")
    @Produces ({MediaType.APPLICATION_JSON})
    public String status(@QueryParam("token") String token,@PathParam("id") String id)throws Exception{
        SMRTUser u = this.validateToken(token);
        
        DB mongodb =this.getDBInstance();
        DBCollection surveyCollection = mongodb.getCollection("survey");
        DBCollection surveyAllocateCollection = mongodb.getCollection("surveyallocate");
        DBCollection surveyResultCollection = mongodb.getCollection("survey_results");
        Iterable<DBObject> surveys = surveyCollection.aggregate(Arrays.asList( 
                (DBObject) new BasicDBObject("$match",
                    new BasicDBObject("customerId",u.getCustomerId())
                ),
                (DBObject) new BasicDBObject("$project",new BasicDBObject()
                    .append("name","$label")
                    .append("displayTab","$displayTab")
                    .append("isDefault","$isDefault") 
                    .append("displayOrder","$displayOrder") 
                    .append("createdOn","$createdOn")
                    .append("surveyType","$survey_type")    
                )
                
        )).results();
        Iterable<DBObject> surveyAllocateResult = surveyAllocateCollection.aggregate(Arrays.asList(
                (DBObject) new BasicDBObject("$match",
                    new BasicDBObject("userId",id)
                ),
                (DBObject) new BasicDBObject("$project",
                    new BasicDBObject("createdOn","$createdOn")
                    .append("surveyId","$survey_id")
                ),
                (DBObject) new BasicDBObject("$group",
                    new BasicDBObject("_id","$surveyId")
                    .append("createdOn",new BasicDBObject("$first","$createdOn"))
                )
        )).results();
        Iterable<DBObject> surveyResultstatus = surveyResultCollection.aggregate(Arrays.asList(
                (DBObject) new BasicDBObject("$match",
                    new BasicDBObject("participantId",id)
                ),
                (DBObject) new BasicDBObject("$project",
                    new BasicDBObject("status","$surveyStatus")
                        .append("completedDate","$completedDate")
                        .append("surveyStarted","$surveyStarted")
                )
        )).results();
       
        BasicDBObject res = new BasicDBObject("survey",surveys)
                .append("surveyallocate", surveyAllocateResult)
                .append("surveyresult", surveyResultstatus);
              return new Gson().toJson(res);
    }
    @GET
    @Path("metalist")
    @Produces({MediaType.APPLICATION_JSON})
    public String metalist(@QueryParam("token") String token, @QueryParam("password") String password) throws Exception {
        SMRTUser u = this.validateToken(token);
        DB mongoDB = this.getDBInstance();
        DBCollection surveyCollection = mongoDB.getCollection("survey");
        
        DBCollection customerCollection = mongoDB.getCollection("customer");
       if(customerCollection.count(new BasicDBObject("_id", u.getCustomerId()).append("superAdminPassword", password)) != 1){
           throw new InvalidParameterException("Invalid password. Access Denied");
       }
   

        BasicDBObject query = new BasicDBObject("customerId", u.getCustomerId());
        
        return new Gson().toJson(
                surveyCollection.find(query, 
                        new BasicDBObject("label",1)
                        .append("survey_type",1)
                        .append("description",1)
                        .append("metaDataFileName",1)
                        .append("displayTab",1)
                        .append("displayOrder",1)
                        .append("url",1)
                        .append("isDefault",1)
                        .append("recruitment",1)
                        .append("intake",1)
                        .append("allocationType",1)
                        .append("surveyEntrySequence",1)
                ).toArray()
        );
    }
    
    @GET
    @Path("demosurvey")
    @Produces({MediaType.APPLICATION_JSON})
    public String demosurvey(@QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token);
        DB mongoDB = this.getDBInstance();
        DBCollection surveyCollection = mongoDB.getCollection("survey");
        //.append("isDefault", new BasicDBObject("$ne", "Yes")).append("survey_type","student")
        BasicDBObject query = new BasicDBObject("customerId", u.getCustomerId())
                .append("survey_type",new BasicDBObject("$eq","student"))
                .append("isDefault",new BasicDBObject("$ne","Yes"));
        
        return new Gson().toJson(
                surveyCollection.find(query, 
                        new BasicDBObject("label",1)
                        .append("survey_type",1)
                        .append("description",1)
                        .append("metaDataFileName",1)
                        .append("displayTab",1)
                        .append("displayOrder",1)
                        .append("isDefault",1)
                        .append("recruitment",1)
                        .append("intake",1)
                        .append("surveyEntrySequence",1)
                ).toArray()
        );
    }
    
    @PUT
    @Path("metaedit")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String metaedit(@QueryParam("token") String token,@QueryParam("password") String password, Survey entity) throws Exception {
        SMRTUser u = this.validateToken(token);
        DB mongoDB = this.getDBInstance();
        DBCollection surveyCollection = mongoDB.getCollection("survey");
        DBCollection customerCollection = mongoDB.getCollection("customer");
        
        if(customerCollection.count(new BasicDBObject("_id", u.getCustomerId()).append("superAdminPassword", password)) != 1){
           throw new InvalidParameterException("Invalid password. Access Denied");
        }
        
        
        if(entity.getIsDefault().equals("Yes")){
            surveyCollection.updateMulti(new BasicDBObject("customerId", u.getCustomerId()), 
                    new BasicDBObject("$set", new BasicDBObject("isDefault","No"))
            );
        }
        if(entity.getRecruitment().equals("Yes")){
                surveyCollection.updateMulti(new BasicDBObject("customerId",u.getCustomerId()),
                        new BasicDBObject("$set",new BasicDBObject("recruitment","No"))
                );
            }
        if(entity.getIntake().equals("Yes")){
                surveyCollection.updateMulti(new BasicDBObject("customerId",u.getCustomerId()),
                        new BasicDBObject("$set",new BasicDBObject("intake","No"))
                );
            }
        
        surveyCollection.update(
                new BasicDBObject("_id", entity.getId()).append("customerId", u.getCustomerId()),
                new BasicDBObject("$set", 
                        new BasicDBObject("label", entity.getLabel())
                        .append("survey_type", entity.getSurveyType())
                        .append("displayTab", entity.getDisplayTab())
                        .append("displayOrder", entity.getDisplayOrder())
                        .append("description", entity.getDescription())
                        .append("isDefault",entity.getIsDefault())
                        .append("url",entity.getUrl())
                        .append("intake",entity.getIntake())
                        .append("recruitment",entity.getRecruitment())
                        .append("allocationType", entity.getAllocationType())
                        .append("surveyEntrySequence",entity.getSurveyEntrySequence())
                        
                )
        );
        surveyCollection.update(
                new BasicDBObject("_id", new BasicDBObject("$ne",entity.getId())).append("customerId", u.getCustomerId()),
                new BasicDBObject("$set", 
                        new BasicDBObject()
                        .append("isDefault","No")
                )
        );
       return new Gson().toJson("OK"); 
    }
    
    @Path("{surveyType}/surveyType")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<Survey> findBySurveyType(@PathParam("customerId") String customerId, @PathParam("surveyType") String surveyType, @QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token, customerId);
        this.setCustomerId(u.getCustomerId());
        //return super.findAll();
        Customer c = this.findCustomer(u);
        
        String masterData = c.getSetting("survey_master_id", "");
        if ( !Util.isEmpty(masterData)  ) { // get surveys from master 
            return this.findSurveysByType(u.getCustomerId(), surveyType);
        } else { // get surveys from customer
            return this.findSurveysByType(u.getCustomerId(), surveyType);
        }
        
    }
    
    @Path("{id}/status/{newstatus}")
    @PUT
    @Produces({MediaType.APPLICATION_JSON})
    public Survey updateStatus(@PathParam("customerId") String customerId, @QueryParam("token") String token
            , @PathParam("id") String sid, @PathParam("newstatus") int status
            , @QueryParam("participantId") String participantId
    ) throws Exception {
        SMRTUser u = this.validateToken(token, customerId);
        this.setCustomerId(u.getCustomerId());
        //return super.findAll();
        
        
        Customer c = this.findCustomer(u);
        Survey s = this.findSurvey(sid);
        s = this.calculateAvailableStates(s, u, participantId);
        boolean found = false;
        for ( NameValuePair nvp : s.getAvailableStates()){
            if ( nvp.getValue().equalsIgnoreCase(status+"")){
                found = true;
            }
        }
        if ( !found){
            throw new InvalidParameterException("Invalid status for survey");
        }
        
        //save the status setting
        String flow = s.getSetting(Survey.FLOW_TAG, "");
        if ( Survey.FLOW_CONTROLLED.equalsIgnoreCase(flow) || Survey.FLOW_SEQUENTIAL.equalsIgnoreCase(flow)){
            //TODO handle periodic controlled
            String controlflow = s.getSetting(Survey.FLOW_CONTROL_TAG, Survey.FLOW_CONTROLLED_ALL_PARTICIPANTS);
            if ( Survey.FLOW_CONTROLLED_ALL_PARTICIPANTS.equalsIgnoreCase(controlflow)){
                //save the status at the survey level
                try {
                    s.setStatus(status);
                    this.getEntityManager().getTransaction().begin();
                    this.getEntityManager().merge(s);
                    this.getEntityManager().getTransaction().commit();
                } catch (Exception e){
                    this.getEntityManager().getTransaction().rollback();
                    throw e;
                }
            } else if (Survey.FLOW_CONTROLLED_PARTICIPANT.equalsIgnoreCase(controlflow)) {
                //save the status at the participant level
                try {
                    SMRTUser p = this.findUser(participantId);                    
                    DB mongoDB = this.getDBInstance();
                    DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");

                    BasicDBObject query = new BasicDBObject();
                    query.put("participantId", p.getId());
                    query.put("customerId", p.getCustomerId());

                    BasicDBObject data = new BasicDBObject(
                        "$set",
                        new BasicDBObject("resultTime", new Date())
                        .append("surveyStatus."+s.getId(), status)
                    );

                    surveyResultCollection.update(query, data, true, false);
                } catch (Exception e){
                    throw e;
                }
            } else {
                throw new Exception("Invalid Survey setting for " + Survey.FLOW_CONTROL_TAG + " for survey id=" + s.getId());
            }
        }
        return s;
        
    }
    
    public Survey calculateAvailableStates(Survey s, SMRTUser user, String participantId) throws Exception{
        List<NameValuePair> states = new ArrayList<NameValuePair>();
        
        boolean isPeriodic = Util.toBoolean(s.getSetting(Survey.IS_PERIODIC_TAG, "false"));
        String flow = s.getSetting(Survey.FLOW_TAG, "");
        if ( Survey.FLOW_CONTROLLED.equalsIgnoreCase(flow)){
            //TODO handle periodic controlled
            String controlflow = s.getSetting(Survey.FLOW_CONTROL_TAG, Survey.FLOW_CONTROLLED_ALL_PARTICIPANTS);
           
            /*if ( Survey.FLOW_CONTROLLED_ALL_PARTICIPANTS.equalsIgnoreCase(controlflow)){
                //if ( s.getStatus() > 0){
                   states.add(new NameValuePair("Not Applicable", SurveyResult.STATUS.NOT_APPLICABLE.ordinal()+""));
                   states.add(new NameValuePair("Pending", SurveyResult.STATUS.PENDING.ordinal()+""));
                   states.add(new NameValuePair("In Progress", SurveyResult.STATUS.IN_PROGRESS.ordinal()+""));
                   states.add(new NameValuePair("Complete", SurveyResult.STATUS.COMPLETE.ordinal()+""));
                //}
            } else if (Survey.FLOW_CONTROLLED_PARTICIPANT.equalsIgnoreCase(controlflow)) {
                //use the state for the participant
                SMRTUser p = this.findUser(participantId);
                SurveyResult sr = this.getLatestSurveyResult(s, user, participantId);
                
                 
                
            } else {
                throw new Exception("Invalid Survey setting for " + Survey.FLOW_CONTROL_TAG + " for survey id=" + s.getId());
            }*/
            states.add(new NameValuePair("Not Applicable", SurveyResult.STATUS.NOT_APPLICABLE.ordinal()+""));
            states.add(new NameValuePair("Pending", SurveyResult.STATUS.PENDING.ordinal()+""));
            states.add(new NameValuePair("In Progress", SurveyResult.STATUS.IN_PROGRESS.ordinal()+""));
            states.add(new NameValuePair("Complete", SurveyResult.STATUS.COMPLETE.ordinal()+""));
        } else if ( Survey.FLOW_SEQUENTIAL.equalsIgnoreCase(flow)){
            //previous survey must be complete to start this survey
            throw new Exception("TODO -- Survey.FLOW_SEQUENTIAL ");
        }

        s.setAvailableStates(states);
        return s;
    }
    
    @Path("menulist")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getSurveyMenuList(@QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token);
        DB mongoDB = this.getDBInstance();
        DBCollection surveyCollection = mongoDB.getCollection("survey");
        return new Gson().toJson(surveyCollection.find(new BasicDBObject("customerId", u.getCustomerId()), 
                        new BasicDBObject("label",1)
                                .append("displayTab",1)
                                .append("displayOrder",1)
                                .append("survey_type",1)
                                .append("isDefault",1))
                        .toArray());
    }
    
    
/*
    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Survey> findRange(@PathParam("customerId") String customerId, @PathParam("from") Integer from, @PathParam("to") Integer to) throws Exception {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST(@PathParam("customerId") String customerId) throws Exception {
        this.setCustomerId(customerId);
        return String.valueOf(super.count());
    }
*/
    
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
    
    @Path("all")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<Survey> findBySurveyType(@QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token);
        //this.setCustomerId(u.getCustomerId());
        return super.findAll();
     }
    
    
}
