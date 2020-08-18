/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.survey.service;

import com.smrtsolutions.exception.InvalidTokenException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author lenny
 */
   
@Provider
public class InvalidTokenExceptionHandler implements ExceptionMapper<InvalidTokenException> 
{
    @Override
    public Response toResponse(InvalidTokenException exception) 
    {
        return Response.status(Status.UNAUTHORIZED).entity(exception.getMessage()).build();  
    }
}
    

