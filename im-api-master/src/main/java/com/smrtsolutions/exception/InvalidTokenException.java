/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.exception;

/**
 *
 * @author lenny
 */
public class InvalidTokenException extends Exception {
    
    public InvalidTokenException(){
        
    }
    public InvalidTokenException(String message){
        super(message);
    }
    
    public InvalidTokenException(String message, Throwable t){
        super(message, t);
    }
    public  int getHttpErrorCode() {
		return 401;
	}

}
