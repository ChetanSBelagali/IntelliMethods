/*
 * SMRT Solutions
 * Data Collection Platform
 */
package com.smrtsolutions.survey.service;


import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author lenny
 */
@Provider
public class SMRTExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Exception> {
    private static final Logger logger = LogManager.getLogger(SMRTExceptionMapper.class);
    
    @Override
    public Response toResponse(Exception exception) {
        exception.printStackTrace();
        logger.error("SMRT ERROR::" + exception.toString(), exception);
        return Response.status(500).build();
    }
}