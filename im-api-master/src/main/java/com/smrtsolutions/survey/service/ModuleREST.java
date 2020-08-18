/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.service;

import java.util.List;

import javax.validation.constraints.Past;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.smrtsolutions.survey.model.Module;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * @author Baskar
 **/

//@Stateless
@Path("/admin/module")
public class ModuleREST extends SMRTAbstractFacade<Module> {
       
    private static final Logger logger = LogManager.getLogger(ModuleREST.class);

    public ModuleREST() {
        super(Module.class);
    }

       public void log(String message){
         logger.debug(message);
    }
    
    public void error(String message, Exception e){
         logger.error(message, e);
    }
    
    @GET
    @Path("/Hello")
    public String hello() {
    	System.out.println("Hello Everyone I am Here");
    	return "Hello Everyone";
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Module> getModules() throws Exception {
        return this.findAll();
    }
    @GET
    @Path("{type}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Module> getModulesByType(@PathParam("type") String moduleType)  throws Exception{
        return this.findAll("type", moduleType,"displayorder");
    }
    
    @GET
    @Path("/children/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Module> getChildren(@PathParam("id") String moduleId)  throws Exception{
        return this.findAll("parent_id", moduleId);
    }

    @Override
    public String[] getRequiredPermissions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
 
}
    
    
    