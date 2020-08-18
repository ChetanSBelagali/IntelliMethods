package com.smrtsolutions.survey.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import com.smrtsolutions.survey.model.Content;
import com.smrtsolutions.survey.model.Customer;
import com.smrtsolutions.survey.model.NameValuePair;
import com.smrtsolutions.survey.model.Partner;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Stateless
@Path("partner")
public class PartnerREST extends SMRTAbstractFacade<Partner> {
    
     private static final Logger logger = LogManager.getLogger(CustomerFacadeREST.class);
    //@PersistenceContext(unitName = "SMRT_PU")
    private EntityManager em;
    
    public String[] REQUIRED_PERMISSIONS = {SMRTRole.ALL};
        

    public PartnerREST() {
        super(Partner.class);
    }

        
    
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Partner create(@QueryParam("token") String token, Partner entity) throws Exception {
        System.out.println("CREATE PARTNER");
        SMRTUser u = this.validateToken(token);
         entity.setCustomerId(u.getCustomerId());
//         entity.setCustomerId(token);
        return super.create(entity);
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
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public List<Partner> findPartnerFromToken(@QueryParam("token") String token,@PathParam("id") String id) throws Exception {
        SMRTUser u = this.validateToken(token);
        
        return this.partnerListByCustomerId(id);
    }

        private   List<Partner> partnerListByCustomerId(String customerId) throws Exception{
     
        try {
            
            String sql = "SELECT p FROM Partner p WHERE p.customerId = :customerId";
            Query q = this.getEntityManager().createQuery(sql, Partner.class);
            q.setParameter("customerId", customerId);
//            q.setParameter("name", name);
               return  q.getResultList();
//            if ( cl == null || cl.size()<= 0){
//            throw new InvalidParameterException("Invalid content name=" + name);
//            }
//            c= cl.get(0);
//            //set the menu
//            Customer cr = this.findCustomer(customerId);
            /*
            List<String> menu = new ArrayList<String>();
            menu.add(cr.getSetting("menu_asset1_label", "Clients"));
            menu.add(cr.getSetting("menu_asset2_label", "Local"));
            menu.add(cr.getSetting("menu_asset3_label", "Resource"));
            c.setCustomerSettings(menu);*/
//            c.setCustomerSettings(cr.getSettings());

        } catch (Exception e){
            logger.error("findContent customerId=" + customerId, e);
            throw e;
        } finally {
            
        }
    }
    @DELETE
    @Path("delete")
    @Produces({MediaType.APPLICATION_JSON})
    public String deletePartner(@QueryParam("token") String token,@QueryParam("id") String id) throws Exception {
        SMRTUser u = this.validateToken(token);
            try {
            DB mongoDB = this.getDBInstance();
            DBCollection partnerCollection = mongoDB.getCollection("partner");
            BasicDBObject fields = new BasicDBObject("_id",id);
            WriteResult result = partnerCollection.remove(fields);

        } catch (Exception e){
            logger.error("delete Partner" +e);
            throw e;
        } finally {
            
        }
         return null;
    }
        
    @PUT
   
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Partner edit(@QueryParam("token") String token,@PathParam("customerId") String customerId, Partner entity) throws Exception {
        SMRTUser u = this.validateToken(token);
//        this.checkPermissions(REQUIRED_PERMISSIONS);
       // entity.setCustomerId(u.getCustomerId());
//        entity.setCustomerId(u.getCustomerId());
        return super.edit(entity);
    }
        

       
}