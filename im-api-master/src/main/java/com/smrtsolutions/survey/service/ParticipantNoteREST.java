/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.smrtsolutions.exception.InvalidParameterException;
import com.smrtsolutions.survey.model.Customer;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.survey.model.NameValuePair;
import com.smrtsolutions.survey.model.ParticipantNote;
import com.smrtsolutions.util.GsonUTCDateAdapter;
import com.smrtsolutions.util.Util;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
//import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
/**
 *
 * @author lenny
 */
//@Stateless
@Path("/participant/{participantId}/note")
public class ParticipantNoteREST extends SMRTAbstractFacade<ParticipantNote> {
    
        
    private static final Logger logger = LogManager.getLogger(ParticipantNoteREST.class);
    public String[] REQUIRED_PERMISSIONS = {SMRTRole.OTHERS_DATA_ADD,SMRTRole.OTHERS_DATA_EDIT};
    
    //@PersistenceProperty(name="test", value="")
    //@PersistenceContext(unitName = "SMRT_PU")
   // private EntityManager em;

    public ParticipantNoteREST() {
        super(ParticipantNote.class);
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
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ParticipantNote create(@PathParam("participantId") String participantId, @QueryParam("token") String token, ParticipantNote entity) throws Exception {
        System.out.println("CREATE CUST");
        SMRTUser u = this.validateToken(token);
        System.out.println("User id=" + u.getId());
        this.checkPermissions(REQUIRED_PERMISSIONS);
        entity.setCustomerId(u.getCustomerId());
        entity.setCreatedAt(Calendar.getInstance().getTime());
        entity.setAuthor(new NameValuePair(u.getName(), u.getId()));
        entity.setStatus(ParticipantNote.STATUS.UNOPENED.ordinal());
//        SMRTUser p = this.findUser(participantId);
//        entity.setParticipantId(p.getId());
        entity.setParticipantId(participantId);
        Customer c = this.findCustomer(u);
        String reasons = c.getSetting("casemanagement_notes_reasons", "Informational=1;Needs followup=2");
        String priority = c.getSetting("casemanagement_notes_priorities", "Low=1;Medium=2;High=3;Critical=4");
        List<NameValuePair> rl = Util.toNameValuePairList(reasons);
        
        if ( entity.getReason() != null && !Util.isEmpty(entity.getReason().getValue())) {
            NameValuePair rm = Util.findNameValuePairByValue(rl, entity.getReason().getValue());
            if ( rm != null) entity.setReason(rm);
            else throw new InvalidParameterException("Invalid reason for note " + entity.getReason().getValue() );
        } else {
            entity.setReason(rl.get(0));// first reason code is assumed as default
        }
        List<NameValuePair> pl = Util.toNameValuePairList(priority);
        if ( entity.getPriority() != null && !Util.isEmpty(entity.getPriority().getValue())) {
            NameValuePair rm = Util.findNameValuePairByValue(pl, entity.getPriority().getValue());
            if ( rm != null) entity.setPriority(rm);
            else throw new InvalidParameterException("Invalid priority for note " + entity.getPriority().getValue() );
        } else {
            entity.setPriority(pl.get(0));// first priority code is assumed as default
        } 
        // create activity for student
        String activity = null;
        
            DB mongoDB = this.getDBInstance();
            DBCollection lastactivityCollection = mongoDB.getCollection("activities");
            BasicDBObject update =  new BasicDBObject();
                update.put("activity","Staff - Note");
                update.put("kind","n/a");
                update.put("createdBy",u.getId());
                update.put("createduname",u.getLastname()+", "+u.getFirstname());
                update.put("createdOn",new Date());
                update.put("customerId",u.getCustomerId());
                update.put("activityfor",participantId);
                update.put("detail","n/a");
                update.put("information",entity.getContent());
                lastactivityCollection.insert(update);
        return super.create(entity);
    }

        
        
    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id, @QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token);
        this.checkPermissions(REQUIRED_PERMISSIONS);
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ParticipantNote find(@PathParam("id") Long id, @QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token);
        this.checkPermissions(REQUIRED_PERMISSIONS);
        return super.find(id);
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String getNotes( @QueryParam("token") String token
            , @PathParam("participantId") String participantId
            , @QueryParam("from") int from, @QueryParam("limit") int limit
            , @QueryParam("orderby") String orderby
            , @QueryParam("filter") String filter
    )  throws Exception{
        try {
            SMRTUser user = this.validateToken(token, null);
            String perm[] = new String[]{ SMRTRole.OTHERS_DATA_ADD,SMRTRole.OTHERS_DATA_EDIT, SMRTRole.OTHERS_DATA_SEARCH};
            this.checkPermissions(perm);
            String customerId = user.getCustomerId();
            this.setCustomerId(customerId);
            
            this.log("Gte notes");
            
//            SMRTUser p = this.findUser(participantId);
            
            String sql= "select n from ParticipantNote n where n.customerId = :customerId and n.participantId = :participantId order by n.createdAt desc";
            Query q = this.getEntityManager().createQuery(sql, ParticipantNote.class);
            q.setParameter("customerId", customerId);
            q.setParameter("participantId", participantId);
            
            q.setMaxResults(limit);
            q.setFirstResult(from);
            List<ParticipantNote> l = q.getResultList();
             Gson json = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
            return json.toJson(l);
         
            
            
         } catch (Exception e){
            error("get Notes failed.", e);
            //throw new Exception("APP ERROR", e);
            throw e;
        }
    }
    

    @GET
    @Path("download")
    @Produces("image/png")
    public Response getFileGridFS( )  throws Exception{
        try {
                String newFileName = "Shanks_at_Edd_War.png";
		DB db = this.getDBInstance();
		DBCollection collection = db.getCollection("downloads_meta");
                GridFS gridfs = new GridFS(db, "downloads");
                GridFSDBFile imageForOutput = gridfs.findOne(newFileName);
//                imageForOutput.writeTo("c://JavaWebHostingNew.png");
                String loc = "c://fileupload/shanks1234.png";
                OutputStream out = new FileOutputStream(new File(loc));
                int read = 0;
                InputStream in = imageForOutput.getInputStream();
                byte[] bytes = new byte[1024];
                while((read = in.read(bytes)) != -1){
                    out.write(bytes, 0, read);
                }
                out.flush();
//                out.close();
                System.out.println(imageForOutput);
                return Response.ok(out).build();
//                return new StreamingOutput() {
//
//                    @Override
//                    public void write(OutputStream out) throws IOException,
//                            WebApplicationException {
//                        MongoClient mongo = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
//                        String newFileName = "Shanks_at_Edd_War.png";
//                        DB db = mongo.getDB("smrt1");
//                        DBCollection collection = db.getCollection("downloads_meta");
//                        GridFS gridfs = new GridFS(db, "downloads");
//                        GridFSDBFile imageForOutput = gridfs.findOne(newFileName);
//        //                imageForOutput.writeTo("c://JavaWebHostingNew.png");
//                        String loc = "c://fileupload/shanks1234.png";
//                        out = new FileOutputStream(new File(loc));
//                        int read = 0;
//                        InputStream in = imageForOutput.getInputStream();
//                        byte[] bytes = new byte[1024];
//                        while((read = in.read(bytes)) != -1){
//                            out.write(bytes, 0, read);
//                        }
//                        out.flush();
//                        out.close();
//                        in.close();
//                    }
//                };
        } catch (Exception e){
            System.out.println("com.smrtsolutions.survey.service.ParticipantNoteREST.getFileGridFS() exception");
            throw e;
        }
    }
    
    @GET
    @Path("download_try2/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFileGridFSTry2(@PathParam("id")  String id )  throws Exception{
        
            Response response = null;
            DB mongoDB = this.getDBInstance();

            BasicDBObject query = new BasicDBObject();
            query.put("_id", id);
            GridFS fileStore = new GridFS(mongoDB, "downloads");
            GridFSDBFile gridFile = fileStore.findOne(query);

            if (gridFile != null && id.equalsIgnoreCase((String)gridFile.getId())) {

              InputStream in = gridFile.getInputStream();

              ByteArrayOutputStream out = new ByteArrayOutputStream();
              int data = in.read();
              while (data >= 0) {
                out.write((char) data);
                data = in.read();
              }
              out.flush();

              ResponseBuilder builder = Response.ok(out.toByteArray());

              builder.header("Content-Disposition", "attachment; filename="
                       + gridFile.getFilename());
              response = builder.build();
              } else {
                response = Response.status(404).
                  entity(" Unable to get file with ID: " + id).
                  type("text/plain").
                  build();
              }
       
            return response;
    }
    
}
