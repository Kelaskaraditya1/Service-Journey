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
