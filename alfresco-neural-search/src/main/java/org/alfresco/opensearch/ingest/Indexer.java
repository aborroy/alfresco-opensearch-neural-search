package org.alfresco.opensearch.ingest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.opensearch.client.OpenSearchClientFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.opensearch.client.ResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
     * @param contentId the id of the content
     * @param name the UUID of the document
     * @param text the text content of the document
     */
    public void index(String uuid, Long dbid, String contentId, String name, String text) {
        if (!text.isEmpty()) {
            Request request = new Request("POST", "/" + indexName + "/_doc");
            String jsonString = """
                    {
                       "id": "%s",
                       "dbid": %s,
                       "contentId": %s,
                       "name": "%s",
                       "text": "%s"
                    }
                    """;
            String formattedJson = String.format(jsonString, uuid, dbid, contentId, name, text);
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
        int retryCount = 0;
        boolean success = false;

        while (retryCount < 3 && !success) {
            try {
                Request request = new Request("POST", "/" + indexName + "/_doc");
                String jsonString = """
                        {
                           "id": "%s",
                           "dbid": %s,
                           "contentId": %s,
                           "name": "%s",
                           "text": "%s"
                        }
                        """;
                String formattedJson = String.format(jsonString, "1", 1L, "1", "verify", "verify");
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

                success = true; // Mark the operation as successful
            } catch (ResponseException e) {
                if (e.getResponse().getStatusLine().getStatusCode() == 403) {
                    // If Forbidden status, retry
                    retryCount++;
                    TimeUnit.SECONDS.sleep(1);
                } else {
                    throw e; // Re-throw if it's not the expected exception
                }
            }
        }

        if (!success) {
            throw new Exception("Failed to verify index status after 3 attempts");
        }
    }

    public String getContentId(String uuid) throws Exception {

        String contentId = "";

        Request request = new Request("GET", "/" + indexName + "/_search");
        String jsonString = """
                {
                  "query": {
                    "match": {
                      "id": "%s_*"
                    }
                  }
                }
                """;
        request.setEntity(new StringEntity(String.format(jsonString, uuid), ContentType.APPLICATION_JSON));
        Response response = restClient().performRequest(request);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
        JsonNode hitsNode = jsonResponse.path("hits").path("hits");
        if (hitsNode.isArray() && !hitsNode.isEmpty()) {
            JsonNode firstHitNode = hitsNode.get(0);
            JsonNode contentIdNode = firstHitNode.path("_source").path("contentId");
            contentId = contentIdNode.asText();
        }

        return contentId;

    }

    /**
     * Deletes document segments from the index if they exist, based on the provided UUID.
     *
     * @param uuid The UUID of the document to be deleted.
     * @throws IOException If an I/O exception occurs while interacting with OpenSearch.
     */
    public void deleteDocumentIfExists(String uuid) throws Exception {

        Request request = new Request("GET", "/" + indexName + "/_search");
        String jsonString = """
                {
                  "query": {
                    "match": {
                      "id": "%s_*"
                    }
                  }
                }
                """;
        request.setEntity(new StringEntity(String.format(jsonString, uuid), ContentType.APPLICATION_JSON));
        Response response = restClient().performRequest(request);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
        int totalValue = jsonResponse.get("hits").get("total").get("value").asInt();

        if (totalValue > 0) {
            deleteDocument(uuid);
        }

    }

    /**
     * Deletes document segments from the index, based on the provided UUID.
     * Retries deletion asynchronously 3 times (5 sec delay) to handle concurrent document modifications.
     *
     * @param uuid The UUID of the document to be deleted.
     * @throws IOException If an I/O exception occurs while interacting with OpenSearch.
     */
    public void deleteDocument(String uuid) throws Exception {
        CompletableFuture.supplyAsync(() -> {
            int attempt = 0;
            while (attempt < 3) {
                Request request = new Request("POST", "/" + indexName + "/_delete_by_query");
                String jsonString = """
                        {
                          "query": {
                            "match": {
                              "id": "%s_*"
                            }
                          }
                        }
                        """;
                request.setEntity(new StringEntity(String.format(jsonString, uuid), ContentType.APPLICATION_JSON));
                try {
                    Response response = restClient().performRequest(request);
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
                    int deleteCount = jsonResponse.get("total").asInt();
                    if (deleteCount > 0) {
                        return null;
                    }
                } catch (IOException e) {
                    LOG.warn(e.getMessage());
                }
                attempt++;
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return null;
        });
    }


}