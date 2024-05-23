package org.alfresco.opensearch.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.opensearch.client.OpenSearchClientFactory;
import org.alfresco.opensearch.index.OpenSearchConfiguration;
import org.alfresco.utils.JsonUtils;
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
 * Component for executing searches in OpenSearch.
 */
@Component
public class Search {

    @Value("${opensearch.index.name}")
    private String indexName;

    @Autowired
    private OpenSearchConfiguration openSearchConfiguration;

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
     * Executes a search query in OpenSearch.
     *
     * @param query the search query
     * @return the search result as a JsonNode
     * @throws IOException if an I/O error occurs during the request
     */
    public JsonNode search(String query) throws IOException {
        Request request = new Request("GET", "/" + indexName + "/_search");
        String jsonString = """
             {
               "_source": {
                 "excludes": [
                   "passage_embedding"
                 ]
               },
               "query": {
                 "neural": {
                   "passage_embedding": {
                     "query_text": "%s",
                     "model_id": "%s",
                     "k": 5
                   }
                 }
               }
             }
             """;

        // Escape the query text to prevent injection attacks
        String escapedQuery = JsonUtils.escape(query);

        // Get the model ID from the OpenSearch configuration
        String modelId = openSearchConfiguration.getModelId();

        // Set the query parameters in the request
        request.setEntity(new StringEntity(String.format(jsonString, escapedQuery, modelId), ContentType.APPLICATION_JSON));

        // Execute the request and retrieve the response
        Response response = restClient().performRequest(request);

        // Parse the response content into a JsonNode
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(response.getEntity().getContent());
    }
}