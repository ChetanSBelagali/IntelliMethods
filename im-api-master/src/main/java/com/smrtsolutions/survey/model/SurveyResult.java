/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.model;


public class SurveyResult {
    //public enum STATUS { DISABLED, OPEN, STARTED, IN_PROGRESS, COMPLETE }
    public static enum STATUS { 
         NOT_APPLICABLE, PENDING, IN_PROGRESS, COMPLETE, RESERVED1, RESERVED2, RESERVED3, RESERVED4, RESERVED5
     }

    public static String[] STATUS_DESCRIPTION  = { 
         "Not Applicable", "Pending", "In Progress", "Complete", "Reserved", "Reserved", "Reserved", "Reserved", "Reserved"
     };
}
