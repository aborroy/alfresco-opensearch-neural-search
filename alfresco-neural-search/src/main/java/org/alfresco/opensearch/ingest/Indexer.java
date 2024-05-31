package org.alfresco.opensearch.ingest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.opensearch.client.OpenSearchClientFactory;
import org.alfresco.repo.service.BatchIndexerService;
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

/**
 * Component for indexing documents into OpenSearch.
 */
@Component
public class Indexer {

    private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    @Value("${opensearch.index.name}")
    private String indexName;

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
     * Indexes a document with the specified UUID and text content into the OpenSearch index.
     *
     * @param uuid the UUID of the document
     * @param name the UUID of the document
     * @param text the text content of the document
     */
    public void index(String uuid, Long dbid, String name, String text) {
        if (!text.isEmpty()) {
            Request request = new Request("POST", "/" + indexName + "/_doc");
            String jsonString = """
                    {
                       "id": "%s",
                       "dbid": "%o",
                       "name": "%s",
                       "text": "%s"
                    }
                    """;
            String formattedJson = String.format(jsonString, uuid, dbid, name, text);
            request.setEntity(new StringEntity(formattedJson, ContentType.APPLICATION_JSON));
            try {
                restClient().performRequest(request);
            } catch (Exception e) {
                LOG.warn("Following segment has not been indexed due to the Exception: {}", e.getMessage());
                LOG.debug(e.getMessage(), e);
                LOG.warn(formattedJson);
            }
        }
    }

    /**
     * Verifies indexing process by using the model is working.
     */
    public void verifyIndexStatus() throws Exception {

        Request request = new Request("POST", "/" + indexName + "/_doc");
        String jsonString = """
                    {
                       "id": "%s",
                       "dbid": "%o",
                       "name": "%s",
                       "text": "%s"
                    }
                    """;
        String formattedJson = String.format(jsonString, "1", 1L, "verify", "verify");
        request.setEntity(new StringEntity(formattedJson, ContentType.APPLICATION_JSON));
        restClient().performRequest(request);

        request = new Request("POST", "/" + indexName + "/_delete_by_query");
        jsonString = """
                    {
                      "query": {
                        "match": {
                          "id": "%s"
                        }
                      }
                    }
                    """;
        request.setEntity(new StringEntity(String.format(jsonString, "1"), ContentType.APPLICATION_JSON));
        restClient().performRequest(request);

    }

    /**
     * Deletes document segments from the index if they exist, based on the provided UUID.
     *
     * @param uuid The UUID of the document to be deleted.
     * @throws IOException If an I/O exception occurs while interacting with OpenSearch.
     */
    public void deleteDocumentIfExists(String uuid) throws IOException {
        // Add _* to cover all document parts (as they are split for indexing)
        deleteDocumentIfExists("id", uuid +"_*");
    }

    /**
     * Deletes document segments from the index if they exist, based on the provided dbid.
     *
     * @param dbid The dbid of the document to be deleted.
     * @throws IOException If an I/O exception occurs while interacting with OpenSearch.
     */
    public void deleteDocumentIfExists(Long dbid) throws IOException {
        deleteDocumentIfExists("dbid", dbid.toString());
    }

    /**
     * Deletes document segments from the index if they exist, based on the provided field and value.
     *
     * @param field The field to match for deletion.
     * @param value The value of the field to be deleted.
     * @throws IOException If an I/O exception occurs while interacting with OpenSearch.
     */
    private void deleteDocumentIfExists(String field, String value) throws IOException {

        Request request = new Request("GET", "/" + indexName + "/_search");
        String jsonString = """
                {
                  "query": {
                    "match": {
                      "%s": "%s"
                    }
                  }
                }
                """;
        request.setEntity(new StringEntity(String.format(jsonString, field, value), ContentType.APPLICATION_JSON));
        Response response = restClient().performRequest(request);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
        int totalValue = jsonResponse.path("hits").path("total").path("value").asInt();

        if (totalValue > 0) {

            request = new Request("POST", "/" + indexName + "/_delete_by_query");
            jsonString = """
                    {
                      "query": {
                        "match": {
                          "%s": "%s"
                        }
                      }
                    }
                    """;
            request.setEntity(new StringEntity(String.format(jsonString, field, value), ContentType.APPLICATION_JSON));
            restClient().performRequest(request);

        }
    }

}