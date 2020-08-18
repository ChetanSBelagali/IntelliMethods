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
public class InvalidParameterException extends Exception {
    
    public InvalidParameterException(){
        
    }
    public InvalidParameterException(String message){
        super(message);
    }
    
    public InvalidParameterException(String message, Throwable t){
        super(message, t);
    }
}
