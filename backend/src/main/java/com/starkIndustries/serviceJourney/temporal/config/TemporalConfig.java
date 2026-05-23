package com.starkIndustries.serviceJourney.temporal.config;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.starkIndustries.serviceJourney.keys.Keys;
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


  @Value("${temporal.server.address:localhost:7233}")
  private String temporalServerAddress; // Thsi is the address at which temporal is hosted and our app will connect to it.

  @Autowired
  private SessionActivitiesImpl sessionActivitiesImpl;

  private WorkflowServiceStubs serviceStubs;
  private WorkerFactory workerFactory;


  @Bean
  public WorkflowServiceStubs workflowServiceStubs() {

    // instead of directly returning the newInstance(). we are manually connecting to the address, but it's depricated.

    log.info("Connecting to Temporal server at: {}", temporalServerAddress);

    this.serviceStubs = WorkflowServiceStubs.newServiceStubs(
        WorkflowServiceStubsOptions.newBuilder()
            .setTarget(temporalServerAddress)
            .build());

    log.info("Temporal connection established");

    return serviceStubs;
  }


  @Bean
  public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
    return WorkflowClient.newInstance(serviceStubs);
  }

  @Bean
  public WorkerFactory workerFactory(WorkflowClient workflowClient) {

    this.workerFactory = WorkerFactory.newInstance(workflowClient);
    Worker worker = workerFactory.newWorker(Keys.TASK_QUEUE);

    // Register workflow implementation class
    worker.registerWorkflowImplementationTypes(SessionWorkflowImpl.class);

    // Register activity implementation instance 
    worker.registerActivitiesImplementations(sessionActivitiesImpl);

    // Start polling the task queue, since we have written this method therefor we donot have to write an extra method. 
    workerFactory.start();

    log.info("Temporal Worker started — listening on task queue: {}", Keys.TASK_QUEUE);

    return workerFactory;
  }


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
