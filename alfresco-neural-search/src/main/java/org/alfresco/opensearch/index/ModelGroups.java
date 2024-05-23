package org.alfresco.opensearch.index;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.opensearch.client.OpenSearchClientFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Component for managing OpenSearch model groups.
 */
@Component
public class ModelGroups {

    @Value("${opensearch.model.group.name}")
    private String modelGroupName;

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
     * Registers a new model group with the specified name.
     *
     * @return the ID of the registered model group
     * @throws IOException if an I/O error occurs during the request
     */
    public String apply() throws IOException {
        Request request = new Request("POST", "/_plugins/_ml/model_groups/_register");
        String jsonString = """
                {
                  "name": "%s",
                  "description": "A model group for NLP models",
                  "access_mode": "public"
                }
                """;
        request.setEntity(new StringEntity(String.format(jsonString, modelGroupName), ContentType.APPLICATION_JSON));
        Response response = restClient().performRequest(request);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
        return jsonResponse.get("model_group_id").asText();
    }

    /**
     * Searches for a model group by name and retrieves its ID.
     *
     * @return the ID of the model group
     * @throws IOException if an I/O error occurs during the request
     */
    public String getModelGroupId() throws IOException {
        Request request = new Request("POST", "/_plugins/_ml/model_groups/_search");
        String jsonString = """
                {
                  "query": {
                    "match": {
                      "name": "%s"
                    }
                  }
                }
                """;
        request.setEntity(new StringEntity(String.format(jsonString, modelGroupName), ContentType.APPLICATION_JSON));
        Response response = restClient().performRequest(request);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
        return jsonResponse.findValue("_id").asText();
    }
}
