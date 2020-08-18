/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smrtsolutions.survey.service;

/**
 *
 * @author joy
 */
import java.util.Properties;  
import javax.mail.*;  
import javax.mail.internet.*;
import com.smrtsolutions.util.Util;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;

import com.smrtsolutions.util.Util;
import com.sun.mail.pop3.POP3Store;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.PasswordAuthentication;

public class Emailer 
{
  
    public static void sendEmail(List<String> toEmail,List<String> ccEmail, String subject, String body) throws Exception {
         
        InternetAddress[] toa = Emailer.toAddresses(toEmail);
        InternetAddress[] tocc = Emailer.toAddresses(ccEmail);
        //Get the session object  
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.1and1.com");
        props.put("mail.smtp.socketFactory.port", "995");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("messaging@smrtdata.info", "smrt1234");//change accordingly  
            }
        });
          
        String msg="\n" 
                    +body+ "\n \n \n \n" + 
                    "Please do not reply to this e-mail."+
                    "";
        
//         String req = "{\n" +
//                    "  \"messages\": [\n" +
//                    "    {\n" +
//                    "      \"callback_url\": \"" + callBackurl + "\",\n" +
//                    "      \"content\": \"" + msg + "\",\n" +
//                    "      \"source_number\": \"18885039775\",\n" +
//                    "      \"source_number_type\": \"INTERNATIONAL\",\n" +
//                    "      \"destination_number\": \"+1" + numbers[idx] + "\",\n" +
//                    "      \"format\": \"SMS\"\n" +
//                    "    }\n" +
//                    "  ]\n" +
//                    "}";
        //compose message  
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("messaging@smrtdata.info","smrtsolutions"));//change accordingly  
            message.addRecipients(Message.RecipientType.TO, toa);
            message.addRecipients(Message.RecipientType.CC, tocc);
            message.setSubject(Util.getValue(subject, "subject"));
            message.setText(Util.getValue(msg, "Email body"));
            //message.setContent("<h1>This is actual message</h1>", "text/html");
            
            //send message  
   
            Transport.send(message);

            System.out.println("message sent successfully");
            
        } catch (MessagingException e) {
            throw (e);
        }

    }  
    
    public static InternetAddress[] toAddresses(List<String> email) throws Exception{
        if ( email == null || email.isEmpty()) return null;
        String l ="";
        for ( String e: email){
            l += (Util.isEmpty(l)?"":",") + e;
        }
        return InternetAddress.parse(l);
    }
    
}

