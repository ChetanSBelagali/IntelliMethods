/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.service;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.smrtsolutions.survey.model.Tab;

/**
 * @author Baskar
 **/

//@Stateless
@Path("/admin/tab")
public class TabREST extends SMRTAbstractFacade<Tab> {
       
    private static final Logger logger = LogManager.getLogger(TabREST.class);

    public TabREST() {
        super(Tab.class);
    }

       public void log(String message){
         logger.debug(message);
    }
    
    public void error(String message, Exception e){
         logger.error(message, e);
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Tab> getTab() throws Exception {
        return this.findAll();
    }

    @Override
    public String[] getRequiredPermissions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
 
}
    
    
    