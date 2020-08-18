/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.survey.service;

import com.smrtsolutions.exception.InvalidParameterException;
import com.smrtsolutions.survey.model.Customer;
import com.smrtsolutions.survey.model.NameValuePair;
import com.smrtsolutions.survey.model.ParticipantTask;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.util.Util;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
 * @author joy
 */
@Path("/participant/{participantId}/task")
public class ParticipantTaskREST  extends SMRTAbstractFacade<ParticipantTask> {
    private static final Logger logger = LogManager.getLogger(ParticipantTaskREST.class);
    public String[] REQUIRED_PERMISSIONS = {SMRTRole.OTHERS_DATA_ADD,SMRTRole.OTHERS_DATA_EDIT};
    
    //@PersistenceProperty(name="test", value="")
    //@PersistenceContext(unitName = "SMRT_PU")
   // private EntityManager em;

    public ParticipantTaskREST() {
        super(ParticipantTask.class);
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
    public ParticipantTask createTask(@PathParam("participantId") String participantId
            , @QueryParam("token") String token
            , @QueryParam("groupTaskType") Boolean groupTaskType
            , ParticipantTask entity) throws Exception {
//        System.out.println("CREATE CUST");
        System.out.println("CREATE CUST\nCREATE CUST\nCREATE CUST\nCREATE CUST\nCREATE CUST\nCREATE CUST\n"+groupTaskType);
//        System.out.println("content is "+entity.getContent());
//        System.out.println("emails are "+toEmail);
//        System.out.println("target email is are "+entity.getReceipient().get(0).getTarget().getEmail());
        SMRTUser u = this.validateToken(token);
        System.out.println("User id=" + u.getId());
        this.checkPermissions(REQUIRED_PERMISSIONS);
        entity.setCustomerId(u.getCustomerId());
        entity.setCreatedAt(Calendar.getInstance().getTime());
//        entity.setAuthor(new NameValuePair(u.getName(), u.getId()));
//        entity.setAssignedByName(u.getName());
//        entity.setAssignedById(u.getId());
        entity.setAssignedBy(new NameValuePair(u.getName(), u.getId()));
        entity.setStatus(ParticipantTask.STATUS.UNOPENED.ordinal());
        SMRTUser p = this.findUser(participantId);
        if(entity.getRelatesTo() == null || Util.isEmpty(entity.getRelatesTo().getName()) || Util.isEmpty(entity.getRelatesTo().getValue())){
//            entity.setRelatesToId(p.getId());
//            entity.setRelatesToName(p.getName());
            entity.setRelatesTo(new NameValuePair(p.getName(), p.getId()));
        }
        System.out.println("akasam"+entity.getAssignedTo());
        if(entity.getAssignedTo() == null ){
//            entity.setAssignedToId(u.getId());
//            entity.setAssignedToName(u.getName());
            List<NameValuePair> at = new ArrayList<>();
            at.add(new NameValuePair(u.getName(), u.getId()));
            entity.setAssignedTo(at);
        } else {
            ArrayList<NameValuePair> at = (ArrayList<NameValuePair>)entity.getAssignedTo();
            for(int i=0; i < at.size(); i++){
                if( Util.isEmpty(at.get(i).getName()) || Util.isEmpty(at.get(i).getValue()) ){
                    at.get(i).setName(u.getName());
                     at.get(i).setValue(u.getId());
                }
            }
        }
        
        Customer c = this.findCustomer(u);
        String reasons = c.getSetting("casemanagement_task_reasons", "Informational=1;Needs followup=2");
        String priority = c.getSetting("casemanagement_task_priorities", "Low=1;Medium=2;High=3;Critical=4");
        String taskType = c.getSetting("casemanagement_task_type", "Transportation Issues=1;Work Issues=2;Family Issues=3;Utility Issues=4;Domestic Issues=5");
        String taskAssignWhere = c.getSetting("casemanagement_task_assignwhere", "Alamo=1;SNAP=2;Health Services=3;Alamo One Stop=4");
        String taskAdminType = c.getSetting("casemanagement_task_adminType", "DATA CHANGE=1;CLIENT/STUDENT OPERATIONS=2;CLIENT/STUDENT TRANSITION=3;STAFF OPERATIONS=4;OTHER=5");
        String taskAssignment = c.getSetting("casemanagement_task_taskAssignment", "Student Specific=0;General=1");
        String clientServiceType = c.getSetting("casemanagement_client_service_type", "Assign or Request Service=0;Service Provided - Enter Note=1");
        String clientCIPCode = c.getSetting("casemanagement_client_cip_code", "Biology=0;Maths=1");
        
        List<NameValuePair> rl = Util.toNameValuePairList(reasons);
        if ( entity.getReasonAction() != null && "task".equals(entity.getOperationType()) && !Util.isEmpty(entity.getReasonAction().getValue())) {
            NameValuePair rm = Util.findNameValuePairByValue(rl,entity.getReasonAction().getValue());
            if ( rm != null) entity.setReasonAction(rm);
            else throw new InvalidParameterException("Invalid reason for task " + entity.getReasonAction().getValue() );
        } else {
//            entity.setReasonActionValue(rl.get(0).getValue());// first reason code is assumed as default
//            entity.setReasonActionName(rl.get(0).getName());
//            entity.setReasonAction(new NameValuePair(rl.get(0).getName(), rl.get(0).getValue()));
        }
        List<NameValuePair> pl = Util.toNameValuePairList(priority);
        if ( entity.getPriority() != null && !Util.isEmpty(entity.getPriority().getValue())) {
            NameValuePair rm = Util.findNameValuePairByValue(pl, entity.getPriority().getValue());
            if ( rm != null) entity.setPriority(rm);
            else throw new InvalidParameterException("Invalid priority for task " + entity.getPriority().getValue() );
        } 
//        else {
//            entity.setPriorityValue(pl.get(0).getValue());// first priority code is assumed as default
//            entity.setPriorityName(pl.get(0).getName());
//        } 
        List<NameValuePair> tl = Util.toNameValuePairList(taskType);
        if ( entity.getTaskType() != null && !Util.isEmpty(entity.getTaskType().getValue())) {
            NameValuePair rm = Util.findNameValuePairByValue(tl, entity.getTaskType().getValue());
            if ( rm != null) entity.setTaskType(rm);
            else throw new InvalidParameterException("Invalid task_type for task " + entity.getTaskType().getValue() );
        } 
//        else {
//            entity.setTypeName(tl.get(0).getValue());// first priority code is assumed as default
//            entity.setTypeValue(tl.get(0).getName());
//        }
        List<NameValuePair> al = Util.toNameValuePairList(taskAssignWhere);
        if ( entity.getAssignWhere() != null && !Util.isEmpty(entity.getAssignWhere().getValue())) {
            NameValuePair rm = Util.findNameValuePairByValue(al, entity.getAssignWhere().getValue());
            if ( rm != null) entity.setAssignWhere(rm);
            else throw new InvalidParameterException("Invalid AssignWhere for task " + entity.getAssignWhere().getValue() );
        } 
//        else {
//            entity.setAssignWhereValue(al.get(0).getValue());// first priority code is assumed as default
//            entity.setAssignWhereName(al.get(0).getName());
//        }
        List<NameValuePair> atl = Util.toNameValuePairList(taskAdminType);
        if ( entity.getAdminTaskType() != null && !Util.isEmpty(entity.getAdminTaskType().getValue())) {
            NameValuePair rm = Util.findNameValuePairByValue(atl, entity.getAdminTaskType().getValue());
            if ( rm != null) entity.setAdminTaskType(rm);
            else throw new InvalidParameterException("Invalid AdminType for task " + entity.getAdminTaskType().getValue() );
        } 
        List<NameValuePair> tassign = Util.toNameValuePairList(taskAssignment);
        if ( entity.getTaskAssignment() != null && !Util.isEmpty(entity.getTaskAssignment().getValue())) {
            NameValuePair rm = Util.findNameValuePairByValue(tassign, entity.getTaskAssignment().getValue());
            if ( rm != null) entity.setTaskAssignment(rm);
            else throw new InvalidParameterException("Invalid TaskAssignment for task " + entity.getTaskAssignment().getValue() );
        }
        
        List<NameValuePair> cst = Util.toNameValuePairList(clientServiceType);
        if ( entity.getClientServiceType() != null && !Util.isEmpty(entity.getClientServiceType().getValue())) {
            NameValuePair rm = Util.findNameValuePairByValue(cst, entity.getClientServiceType().getValue());
            if ( rm != null) entity.setClientServiceType(rm);
            else throw new InvalidParameterException("Invalid client service type " + entity.getClientServiceType().getValue() );
        }
        
        List<NameValuePair> cip = Util.toNameValuePairList(clientCIPCode);
        if ( entity.getClientServiceCIPCode() != null && !Util.isEmpty(entity.getClientServiceCIPCode().getValue())) {
            NameValuePair rm = Util.findNameValuePairByValue(cip, entity.getClientServiceCIPCode().getValue());
            if ( rm != null) entity.setClientServiceCIPCode(rm);
            else throw new InvalidParameterException("Invalid client service type " + entity.getClientServiceCIPCode().getValue() );
        }
        
        if(entity.getAssignedTo() == null ){
//            entity.setAssignedToId(u.getId());
//            entity.setAssignedToName(u.getName());
                List<NameValuePair> n = new ArrayList<>();
                n.add(new NameValuePair(u.getName(), u.getId()));
                entity.setAssignedTo(n);
        } else {
            for(NameValuePair n: entity.getAssignedTo()){
                if(Util.isEmpty(n.getValue()) || Util.isEmpty(n.getName())){
                    n.setName(u.getName());
                    n.setValue(u.getId());
                }
            }
        }
        if(entity.getTaskAssignment() != null && "1".equals(entity.getTaskAssignment().getValue())){
//            System.out.println("task_assign_check");
            entity.setRelatesTo(null);
        }
        if("service".equals(entity.getOperationType()) && "Service Provided - Enter Note".equals(entity.getClientServiceType().getName())){
            entity.setStatus(ParticipantTask.STATUS.COMPLETE.ordinal());
        }
//        else {
//            entity.setAdminTaskTypeValue(atl.get(0).getValue());// first priority code is assumed as default
//            entity.setAdminTaskTypeName(atl.get(0).getName());
//        }
//        List<String> ccEmail = new ArrayList<>();
//        ccEmail = new ArrayList<>();
//        toEmail.add("smrtsolutionstest1@gmail.com");
//        Emailer.sendEmail(toEmail, ccEmail, entity.getPriority().getName(), entity.getContent());
        ParticipantTask retPT = entity;
        if(groupTaskType){
//            System.out.println("groupTaskType_check");
            List<NameValuePair> groupAssignToList = entity.getAssignedTo();
//            this.getEntityManager().getTransaction().begin();
            for(NameValuePair gtt: groupAssignToList){
//                System.out.println("groupTaskType_check\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
                List<NameValuePair> tat = new ArrayList<>();
                tat.add(gtt);
                ParticipantTask pt = new ParticipantTask(entity);
                pt.setAssignedTo(tat);
                retPT = super.create(pt);
//                this.getEntityManager().persist(entity);
            }
//            this.getEntityManager().getTransaction().commit();
        } else{
            retPT = super.create(entity);
        }

        return retPT;
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
    public ParticipantTask find(@PathParam("id") String id, @QueryParam("token") String token) throws Exception {
        SMRTUser u = this.validateToken(token);
//        this.checkPermissions(REQUIRED_PERMISSIONS);
        this.checkPermissions(new String[]{SMRTRole.OTHERS_DATA_ADD,SMRTRole.OTHERS_DATA_EDIT,SMRTRole.OTHERS_DATA_SEARCH});
        return super.find(id);
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ParticipantTask> getTasks( @QueryParam("token") String token
            , @QueryParam("from") int from, @QueryParam("limit") int limit
            , @QueryParam("orderby") String orderby
            , @QueryParam("filter") String filter
    )  throws Exception{
        try {
            SMRTUser user = this.validateToken(token, null);
            this.checkPermissions(REQUIRED_PERMISSIONS);
            String customerId = user.getCustomerId();
            this.setCustomerId(customerId);
            
            this.log("Gte tasks");
            
//            SMRTUser p = this.findUser(assignedToId);
            
//            String sql= "select n from ParticipantTask n where n.customerId = :customerId and n.assignedById = :assignedById order by n.createdAt desc";
            String sql= "select n from ParticipantTask n where n.customerId = :customerId and n.assignedBy.value = :assignedById";
            Query q = this.getEntityManager().createQuery(sql, ParticipantTask.class);
            q.setParameter("customerId", customerId);
            q.setParameter("assignedById", user.getId());
            
            q.setMaxResults(limit);
            q.setFirstResult(from);
            List<ParticipantTask> l = q.getResultList();
            System.out.println("1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n"+l.toString());
            for(ParticipantTask p : l){
                System.out.println(p.getAssignedTo().get(0).getName());
            }
            return l;
            
            
         } catch (Exception e){
            error("get Notes failed.", e);
            //throw new Exception("APP ERROR", e);
            throw e;
        }
    }
    
    @GET
    @Path("sort")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ParticipantTask> getSortTasks( @QueryParam("token") String token
            , @QueryParam("from") int from, @QueryParam("limit") int limit
            , @QueryParam("orderby") String orderby
            , @QueryParam("filter") String filter
            , @QueryParam("reason") String reason
            , @QueryParam("status") String status
            , @QueryParam("sortBy") String sortBy
            , @QueryParam("assignToBy") String assignToBy
            , @QueryParam("relatesTo") String relatesTo
            , @QueryParam("cipCode") String cipCode
    )  throws Exception{
        try {
            SMRTUser user = this.validateToken(token, null);
            this.checkPermissions(new String[]{SMRTRole.OTHERS_DATA_ADD,SMRTRole.OTHERS_DATA_EDIT,SMRTRole.OTHERS_DATA_SEARCH});
            String customerId = user.getCustomerId();
            this.setCustomerId(customerId);
            
            this.log("Get tasks");
            Customer c = this.findCustomer(user);
            
//            SMRTUser p = this.findUser(assignedToId);
            
//            String sql= "select n from ParticipantTask n where n.customerId = :customerId and n.assignedById = :assignedById order by n.createdAt desc";
            String sql= "select n from ParticipantTask n join n.assignedTo a where n.customerId = :customerId";
            System.out.println("1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n"+relatesTo);
            if(!Util.isEmpty(reason)){
                if(reason.equalsIgnoreCase("all")){
                    
                } else {
//                    sql += " and n.reasonActionValue = "+reason;
                    sql += " and n.reasonAction.value = "+reason;
                }
            }
            if(!Util.isEmpty(status)){
                if(status.equalsIgnoreCase("all")){
                    sql += "";
                } else if(status.equalsIgnoreCase("Not Completed")){
                    sql += " and n.status = 0";
                } else if(status.equalsIgnoreCase("Completed")){
                    sql += " and n.status = 3";
                } else if(status.equalsIgnoreCase("Future")){
                    sql += " and n.status = 2";
                }
            }
            if(!Util.isEmpty(cipCode)){
                if(cipCode.equalsIgnoreCase("all")){
                    
                } else {
//                    sql += " and n.reasonActionValue = "+reason;
                    sql += " and n.clientServiceCIPCode.value = "+cipCode;
                }
            }
            if(this.hasPermission(user, SMRTRole.OBSERVER_DASH)){
                
            }else if(!Util.isEmpty(assignToBy)){
                if(assignToBy.equalsIgnoreCase("My Tasks")){
//                    sql += " and n.assignedToId = '"+user.getId()+"'";
                    sql += " and a.value = '"+user.getId()+"'";
                } else if(assignToBy.equalsIgnoreCase("All Tasks")){
//                    sql += " and (n.assignedToId = '"+user.getId()+"' or n.assignedById = '"+user.getId()+"')";
                    sql += " and (a.value = '"+user.getId()+"' or n.assignedBy.value = '"+user.getId()+"')";
                } 
            }
            if(!Util.isEmpty(relatesTo)){
//                sql += " and n.relatesToId = '"+relatesTo+"'";
                sql += " and n.relatesTo.value = '"+relatesTo+"'";
                
            }
            if(!Util.isEmpty(sortBy)){
                if(sortBy.equalsIgnoreCase("Oldest")){
                    sql += " order by n.createdAt asc";
                } else if(sortBy.equalsIgnoreCase("Newest")){
                    sql += " order by n.createdAt desc";
                } 
            }
            
            
            Query q = this.getEntityManager().createQuery(sql, ParticipantTask.class);
            q.setParameter("customerId", customerId);
//            q.setParameter("assignedById", user.getId());
            
            q.setMaxResults(limit);
            q.setFirstResult(from);
            List<ParticipantTask> l = q.getResultList();
            return l;
            
            
         } catch (Exception e){
            error("get Notes failed.", e);
            //throw new Exception("APP ERROR", e);
            throw e;
        }
    }
    
    @PUT
    @Path ("complete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public void complete(@QueryParam("token") String token
            , @QueryParam("taskId") String taskId
            , @FormParam("messageComplete") String messageComplete
            , @FormParam("status") int status
    ) throws Exception {
        log("*** Complete Task ***");
        System.out.println("*** Complete Task ***");
        SMRTUser user = this.validateToken(token, null);
        this.checkPermissions(REQUIRED_PERMISSIONS);
        String customerId = user.getCustomerId();
        this.setCustomerId(customerId);
//        System.out.println("2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n"+ taskId);
        ParticipantTask pt = this.findParticipantTask(taskId);
        if ( pt == null){
//            System.out.println("2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n"+ taskId);
            throw new InvalidParameterException("Invalid ParticipantTask id="+ taskId);
        }
        if(messageComplete != null){
            pt.setMessageComplete(messageComplete);
        }
        if(status == 2){
            pt.setStatus(ParticipantTask.STATUS.NOTCOMPLETED_CLOSED.ordinal());
        }else if(status == 3){
            pt.setStatus(ParticipantTask.STATUS.COMPLETE.ordinal());
        }
        
        super.edit(pt);
    }
    
}
