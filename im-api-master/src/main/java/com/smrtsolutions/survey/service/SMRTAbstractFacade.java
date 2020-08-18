/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MongoClient;
import com.smrtsolutions.exception.ForbiddenException;
import com.smrtsolutions.exception.InvalidParameterException;
import com.smrtsolutions.exception.InvalidTokenException;
import com.smrtsolutions.survey.model.Customer;
import com.smrtsolutions.survey.model.EntityBase;
import com.smrtsolutions.survey.model.NameValuePair;
import com.smrtsolutions.survey.model.ParticipantSurveyStatus;
import com.smrtsolutions.survey.model.ParticipantTask;
import com.smrtsolutions.survey.model.Permission;
import com.smrtsolutions.survey.model.PermissionTemplates;

import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.survey.model.Survey;
import com.smrtsolutions.survey.model.SurveyAllocate;
import com.smrtsolutions.survey.model.SurveyResult;

import com.smrtsolutions.survey.model.SurveySection;
import com.smrtsolutions.survey.model.SurveySectionQuestion;
import com.smrtsolutions.survey.model.Task;
import com.smrtsolutions.util.TokenUtil;
import com.smrtsolutions.util.Util;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.sql.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.query.JsonQLQueryExecuterFactory;

/**
 *
 * @author lenny
 */
public abstract class SMRTAbstractFacade<T> {
    private Class<T> entityClass;
    Map<String, String> hmap = new HashMap<String, String>();
    Map<String, String> hmapVal = new HashMap<String, String>();
    private String customerId ;
    
    public static final String SYSTEM_TOKEN="grt5wra527jbgtjwK*@4kdkdk(udhdsg1ggvRRDfqr452lhsv";
    
    private static MongoClient mongoClient;

    public SMRTAbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }
    
    abstract public void log(String message );
    abstract public String[] getRequiredPermissions();
    
    
    private EntityManager em = null;
    
    
    
    public Customer findCustomer(SMRTUser u) throws Exception{
        if ( u.isSystemTokenUser()) return u.getCustomer();
        Customer c = this.getEntityManager().find(Customer.class, u.getCustomerId());
        if ( c == null) {
            throw new InvalidParameterException("Invalid Customer key " + this.getCustomerId());
        }
       /*
        String s = c.getSettings();
        if ( s != null) {
            c.setSettingsMap(Util.parseMap(s, ":", "~~"));
        }*/
        return c;
    }
    
        
    public Customer findCustomer(String id) throws Exception{
        //if ( u.isSystemTokenUser()) return u.getCustomer();
        Customer c = this.getEntityManager().find(Customer.class, id);
        if ( c == null) {
            throw new InvalidParameterException("Invalid Customer key " + id);
        }
       /*
        String s = c.getSettings();
        if ( s != null) {
            c.setSettingsMap(Util.parseMap(s, ":", "~~"));
        }*/
        return c;
    }
    
    public Customer findCustomerByURLKey(String key) throws Exception{
        Query q  = this.getEntityManager().createQuery("select c from Customer c WHERE c.urlKey = :urlKey");
        q.setParameter("urlKey", key);
        Customer c = (Customer) q.getSingleResult();
        if ( c == null) {
            throw new Exception("Invalid Customer key " + key);
        }
        return c;
    }
    
    
    public Customer findMasterCustomer() throws Exception{
        String masterCustomerId = Customer.SYSTEM_MASTER_CUSTOMER_ID;
        Customer c = this.getEntityManager().find(Customer.class, masterCustomerId);
        if ( c == null) {
            throw new InvalidParameterException("Invalid Master Customer ");
        }
        return c;
    }
    
    public Survey findSurvey(String id) throws Exception{
        Survey c = this.getEntityManager().find(Survey.class, id);
        if ( c == null) {
            throw new InvalidParameterException("Invalid Survey id " + id);
        }
        return c;
    }
    
    public Task findTask(String id) throws Exception{
        Task c = this.getEntityManager().find(Task.class, id);
        if ( c == null) {
            throw new InvalidParameterException("Invalid Survey id " + id);
        }
        return c;
    }
    
    public List<Survey> findAllSurveys(String customerId) throws Exception{
        Query q = this.getEntityManager().createQuery("select s from Survey s WHERE s.customerId = :customerId", Survey.class);
        q.setParameter("customerId", customerId);
        return q.getResultList();
    }
    
    public List<Survey> findSurveysWithoutConfig(String customerId) throws Exception{
        Query q = this.getEntityManager().createQuery("select s.id,s.label from Survey s WHERE s.customerId = :customerId", Survey.class);
        q.setParameter("customerId", customerId);
        return q.getResultList();
    }
    
    public List<Survey> findUserSurvey(String customerId,String list) throws Exception{
        
        Query q =this.getEntityManager().createQuery("select s from SurveyAllocate s WHERE s.customerId = :customerId AND s.userId=:userId",Survey.class);
        q.setParameter("customerId",customerId);

        return q.getResultList();
    }
    
     public List<Survey> findDefaultSurveys(String CustomerId,List<String> ids) throws Exception{
       //  List<String> uid = new ArrayList<String>(Arrays.asList(id.split(",")));
        Query q = this.getEntityManager().createQuery("select s from Survey s WHERE s.customerId = :customerId AND (s.isDefault = 'Yes' OR s.allocationType = 'auto' OR s.id IN :ids) ORDER BY s.isDefault DESC, s.surveyEntrySequence ASC", Survey.class);
        q.setParameter("customerId", customerId);
        q.setParameter("ids", ids);
      //  q.setParameter("userId", uid);
        return q.getResultList();
    }
    public List<Survey> findSurveysByType(String customerId, String surveyType) throws Exception{
        Query q = this.getEntityManager().createQuery("select s from Survey s WHERE s.customerId = :customerId AND s.surveyType = :surveyType", Survey.class);
        q.setParameter("customerId", customerId);
        q.setParameter("surveyType", surveyType);
        return q.getResultList();
    }
    
    public Survey findSurveyByName(String customerId, String name) throws Exception{
        Query q = this.getEntityManager().createQuery("select s from Survey s WHERE s.customerId = :customerId and s.name = :surveyName", Survey.class);
        q.setParameter("customerId", customerId);
        q.setParameter("surveyName", name);
        
        List<Survey> l =  q.getResultList();
        if ( l == null || l.size()<=0) return null;
        else return l.get(0);
    }
    
    public SurveySection findSurveySection(long id) throws Exception{
        SurveySection c = this.getEntityManager().find(SurveySection.class, id);
        if ( c == null) {
            throw new InvalidParameterException("Invalid SurveySection id " + id);
        }
        return c;
    }
    
    public SurveyResult findSurveyResult(String id) throws Exception{
        SurveyResult c = this.getEntityManager().find(SurveyResult.class, id);
        if ( c == null) {
            throw new InvalidParameterException("Invalid Survey Result id " + id);
        }
        return c;
    }
    
    public ParticipantTask findParticipantTask(String id) throws Exception{
        ParticipantTask c = this.getEntityManager().find(ParticipantTask.class, id);
        if ( c == null) {
            throw new InvalidParameterException("Invalid Participant Task id " + id);
        }
        return c;
    }
    
    public SurveySectionQuestion findSurveyQuestion(Survey s, String keyName){
        for ( SurveySection ss : s.getSections()){
            for ( SurveySectionQuestion sq : ss.getQuestions()){
                if ( sq.getName().equalsIgnoreCase(keyName)){
                    return sq;
                }
            }
        }
        return null;
    }
    
    /*
    public Participant findParticipant(long id) throws Exception{
        Participant c = this.getEntityManager().find(Participant.class, id);
        if ( c == null) {
            throw new InvalidParameterException("Invalid Partcicipant id " + id);
        }
        return c;
    }*/
    
    public SMRTUser findUser(String id) throws Exception{
        SMRTUser c = this.getEntityManager().find(SMRTUser.class, id);
        if ( c == null) {
            throw new InvalidParameterException("Invalid User id " + id);
        }
        return c;
    }
    
    public PermissionTemplates findTemplate(String id) throws Exception{
        PermissionTemplates c = this.getEntityManager().find(PermissionTemplates.class, id);
        if ( c == null) {
            throw new InvalidParameterException("Invalid Template id " + id);
        }
        return c;
    }
    
    public PermissionTemplates findTemplateName(String name) throws Exception{
        //Query c = this.getEntityManager().createQuery("select s from permission_templates s WHERE  s.name = :name", PermissionTemplates.class);
        Query c = this.getEntityManager().createQuery("select s from PermissionTemplates s WHERE s.name = :name", PermissionTemplates.class);
        c.setParameter("name", name);
        //PermissionTemplates c = this.getEntityManager().find(PermissionTemplates.class, q);
        if ( c == null) { 
            return null;
        }
        return (PermissionTemplates) c.getResultList().get(0);
    }
    
    /*
    public SMRTRole findRole(long id) throws Exception{
        SMRTRole c = this.getEntityManager().find(SMRTRole.class, id);
        if ( c == null) {
            throw new InvalidParameterException("Invalid role id " + id);
        }
        return c;
    }*/
    
    public ParticipantSurveyStatus findParticipantSurveyStatus(String customerId, String surveyId, String participantId) throws Exception{
        Query q = this.getEntityManager().createQuery("select s from ParticipantSurveyStatus s WHERE s.customerId = :customerId and s.surveyId = :surveyId and s.participantId = :participantId", ParticipantSurveyStatus.class);
        q.setParameter("customerId", customerId);
        q.setParameter("surveyId", surveyId);
        q.setParameter("participantId", participantId);
        q.setMaxResults(1);
        List<ParticipantSurveyStatus> list = q.getResultList();
        if ( list == null || list.size()<= 0) return null;
        else return list.get(0);
    }
    

    public List<NameValuePair> setNameValuePairContentValue(List<NameValuePair> content, String keyName, String value){
         
        //find list by Key
        Optional<NameValuePair> pairFind = content.stream().filter(x -> x.getName().equalsIgnoreCase(keyName)).findFirst();
         
        //check is present
         if(pairFind.isPresent()){
             
             //present, get and set value
             NameValuePair pair = pairFind.get();
             pair.setValue(value);
         }else{
             //not present, create new and add to list
             NameValuePair pair = new NameValuePair(keyName, value);
             content.add(pair);
         }
        
         
        return content;
    } 
    
   /*protected EntityManager getEntityManager() throws Exception {
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SMRT_PU");
        return emf.createEntityManager();
  }*/

    //protected EntityManager em = null;
    //protected abstract EntityManager getEntityManager();

    public T create(T entity) throws Exception{
        this.getEntityManager().getTransaction().begin();
        try {
            //EntityBase be = (EntityBase) entity;
            //if ( this.getUser() != null ) be.setCreatedBy(this.getUser().getId());
           
            getEntityManager().persist(entity);
            this.getEntityManager().getTransaction().commit();
        } catch ( Exception e){
            
            this.getEntityManager().getTransaction().rollback();
            throw e;
        }
        
        return entity;
    }

    public T edit(T entity) throws Exception {
        try {
            EntityTransaction tx = this.getEntityManager().getTransaction();
            tx.begin();
            this.getEntityManager().merge(entity);
            tx.commit();
        } catch ( Exception e){
            System.out.println("Error:" + e.getMessage());
            this.getEntityManager().getTransaction().rollback();
        }
        return entity;
    }

    public void remove(T entity) throws Exception {
        try {
            this.getEntityManager().getTransaction().begin();
            getEntityManager().remove(getEntityManager().merge(entity));
            this.getEntityManager().getTransaction().commit();
        } catch ( Exception e){
            this.getEntityManager().getTransaction().rollback();
        }
    }

    public T find(Object id)  throws Exception{
        return getEntityManager().find(entityClass, id);
    }

        
    public List<T> findAll() throws Exception {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        //TODO use the customerId filter from findRange
        return getEntityManager().createQuery(cq).getResultList();
    }
    
    public List<T> findAll(Class t) throws Exception {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(t));
//TODO use the customerId filter from findRange
        return getEntityManager().createQuery(cq).getResultList();
    }
    
    //KriyaTec - Starts
    
    public T find(Class t, String ColumnName, String Condition) throws Exception {       
        CriteriaBuilder qb = getEntityManager().getCriteriaBuilder();
	CriteriaQuery<T> q = qb.createQuery(t);
	Root<T> root = q.from(t);
	q.where(qb.equal(root.get(ColumnName), Condition));
        return getEntityManager().createQuery(q).getSingleResult();
    }
    
    public List<T> findAll(String ColumnName, String Condition) throws Exception {       
        CriteriaBuilder qb = getEntityManager().getCriteriaBuilder();
	CriteriaQuery<T> q = qb.createQuery(entityClass);
	Root<T> root = q.from(entityClass);
	q.where(qb.equal(root.get(ColumnName), Condition));
        return getEntityManager().createQuery(q).getResultList();
    }
    
    public List<T> findAll(String ColumnName, String Condition, String OrderBy) throws Exception {       
        CriteriaBuilder qb = getEntityManager().getCriteriaBuilder();
	CriteriaQuery<T> q = qb.createQuery(entityClass);
	Root<T> root = q.from(entityClass);
	q.where(qb.equal(root.get(ColumnName), Condition));
        q.orderBy(qb.asc(root.get(OrderBy)));
        return getEntityManager().createQuery(q).getResultList();
    }
    //KriyaTec - Ends
    
    public List<T> findAllPublic() throws Exception {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        //TODO use the customerId filter from findRange
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<T> findRange(int[] range)  throws Exception{
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        
        //javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        javax.persistence.criteria.CriteriaQuery cq = cb.createQuery(entityClass);
        //cq.select(cq.from(entityClass));
        Root<EntityBase> c = cq.from(entityClass);
        //Metamodel m = getEntityManager().getMetamodel();
        //EntityType<EntityBase> C_ = (EntityType<EntityBase>) c.getModel(); //entityClass);
        
        //cq.where(cb.equal(c.get(C_.getAttribute(customerId)), this.getCustomerId()));
        cq.where(cb.equal(c.get("customerId"),  (String)this.getCustomerId()));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }
    //change in future fixed just for demo to show all users. Change according to support pagination later.
    public List<T> findRangeChangeInFuture(int[] range)  throws Exception{
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        
        //javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        javax.persistence.criteria.CriteriaQuery cq = cb.createQuery(entityClass);
        //cq.select(cq.from(entityClass));
        Root<EntityBase> c = cq.from(entityClass);
        //Metamodel m = getEntityManager().getMetamodel();
        //EntityType<EntityBase> C_ = (EntityType<EntityBase>) c.getModel(); //entityClass);
        
        //cq.where(cb.equal(c.get(C_.getAttribute(customerId)), this.getCustomerId()));
        cq.where(cb.equal(c.get("customerId"),  (String)this.getCustomerId()));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        
//        q.setMaxResults(range[1] - range[0] + 1);
//        q.setFirstResult(range[0]);
        return q.getResultList();
    }
    
    public long count()  throws Exception{
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        //TODO use the customerId filter from findRange
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).longValue();

    }
    
    //Method to get survey question key Labels
    public BasicDBObject getSurveyQuestionLabels(String customerId, BasicDBObject fields) throws UnknownHostException{
        DB mongoDB = this.getDBInstance();
        DBCollection surveyCollection = mongoDB.getCollection("survey");

        Iterable<DBObject> result = null;

        Gson json = new Gson();

        String keysString = json.toJson(fields);

        String map = "function () {" +
                "var keys = "+ keysString + ";"
                + "this.sections.forEach(section=>{" +
                "	section.questions.forEach(question=>{" +
                "	    if(keys[question.name]==1){" +
                "	     emit(null,{id:question.name, value: question.alias||question.label}); " +
                "	    }" +
                "       });" +
                "   });"+
                "}";

        String reduce = "function (key, values) {" +
                "   var result = {};\n" +
                "   values.forEach(item=>{\n" +
                "    result[item.id] = item.value;\n" +
                "  });\n" +
                "  return result;" +
                "}";

        MapReduceCommand cmd = new MapReduceCommand(surveyCollection, map, reduce,
                         null, MapReduceCommand.OutputType.INLINE, new BasicDBObject("customerId", customerId));
        result = surveyCollection.mapReduce(cmd).results();


        if(result.iterator().hasNext()){
            return (BasicDBObject) result.iterator().next().get("value");
        }
        return new BasicDBObject();
    } 

    /**
     * @return the customerId
     */
    public String  getCustomerId() {
        return customerId;
    }

    /**
     * @param customerId the customerId to set
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public void setCustomerId(long customerId) {
        this.customerId = customerId+"";
    }
    
    protected EntityManager getEntityManager() {
        /*
        if ( em == null ) {
            HashMap properties = new HashMap();
            properties.put(PersistenceUnitProperties.MULTITENANT_PROPERTY_DEFAULT, this.getCustomerId());   
            em = Persistence.createEntityManagerFactory("SMRT_PU", properties).createEntityManager();
        }
        return em;*/
        if ( em == null) {
            em = Persistence.createEntityManagerFactory("SMRT_PU").createEntityManager();
        }
        return em;
    }
    
    protected DB getDBInstance() throws UnknownHostException {
        if(mongoClient == null){
            String host = (String) getEntityManager().getProperties().get("eclipselink.nosql.property.mongo.host");
            int port = Integer.parseInt((String) getEntityManager().getProperties().get("eclipselink.nosql.property.mongo.port"));
            mongoClient = new MongoClient(host, port);
        }
        String db = (String) getEntityManager().getProperties().get("eclipselink.nosql.property.mongo.db");
        return mongoClient.getDB(db);
    }
    
    public void closeEntityManager() {
        if ( em != null) {
            em.close();
        }
    }
    
    
    public long decodeId(String id){
        //TODO DECODE
        return Long.parseUnsignedLong(id);
    }

    public String encodeId(long id){
        //TODO DECODE
        return id +"";
    }
    
    protected String loadResultItemsContent(long customerId, long surveyId, long userId, long resultId) throws Exception{
     /*   //Query q = this.getEntityManager().createQuery("select sri from SurveyResultItem sri where sri.customerId = :customerId "
        //            + " and sri.surveyId = :surveyId  and sri.surveyResult.id= :resultId", SurveyResultItem.class);
        log("loadResultItemsContent customerId=" + customerId + " serveyId=" + surveyId + " userid=" + userId + " resultId=" + resultId);
        Query q = this.getEntityManager().createNativeQuery("select sri.* from Survey_Result_Item sri where sri.customer_Id = ?customerId "
                   + " and sri.survey_Id = ?surveyId  and sri.survey_Result_id= ?resultId", SurveyResultItem.class);
        q.setHint("javax.persistence.cache.storeMode", "BYPASS");
        q.setParameter("customerId", customerId);
        q.setParameter("surveyId", surveyId );
        //q.setParameter("userId", userId);
        q.setParameter("resultId", resultId);
        List<SurveyResultItem> list = null;
        q.setMaxResults(10000);
        list = (List<SurveyResultItem>) q.getResultList();
        
        String s = "{";
        int f = 0;
        for ( SurveyResultItem sri: list){
            if ( f != 0) s+= ",";
            s += "'" + sri.getKeyName() + "' : '" + sri.getKeyValue() + "'";
            f = 1;
        }
        s += "}";
        log ( "data=" + s);
        return s;
        */
     throw new Exception("nyi");
    }
 
    
       protected String loadResultsItemsForAllSurveysForUser(long customerId, long participantId) throws Exception{
      /*  log("loadResultsItemsForAllSurveysForUser customerId=" + customerId + " userid=" + participantId );
        Query q = this.getEntityManager().createNativeQuery("select sri.* from survey_result_item sri join survey_result sr on sri.survey_result_id = sr.survey_result_id where sri.customer_Id = ?customerId and sr.customerId = sri.customer_id and sr.user_id = ?participantId order by sri.survey_id"
                   , SurveyResultItem.class);
        q.setHint("javax.persistence.cache.storeMode", "BYPASS");
        q.setParameter("customerId", customerId);
        q.setParameter("participantId", participantId );
        
        List<SurveyResultItem> list = null;
        q.setMaxResults(10000);
        list = (List<SurveyResultItem>) q.getResultList();
        
        String s = "{";
        int f = 0;
        for ( SurveyResultItem sri: list){
            if ( f != 0) s+= ",";
            s += "'" + sri.getKeyName() + "' : '" + sri.getKeyValue() + "'";
            f = 1;
        }
        s += "}";
        log ( "data=" + s);
        return s;*/
      
      throw new Exception("nyi");
    }
  
    public boolean isEmpty(String s){
        if ( s == null || s.trim().isEmpty()) return true;
        else return false;
    }
    
    public boolean isSystemToken(String token) {
        return Util.getValue(token).equals(SYSTEM_TOKEN);
    }
    
    public SMRTUser validateToken(String token) throws Exception {
        return this.validateToken(token, null);
    }
    
    public SMRTUser validateToken(String token, String customerId) throws Exception {
        SMRTUser user = null;
        try {
            if ( isSystemToken(token) ){
                this.log("SYSTEM TOKEN identified, so mock System user");
                //hack, to use some admin apis w/o having to create a token
                user = new SMRTUser();
                Customer m = new Customer();
                if ( !Util.isEmpty(customerId)){
                    m.setId(customerId);
                } else {
                    m.setId(Customer.SYSTEM_MASTER_CUSTOMER_ID);
                }
                List<SMRTRole> sroles = new ArrayList<SMRTRole>();
                SMRTRole sr = new SMRTRole();
                sr.setName("superuser");
                sr.setPermissions(SMRTRole.ALL);
                sroles.add(sr);
                m.setRoles(sroles);
                
                ///user.setCustomer(m);
                user.setCustomer(m);
                user.setId(SMRTUser.SYSTEM_USER_ID); //System 1
                user.setCustomerId(m.getId()+"");
                SMRTRole r = new SMRTRole();
                r.setPermissions(SMRTRole.ALL);
                List<String> roles = new ArrayList<String>();
                roles.add("superuser");
                user.setRoles(roles);
                user.setSystemTokenUser(true);
                
                
                
                System.out.println("SYS TOKEN, user role=superuser permissions="  );
            } else {
                System.out.println("NOT SYS TOKEN, validate token");
                user= TokenUtil.validateToken(token);
                user.setSystemTokenUser(false);
                
                
            }
        } catch (InvalidTokenException ite){
            //Response.status(Response.Status.UNAUTHORIZED).entity(user).build();
            throw ite;
        } 
        
        this.setUser(user);
        this.setCustomerId(user.getCustomerId());
        return user;
    }
    
    public List<Permission> GetUserPermissions(SMRTUser user){
        user = getEntityManager().find(user.getClass(), user.getId());
        return user.getPermissions();
    }
    
    public boolean CheckUserPermission(SMRTUser user, String moduleId, String permission){
        List<Permission> permissions = this.GetUserPermissions(user);
        if(permissions==null)
            return false;
        
        //filter permission by Module ID
        permissions = permissions.stream()
        .filter(item -> item.getModule_id().equals(moduleId))
        .collect(Collectors.toList());
        
        if(permissions.size()==0)
            return false;
        if(permission.equals("canview"))
          return permissions.get(0).getCanview().equals(true);
        if(permission.equals("canadd"))
          return permissions.get(0).getCanadd().equals(true);

        return permissions.get(0).getCanedit().equals(true);        
    }
    
    private SMRTUser _user = null;
    
    
    public void checkPermissions (String[] permissions ) throws Exception{
        for ( String p : permissions ){
            if ( this.hasPermission(this.getUser(), p)){
                return;
            }
        }
        throw new ForbiddenException("NO PERMISSION");
    }
    
     public List<SMRTRole> getCustomerRoles(SMRTUser user) throws Exception{
         System.out.println("getCustomRoles sys?=" + user.isSystemTokenUser());
         Customer c = user.isSystemTokenUser() ? user.getCustomer() : this.findCustomer(user);
         return c.getRoles();
     }
     
     public SMRTRole findRole(List<SMRTRole> roles, String name) {
         System.out.println("findRole name=" + name + " roles size=" + ( roles == null ? "NULL": roles.size()+""));
         for ( SMRTRole r : roles){
             System.out.println("findRole roleName=" + r.getName() + " name=" + name);
             if ( r.getName().equalsIgnoreCase(name)){
                 return r;
             }
         } 
         return null;
     }
    
    public boolean hasPermission(SMRTUser user, String permissions) throws Exception{
      String pc = Util.getValue(permissions).toUpperCase();
      System.out.println("pc=" + pc);
      //for ( SMRTRole r : user.getRoles()) {
      List<SMRTRole> customerRoles = this.getCustomerRoles(user);
      for ( String rn : user.getRoles()) {
          System.out.println("rnRole=" + rn);
          SMRTRole r = this.findRole(customerRoles, rn);
          if ( r != null){
            String permission = Util.getValue(r.getPermissions()).toUpperCase();
            if ( Util.isEmpty(permission)){ //load permissions for role
                //SMRTRole sr = this.findRole(r.getIdentifier()); //@TODO this needs to be fixed
                SMRTRole sr = this.findCustomer(user).findRole(r.getName());
                if ( sr != null) {
                    permission = sr.getPermissions();
                }
            }
            
            System.out.println("role perm=" + permission);
            String[] p = permission.split(",");
            //String pu = Util.getValue(permission).toUpperCase();
            for (String p1 : p) {
              String pi = Util.getValue(p1).toUpperCase();
              System.out.println("pi=" + pi);
              if (  pi.equals(SMRTRole.ALL) || pi.equals(pc)){
                  System.out.println("pi matched=" + pi);
                  return true;
              }
            }
          } else {
              System.out.println("hasPermission USER has NO role=" + rn );
          }
        
      }
      return false;
  }

    /**
     * @return the _user
     */
    public SMRTUser getUser() {
        return _user;
    }

    /**
     * @param _user the _user to set
     */
    public void setUser(SMRTUser _user) {
        this._user = _user;
    }
    
    public Date getResultDate(Survey s) throws Exception{
        Date dt = null;
        String periodicity = s.getSetting(Survey.PERIODICITY_TAG, Survey.PERIODICITY_ONCE);
        
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        Date current = new Date();
        DateFormat df = new SimpleDateFormat("mm/dd/yyyy");
        if ( Survey.PERIODICITY_ONCE.equalsIgnoreCase(periodicity) ){
            return df.parse("01/01/2000");
        }
        if ( Survey.PERIODICITY_RANGE.equalsIgnoreCase(periodicity) ){
            String rg = s.getSetting(Survey.PERIODICITY_RANGE_TAG, "");
            if ( Util.isEmpty(rg)) throw new Exception("Invalid survey config, periodicity range value is required");
            String[] rga = rg.split(":");
            Date dt1 = null;
            Date dt2 = null;
            if ( rga.length > 0 ){
                try {
                    dt1 = df.parse(rga[0]);
                    dt2 = df.parse(rga[1]);
                } catch (Exception ce ){
                    throw new Exception("Invalid survey config, periodicity range value must be valid date range mm/dd/yyyy:mm/dd/yyyy");

                }
                if ( current.before(dt2) && current.before(dt1)) {
                    dt = dt1;
                } else {
                    throw new Exception ("Survey is not available. Range date has exceeded!");
                }
            } else {
                if ( Util.isEmpty(rg)) throw new Exception("Invalid survey config, periodicity range value is required");

            }
               
        } else if ( Survey.PERIODICITY_CALENDAR_ANNUAL.equalsIgnoreCase(periodicity) ){
            dt = df.parse("01/01/"+ year);
        } else if ( Survey.PERIODICITY_MONTHLY.equalsIgnoreCase(periodicity) ){
            int month = cal.get(Calendar.MONTH);
            dt = df.parse("01/" + month + "/" + year);
        } else { //if ( Survey.PERIODICITY_TIMESTAMP.equalsIgnoreCase(periodicity) ){
            dt = current;
        }
        return dt;
    }
    
    protected String getSubDomain(UriInfo uriInfo){
        System.out.println("getSubDomain");
        URI path = uriInfo.getAbsolutePath();
        System.out.println("URI path "+path);
        String hostname = path.getHost();
        System.out.println("URI hostname "+hostname);
        String subdomain = "";
        int firstDot = -1;
        firstDot = hostname.indexOf(".");
        if ( firstDot > 0) {
            subdomain = hostname.substring(0, firstDot);
        }
        System.out.println("URI subdomain "+subdomain);
        return subdomain;
    }
    
    protected Customer findCustomerBySubDomain(String subdomain) throws Exception{
        Customer c = null;
        try {
            
            String sql = "SELECT c FROM Customer c WHERE c.urlKey = :subdomain";
            Query q = this.getEntityManager().createQuery(sql, Customer.class);
            q.setParameter("subdomain", subdomain);
            List<Customer> cl = q.getResultList();
            if ( cl == null || cl.size()<= 0){
                throw new InvalidParameterException("Invalid subdomain " + subdomain);
            }
            return cl.get(0);
        } catch (Exception e){
            //logger.error("findCustomerBySubDmomain sundomain=" + subdomain, e);
            throw e;
        } finally {
            
        }
    }
    
    private String getReportFolderPath() {
        return Paths.get(System.getenv("SMRT_DOCS"),"Reports").toString();
    }
    
    public Response exportReport(String jasperRptName,InputStream io, String format,String exportName) {
         String jasperRpt = this.getReportFolderPath() + "/" + jasperRptName;
         JasperPrint  jasperPrintNew;
         File file = null;
            try {
                Map parameters = new HashMap(); 
                parameters.put(JsonQLQueryExecuterFactory.JSON_INPUT_STREAM,io);
                jasperPrintNew = JasperFillManager.fillReport(jasperRpt,parameters);
                 file = File.createTempFile("output.", ".tmp");
                 if (format.equals("pdf")) {
                        JasperExportManager.exportReportToPdfFile(jasperPrintNew, file.getAbsolutePath());
                        return Response
                            .ok(file, MediaType.APPLICATION_OCTET_STREAM)
                            .header("content-disposition",".pdf" + "attachment; filename = " + exportName)
                            .build();
                 } else {
                     JasperExportManager.exportReportToHtmlFile(jasperPrintNew, file.getAbsolutePath());
                        return Response
                            .ok(file) 
                            .build(); 
                 }
            } catch (JRException ex) {
                Logger.getLogger(SMRTAbstractFacade.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (IOException ex) {
                Logger.getLogger(SMRTAbstractFacade.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (Exception ex) {
                Logger.getLogger(SMRTAbstractFacade.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            finally {
                if(file!= null)
                    file.deleteOnExit();
            }
    }
    public String updatestatus(String id,String surveyId,String loginId) throws UnknownHostException, Exception  
            {
            DB mongoDB=this.getDBInstance();
            //SMRTUser u=new SMRTUser();
            SMRTUser u=this.findUser(loginId);
            DBCollection surveyResultCollection = mongoDB.getCollection("survey_results");
            BasicDBObject surveyFields = new BasicDBObject();
            BasicDBObject obj = new BasicDBObject();
            surveyFields.put("st_st_30", 1);
            surveyFields.put("st_st_31", 1);
            surveyFields.put("st_st_33", 1);
            surveyFields.put("st_st_23", 1);
            surveyFields.put("st_st_24", 1);
            surveyFields.put("st_st_26", 1);
            surveyFields.put("st_st_16", 1);
            surveyFields.put("st_st_17", 1);
            surveyFields.put("st_st_19", 1);
            surveyFields.put("st_st_05", 1);
            surveyFields.put("st_st_11", 1);
            surveyFields.put("st_st_13", 1);
            surveyFields.put("st_st_10", 1);
            surveyFields.put("st_st_06", 1);
            surveyFields.put("st_st_06a", 1);
            surveyFields.put("st_st_37", 1);
            surveyFields.put("st_st_40", 1);
            surveyFields.put("st_st_38", 1);
            surveyFields.put("st_st_44", 1);
            surveyFields.put("st_st_47", 1);
            surveyFields.put("st_st_45", 1);
            surveyFields.put("oc_ins_8", 1);
            surveyFields.put("isimport", 1);
            surveyFields.put("status", 1);
            surveyFields.put("surveyStatus", 1);
            BasicDBObject statusquery = new BasicDBObject();
            statusquery.put("participantId", id);
            
            
            hmapVal = surveyFields.toMap();
            DBCursor cursor = surveyResultCollection.find(statusquery,surveyFields);
            //surveyFields.remove("status");
            hmap = surveyFields.toMap();
            hmap.remove("status");
            if (cursor.hasNext()) { 
                obj = (BasicDBObject) cursor.next();
                for(Map.Entry<String, String> entry :hmap.entrySet()){
                    BasicDBObject val = (BasicDBObject) obj.get(entry.getKey());
                    if(val!=null){
                        //check if txt exists, else set val
                        entry.setValue((String)(val.get("txt")!=null ? val.get("txt"): val.get("val")));
                        hmapVal.put(entry.getKey(),(String) val.get("val"));
                    }else{
                        entry.setValue("");
                        hmapVal.put(entry.getKey(),"");
                    }
                }
                hmapVal.put("status",(String)obj.get("status"));
                //String oldStatus =obj.get("status");
            }else{
                for(Map.Entry<String, String> entry :hmap.entrySet()){
                    entry.setValue("");
                    hmapVal.put(entry.getKey(),"");
                }
            }
              if(!surveyId.isEmpty()){
                  try{
                    Survey s = this.findSurvey(surveyId);
                    BasicDBObject surveyField = new BasicDBObject();
                    surveyField.put("surveyStatus", 1);
                    DBObject status = new BasicDBObject();
                    status.put("participantId", id);
                    DBObject  result = surveyResultCollection.findOne(status, surveyField);
                    DBObject list= (DBObject)result.get("surveyStatus");
                    Object surveyStatus=list.get(surveyId);
                    
                    String type = s.getSurveyType();
                    String isdefault = s.getIsDefault();
                        if(type.equals("student") && isdefault.equals("Yes")&& surveyStatus.equals(3)){
                            surveyResultCollection.update(status,new BasicDBObject("$set",new BasicDBObject("st_st_06",new BasicDBObject("val", new SimpleDateFormat("MM/dd/yyyy").format(new Date()))
                                    .append("txt", new SimpleDateFormat("MM/dd/yyyy").format(new Date())))));
                        }
                    }
                    catch(Exception ex){
                    System.out.println("Exception finding survey" +ex);  
                  }
               
              }
              String oldStatus = hmapVal.get("status");
              
              
                //Status
                hmap.put("status", "");
                
                    if(!hmap.get("isimport").isEmpty() && hmap.get("isimport").equals("true")){
                        hmap.replace("status","Import");
                    }
                    if(!hmap.get("st_st_06a").isEmpty()){
                         hmap.replace("status","Recruitment");
                    }
                    
                    if(!hmap.get("st_st_06").isEmpty()){
                        hmap.replace("status","Intake");
                    }
                    if(!hmap.get("st_st_10").isEmpty()){
                         hmap.replace("status","Enrolling");
                    }
                    
                    if(!hmap.get("st_st_11").isEmpty()){
                         hmap.replace("status","Enrolled");
                    }
                    
                    if(!hmap.get("st_st_13").isEmpty()) {
                            hmap.replace("status","Exited Enrollment"); 
                        }
                    if(hmap.get("status").isEmpty()){
                        hmap.replace("status","Intake");
                    }
                    
              // if(hmap.get("status").isEmpty()){
                    //Status Change 1 - status
                    if (!hmap.get("st_st_17").isEmpty() && !hmap.get("st_st_16").isEmpty()) {
                        if(hmapVal.get("st_st_16").equals("1")){
                            hmap.put("status", "Stop Out");
                        }else if(hmapVal.get("st_st_16").equals("2")){
                            if(!hmapVal.get("st_st_19").isEmpty()){
                                if(hmapVal.get("st_st_19").equals("1")){
                                    hmap.put("status", "Exited Complete");
                                }else{
                                    hmap.put("status", "Exited Not Complete");
                                }
                            }
                        }
                        
                    }
               // }
               //if(hmap.get("status").isEmpty()){
                    //Status Change 2 - status
                    if (!hmap.get("st_st_24").isEmpty() && !hmap.get("st_st_23").isEmpty()) {
                        if(hmapVal.get("st_st_23").equals("1")){
                            hmap.put("status", "Stop Out");
                        }else if(hmapVal.get("st_st_23").equals("2")){
                            if(!hmapVal.get("st_st_26").isEmpty()){
                                if(hmapVal.get("st_st_26").equals("1")){
                                    hmap.put("status", "Exited Complete");
                                }else{
                                    hmap.put("status", "Exited Not Complete");
                                }
                            }
                        }else if(hmapVal.get("st_st_23").equals("3")){
                            hmap.put("status", "Enrolled");
                        }
                    }
               // }
               //Status Change 3 - status
                if (!hmap.get("st_st_31").isEmpty() && !hmap.get("st_st_30").isEmpty()) {
                    if(hmapVal.get("st_st_30").equals("1")){
                        hmap.put("status", "Stop Out");
                    }else if(hmapVal.get("st_st_30").equals("2")){
                        if(!hmapVal.get("st_st_33").isEmpty()){
                            if(hmapVal.get("st_st_33").equals("1")){
                                hmap.put("status", "Exited Complete");
                            }else{
                                hmap.put("status", "Exited Not Complete");
                            }
                        }
                    }else if(hmapVal.get("st_st_30").equals("3")){
                        hmap.put("status", "Enrolled");
                    }
                }
               //Status Change 4 - status
                if (!hmap.get("st_st_38").isEmpty() && !hmap.get("st_st_37").isEmpty()) {
                    if(hmapVal.get("st_st_37").equals("1")){
                        hmap.put("status", "Stop Out");
                    }else if(hmapVal.get("st_st_37").equals("2")){
                        if(!hmapVal.get("st_st_40").isEmpty()){
                            if(hmapVal.get("st_st_40").equals("1")){
                                hmap.put("status", "Exited Complete");
                            }else{
                                hmap.put("status", "Exited Not Complete");
                            }
                        }
                    }else if(hmapVal.get("st_st_37").equals("3")){
                        hmap.put("status", "Enrolled");
                    }
                }
               //Status Change 5 - status
                if (!hmap.get("st_st_45").isEmpty() && !hmap.get("st_st_44").isEmpty()) {
                    if(hmapVal.get("st_st_44").equals("1")){
                        hmap.put("status", "Stop Out");
                    }else if(hmapVal.get("st_st_44").equals("2")){
                        if(!hmapVal.get("st_st_47").isEmpty()){
                            if(hmapVal.get("st_st_47").equals("1")){
                                hmap.put("status", "Exited Complete");
                            }else{
                                hmap.put("status", "Exited Not Complete");
                            }
                        }
                    }else if(hmapVal.get("st_st_44").equals("3")){
                        hmap.put("status", "Enrolled");
                    }
                }
                
                
                if(oldStatus == null || !oldStatus.equals(hmap.get("status").toString()) ){
                   // set activity for user
            DBCollection lastactivityCollection = mongoDB.getCollection("activities");
            BasicDBObject update =  new BasicDBObject();
                Date newUser = null;
                if(u.getUsertype().equals("casemanager")){
                    update.put("activity","Staff - Status");
                    SMRTUser ta = this.findUser(id);
                    newUser = ta.getLast_login_date();
                }
                if(u.getUsertype().equals("student")){
                    update.put("activity","Client - Status");
                    newUser = u.getLast_login_date();
                }
                if(newUser != null){
                    update.put("kind","n/a");
                    update.put("createdBy",loginId);
                    update.put("createduname",u.getLastname()+", "+u.getFirstname());
                    update.put("createdOn",new Date());
                    update.put("surveyId",surveyId);
                    update.put("customerId",u.getCustomerId());
                    update.put("activityfor",id);
                    update.put("detail",hmap.get("status"));
                    update.put("information","n/a");
                    lastactivityCollection.insert(update);
                }
                }
                
                BasicDBObject document= new BasicDBObject();
                document.put("status", hmap.get("status"));
                BasicDBObject searchQuery = new BasicDBObject();
                searchQuery.append("participantId",id);
                BasicDBObject updateFields = new BasicDBObject("$set", document);
                surveyResultCollection.update(searchQuery,updateFields);
        return null;
            }
}

