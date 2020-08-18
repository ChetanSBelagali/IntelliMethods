/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.survey.service;

//import com.jaspersoft.mongodb.MongoDbDataSource;
//import com.jaspersoft.mongodb.connection.MongoDbConnection;
import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SurveyResult;
import javax.ws.rs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author joy
 */
@Path("/jasper")
public class JasperReportsRest extends SMRTAbstractFacade<SurveyResult> {
    private static final Logger logger = LogManager.getLogger(SMRTUserREST.class);
    public String[] REQUIRED_PERMISSIONS = {SMRTRole.NONE};
    
    public JasperReportsRest() {
        super(SurveyResult.class);
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
    
//    @GET
//    @Path("nrst2")
//    @Produces({MediaType.TEXT_HTML})
//    public String generateReport(@QueryParam("customerId") String customerId, @QueryParam("token") String token
//            ,@QueryParam("surveyId") String surveyId)  throws Exception{
//        SMRTUser user = this.validateToken(token, null);
//        JasperTest1 j1 = new JasperTest1();
//        j1.genReport("pirld", "pirld31");
//        return "";
//    }

}
