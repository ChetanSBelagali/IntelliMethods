/*
 * SMRT
 */
package com.smrtsolutions.survey.service;

import com.smrtsolutions.exception.ForbiddenException;


import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author lenny
 */
   
@Provider
public class ForbiddenExceptionHandler implements ExceptionMapper<ForbiddenException> 
{
    @Override
    public Response toResponse(ForbiddenException exception) 
    {
        return Response.status(Status.FORBIDDEN).entity(exception.getMessage()).build();  
    }
}
    

