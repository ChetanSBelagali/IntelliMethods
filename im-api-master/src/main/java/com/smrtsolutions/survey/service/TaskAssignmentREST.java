package com.smrtsolutions.survey.service;

import com.smrtsolutions.exception.InvalidParameterException;
import com.smrtsolutions.survey.model.Content;
import com.smrtsolutions.survey.model.Customer;
import com.smrtsolutions.survey.model.NameValuePair;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.survey.model.TaskAssignment;
import com.smrtsolutions.util.Util;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author SRINATH
 */
@Stateless
@Path("taskassignment")
public class TaskAssignmentREST extends SMRTAbstractFacade<TaskAssignment> {
    
     private static final Logger logger = LogManager.getLogger(TaskAssignment.class);
    //@PersistenceContext(unitName = "SMRT_PU")
    private EntityManager em;
    
    public String[] REQUIRED_PERMISSIONS = {SMRTRole.ALL};
        

    public TaskAssignmentREST() {
        super(TaskAssignment.class);
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
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public TaskAssignment create(@QueryParam("token") String token, TaskAssignment entity) throws Exception {
        System.out.println("CREATE TASK");
        SMRTUser u = this.validateToken(token);
//         entity.setCustomerId(u.getCustomerId());
//         entity.setCustomerId(token);
        return super.create(entity);
    } 
    
    
}
