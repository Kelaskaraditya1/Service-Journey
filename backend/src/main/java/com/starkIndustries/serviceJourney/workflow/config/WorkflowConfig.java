package com.starkIndustries.serviceJourney.workflow.config;

import java.util.Map;

import com.starkIndustries.serviceJourney.workflow.WorkflowDefinition;

import lombok.Data;

@Data
public class WorkflowConfig {

  /* 
  This file stores all the workflows in a Hashmap format. 
  Hierarchy:
      1) WorkFlowConfig: contains Hashmap of workflows
                          ||
                          \/
      2) WorkFlowdefinition: contains Hashmap of Workflow states.(Active/Complete)
                          ||
                          \/
      3) StateDefinition: Consists actual events which happens in that State.


  */

    private Map<String, WorkflowDefinition> workflows;
}