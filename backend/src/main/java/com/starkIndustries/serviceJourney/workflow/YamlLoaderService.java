package com.starkIndustries.serviceJourney.workflow;

import java.io.InputStream;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import com.starkIndustries.serviceJourney.workflow.config.WorkflowConfig;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Getter
public class YamlLoaderService {

    private WorkflowConfig workflowConfig;

    @PostConstruct
    public void loadYaml() { // This function is responsible for loading the .yml file

        try {

            Yaml yaml = new Yaml();

            InputStream inputStream =
                    this.getClass()
                            .getClassLoader()
                            .getResourceAsStream("workflow.yml"); // creates a stream of res/workflow.yml

            this.workflowConfig =
                    yaml.loadAs(inputStream, WorkflowConfig.class); // => reads the workflow.yml file and maps it into WorkFlow which has a Map of workflows.

            log.info("Workflow YAML loaded successfully");

            log.info("Loaded workflows: {}",
                    workflowConfig.getWorkflows().keySet());

        } catch (Exception e) {

            log.error("Failed to load workflow.yml", e);
            throw new RuntimeException(e);
        }
    }


//     @PostConstruct
    public void printWorkflowStructure() {

    com.starkIndustries.serviceJourney.workflow.WorkflowDefinition workflow =
            workflowConfig.getWorkflows().get("sessionFlow");

    StateDefinition activeState =
            workflow.getStates().get("ACTIVE");

    log.info("Transition Steps: {}",
            activeState.getOnTransitionEvent());

    log.info("End Steps: {}",
            activeState.getOnEndSession());

    log.info("Timeout Steps: {}",
            activeState.getOnTimeout());
}

    
}