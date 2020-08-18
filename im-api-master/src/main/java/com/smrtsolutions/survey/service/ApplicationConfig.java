/*
 * SMRT Solutions
 * Data Collection Portal
 */
package com.smrtsolutions.survey.service;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author lenny
 */
@javax.ws.rs.ApplicationPath("/services")
public class ApplicationConfig extends Application {
    


    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        //addRestResourceClasses(resources);
        myaddRestResourceClasses(resources);
        return resources;
    }

    
        private void myaddRestResourceClasses(Set<Class<?>> resources) {
        //resources.add(com.smrtsolutions.survey.service.Page.class);
        //resources.add(Page.class);
        resources.add(com.smrtsolutions.survey.service.ActivitesREST.class);
        resources.add(com.smrtsolutions.survey.service.CalendarREST.class);
        resources.add(com.smrtsolutions.survey.service.CustomerContentFacadeREST.class);
        resources.add(com.smrtsolutions.survey.service.CustomerFacadeREST.class);
        resources.add(com.smrtsolutions.survey.service.ForbiddenExceptionHandler.class);
        resources.add(com.smrtsolutions.survey.service.InvalidParameterExceptionHandler.class);
        resources.add(com.smrtsolutions.survey.service.InvalidTokenExceptionHandler.class);
        //resources.add(com.smrtsolutions.survey.service.Page.class);
        resources.add(com.smrtsolutions.survey.service.SMRTContextResolver.class);
        resources.add(com.smrtsolutions.survey.service.SMRTExceptionMapper.class);
        //resources.add(com.smrtsolutions.survey.service.SMRTGroupFacadeREST.class);
        resources.add(com.smrtsolutions.survey.service.SMRTRoleFacadeREST.class);
        resources.add(com.smrtsolutions.survey.service.SMRTUserREST.class);
        resources.add(com.smrtsolutions.survey.service.SurveyMetaDataREST.class);
        resources.add(com.smrtsolutions.survey.service.SurveyResultREST.class);
        resources.add(com.smrtsolutions.survey.service.SurveyReportREST.class);
        resources.add(com.smrtsolutions.survey.service.SMRTDocLibraryREST.class);
        resources.add(com.smrtsolutions.survey.service.GroupREST.class);
        resources.add(com.smrtsolutions.survey.service.SMRTImportExportREST.class);
        resources.add(com.smrtsolutions.survey.service.ParticipantNoteREST.class);
//        resources.add(com.smrtsolutions.survey.service.CustomerContentFacadeREST.class);
        resources.add(com.smrtsolutions.survey.service.DashboardMetaFacadeREST.class);
        resources.add(com.smrtsolutions.survey.service.ParticipantTaskREST.class);
        resources.add(com.smrtsolutions.survey.service.JasperReportsRest.class);
        resources.add(com.smrtsolutions.survey.service.PermissionTemplateREST.class);
        resources.add(com.smrtsolutions.survey.service.ModuleREST.class);
        resources.add(com.smrtsolutions.survey.service.SurveyAllocateREST.class);
        resources.add(com.smrtsolutions.survey.service.TabREST.class);
        resources.add(com.smrtsolutions.survey.service.PartnerREST.class);
        resources.add(com.smrtsolutions.survey.service.TaskAssignmentREST.class);
        resources.add(com.smrtsolutions.survey.service.TaskREST.class);
        resources.add(com.smrtsolutions.survey.service.LocationREST.class);
        resources.add(com.smrtsolutions.survey.service.ServiceNameREST.class);
        resources.add(com.smrtsolutions.survey.service.ScheduleREST.class);
//        resources.add(com.smrtsolutions.survey.service.AppointmentsScheduler.class);
//        resources.add(com.smrtsolutions.survey.service.HelloJob.class);
      }

    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.smrtsolutions.survey.service.ActivitesREST.class);
        resources.add(com.smrtsolutions.survey.service.CalendarREST.class);
        resources.add(com.smrtsolutions.survey.service.CustomerContentFacadeREST.class);
        resources.add(com.smrtsolutions.survey.service.CustomerFacadeREST.class);
        resources.add(com.smrtsolutions.survey.service.DashboardMetaFacadeREST.class);
        resources.add(com.smrtsolutions.survey.service.ForbiddenExceptionHandler.class);
        resources.add(com.smrtsolutions.survey.service.GroupREST.class);
        resources.add(com.smrtsolutions.survey.service.InvalidParameterExceptionHandler.class);
        resources.add(com.smrtsolutions.survey.service.InvalidTokenExceptionHandler.class);
        resources.add(com.smrtsolutions.survey.service.JasperReportsRest.class);
        resources.add(com.smrtsolutions.survey.service.LocationREST.class);
        resources.add(com.smrtsolutions.survey.service.ModuleREST.class);
        resources.add(com.smrtsolutions.survey.service.Page.class);
        resources.add(com.smrtsolutions.survey.service.ParticipantNoteREST.class);
        resources.add(com.smrtsolutions.survey.service.ParticipantTaskREST.class);
        resources.add(com.smrtsolutions.survey.service.PartnerREST.class);
        resources.add(com.smrtsolutions.survey.service.PermissionTemplateREST.class);
        resources.add(com.smrtsolutions.survey.service.SMRTContextResolver.class);
        resources.add(com.smrtsolutions.survey.service.SMRTDocLibraryREST.class);
        resources.add(com.smrtsolutions.survey.service.SMRTExceptionMapper.class);
        resources.add(com.smrtsolutions.survey.service.SMRTGroupFacadeREST.class);
        resources.add(com.smrtsolutions.survey.service.SMRTImportExportREST.class);
        resources.add(com.smrtsolutions.survey.service.SMRTRoleFacadeREST.class);
        resources.add(com.smrtsolutions.survey.service.SMRTUserREST.class);
        resources.add(com.smrtsolutions.survey.service.ScheduleREST.class);
        resources.add(com.smrtsolutions.survey.service.ServiceNameREST.class);
        resources.add(com.smrtsolutions.survey.service.SurveyAllocateREST.class);
        resources.add(com.smrtsolutions.survey.service.SurveyMetaDataREST.class);
        resources.add(com.smrtsolutions.survey.service.SurveyReportREST.class);
        resources.add(com.smrtsolutions.survey.service.SurveyResultREST.class);
        resources.add(com.smrtsolutions.survey.service.TabREST.class);
        resources.add(com.smrtsolutions.survey.service.TaskAssignmentREST.class);
        resources.add(com.smrtsolutions.survey.service.TaskREST.class);
    }
    
}
