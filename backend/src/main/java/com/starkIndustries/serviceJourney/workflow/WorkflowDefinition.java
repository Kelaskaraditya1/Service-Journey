package com.starkIndustries.serviceJourney.workflow;

import java.util.Map;

import lombok.Data;

@Data
public class WorkflowDefinition {
  /*
  This class is responsible for storing the definition of the workflow in a Map format.
  like name of the workFlow and the State that belongs to it 

   */
      private Map<String, StateDefinition> states;

}
