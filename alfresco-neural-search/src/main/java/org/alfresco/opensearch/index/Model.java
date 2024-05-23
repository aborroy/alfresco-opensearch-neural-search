package org.alfresco.opensearch.index;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.opensearch.client.OpenSearchClientFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Component for managing Model deployment and configuration.
 */
@Component
public class Model {

    static final Logger LOG = LoggerFactory.getLogger(Model.class);

    static final int MAX_RETRIES = 10;
    static final int RETRY_DELAY_MS = 10000;

    @Value("${opensearch.model.name}")
    private String modelName;

    @Autowired
    private OpenSearchClientFactory openSearchClientFactory;

    /**
     * Retrieves an instance of RestClient from the factory.
     *
     * @return RestClient instance
     */
    private RestClient restClient() {
        return openSearchClientFactory.getRestClient();
    }

    /**
     * Registers, monitors, and deploys a model to the OpenSearch cluster.
     *
     * @param modelGroupId the model group ID to associate the model with
     * @return the deployed model ID
     * @throws Exception if an error occurs during the process
     */
    public String apply(String modelGroupId) throws Exception {
        String taskId = registerModel(modelGroupId);
        waitForTaskCompletion(taskId);

        String modelId = getModelIdFromTask(taskId);
        taskId = deployModel(modelId);
        waitForTaskCompletion(taskId);

        LOG.info("Model {} has been registered and deployed to model group {}", modelId, modelGroupId);

        return modelId;
    }

    /**
     * Searches and retrieves the model ID based on the model name.
     *
     * @return the model ID
     * @throws Exception if an error occurs during the process
     */
    public String getModelId() throws Exception {
        Request request = new Request("POST", "/_plugins/_ml/models/_search");
        String jsonString = """
                {
                  "query": {
                    "match": {
                      "name": "%s"
                    }
                  }
                }
                """;
        request.setEntity(new StringEntity(String.format(jsonString, modelName), ContentType.APPLICATION_JSON));
        Response response = restClient().performRequest(request);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
        return jsonResponse.findValue("model_id").asText();
    }

    /**
     * Registers the model with the given model group ID.
     *
     * @param modelGroupId the model group ID
     * @return the task ID associated with the registration
     * @throws IOException if an I/O error occurs
     */
    private String registerModel(String modelGroupId) throws IOException {
        Request request = new Request("POST", "/_plugins/_ml/models/_register");
        String jsonString = """
                {
                  "name": "%s",
                  "version": "1.0.1",
                  "model_group_id": "%s",
                  "model_format": "TORCH_SCRIPT"
                }
                """;
        request.setEntity(new StringEntity(String.format(jsonString, modelName, modelGroupId), ContentType.APPLICATION_JSON));
        Response response = restClient().performRequest(request);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
        return jsonResponse.get("task_id").asText();
    }

    /**
     * Waits for the task with the specified task ID to complete by periodically checking its state.
     * This method will retry a specified number of times and exit with an error if the task state is not "CREATED".
     *
     * @param taskId the ID of the task to monitor
     * @throws IOException if an I/O error occurs during the request
     * @throws InterruptedException if the thread is interrupted while sleeping between retries
     */
    private void waitForTaskCompletion(String taskId) throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        String taskState = "CREATED";
        int attempt = 0;

        while (attempt < MAX_RETRIES && "CREATED".equals(taskState)) {
            TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
            Request request = new Request("GET", "/_plugins/_ml/tasks/" + taskId);
            Response response = restClient().performRequest(request);
            JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
            taskState = jsonResponse.get("state").asText();
            attempt++;
        }

        if (!"COMPLETED".equals(taskState)) {
            throw new IOException("Task " + taskId + " failed to complete after " + MAX_RETRIES + " attempts, task state: " + taskState);
        }
    }

    /**
     * Retrieves the model ID from the specified task.
     *
     * @param taskId the task ID
     * @return the model ID
     * @throws IOException if an I/O error occurs
     */
    private String getModelIdFromTask(String taskId) throws IOException {
        Request request = new Request("GET", "/_plugins/_ml/tasks/" + taskId);
        Response response = restClient().performRequest(request);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
        return jsonResponse.get("model_id").asText();
    }

    /**
     * Deploys the model with the specified model ID.
     *
     * @param modelId the model ID to deploy
     * @return the task ID associated with the registration
     * @throws IOException if an I/O error occurs
     */
    private String deployModel(String modelId) throws IOException {
        Request request = new Request("POST", "/_plugins/_ml/models/" + modelId + "/_deploy");
        Response response = restClient().performRequest(request);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
        return jsonResponse.get("task_id").asText();
    }
}