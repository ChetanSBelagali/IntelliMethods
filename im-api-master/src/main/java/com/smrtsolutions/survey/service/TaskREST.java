package com.smrtsolutions.survey.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import com.smrtsolutions.exception.InvalidParameterException;
import com.smrtsolutions.survey.model.Content;
import com.smrtsolutions.survey.model.Customer;
import com.smrtsolutions.survey.model.NameValuePair;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import com.smrtsolutions.survey.model.Task;
import com.smrtsolutions.survey.model.TaskAssignment;
import com.smrtsolutions.survey.model.TaskNote;
import com.smrtsolutions.util.GsonUTCDateAdapter;
import com.smrtsolutions.util.Util;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static javafx.scene.Cursor.cursor;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.jose4j.json.internal.json_simple.JSONArray;
import org.jose4j.json.internal.json_simple.JSONObject;

/**
 *
 * @author SRINATH
 */
@Stateless
@Path("Task")
public class TaskREST extends SMRTAbstractFacade<Task>{

	private static final Logger logger = LogManager.getLogger(Task.class);
	//@PersistenceContext(unitName = "SMRT_PU")
	private EntityManager em;

	public String[] REQUIRED_PERMISSIONS = {SMRTRole.ALL};


	public TaskREST() {
		super(Task.class);
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
	public Task create(@QueryParam("token") String token, Task entity,@HeaderParam("Referer") String referer) throws Exception {
		try{
			System.out.println("CREATE TASK");
			SMRTUser u = this.validateToken(token);
			entity.setCustomerId(u.getCustomerId());
			entity.setCreatedBy(u.getId());
			entity.setCreatedOn(new Date());
			entity.setStatus("Open");
			//            if(entity.getTaskFor().equals("General")){
			//            entity.setClientId(u.getId());    
			//            }
			if(entity.getType().equals("Client Task")) {
				entity.setClientId(entity.getClientId());    
			}   
			Task task = this.create(entity);

			DB mongoDB = this.getDBInstance();

			String activity = null;
			String detail = null;
			// set activity
			if(entity.getTaskCategory().equals("Task")){
				activity = "Staff - Task; Create";
				detail = entity.getType()+"-"+entity.getTaskNature();
			}else{
				detail = entity.getTaskFor();
				if(entity.getType().equals("partnerService")){
					activity = "Partner - Service; Create";
				}
				else if(entity.getType().equals("staffService")){
					activity = "Staff - Service; Create";
				}
				else{
					activity = "Service - Provided; Create";
				}
			}
			DBCollection userCollection = mongoDB.getCollection("SMRTUser");
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.append("$set",new BasicDBObject()
					.append("lastactivity","Staff - Task")
					.append("last_login_date",new Date()));
			System.out.println("I am inside Staff - Task");
			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.append("_id",entity.getClientId());
			WriteResult result = userCollection.update(searchQuery,updateQuery);
			BasicDBList users = new BasicDBList();

			DBCollection lastactivityCollection = mongoDB.getCollection("activities");
			BasicDBObject update =  new BasicDBObject();
			if(entity.getAssignmentType().equals("Group")){
				//to set assignedTo from group
				DBCollection groupCollection = mongoDB.getCollection("group");


				BasicDBList classes = new BasicDBList();

				BasicDBObject group = (BasicDBObject) groupCollection.findOne(new BasicDBObject("_id", entity.getGroupId()));
				if(group.get("groupType").equals("SuperGroup")||group.get("groupType").equals("MyGroup")){
					BasicDBObject groupContents = getGroupContents(groupCollection, (BasicDBList)group.get("contents"));
					users.addAll((BasicDBList)group.get("contents"));
					classes.addAll((BasicDBList)group.get("contents"));
				}else if(group.get("groupType").equals("Classes")){
					classes.addAll((BasicDBList)group.get("contents"));
					users.addAll((BasicDBList)group.get("contents"));
				}else{ //client
					String us=getAllClients(u.getCustomerId());
					JsonParser parser = new JsonParser();
					JsonArray jArray = parser.parse(us).getAsJsonArray();
					int limit = jArray.size();
					ArrayList<String> list = new ArrayList<>();
					for(int i=0;i<limit;i++)
					{
						JsonObject jsObj = jArray.get(i).getAsJsonObject();
						list.add(jsObj.get("_id").getAsString());
					}                
					//users.addAll((BasicDBList)group.get("contents"));
					BasicDBList idList = new BasicDBList();
					idList.addAll(list);
					users.addAll(idList);
				}

				List<TaskAssignment> userList = new ArrayList<TaskAssignment>();

				if(classes.size()>0){
					//todo get all participants of classes
					DBCollection surveyResultColl = mongoDB.getCollection("survey_results");
					BasicDBObject query = new BasicDBObject("customerId", u.getCustomerId())
							.append("$or", new BasicDBList(){
								{
									add(new BasicDBObject("e2_en_74.val", new BasicDBObject("$in", classes)));
									add(new BasicDBObject("e2_en_80.val", new BasicDBObject("$in", classes)));
									add(new BasicDBObject("e2_en_86.val", new BasicDBObject("$in", classes)));
									add(new BasicDBObject("e2_en_92.val", new BasicDBObject("$in", classes)));
									add(new BasicDBObject("e2_en_98.val", new BasicDBObject("$in", classes)));
								}
							});
					DBCursor results = surveyResultColl.find(query, new BasicDBObject("_id", 1));
					while(results.hasNext()){
						BasicDBObject data = (BasicDBObject) results.next();
						if(!userList.contains((String)data.get("_id"))){
							TaskAssignment taskAssign = new TaskAssignment();
							taskAssign.setAssignedTo((String)data.get("_id"));
							userList.add(taskAssign);
						}
					}
				}

				for(Object el: users) {
					if(!userList.contains((String)el)){
						TaskAssignment taskAssign = new TaskAssignment();
						taskAssign.setAssignedTo((String)el);
						userList.add(taskAssign);
					}
					update.put("activity",activity);
					update.put("kind","n/a");
					update.put("createdBy",u.getId());
					update.put("createduname",u.getLastname()+", "+u.getFirstname());
					update.put("createdOn",new Date());
					update.put("taskId",task.getId());
					update.put("customerId",u.getCustomerId());
					update.put("activityfor",(String)el);
					update.put("detail",detail);
					update.put("information",entity.getDesc());
					update.remove("_id");
					lastactivityCollection.insert(update);
				}

				entity.setAssignedTo(userList);
			}

			if(entity.getAssignmentType().equals("Me")){
				List<TaskAssignment> assignees =  new ArrayList<TaskAssignment>();
				TaskAssignment assignee = new TaskAssignment();
				assignee.setAssignedTo(u.getId());
				assignees.add(assignee);
				entity.setAssignedTo(assignees);
			}

			if(entity.getAssignedTo()!=null){
				for(TaskAssignment ta : entity.getAssignedTo()) {
					ta.setTaskId(task.getId());
					ta.setCreatedBy(u.getId());
					ta.setStatus("Open");
					ta.setDueDate(task.getDueDate());
					ta.setUserType(task.getType());
					ta.setCustomerId(task.getCustomerId());


					this.SaveTaskAssignment(ta);
				}
			}
			//save lastactivity 
			String clientid = null;
			if(entity.getClientId() !=null){  clientid= entity.getClientId();}
			if(entity.getAssignmentType().equals("Me")) clientid = u.getId();
			if(!entity.getAssignmentType().equals("Group")){
				update.put("activity",activity);
				update.put("kind","n/a");
				update.put("createdBy",u.getId());
				update.put("createduname",u.getLastname()+", "+u.getFirstname());
				update.put("createdOn",new Date());
				update.put("taskId",task.getId());
				update.put("customerId",u.getCustomerId());
				update.put("activityfor",clientid);
				update.put("detail",detail);
				update.put("information",entity.getDesc());
				lastactivityCollection.insert(update);
			}
			//send notification msg to client
			if(task.getType().equals("Client Task") && !(u.getUsertype().equals("student"))){
				BasicDBObject matchQuery;

				if(task.getAssignmentType().equals("Group")){
					matchQuery = new BasicDBObject("participantId",new BasicDBObject("$in", users))
							.append("customerId",entity.getCustomerId());
				}else{
					matchQuery = new BasicDBObject("participantId",task.getClientId())
							.append("customerId",entity.getCustomerId());
				}

				DBCollection surveyCollection = mongoDB.getCollection("survey_results");
				Iterator<DBObject> output = surveyCollection.aggregate(Arrays.asList(
						(DBObject) new BasicDBObject("$match", matchQuery)
						,(DBObject) new BasicDBObject("$lookup", 
								new BasicDBObject("from" ,"customer")
								.append("localField","customerId")
								.append("foreignField","_id")
								.append("as","customer")
								)
						,(DBObject) new BasicDBObject("$unwind", 
								new BasicDBObject("path", "$customer")
								.append("preserveNullAndEmptyArrays", true)
								)  
						,(DBObject)new BasicDBObject("$project",
								new BasicDBObject("phonenumber","$s1_req_7.txt")
								.append("lastname", "$s1_req_2.txt")
								.append("customerId","$customer.urlKey")        
								.append("firstname","$s1_req_1.txt"))        
						)).results().iterator();

				while(output.hasNext()){
					System.out.println("I am here tried to send notification message======================================");
					BasicDBObject res = (BasicDBObject)output.next();
					String phone = (String)(res).get("phonenumber");
					String fname = (String) (res).get("firstname");
					String lname = (String) (res).getString("lastname");
					String customerId = (String) (res).getString("customerId");
					if (fname ==null){ fname =""; }
					String msg = " Hi " + fname + ",\r\n\r\n A New Task has been assigned. Sign in to your "+customerId.toUpperCase()+" student portal. \r\n\r\n"+ "https://"+customerId.toLowerCase()+"student.smrtdata.info";
					//String msg=" Hi " + fname + ",\r\n New Form - Please Sign in to your COM student portal. "+ " https://"+customerId.toLowerCase()+"student.smrtdata.info";
					if(phone !=null && !phone.isEmpty()){   
						phone = phone.replaceAll(" ", "").replaceAll("-", "").replaceAll("\\(", "").replaceAll("\\)", "");
						try{

							BasicDBObject message=new BasicDBObject();

							message.append("content", msg)
							.append("source_number", "18885039775")
							.append("source_number_type","INTERNATIONAL")
							.append("destination_number", "+1"+phone)
							.append("format","SMS");

							String req = "{\n" +
									"  \"messages\": [\n" + new Gson().toJson(message)+
									"  ]\n" +
									"}";
							String apiRes = Util.sendPostRequest("http://api.messagemedia.com/v1/messages",req);


							BasicDBObject resultJson = new Gson().fromJson(apiRes, BasicDBObject.class);
							DBCollection communicat= mongoDB.getCollection("communications");
							BasicDBObject document= new BasicDBObject();
							ObjectId id = new ObjectId();
							document.put("_id", id);
							document.put("type","SMS");
							document.put("customerId",u.getCustomerId());
							document.put("receiver",phone);
							document.put("userId",entity.getClientId());
							document.put("message",msg);
							document.put("status",resultJson);
							document.put("errormsg","");
							document.put("senton",Calendar.getInstance().getTime());
							document.put("sentby",u.getId());
							communicat.insert(document);
							System.out.println("Could be able to send notification====================================================");
						}
						catch(Exception e){
							this.logger.error("Cannot send text message - survey allocate", e);

							DBCollection communication= mongoDB.getCollection("communications");
							BasicDBObject document=new BasicDBObject();
							ObjectId id = new ObjectId();
							document.put("_id", id);
							document.put("type","SMS");
							document.put("customerId",u.getCustomerId());
							document.put("receiver",phone);
							document.put("userId",entity.getClientId());
							document.put("message",msg);
							document.put("status","failed");
							document.put("errormsg",e.getMessage());
							document.put("senton",Calendar.getInstance().getTime());
							document.put("sentby",u.getId());
							communication.insert(document);  
						}
					}
				}
			}



			return task;
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
			throw ex;
		} finally {
			//todo
		}   
	}
	private String getAllClients(String customerid) throws UnknownHostException 
	{
		DB mongoDB = this.getDBInstance();
		BasicDBObject document=new BasicDBObject();
		DBCollection studentCollection = mongoDB.getCollection("SMRTUser");
		BasicDBObject condition = new BasicDBObject("usertype","student").append("customerId",customerid);
		document.put("_id",1);
		DBCursor cursor = studentCollection.find(condition,document);
		Gson json = new Gson();
		return json.toJson(cursor.toArray());
	}
	private BasicDBObject getGroupContents(DBCollection groupCOllection, BasicDBList groupIds){
		DBCursor cursor = groupCOllection.find(new BasicDBObject("_id", new BasicDBObject("$in", groupIds)));

		BasicDBList users = new BasicDBList();
		BasicDBList classes = new BasicDBList();

		while(cursor.hasNext()){
			BasicDBObject group = (BasicDBObject) cursor.next();

			if(group.get("type").equals("Group")){
				BasicDBObject groupContents = getGroupContents(groupCOllection, (BasicDBList)group.get("contents"));
				users.addAll((BasicDBList)groupContents.get("users"));
				classes.addAll((BasicDBList)groupContents.get("classes"));
			}else if(group.get("type").equals("Class")){
				classes.addAll((BasicDBList)group.get("contents"));
			}else{ //client
				users.addAll((BasicDBList)group.get("contents"));
			}
		}

		return new BasicDBObject("users", users).append("classes", classes);
	}
	@GET
	@Path("getService")
	@Produces({MediaType.APPLICATION_JSON})
	public String getService(@QueryParam("id") String id,@QueryParam("token") String token)throws Exception
	{
		SMRTUser user=this.validateToken(token);
		//        Task userTask = this.find(id);
		//        return userTask;
		DB mongoDB=this.getDBInstance();
		DBCollection taskCollection = mongoDB.getCollection("Task");
		BasicDBList firstNameCond = new BasicDBList();
		firstNameCond.add("$client.s1_req_1.txt");
		firstNameCond.add("$clientUser.firstname");

		//ifNull cond for Client lastname field
		BasicDBList lastNameCond = new BasicDBList();
		lastNameCond.add("$client.s1_req_2.txt");
		lastNameCond.add("$clientUser.lastname");

		//ifNull cond for Assigned To firstname field
		BasicDBList firstNameAssignCond = new BasicDBList();
		firstNameAssignCond.add("$assignedParticipant.s1_req_1.txt");
		firstNameAssignCond.add("$assignedUser.firstname");

		//ifNull cond for Assigned To lastname field
		BasicDBList lastNameAssignCond = new BasicDBList();
		lastNameAssignCond.add("$assignedParticipant.s1_req_2.txt");
		lastNameAssignCond.add("$assignedUser.lastname");
		List<DBObject>  aggQuery= new ArrayList<DBObject>(Arrays.asList(
				(DBObject) new BasicDBObject("$match", 
						new BasicDBObject("_id",id).append("customerId",user.getCustomerId())
						),
				(DBObject) new BasicDBObject("$lookup", 
						new BasicDBObject("from" ,"TaskAssignment")
						.append("localField","_id")
						.append("foreignField","taskId")
						.append("as","assignedTo")
						),
				(DBObject) new BasicDBObject("$unwind", 
						new BasicDBObject("path", "$assignedTo")
						.append("preserveNullAndEmptyArrays", true)
						),
				(DBObject) new BasicDBObject("$lookup", 
						new BasicDBObject("from" ,"survey_results")
						.append("localField","assignedTo.assignedTo")
						.append("foreignField","participantId")
						.append("as","assignedParticipant")
						),
				(DBObject) new BasicDBObject("$lookup", 
						new BasicDBObject("from" ,"SMRTUser")
						.append("localField","assignedTo.assignedTo")
						.append("foreignField","_id")
						.append("as","assignedUser")
						),
				(DBObject) new BasicDBObject("$unwind", 
						new BasicDBObject("path", "$assignedUser")
						.append("preserveNullAndEmptyArrays", true)
						),
				(DBObject) new BasicDBObject("$unwind", 
						new BasicDBObject("path", "$assignedParticipant")
						.append("preserveNullAndEmptyArrays", true)
						),
				(DBObject) new BasicDBObject("$lookup", 
						new BasicDBObject("from" ,"SMRTUser")
						.append("localField","createdBy")
						.append("foreignField","_id")
						.append("as","createdBy")
						),
				(DBObject) new BasicDBObject("$unwind", 
						new BasicDBObject("path", "$createdBy")
						.append("preserveNullAndEmptyArrays", true)
						),
				(DBObject) new BasicDBObject("$lookup", 
						new BasicDBObject("from" ,"survey_results")
						.append("localField","clientId")
						.append("foreignField","participantId")
						.append("as","client")
						),
				(DBObject) new BasicDBObject("$lookup", 
						new BasicDBObject("from" ,"SMRTUser")
						.append("localField","clientId")
						.append("foreignField","_id")
						.append("as","clientUser")
						),
				(DBObject) new BasicDBObject("$unwind", 
						new BasicDBObject("path", "$client")
						.append("preserveNullAndEmptyArrays", true)
						),
				(DBObject) new BasicDBObject("$unwind", 
						new BasicDBObject("path", "$clientUser")
						.append("preserveNullAndEmptyArrays", true)
						),
				(DBObject) new BasicDBObject("$lookup", 
						new BasicDBObject("from" ,"partner")
						.append("localField","partnerId")
						.append("foreignField","_id")
						.append("as","partner")
						),
				(DBObject) new BasicDBObject("$lookup", 
						new BasicDBObject("from" ,"group")
						.append("localField","groupId")
						.append("foreignField","_id")
						.append("as","group")
						),
				(DBObject) new BasicDBObject("$unwind", 
						new BasicDBObject("path", "$group")
						.append("preserveNullAndEmptyArrays", true)
						),
				(DBObject) new BasicDBObject("$unwind", 
						new BasicDBObject("path", "$partner")
						.append("preserveNullAndEmptyArrays", true)
						),
				(DBObject) new BasicDBObject("$group", 
						new BasicDBObject("_id" ,"$_id")
						.append("client", new BasicDBObject("$first", 
								new BasicDBObject("_id","$client._id")
								.append("firstname",new BasicDBObject("$ifNull", firstNameCond))
								.append("lastname",new BasicDBObject("$ifNull", lastNameCond))
								.append("hphone","$client.s1_req_7.txt")
								.append("mphone","$client.s1_req_8.txt")
								.append("address","$client.s1_req_3.txt")
								.append("city","$client.s1_req_4.txt")
								.append("state","$client.s1_req_5.txt")
								.append("zip","$client.s1_req_6.txt")
								))
						.append("partner", new BasicDBObject("$first",new BasicDBObject("_id","$partner._id").append("name","$partner.partnerName")))
						.append("dueDate", new BasicDBObject("$first","$dueDate"))
						.append("type", new BasicDBObject("$first","$type"))
						.append("priority", new BasicDBObject("$first","$priority"))
						.append("createdOn", new BasicDBObject("$first","$createdOn"))
						.append("assignmentType", new BasicDBObject("$first","$assignmentType"))
						.append("taskNature", new BasicDBObject("$first","$taskNature"))
						.append("createdBy", new BasicDBObject("$first", 
								new BasicDBObject("_id","$createdBy._id")
								.append("firstname","$createdBy.firstname")
								.append("lastname","$createdBy.lastname")
								))
						.append("completedOn", new BasicDBObject("$first","$completedOn"))
						.append("adminTaskType", new BasicDBObject("$first","$adminTaskType"))
						.append("groupTaskType", new BasicDBObject("$first","$groupTaskType"))
						.append("groupName", new BasicDBObject("$first","$group.name"))
						.append("taskFor", new BasicDBObject("$first","$taskFor"))
						.append("partnerId", new BasicDBObject("$first","$partnerId"))
						.append("category", new BasicDBObject("$first","$category"))
						.append("desc", new BasicDBObject("$first","$desc"))
						.append("completedNotes", new BasicDBObject("$first","$notes"))
						.append("status", new BasicDBObject("$first","$status"))
						.append("taskCategory", new BasicDBObject("$first","$taskCategory"))
						.append("assignedTo", new BasicDBObject("$addToSet",
								new BasicDBObject("_id","$assignedUser._id")
								.append("firstname",new BasicDBObject("$ifNull", firstNameAssignCond))
								.append("lastname",new BasicDBObject("$ifNull", lastNameAssignCond))
								.append("completedOn","$assignedTo.completedOn")
								.append("status","$assignedTo.status")
								))
						)
				));
		Iterable<DBObject> count = taskCollection.aggregate(aggQuery).results();
		return new Gson().toJson(count);
	}
	@GET
	@Produces({ MediaType.APPLICATION_JSON})
	public String get(
			@QueryParam("token") String token,
			@QueryParam("type") String type,
			@QueryParam("for") String taskfor,
			@QueryParam("status") String status,
			//        @QueryParam("p") String priority,
			@QueryParam("sort") String sort,
			//        @QueryParam("order") String order,
			//        @QueryParam("cip") String cipcode,
			@QueryParam("clientid") String clientid,
			@QueryParam("order") int order,
			@QueryParam("start") int start,
			@QueryParam("limit") int limit,
			@QueryParam("client") String client,
			@QueryParam("showTask") String showtask, 
			@QueryParam("assign") String assignment


			) throws Exception{
		SMRTUser u = this.validateToken(token);

		DB mongoDB = this.getDBInstance();
		DBCollection taskCollection = mongoDB.getCollection("Task");
		List<DBObject> aggQuery=null;
		Iterable<DBObject> tasks=null;
		BasicDBList firstNameAssignCond =null;
		BasicDBList lastNameAssignCond =null;
		BasicDBObject cond=null;
		BasicDBObject conds=null;
		BasicDBList firstNameCond=null;
		BasicDBList list=null;
		BasicDBList list1=null;

		try {
			//list = new BasicDBList();
			//list.add(new BasicDBObject("assignedTo.assignedTo", u.getId()));
			//list.add(new BasicDBObject("createdBy", u.getId()));
			BasicDBObject matchStart = new BasicDBObject("customerId" ,u.getCustomerId())
					.append("status", new BasicDBObject("$ne", "Cancel"));
			//.append("$or", list);
			if (!type.isEmpty()) matchStart.append("type",type);
			if (!taskfor.isEmpty()) matchStart.append("taskFor",taskfor);
			//if (!priority.isEmpty()) matchStart.append("priority",priority);
			if (!status.isEmpty()) matchStart.append("status",status);
			//if (!client.isEmpty()) matchStart.append("clientId", client);
			BasicDBObject match = new BasicDBObject();
			if(!client.isEmpty()){
				BasicDBList nameList = new BasicDBList();
				nameList.add(new BasicDBObject("client.firstname",new BasicDBObject("$regex", client).append("$options","i")));
				nameList.add(new BasicDBObject("client.lastname",new BasicDBObject("$regex", client).append("$options","i")));
				//          nameList.add(new BasicDBObject("email",new BasicDBObject("$regex", search).append("$options","i")));
				match.put("$or", nameList);
			}
			//if (!cipcode.isEmpty())matchStart.append("cipCode.name",cipcode);
			if (!assignment.isEmpty()) matchStart.append("assignmentType",assignment);
			// if (!start.isEmpty()) matchStart.append("assignmentType",assignment);
			//int orderby=Integer.parseInt(order);
			//if (!issuetype.isEmpty()) matchStart.append("taskNature",issuetype);
			if(!showtask.isEmpty() && (showtask.equals("myservices")||showtask.equals("otherServices")||showtask.equals("allServices")))
			{
				matchStart.append("taskCategory","Service");
			}
			else if(!showtask.isEmpty() && (showtask.equals("myTask")||showtask.equals("otherTask")||showtask.equals("allTask")))
			{
				matchStart.append("taskCategory","Task");
			}
			else if(!showtask.isEmpty() && (showtask.equals("My Tasks and Services")||showtask.equals("Assigned Tasks and Services")))
			{
				list = new BasicDBList();
				list.add(new BasicDBObject("taskCategory","Task"));
				list.add(new BasicDBObject("taskCategory","Service"));
				matchStart.append("$or", list);

			}

			BasicDBObject matchAssignees = new BasicDBObject("customerId" ,u.getCustomerId());
			if(!u.getUsertype().equals("student") && !u.getUsertype().equals("partner")){
				//|| showtask.equals("myservices")
				if(!showtask.isEmpty() && (showtask.equals("clientTask")))
				{
					matchAssignees.append("assignedTo.assignedTo", clientid);
				}
				if(!showtask.isEmpty() && (showtask.equals("myTask")||showtask.equals("myservices")||showtask.equals("My Tasks and Services"))){ 
					matchAssignees.append("assignedTo.assignedTo", u.getId());

					//|| showtask.equals("otherServices")
				}else if(!showtask.isEmpty() && (showtask.equals("otherTask")||showtask.equals("otherServices")||showtask.equals("Assigned Tasks and Services"))){
					list = new BasicDBList();
					list.add(u.getId());
					matchAssignees.append("assignedTo.assignedTo", new BasicDBObject("$nin", list))
					.append("createdBy", u.getId());
				}
				else
				{
					list = new BasicDBList();
					list.add(new BasicDBObject("assignedTo.assignedTo", u.getId()));
					list.add(new BasicDBObject("createdBy", u.getId()));
					matchAssignees.append("$or", list);
				}
			}else{
				list = new BasicDBList();
				list.add(new BasicDBObject("assignedTo.assignedTo", u.getId()));
				list.add(new BasicDBObject("createdBy", u.getId()));
				matchAssignees.append("$or", list); 
			}



			list = new BasicDBList();
			list.add("$assignedUser._id");
			list.add(u.getId());

			cond = new BasicDBObject("if", new BasicDBObject("$eq",list));

			list1 = new BasicDBList();
			list1.add("$createdBy._id");
			list1.add(u.getId());


			conds = new BasicDBObject("if", new BasicDBObject("$eq",list1));

			//ifNull cond for Client firstname field
			firstNameCond = new BasicDBList();
			firstNameCond.add("$client.s1_req_1.txt");
			firstNameCond.add("$clientUser.firstname");

			//ifNull cond for Client lastname field
			BasicDBList lastNameCond = new BasicDBList();
			lastNameCond.add("$client.s1_req_2.txt");
			lastNameCond.add("$clientUser.lastname");

			//ifNull cond for Assigned To firstname field
			firstNameAssignCond = new BasicDBList();
			firstNameAssignCond.add("$assignedParticipant.s1_req_1.txt");
			firstNameAssignCond.add("$assignedUser.firstname");

			//ifNull cond for Assigned To lastname field
			lastNameAssignCond = new BasicDBList();
			lastNameAssignCond.add("$assignedParticipant.s1_req_2.txt");
			lastNameAssignCond.add("$assignedUser.lastname");


			aggQuery= new ArrayList<DBObject>(Arrays.asList(
					(DBObject) new BasicDBObject("$match", 
							matchStart
							),
					(DBObject) new BasicDBObject("$lookup", 
							new BasicDBObject("from" ,"TaskAssignment")
							.append("localField","_id")
							.append("foreignField","taskId")
							.append("as","assignedTo")
							),
					(DBObject) new BasicDBObject("$match", 
							matchAssignees   
							),
					(DBObject) new BasicDBObject("$unwind", 
							new BasicDBObject("path", "$assignedTo")
							.append("preserveNullAndEmptyArrays", true)
							),
					(DBObject) new BasicDBObject("$lookup", 
							new BasicDBObject("from" ,"survey_results")
							.append("localField","assignedTo.assignedTo")
							.append("foreignField","participantId")
							.append("as","assignedParticipant")
							),
					(DBObject) new BasicDBObject("$lookup", 
							new BasicDBObject("from" ,"SMRTUser")
							.append("localField","assignedTo.assignedTo")
							.append("foreignField","_id")
							.append("as","assignedUser")
							),
					(DBObject) new BasicDBObject("$unwind", 
							new BasicDBObject("path", "$assignedUser")
							.append("preserveNullAndEmptyArrays", true)
							),
					(DBObject) new BasicDBObject("$unwind", 
							new BasicDBObject("path", "$assignedParticipant")
							.append("preserveNullAndEmptyArrays", true)
							),
					(DBObject) new BasicDBObject("$lookup", 
							new BasicDBObject("from" ,"SMRTUser")
							.append("localField","createdBy")
							.append("foreignField","_id")
							.append("as","createdBy")
							),
					(DBObject) new BasicDBObject("$unwind", 
							new BasicDBObject("path", "$createdBy")
							.append("preserveNullAndEmptyArrays", true)
							),
					(DBObject) new BasicDBObject("$lookup", 
							new BasicDBObject("from" ,"survey_results")
							.append("localField","clientId")
							.append("foreignField","participantId")
							.append("as","client")
							),
					(DBObject) new BasicDBObject("$lookup", 
							new BasicDBObject("from" ,"SMRTUser")
							.append("localField","clientId")
							.append("foreignField","_id")
							.append("as","clientUser")
							),
					(DBObject) new BasicDBObject("$unwind", 
							new BasicDBObject("path", "$client")
							.append("preserveNullAndEmptyArrays", true)
							),
					(DBObject) new BasicDBObject("$unwind", 
							new BasicDBObject("path", "$clientUser")
							.append("preserveNullAndEmptyArrays", true)
							),
					(DBObject) new BasicDBObject("$lookup", 
							new BasicDBObject("from" ,"partner")
							.append("localField","partnerId")
							.append("foreignField","_id")
							.append("as","partner")
							),
					(DBObject) new BasicDBObject("$lookup", 
							new BasicDBObject("from" ,"group")
							.append("localField","groupId")
							.append("foreignField","_id")
							.append("as","group")
							),
					(DBObject) new BasicDBObject("$unwind", 
							new BasicDBObject("path", "$group")
							.append("preserveNullAndEmptyArrays", true)
							),
					(DBObject) new BasicDBObject("$unwind", 
							new BasicDBObject("path", "$partner")
							.append("preserveNullAndEmptyArrays", true)
							),
					(DBObject) new BasicDBObject("$group", 
							new BasicDBObject("_id" ,"$_id")
							.append("client", new BasicDBObject("$first", 
									new BasicDBObject("_id","$client._id")
									.append("firstname",new BasicDBObject("$ifNull", firstNameCond))
									.append("lastname",new BasicDBObject("$ifNull", lastNameCond))
									.append("hphone","$client.s1_req_7.txt")
									.append("mphone","$client.s1_req_8.txt")
									.append("address","$client.s1_req_3.txt")
									.append("city","$client.s1_req_4.txt")
									.append("state","$client.s1_req_5.txt")
									.append("zip","$client.s1_req_6.txt")
									))
							.append("partner", new BasicDBObject("$first",new BasicDBObject("_id","$partner._id").append("name","$partner.partnerName")))
							.append("dueDate", new BasicDBObject("$first","$dueDate"))
							.append("type", new BasicDBObject("$first","$type"))
							.append("priority", new BasicDBObject("$first","$priority"))
							.append("createdOn", new BasicDBObject("$first","$createdOn"))
							.append("assignmentType", new BasicDBObject("$first","$assignmentType"))
							.append("taskNature", new BasicDBObject("$first","$taskNature"))
							.append("createdBy", new BasicDBObject("$first", 
									new BasicDBObject("_id","$createdBy._id")
									.append("firstname","$createdBy.firstname")
									.append("lastname","$createdBy.lastname")
									))
							.append("completedOn", new BasicDBObject("$first","$completedOn"))
							.append("adminTaskType", new BasicDBObject("$first","$adminTaskType"))
							.append("groupTaskType", new BasicDBObject("$first","$groupTaskType"))
							.append("groupName", new BasicDBObject("$first","$group.name"))
							.append("taskFor", new BasicDBObject("$first","$taskFor"))
							.append("partnerId", new BasicDBObject("$first","$partnerId"))
							.append("category", new BasicDBObject("$first","$category"))
							.append("desc", new BasicDBObject("$first","$desc"))
							.append("completedNotes", new BasicDBObject("$first","$notes"))
							.append("status", new BasicDBObject("$first","$status"))
							.append("taskCategory", new BasicDBObject("$first","$taskCategory"))
							.append("editduedate",new BasicDBObject("$first",new BasicDBObject("$cond",
									conds
									.append("then", true)
									.append("else", false)
									)
									)
									)       
							.append("cipCode",new BasicDBObject("$first","$cipCode"))        
							.append("assignedTo", new BasicDBObject("$addToSet",
									new BasicDBObject("_id","$assignedUser._id")
									.append("firstname",new BasicDBObject("$ifNull", firstNameAssignCond))
									.append("lastname",new BasicDBObject("$ifNull", lastNameAssignCond))
									.append("completedOn","$assignedTo.completedOn")
									.append("status","$assignedTo.status")
									.append("current",
											new BasicDBObject("$cond", 
													cond
													.append("then", true)
													.append("else", false)
													)
											)
									))
							),
					(DBObject) new BasicDBObject("$match", 
							match
							)
					));
			Iterable<DBObject> count = taskCollection.aggregate(aggQuery).results();
			long size = count.spliterator().getExactSizeIfKnown();
			aggQuery.add((DBObject) new BasicDBObject("$sort",new BasicDBObject(sort,order)));
			aggQuery.add((DBObject) new BasicDBObject("$skip",start));
			aggQuery.add((DBObject) new BasicDBObject("$limit",limit));
			//            aggQuery.add((DBObject)new BasicDBObject("total",new BasicDBObject("$sum",1)));
			//                aggQuery.add((DBObject)new BasicDBObject("$skip", start));
			//                aggQuery.add((DBObject) new BasicDBObject("$limit",limit));

			tasks = taskCollection.aggregate(aggQuery).results();

			for(DBObject task: tasks){
				((BasicDBObject)task).append("isCreator", ((String)((BasicDBObject)task.get("createdBy")).get("_id")).equals(u.getId()));
				((BasicDBObject)task).append("total", size);
			}

			//Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
			Gson json = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
			return json.toJson(tasks);
		} catch (Exception ex) {
			return "{}";
		} finally {
			aggQuery = null;
			tasks = null;
			firstNameAssignCond=null;
			lastNameAssignCond=null;
			cond=null;
			firstNameCond=null;
		}
	}

	private void SaveTaskAssignment(TaskAssignment t) {
		this.getEntityManager().getTransaction().begin();
		try {
			getEntityManager().persist(t);
			this.getEntityManager().getTransaction().commit();

		} catch ( Exception e){
			this.getEntityManager().getTransaction().rollback();
			throw e;
		}
	}

	private TaskNote SaveTaskNote(TaskNote t) {
		this.getEntityManager().getTransaction().begin();
		try {
			getEntityManager().persist(t);
			this.getEntityManager().getTransaction().commit();
			return t;
		} catch ( Exception e){
			this.getEntityManager().getTransaction().rollback();
			throw e;
		}
	}

	@POST
	@Path("savenote/{userId}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public TaskNote SaveTaskNote(@QueryParam("token") String token,@PathParam("userId") String userId, TaskNote entity,@HeaderParam("Referer") String referer,@Context HttpServletRequest context)  throws Exception {
		//     try{
		SMRTUser u = this.validateToken(token);
		entity.setCreatedBy(u.getId());
		entity.setCustomerId(u.getCustomerId());
		DB mongoDB = this.getDBInstance();

		DBCollection userCollection = mongoDB.getCollection("SMRTUser");
		DBCollection lastactivityCollection = mongoDB.getCollection("activities");
		DBCollection groupCollection = mongoDB.getCollection("group");
		BasicDBObject update =  new BasicDBObject();
		Task ta = this.findTask(entity.getTaskId());
		String activity = null;
		String activityfor = null;
		if(u.getUsertype().equals("student")){
			activity = "Client - Task; Note";
			activityfor = u.getId();
			update.put("activity",activity);
			update.put("kind","n/a");
			update.put("createdBy",u.getId());
			update.put("createduname",u.getLastname()+", "+u.getFirstname());
			update.put("createdOn",new Date());
			update.put("taskId",entity.getTaskId());
			update.put("customerId",u.getCustomerId());
			update.put("activityfor",u.getId());
			update.put("detail",ta.getType()+"-"+ta.getTaskNature());
			update.put("information",ta.getDesc());
			lastactivityCollection.insert(update);

			BasicDBObject updateQuery = new BasicDBObject();
			BasicDBObject searchQuery = new BasicDBObject();
			updateQuery.append("$set",new BasicDBObject()
					.append("lastactivity","Client - Task"));
			searchQuery.append("_id",u.getId());
			WriteResult result = userCollection.update(searchQuery,updateQuery);

		}
		else
		{

			BasicDBObject updateQuery = new BasicDBObject();
			BasicDBObject searchQuery = new BasicDBObject();
			String id=entity.getTaskId();
			TaskAssignment task=new TaskAssignment();
			task.setTaskId(id); 
			updateQuery.append("$set",new BasicDBObject()
					.append("lastactivity","Staff - Task"));
			searchQuery.append("_id",task.getAssignedTo());
			WriteResult result = userCollection.update(searchQuery,updateQuery);

			//for save the task activities in activity table
			Task userTask = this.find(entity.getTaskId());
			if(userTask.getTaskCategory().equals("Task")){
				activity = "Staff - Task; Note"; 
			}else{
				activity = "Staff - Service; Note"; 
			}
			if(userTask.getGroupId() == null){
				update.put("activity",activity);
				update.put("kind","n/a");
				update.put("createdBy",u.getId());
				update.put("createduname",u.getLastname()+", "+u.getFirstname());
				update.put("createdOn",new Date());
				update.put("taskId",entity.getTaskId());
				update.put("customerId",u.getCustomerId());
				update.put("activityfor",userTask.getClientId());
				update.put("detail",ta.getType()+"-"+ta.getTaskNature());
				update.put("information",ta.getDesc());
				lastactivityCollection.insert(update);
			}
			else{
				BasicDBList users = new BasicDBList();
				BasicDBObject group = (BasicDBObject) groupCollection.findOne(new BasicDBObject("_id", userTask.getGroupId()));
				if(group.get("groupType").equals("All Clients") && group.get("isDefault").equals(true) ){
					String us=getAllClients(u.getCustomerId());
					JsonParser parser = new JsonParser();
					JsonArray jArray = parser.parse(us).getAsJsonArray();
					int limit = jArray.size();
					ArrayList<String> list = new ArrayList<>();
					for(int i=0;i<limit;i++)
					{
						JsonObject jsObj = jArray.get(i).getAsJsonObject();
						list.add(jsObj.get("_id").getAsString());
					}                
					//users.addAll((BasicDBList)group.get("contents"));
					BasicDBList idList = new BasicDBList();
					idList.addAll(list);
					users.addAll(idList);
					for(Object content : users){
						update.put("activity",activity);
						update.put("kind","n/a");
						update.put("createdBy",u.getId());
						update.put("createduname",u.getLastname()+", "+u.getFirstname());
						update.put("createdOn",new Date());
						update.put("taskId",entity.getTaskId());
						update.put("customerId",u.getCustomerId());
						update.put("activityfor",(String)content);
						update.put("detail",ta.getType()+"-"+ta.getTaskNature());
						update.put("information",ta.getDesc());
						update.remove("_id");
						lastactivityCollection.insert(update);
					}
				}
				else{
					BasicDBObject groupContents = getGroupContents(groupCollection, (BasicDBList)group.get("contents"));
					users.addAll((BasicDBList)group.get("contents"));
					for(Object content : users){
						update.put("activity",activity);
						update.put("kind","n/a");
						update.put("createdBy",u.getId());
						update.put("createduname",u.getLastname()+", "+u.getFirstname());
						update.put("createdOn",new Date());
						update.put("taskId",entity.getTaskId());
						update.put("customerId",u.getCustomerId());
						update.put("activityfor",(String)content);
						update.put("detail",ta.getType()+"-"+ta.getTaskNature());
						update.put("information",ta.getDesc());
						update.remove("_id");
						lastactivityCollection.insert(update);
					}
				}

			}
		}

		entity.setCreatedAt(Calendar.getInstance().getTime());
		entity = SaveTaskNote(entity);

		DB mongodb=this.getDBInstance();


		Task task = this.find(entity.getTaskId());

		if(task.getType().equals("Client Task") && !u.getUsertype().equals("student")){
			DBCollection surveyCollection=mongodb.getCollection("survey_results");
			Iterable<DBObject> output;
			output = surveyCollection.aggregate(Arrays.asList(
					(DBObject) new BasicDBObject("$match",new BasicDBObject("participantId",task.getClientId())
							.append("customerId",entity.getCustomerId()))
					,(DBObject) new BasicDBObject("$lookup", 
							new BasicDBObject("from" ,"customer")
							.append("localField","customerId")
							.append("foreignField","_id")
							.append("as","customer")
							)
					,(DBObject) new BasicDBObject("$unwind", 
							new BasicDBObject("path", "$customer")
							.append("preserveNullAndEmptyArrays", true)
							)  
					,(DBObject)new BasicDBObject("$project",
							new BasicDBObject("phonenumber","$s1_req_7.txt")
							.append("lastname", "$s1_req_2.txt")
							.append("customerId","$customer.urlKey")            
							.append("firstname","$s1_req_1.txt"))        
					)).results();
			BasicDBObject res = (BasicDBObject)output.iterator().next();
			String phone = (String)(res).get("phonenumber");
			String fname = (String) (res).get("firstname");
			String lname = (String) (res).getString("lastname");
			String customerId = (String) (res).get("customerId");
			if (fname ==null){ fname =""; }
			String msg =" Hi " + fname + ",\r\n  New Task Note - Please Sign in to your "+customerId.toUpperCase()+" student portal.\r\n"+ "https://"+customerId.toLowerCase()+"student.smrtdata.info";
			if(phone !=null && !phone.isEmpty()){ 
				phone = phone.replaceAll(" ", "").replaceAll("-", "").replaceAll("\\(", "").replaceAll("\\)", "");
				try{


					BasicDBObject message=new BasicDBObject();

					message.append("content", msg)
					.append("source_number", "18885039775")
					.append("source_number_type","INTERNATIONAL")
					.append("destination_number", "+1"+phone)
					.append("format","SMS");

					String req = "{\n" +
							"  \"messages\": [\n" + new Gson().toJson(message)+
							"  ]\n" +
							"}";

					String result = Util.sendPostRequest("http://api.messagemedia.com/v1/messages",req);

					ObjectId id = new ObjectId();
					BasicDBObject resultJson = new Gson().fromJson(result, BasicDBObject.class);
					DBCollection communicat= mongoDB.getCollection("communications");
					BasicDBObject document= new BasicDBObject();

					document.put("_id", id);
					document.put("type","SMS");
					document.put("customerId",u.getCustomerId());
					document.put("receiver",phone);
					document.put("userId",task.getClientId());
					document.put("message",msg);
					document.put("status",resultJson);
					document.put("errormsg","");
					document.put("senton",Calendar.getInstance().getTime());
					document.put("sentby",u.getId());
					communicat.insert(document);
					//                  DBCollection txtreply= mongoDB.getCollection("sms_reply");
					//                  BasicDBObject updateQuery = new BasicDBObject();
					//                  updateQuery.append("$set",new BasicDBObject()
					//                  .append("replystatus",true)
					//                   );
					//                  BasicDBObject searchQuery = new BasicDBObject();
					//                  searchQuery.append("userId",u.getId());
					//                  txtreply.updateMulti(searchQuery,updateQuery);


				}
				catch(Exception e){
					this.logger.error("Cannot send text message - task note", e);

					DBCollection communication= mongoDB.getCollection("communications");
					BasicDBObject document=new BasicDBObject();
					ObjectId id = new ObjectId();
					document.put("_id", id);
					document.put("type","SMS");
					document.put("customerId",u.getCustomerId());
					document.put("receiver",phone);
					document.put("userId",task.getClientId());
					document.put("message",msg);
					document.put("status","failed");
					document.put("errormsg",e.getMessage());
					document.put("senton",Calendar.getInstance().getTime());
					document.put("sentby",u.getId());
					communication.insert(document);  

				}
			}

		}
		return entity;
		//}

		//    catch (Exception ex) {  
		//            System.out.println(ex.getMessage());
		//            throw new Exception("Error ");
		//        }
	}
	private DBObject getTaskAssignedTo(String id) throws Exception  {
		DB mongoDB;
		try {
			mongoDB = this.getDBInstance();
			DBCollection exportCollection = mongoDB.getCollection("TaskAssignment");
			BasicDBObject condition = new BasicDBObject("taskid",id);
			DBCursor cursor = exportCollection.find(condition);
			if (cursor.hasNext()) return cursor.next();
		} catch (Exception ex) {  

		}
		return null;
	}


	@GET
	@Path("notes/{id}")
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public String find(@QueryParam("token")String token,@PathParam("id") String id,@QueryParam("limit")int limit) throws Exception {
		SMRTUser u = this.validateToken(token);
		DB mongoDB = this.getDBInstance();
		DBCollection taskNoteCollection = mongoDB.getCollection("task_note");

		List<DBObject> aggQuery = Arrays.asList(
				(DBObject) new BasicDBObject("$match", 
						new BasicDBObject("taskId", id).append("customerId", u.getCustomerId())
						),
				(DBObject) new BasicDBObject("$lookup", 
						new BasicDBObject("from" ,"SMRTUser")
						.append("localField","createdBy")
						.append("foreignField","_id")
						.append("as","user")
						),
				(DBObject) new BasicDBObject("$unwind", 
						new BasicDBObject("path", "$user")
						.append("preserveNullAndEmptyArrays", true)
						),
				(DBObject) new BasicDBObject("$project", 
						new BasicDBObject("_id" ,"$_id")
						.append("createdAt","$createdAt")
						.append("reason","$reason")
						.append("createdBy","$createdBy")
						.append("priority","$priority")
						.append("content","$content")
						.append("author", new BasicDBObject("firstname", "$user.firstname").append("lastname", "$user.lastname"))
						),
				(DBObject)new BasicDBObject("$sort", new BasicDBObject("createdAt",-1)), 
				(DBObject) new BasicDBObject("$limit",limit)
				);

		Iterable<DBObject> tasks = taskNoteCollection.aggregate(aggQuery).results();
		return new Gson().toJson(tasks);
	}

	@POST
	@Path("savecompleted/{userId}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String SaveTaskCompleted(@QueryParam("token") String token,@PathParam("userId") String userId, Task entity) throws Exception {
		try{
			SMRTUser u = this.validateToken(token);
			DB mongoDB = this.getDBInstance();
			System.out.println(u.getUsertype());
			System.out.println("Token is: "+token);
			//        if(u.getUsertype().equals("student")){
			//        DBCollection userCollection = mongoDB.getCollection("SMRTUser");
			//        BasicDBObject updateQuery = new BasicDBObject();
			//        updateQuery.append("$set",new BasicDBObject()
			//                    .append("lastactivity","Client - Task"));
			//        BasicDBObject searchQuery = new BasicDBObject();
			//        searchQuery.append("_id",u.getId());
			//        WriteResult result = userCollection.update(searchQuery,updateQuery);
			//        }        
			DBCollection taskCollection = mongoDB.getCollection("Task");
			System.out.println("================================="+taskCollection.getCount());
			System.out.println("User Id is: "+userId);
			System.out.println("Id is: "+entity.getId());
			BasicDBObject task = (BasicDBObject) taskCollection.findOne(new BasicDBObject("_id", entity.getId()).append("status","Open"));
			System.out.println("Be here: =================================================="+task.hashCode());
			if(task!=null){
				System.out.println("Task is null================================================================");
				//set task Assignee Completed status
				DBCollection taskAssignCollection = mongoDB.getCollection("TaskAssignment");
				BasicDBObject taskAssignee = (BasicDBObject) taskAssignCollection.findOne(new BasicDBObject("taskId", entity.getId()).append("status", "Open"));
				//this method check task is group or individual 
				String uid = null;
				if(u.getUsertype().equals("casemanager") && taskAssignee!=null){
					System.out.println("User type is case manager=================================================");
					uid = ((Object)taskAssignee.get("assignedTo")).toString();
				}else{
					uid = u.getId();
				}
				if(u.getUsertype().equals("student") || ((Object)task.get("assignmentType")).toString().equals("Individual(s)")){
				    System.out.println("user type is student====================================================");
					WriteResult result = taskAssignCollection.update(
							new BasicDBObject("taskId", ((Object)task.get("_id")).toString())
							.append("assignedTo", uid)
							.append("status","Open"),
							new BasicDBObject(
									"$set",
									new BasicDBObject("status", entity.getStatus())
									.append("notes", entity.getNotes())
									.append("completedOn", Calendar.getInstance().getTime())
									),
							false,
							false
							);
					if(result.getN()==0 && !entity.getStatus().equals("Cancel")){
						System.out.println("get status equals cancel=======================================================");
						return new Gson().toJson("Task already completed or User doesn't belongs to Task Assignee's list");
					}
				}
				else{
					System.out.println("I am updating something here============================================================");
					WriteResult result = taskAssignCollection.update(
							new BasicDBObject("taskId", ((Object)task.get("_id")).toString())
							.append("assignedTo", ((Object)task.get("groupId")).toString())
							.append("status","Open"),
							new BasicDBObject(
									"$set",
									new BasicDBObject("status", entity.getStatus())
									.append("notes", entity.getNotes())
									.append("completedOn", Calendar.getInstance().getTime())
									),
							false,
							false
							);
					if(result.getN()==0 && !entity.getStatus().equals("Cancel")){
						return new Gson().toJson("Task already completed or User doesn't belongs to Task Assignee's list");
					}
				}
				if(task.getString("groupTaskType")==null){
					System.out.println("group task type========================================================================");
					task.put("groupTaskType","none");
				}
				if(task.getString("taskCategory").equals("Service") ||entity.getStatus().equals("Cancel")|| task.getString("groupTaskType").equals("First party to complete") ||entity.getStatus().equals("Closed Incomplete") ){
					System.out.println("Task catagory is service=======================================================");
					taskCollection.update(
							new BasicDBObject("_id", entity.getId())
							.append("status","Open"),
							new BasicDBObject(
									"$set",
									new BasicDBObject("status", entity.getStatus())
									.append("completedOn", Calendar.getInstance().getTime())
									),
							false,
							false
							);
				}else{
					//check if all Assigneed completed task and change status
                    System.out.println("check if all Assigneed completed task=======================================================");
					//if(taskAssignee == null){
					//every once completed
					taskCollection.update(
							new BasicDBObject("_id", entity.getId())
							.append("status","Open"),
							new BasicDBObject(
									"$set",
									new BasicDBObject("status", entity.getStatus())
									.append("completedOn", Calendar.getInstance().getTime())
									),
							false,
							false
							);
					// }
				}
			}else{
				throw new Exception("Unable to complete task");
			}
			// save the user activity in activity table
			String activityfor = null;
			String activity = null;
			if(u.getUsertype().equals("student")){
				activityfor = u.getId();
				if(entity.getStatus().equals("Completed")){
					activity = "Client - Task; Complete";
				}else{
					activity = "Client - Task; InComplete";
				}
			}else{
				activityfor = ((Object)task.get("clientId")).toString(); 
				if(task.get("taskCategory").equals("Task")){
					if(entity.getStatus().equals("Completed")){
						activity = "Staff - Task; Complete";
					}else{
						activity = "Staff - Task; InComplete";
					}

				}else{
					if(entity.getStatus().equals("Completed")){
						activity = "Staff - Service; Complete";
					}else{
						activity = "Staff - Service; InComplete";
					}
				}
			}
			DBCollection groupCollection = mongoDB.getCollection("group");
			if(task.get("groupId")==null || u.getUsertype().equals("student")){
				DBCollection lastactivityCollection = mongoDB.getCollection("activities");
				BasicDBObject update =  new BasicDBObject();
				update.put("activity",activity);
				update.put("kind","n/a");
				update.put("createdBy",u.getId());
				update.put("createduname",u.getLastname()+", "+u.getFirstname());
				update.put("createdOn",new Date());
				update.put("taskId",((Object)task.get("_id")).toString());
				update.put("customerId",u.getCustomerId());
				update.put("activityfor",activityfor);
				update.put("detail","n/a");
				update.put("information","n/a");
				lastactivityCollection.insert(update);
			}
			else{
				BasicDBList users = new BasicDBList();
				BasicDBObject group = (BasicDBObject) groupCollection.findOne(new BasicDBObject("_id", ((Object)task.get("groupId")).toString()));
				BasicDBObject groupContents = getGroupContents(groupCollection, (BasicDBList)group.get("contents"));
				users.addAll((BasicDBList)group.get("contents"));
				DBCollection lastactivityCollection = mongoDB.getCollection("activities");
				BasicDBObject update =  new BasicDBObject();
				for(Object usr:users){
					update.put("activity",activity);
					update.put("kind","n/a");
					update.put("createdBy",u.getId());
					update.put("createduname",u.getLastname()+", "+u.getFirstname());
					update.put("createdOn",new Date());
					update.put("taskId",entity.getId());
					update.put("customerId",u.getCustomerId());
					update.put("activityfor",(String)usr);
					update.put("detail","n/a");
					update.put("information","n/a");
					update.remove("_id");
					lastactivityCollection.insert(update);
				}
			}

			return new Gson().toJson("Task Completed Successfully");
		}

		catch (Exception e) {
			System.out.println(e.getMessage());
			throw new Exception("Error ");
		} finally{
			//ToDO 
		}
	}

	@GET
	@Path("cipcode")
	@Produces({MediaType.APPLICATION_JSON})
	public String listSocialCIPCode(
			@QueryParam("type") String type,
			@QueryParam("token") String token) throws Exception {
		try {
			//validate user token and accordingly get the customer id     
			SMRTUser user = this.validateToken(token); 
			DB mongoDB = this.getDBInstance();
			DBCollection cipCodeCollection = mongoDB.getCollection("cip_code");
			BasicDBObject fields = new BasicDBObject("codes",1);
			//TODO to get state code for specific customer
			//BasicDBObject condition = new BasicDBObject("state_id",user.getCustomer().getState());
			BasicDBObject condition = new BasicDBObject("state_id","00");
			BasicDBList list = new BasicDBList();
			list.add(new BasicDBObject("service_type",type));
			condition.append("$and", list);  

			DBCursor cursor = cipCodeCollection.find(condition,fields);
			Gson json = new Gson();
			if (cursor.hasNext()) {
				return json.toJson(((BasicDBObject) cursor.next()).get("codes"));
			} else {
				return "{}";
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new Exception("Error ");
		} finally{
			//ToDO 
		}
	}

	@PUT
	@Path("createOrUpdateFilter/{id}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response createOrUpdateFilter(
			@PathParam("id") String id,    
			@QueryParam("token") String token,
			@QueryParam("t") String type,
			@QueryParam("name") String filtername,
			@QueryParam("f") String taskfor,
			@QueryParam("s") String status,
			@QueryParam("p") String priority,
			@QueryParam("a") String assignment,
			@QueryParam("m") String showtask,
			@QueryParam("cip") String cipcode,
			@QueryParam("sortby") String sortby,
			@QueryParam("isDefault") Boolean isDefault

			) throws Exception {
		try {
			SMRTUser user = this.validateToken(token, null); 
			DB mongoDB = this.getDBInstance();
			DBCollection filterCollection = mongoDB.getCollection("filters");
			BasicDBObject condition = new BasicDBObject("isdefault",true);
			//if(filterCollection.find(condition).length()>1){}
			BasicDBObject document = new BasicDBObject();
			if(id.isEmpty()||id.equals("null") || id.equals("")){id =ObjectId.get().toString();}
			if(isDefault.equals(true))
			{
				BasicDBObject updateQuery = new BasicDBObject();
				updateQuery.append("$set",
						new BasicDBObject().append("isdefault", false));
				BasicDBObject searchQuery = new BasicDBObject();
				searchQuery.append("isdefault",true);
				filterCollection.updateMulti(searchQuery,updateQuery);
			}
			//String id =ObjectId.get().toString();
			document.put("$set", 
					new BasicDBObject("createdBy",user.getId())
					.append("name",filtername)
					.append("type",type)
					.append("taskfor",taskfor)
					.append("status",status)
					.append("priority",priority)
					.append("assignment",assignment)
					.append("showtask",showtask)
					.append("cipcode",cipcode)
					.append("sortby",sortby)
					.append("isdefault",isDefault)

					);
			document.put("$setOnInsert", new BasicDBObject("_id", id ));
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
	@Path("getFilters")
	@Produces({MediaType.APPLICATION_JSON})
	public String getFilters(@QueryParam("token") String token)throws Exception 
	{
		try {
			SMRTUser user = this.validateToken(token); 
			DB mongoDB = this.getDBInstance();
			DBCollection filterCollection = mongoDB.getCollection("filters");
			BasicDBObject condition = new BasicDBObject("createdBy",user.getId());
			DBCursor cursor = filterCollection.find(condition);
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
	@DELETE
	@Path ("deleteFilter/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	public String deleteFilter(
			@QueryParam("token") String token,
			@PathParam("id") String id,
			@Context HttpServletRequest req) 
					throws Exception {
		SMRTUser user = this.validateToken(token, null);
		try{
			DB mongoDB = this.getDBInstance();
			DBCollection filtersCollection = mongoDB.getCollection("filters");
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

	@PUT  
	@Path("editDueDate")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Task updateClient (@QueryParam("token") String token, Task entity) throws Exception{
		SMRTUser user=this.validateToken(token);

		Task tasks=this.find(entity.getId());
		tasks.setDueDate(entity.getDueDate());
		this.edit(tasks);

		DB mongoDB=this.getDBInstance();
		DBCollection taskAssignCollection=mongoDB.getCollection("TaskAssignment");
		WriteResult result=taskAssignCollection.update(new BasicDBObject("taskId",entity.getId()),
				new BasicDBObject("$set",new BasicDBObject("dueDate",entity.getDueDate())),false,false

				);
		return tasks;
	}

	@GET
	@Path("kindoftask")
	@Produces({MediaType.APPLICATION_JSON})
	public String kindoftask(@QueryParam("token") String token)throws Exception 
	{
		try {
			SMRTUser user = this.validateToken(token); 
			DB mongoDB = this.getDBInstance();
			DBCollection taskkindCollection = mongoDB.getCollection("taskkind");
			BasicDBObject condition = new BasicDBObject("customerId",user.getCustomerId());
			DBCursor cursor = taskkindCollection.find(condition);
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
	@POST
	@Path("addtaskkind")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
	public String addTaskKind(
			@FormParam("id") String id,
			@FormParam("token") String token,
			@FormParam("taskkind") String taskkind,
			@FormParam("tasktype") String tasktype
			) throws Exception {
		try {
			SMRTUser user = this.validateToken(token); 
			DB mongoDB = this.getDBInstance();
			DBCollection taskkindCollection = mongoDB.getCollection("taskkind");
			if(id==null || id.equals("")){
				id = ObjectId.get().toString();
			}
			System.out.println(id);

			BasicDBObject document=new BasicDBObject();
			document.put("$set", 
					new BasicDBObject("customerId",user.getCustomerId())
					.append("customerId",user.getCustomerId())
					.append("createdBy",user.getId())
					.append("kindoftask",taskkind)
					.append("tasktype",tasktype)
					.append("createdOn",Calendar.getInstance().getTime())
					);
			document.put("$setOnInsert", new BasicDBObject("_id", id ));
			taskkindCollection.update(new BasicDBObject("_id",id),document, true, true, WriteConcern.ACKNOWLEDGED);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new Exception("Error ");
		} finally{
			//ToDO 
		}
		return null;

	}
	@DELETE
	@Path ("kindoftask/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	public String deleteKindOfTask(
			@QueryParam("token") String token,
			@PathParam("id") String id) 
					throws Exception {
		SMRTUser user = this.validateToken(token, null);
		try{
			DB mongoDB = this.getDBInstance();
			DBCollection kindoftaskCollection = mongoDB.getCollection("taskkind");
			BasicDBObject fields = new BasicDBObject("_id",id);
			WriteResult result = kindoftaskCollection.remove(fields);
		}catch (Exception e) {
			System.out.println(e.getMessage());
			throw new Exception("Error ");
		} finally{
			//ToDO
		}
		return null;
	}
}
