package org.alfresco.opensearch.index;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.opensearch.client.OpenSearchClientFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.ResponseException;
import org.opensearch.client.RestClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Component for managing OpenSearch indices.
 */
@Component
public class Index {

    static final Logger LOG = LoggerFactory.getLogger(Index.class);
    public static final String ZERO_TIME_DATE = "1970-01-01T00:00:00.000Z";

    @Value("${opensearch.index.name}")
    private String indexName;

    @Value("${opensearch.ingest.pipeline.name}")
    private String ingestPipelineName;

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
     * Retrieves an instance of OpenSearchClient from the factory.
     *
     * @return OpenSearchClient instance
     */
    private OpenSearchClient openSearchClient() {
        return openSearchClientFactory.getOpenSearchClient();
    }

    /**
     * Applies the index settings and mappings to the OpenSearch cluster.
     *
     * @throws Exception if an error occurs while applying the settings
     */
    public void createKnnIndex() throws Exception {
        Request request = new Request("PUT", "/" + indexName);
        String jsonString = String.format("""
                {
                  "settings": {
                    "index.knn": true,
                    "default_pipeline": "%s"
                  },
                  "mappings": {
                    "properties": {
                      "id": {
                        "type": "text"
                      },
                      "passage_embedding": {
                        "type": "knn_vector",
                        "dimension": 768,
                        "method": {
                          "engine": "lucene",
                          "space_type": "l2",
                          "name": "hnsw",
                          "parameters": {}
                        }
                      },
                      "text": {
                        "type": "text"
                      }
                    }
                  }
                }
                """, ingestPipelineName);

        request.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));
        restClient().performRequest(request);

        LOG.info("Index {} associated to pipeline {} has been created with Knn configuration", indexName, ingestPipelineName);

    }

    /**
     * Create index to control alfresco indexing information (mainly last time the folder was synchronized)
     */
    public void createAlfrescoIndex() throws Exception {

        Request request = new Request("PUT", "/alfresco-control");
        String jsonString = """
                {
                  "mappings": {
                    "properties": {
                      "lastSyncTime": {
                        "type": "text"
                      }
                    }
                  }
                }
                """;

        request.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));
        restClient().performRequest(request);

        LOG.info("Internal index alfresco-control for alfresco indexing information has been created");

    }

    /**
     * Updates the last synchronization time in the Alfresco index.
     *
     * @param lastSyncTime The last synchronization time to be updated in the Alfresco index.
     * @throws Exception If an error occurs during the update process.
     */
    public void updateAlfrescoIndex(String lastSyncTime) throws Exception {
        Request request = new Request("PUT", "/alfresco-control/_doc/1");
        String jsonString = """
                {
                  "lastSyncTime": "%s"
                }
                """;
        request.setEntity(new StringEntity(String.format(jsonString, lastSyncTime), ContentType.APPLICATION_JSON));
        restClient().performRequest(request);
    }

    /**
     * Retrieves the value of the last synchronization time field from the Alfresco index.
     *
     * @return The value of the last synchronization time field.
     * @throws Exception If an error occurs during the retrieval process.
     */
    public String getAlfrescoIndexField() throws Exception {
        Request request = new Request("GET", "/alfresco-control/_doc/1");
        try {
            Response response = restClient().performRequest(request);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
            return jsonResponse.findValue("lastSyncTime").asText();
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return ZERO_TIME_DATE;
            } else {
                throw e;
            }
        }

    }

    /**
     * Checks if the index exists in the OpenSearch cluster.
     *
     * @return true if the index exists, false otherwise
     * @throws IOException if an I/O error occurs
     */
    public boolean existIndex() throws IOException {
        return openSearchClient().indices().exists(new ExistsRequest.Builder().index(indexName).build()).value();
    }
}
