package com.starkIndustries.serviceJourney.workflow.engine;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.starkIndustries.serviceJourney.workflow.StateDefinition;
import com.starkIndustries.serviceJourney.workflow.WorkflowDefinition;
import com.starkIndustries.serviceJourney.workflow.YamlLoaderService;
import com.starkIndustries.serviceJourney.workflow.context.WorkflowContext;
import com.starkIndustries.serviceJourney.workflow.executor.WorkflowStepExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * WorkflowEngineService — YAML-driven dynamic workflow execution engine.
 *
 * Reads workflow.yml to determine which steps to execute for a given
 * (workflow, state, eventType) combination, then invokes each step
 * on WorkflowStepExecutor via reflection.
 *
 * This engine is completely decoupled from Temporal.
 * It receives a pre-built WorkflowContext from the caller and
 * executes the steps defined in YAML sequentially.
 */
@Service
@Slf4j
public class WorkflowEngineService {

    @Autowired
    public YamlLoaderService yamlLoaderService;

    @Autowired
    public WorkflowStepExecutor workflowStepExecutor;

    /**
     * Executes a workflow event dynamically based on YAML configuration.
     *
     * @param workflow        The workflow name (e.g., "sessionFlow")
     * @param state           The current state (e.g., "ACTIVE")
     * @param eventType       The event type (e.g., "transitionEvent", "endSession", "timeout")
     * @param workflowContext Pre-built context containing all runtime data
     */
    public void execute(String workflow,
                        String state,
                        String eventType,
                        WorkflowContext workflowContext) {

        log.info("[Engine] Executing workflow='{}', state='{}', eventType='{}'",
                workflow, state, eventType);

        // =====================================================
        // STEP 1: Resolve workflow definition from YAML
        // =====================================================

        WorkflowDefinition workflowDefinition =
                yamlLoaderService
                        .getWorkflowConfig()
                        .getWorkflows()
                        .get(workflow);

        if (workflowDefinition == null) {
            log.error("[Engine] Workflow '{}' not found in YAML configuration", workflow);
            throw new RuntimeException("Workflow not found: " + workflow);
        }

        // =====================================================
        // STEP 2: Resolve state definition
        // =====================================================

        StateDefinition stateDefinition =
                workflowDefinition
                        .getStates()
                        .get(state);

        if (stateDefinition == null) {
            log.error("[Engine] State '{}' not found in workflow '{}'", state, workflow);
            throw new RuntimeException("State not found: " + state + " in workflow: " + workflow);
        }

        // =====================================================
        // STEP 3: Resolve event type → step list via reflection
        // =====================================================

        List<String> steps;

        try {

            String getterMethodName =
                    "getOn" +
                    Character.toUpperCase(eventType.charAt(0)) +
                    eventType.substring(1);

            Method getterMethod =
                    stateDefinition
                            .getClass()
                            .getMethod(getterMethodName);

            steps = (List<String>) getterMethod.invoke(stateDefinition);

        } catch (Exception e) {

            log.error("[Engine] Failed to resolve event type '{}' on state '{}'",
                    eventType, state, e);
            throw new RuntimeException(
                    "Failed to resolve event type: " + eventType, e);
        }

        if (steps == null || steps.isEmpty()) {
            log.warn("[Engine] No steps defined for eventType='{}' in state='{}'",
                    eventType, state);
            return;
        }

        log.info("[Engine] Resolved {} step(s) from YAML: {}", steps.size(), steps);
        log.info("[Engine] Context data: {}", workflowContext.getAll());

        // =====================================================
        // STEP 4: Execute each step via reflection on WorkflowStepExecutor
        // =====================================================

        for (String methodName : steps) {

            log.info("[Engine] Executing step: '{}'", methodName);

            try {

                Method method =
                        workflowStepExecutor
                                .getClass()
                                .getMethod(
                                        methodName,
                                        WorkflowContext.class);

                method.invoke(
                        workflowStepExecutor,
                        workflowContext);

                log.info("[Engine] Step '{}' completed successfully", methodName);

            } catch (NoSuchMethodException e) {

                log.error("[Engine] Method '{}' not found on WorkflowStepExecutor. "
                        + "Check workflow.yml matches executor methods.", methodName, e);
                throw new RuntimeException(
                        "Executor method not found: " + methodName, e);

            } catch (Exception e) {

                log.error("[Engine] Step '{}' failed during execution", methodName, e);
                throw new RuntimeException(
                        "Failed to execute step: " + methodName, e);
            }
        }

        log.info("[Engine] Workflow execution complete — workflow='{}', state='{}', eventType='{}'",
                workflow, state, eventType);
    }
}