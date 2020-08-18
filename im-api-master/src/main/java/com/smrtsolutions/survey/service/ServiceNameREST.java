
package com.smrtsolutions.survey.service;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.smrtsolutions.exception.InvalidParameterException;
import com.mongodb.WriteResult;
import com.smrtsolutions.survey.model.Content;
import com.smrtsolutions.survey.model.Location;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.survey.model.ServiceName;
import com.smrtsolutions.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import org.bson.types.ObjectId;

/**
 *
 * @author SRINATH
 */
@Stateless
@Path("servicename")
public class ServiceNameREST  extends SMRTAbstractFacade<ServiceName> {
    
    private static final Logger logger = LogManager.getLogger(ServiceNameREST.class);
    //@PersistenceContext(unitName = "SMRT_PU")
    private EntityManager em;
    
    //public String[] REQUIRED_PERMISSIONS = {SMRTRole.CUSTOMER_ADMIN, SMRTRole.ALL};
        

    public ServiceNameREST() {
         super(ServiceName.class);
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ServiceName create(@QueryParam("token") String token,ServiceName entity,@QueryParam("id") String id) throws Exception {
        System.out.println("CREATE CUST");
        SMRTUser u = this.validateToken(token);
        entity.setCustomerId(u.getCustomerId());
        entity.setCreatedBy(u.getId());
        entity.setCreatedOn(new Date());
        if(!id.isEmpty()){
        DB mongoDB = this.getDBInstance();
        DBCollection serviceNameCollection = mongoDB.getCollection("ServiceName");
        BasicDBObject fields = new BasicDBObject();
        fields.put("servicename", entity.getServiceName());
        fields.put("createdOn", entity.getCreatedOn());
        fields.put("createdBy", entity.getCreatedBy());
        fields.put("customerId", entity.getCustomerId());        
        BasicDBObject condition = new BasicDBObject("_id",id);
        WriteResult result = serviceNameCollection.update(condition,fields);
        }
        else
        {
        return super.create(entity);
        }
        return null;
    }

    @GET
    @Path("list")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON}) 
    public String list(@QueryParam("token")String token)throws Exception{
        
        SMRTUser u=this.validateToken(token);
        
        try{
            DB mongoDB=this.getDBInstance();
            DBCollection servicenamecollection=mongoDB.getCollection("ServiceName");
            
            Iterable<DBObject> output=servicenamecollection.aggregate(Arrays.asList(
              (DBObject)new BasicDBObject("$match",new BasicDBObject("customerId",u.getCustomerId()))   
              ,(DBObject) new BasicDBObject("$lookup",
              new BasicDBObject("from","SMRTUser")
              .append("localField","createdBy")
              .append("foreignField","_id")
              .append("as","createdBy"))      
                    
             ,(DBObject) new BasicDBObject("$unwind", 
                     new BasicDBObject("path", "$createdBy")
                        .append("preserveNullAndEmptyArrays", true)
                    )
             ,(DBObject)new BasicDBObject("$project",
                     new BasicDBObject("createdBy",new BasicDBObject("firstname","$createdBy.firstname").append("lastname","$createdBy.lastname"))
                    .append("servicename","$servicename")
                    .append("createdOn","$createdOn")
                    
                )
             ,(DBObject)new BasicDBObject("$sort",new BasicDBObject("servicename",1))
            
            )).results();  
                return new Gson().toJson(output);
        }
        catch(Exception ex){
            
            throw ex;
        }
    }
    @DELETE
    @Path("deletename/{id}")
    public String remove(@QueryParam("token")String token,@PathParam("id")String id)throws Exception{
        SMRTUser u=this.validateToken(token);
        
        DB mongoDB=this.getDBInstance();
        DBCollection usercollection=mongoDB.getCollection("SMRTUser");
        if(usercollection.count(new BasicDBObject("customerId",u.getCustomerId()).append("servicename",id))==0)
        {
        super.remove(super.find(id));
        return "true";
        }
        return "false";
               
    }
      @DELETE
    @Path("deletefilter/{id}")
//    @Produces({MediaType.APPLICATION_JSON})
    public String rmv(@QueryParam("token")String token,
            @PathParam("id")String id)throws Exception{
        
        SMRTUser user = this.validateToken(token, null);
            try{
                DB mongoDB = this.getDBInstance();
                DBCollection filtersCollection = mongoDB.getCollection("formfilters");
                BasicDBObject fields = new BasicDBObject("_id",id);
                WriteResult result = filtersCollection.remove(fields);
            }catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("Error ");
    } finally{
        //ToDO
    }
        return null;
    }
    @GET
    @Path("edit/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON}) 
    public String edit(@QueryParam("token")String token,@PathParam("id")String id)throws Exception{
    SMRTUser u=this.validateToken(token);
    DB mongoDB=this.getDBInstance();
    DBCollection servicecollection=mongoDB.getCollection("ServiceName");
    BasicDBObject fields = new BasicDBObject("_id",1)
            .append("servicename",1);
      BasicDBObject searchQuery=new BasicDBObject();
    searchQuery.put("_id", id);
    List<DBObject> results = servicecollection.find(searchQuery,fields).toArray();
    return new Gson().toJson(results);
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
             if(id.equals("1")){id=user.getId();}    
             if(user.getUsertype().equals("student")){
                 id = user.getId();
             }
             
             //set file path
             String filePath = Paths.get(System.getenv("SMRT_DOCS"),"Uploads","SurveyDocs",user.getCustomerId(),id).toString();
             File dir = new File(filePath);
             
             //check for directory exists or not and create 
             if(!dir.exists()){
                 dir.mkdirs();
             }
             
             if(fileItem != null){ //check if uploaded file was received
                String fileName = "userpicture.png";
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

     @GET
        @Path("getUserPhoto/{id}")
        @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
        public byte[] getLogo(@PathParam("id") String id,@QueryParam("token") String token) throws Exception {
            SMRTUser user = this.validateToken(token);
            try {
                File file = null;
               
                if(id.equals("1")){id=user.getId();} 
                    String filePath = Paths.get(System.getenv("SMRT_DOCS"),"Uploads","SurveyDocs",user.getCustomerId(),id,"userpicture.png").toString();
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
        
 @PUT
 @Path("createfilter/{id}")
   @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createOrUpdateFilter(
        @PathParam("id") String id,    
        @QueryParam("token") String token,
        @QueryParam("myfname") String myfiltername,
        @QueryParam("allChk") Boolean allchk,
        @QueryParam("fn") String filtername,
        @QueryParam("gn") String groupname,
        @QueryParam("s") String status,
        @QueryParam("isDflt") Boolean isDefault    
        
        ) throws Exception {
    try {
        SMRTUser user = this.validateToken(token, null); 
        DB mongoDB = this.getDBInstance();
        DBCollection filterCollection = mongoDB.getCollection("formfilters");
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
                .append("alluser",allchk)
                .append("filtername",filtername)
                .append("groupname",groupname)
                .append("status",status)
                .append("isDefault",isDefault)
                
                
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
    @Path("getFormFilter")
    @Produces({MediaType.APPLICATION_JSON})
    public String getFilters(@QueryParam("token") String token)throws Exception 
    {
    try {
        SMRTUser user = this.validateToken(token); 
        DB mongoDB = this.getDBInstance();
        DBCollection formFilterCollection = mongoDB.getCollection("formfilters");
        BasicDBObject condition = new BasicDBObject("createdBy",user.getId());
        DBCursor cursor = formFilterCollection.find(condition);
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
    @Override
    public void log(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getRequiredPermissions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    

}
