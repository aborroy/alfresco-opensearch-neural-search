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

    @Value("${opensearch.ingest.pipeline.name}")
    private String pipelineName;

    @Value("${opensearch.results.count}")
    private int resultsCount;

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
     * Executes a keyword search query in OpenSearch.
     *
     * @param query the search query
     * @return the search result as a JsonNode
     * @throws IOException if an I/O error occurs during the request
     */
    public JsonNode keywordSearch(String query) throws IOException {
        Request request = new Request("GET", "/" + indexName + "/_search");
        String jsonSearchPayload = """
            {
              "_source": {
                "excludes": [
                  "passage_embedding"
                ]
              },
              "query": {
                "match": {
                  "text": {
                    "query": "%s"
                  }
                }
              }
            }
            """;

        String escapedQuery = JsonUtils.escape(query);
        request.setEntity(new StringEntity(String.format(jsonSearchPayload, escapedQuery), ContentType.APPLICATION_JSON));
        return search(request);
    }

    /**
     * Executes a neural search query in OpenSearch.
     *
     * @param query the search query
     * @return the search result as a JsonNode
     * @throws IOException if an I/O error occurs during the request
     */
    public JsonNode neuralSearch(String query) throws IOException {
        Request request = new Request("GET", "/" + indexName + "/_search");
        String jsonSearchPayload = """
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
                     "k": %s
                   }
                 }
               }
             }
             """;

        String escapedQuery = JsonUtils.escape(query);
        String modelId = openSearchConfiguration.getModelId();
        request.setEntity(new StringEntity(String.format(jsonSearchPayload, escapedQuery, modelId, resultsCount), ContentType.APPLICATION_JSON));
        return search(request);
    }

    /**
     * Executes a hybrid search (keyword + neural) query in OpenSearch.
     *
     * @param query the search query
     * @return the search result as a JsonNode
     * @throws IOException if an I/O error occurs during the request
     */
    public JsonNode hybridSearch(String query) throws IOException {
        Request request = new Request("GET", "/" + indexName + "/_search?search_pipeline=" + pipelineName);
        String jsonSearchPayload = """
            {
              "_source": {
                "exclude": [
                  "passage_embedding"
                ]
              },
              "query": {
                "hybrid": {
                  "queries": [
                    {
                      "match": {
                        "text": {
                          "query": "%s"
                        }
                      }
                    },
                    {
                      "neural": {
                        "passage_embedding": {
                          "query_text": "%s",
                          "model_id": "%s",
                          "k": %s
                        }
                      }
                    }
                  ]
                }
              }
            }
            """;

        String escapedQuery = JsonUtils.escape(query);
        String modelId = openSearchConfiguration.getModelId();
        request.setEntity(new StringEntity(String.format(jsonSearchPayload, escapedQuery, escapedQuery, modelId, resultsCount), ContentType.APPLICATION_JSON));
        return search(request);
    }

    /**
     * Executes a search request using the provided {@link Request} object and parses the response into a {@link JsonNode}.
     *
     * <p>This method uses an HTTP client to perform the search request, then converts the response content into a JSON
     * representation using the Jackson library.</p>
     *
     * @param request the search {@link Request} to be executed
     * @return a {@link JsonNode} representing the JSON response from the search request
     * @throws IOException if an I/O error occurs during the execution of the request or parsing the response
     */
    private JsonNode search(Request request) throws IOException {
        Response response = restClient().performRequest(request);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(response.getEntity().getContent());
    }

}