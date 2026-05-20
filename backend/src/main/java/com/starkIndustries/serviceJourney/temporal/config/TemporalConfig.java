package com.starkIndustries.serviceJourney.temporal.config;

import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.starkIndustries.serviceJourney.temporal.activity.SessionActivitiesImpl;
import com.starkIndustries.serviceJourney.temporal.workflow.SessionWorkflowImpl;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * ============================================================
 * TemporalConfig — Spring Configuration for Temporal.io
 * ============================================================
 * 
 * Sets up:
 *   1. WorkflowServiceStubs → connection to Temporal server
 *   2. WorkflowClient       → used by controllers to start/signal workflows
 *   3. WorkerFactory         → manages worker lifecycle
 *   4. Worker                → polls the task queue and executes workflows/activities
 * 
 * Task Queue: SERVICE_JOURNEY_QUEUE
 * 
 * PREREQUISITE:
 *   A Temporal server must be running at the configured address.
 *   Default: localhost:7233
 * 
 *   Quick start with Docker:
 *     docker run --rm -p 7233:7233 -p 8233:8233 temporalio/auto-setup:latest
 * 
 *   Or use Temporal CLI:
 *     temporal server start-dev
 */
@Configuration
@Slf4j
public class TemporalConfig {

  /** Task queue name — shared between worker and workflow starters */
  public static final String TASK_QUEUE = "SERVICE_JOURNEY_QUEUE";

  /** Temporal server address — configurable via application.properties */
  @Value("${temporal.server.address:localhost:7233}")
  private String temporalServerAddress;

  /** Spring-managed activity implementation (has @Autowired repos) */
  @Autowired
  private SessionActivitiesImpl sessionActivitiesImpl;

  private WorkflowServiceStubs serviceStubs;
  private WorkerFactory workerFactory;

  // ============================================================
  // BEANS
  // ============================================================

  /**
   * Connection to the Temporal server (gRPC).
   * This is a long-lived connection used throughout the app lifecycle.
   */
  @Bean
  public WorkflowServiceStubs workflowServiceStubs() {
    log.info("Connecting to Temporal server at: {}", temporalServerAddress);

    this.serviceStubs = WorkflowServiceStubs.newServiceStubs(
        WorkflowServiceStubsOptions.newBuilder()
            .setTarget(temporalServerAddress)
            .build());

    log.info("Temporal connection established");
    return serviceStubs;
  }

  /**
   * Client for starting workflows, sending signals, and running queries.
   * Used by controllers to interact with Temporal.
   */
  @Bean
  public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
    return WorkflowClient.newInstance(serviceStubs);
  }

  /**
   * Creates and starts the worker that listens on the task queue.
   * 
   * The worker:
   *   - Registers the SessionWorkflowImpl (workflow logic)
   *   - Registers the SessionActivitiesImpl (DB operations)
   *   - Polls SERVICE_JOURNEY_QUEUE for tasks
   *   - Executes workflows and activities when tasks arrive
   */
  @Bean
  public WorkerFactory workerFactory(WorkflowClient workflowClient) {

    this.workerFactory = WorkerFactory.newInstance(workflowClient);

    Worker worker = workerFactory.newWorker(TASK_QUEUE);

    // Register workflow implementation class
    worker.registerWorkflowImplementationTypes(SessionWorkflowImpl.class);

    // Register activity implementation instance (Spring-managed, has autowired repos)
    worker.registerActivitiesImplementations(sessionActivitiesImpl);

    // Start polling the task queue
    workerFactory.start();

    log.info("Temporal Worker started — listening on task queue: {}", TASK_QUEUE);

    return workerFactory;
  }

  /**
   * Graceful shutdown — stops the worker and closes the connection.
   */
  @PreDestroy
  public void cleanup() {
    log.info("Shutting down Temporal worker and connection...");

    if (workerFactory != null) {
      workerFactory.shutdown();
    }
    if (serviceStubs != null) {
      serviceStubs.shutdown();
    }

    log.info("Temporal shutdown complete");
  }

}
