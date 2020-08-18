/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.survey.service;

import com.smrtsolutions.exception.InvalidParameterException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author lenny
 */
   
@Provider
public class InvalidParameterExceptionHandler implements ExceptionMapper<InvalidParameterException> 
{
    @Override
    public Response toResponse(InvalidParameterException exception) 
    {
        return Response.status(Status.BAD_REQUEST).entity(exception.getMessage()).build();  
    }
}
    

