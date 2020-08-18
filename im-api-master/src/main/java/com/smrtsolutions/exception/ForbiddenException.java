/*
 * SMRT
 */
package com.smrtsolutions.exception;

/**
 *
 * @author lenny
 */
public class ForbiddenException extends Exception {
    
    public ForbiddenException(){
        
    }
    public ForbiddenException(String message){
        super(message);
    }
    
    public ForbiddenException(String message, Throwable t){
        super(message, t);
    }
}
