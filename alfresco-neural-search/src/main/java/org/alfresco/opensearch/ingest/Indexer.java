package org.alfresco.opensearch.ingest;

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
 * Component for indexing documents into OpenSearch.
 */
@Component
public class Indexer {

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
     * @param text the text content of the document
     * @throws IOException if an I/O error occurs during the request
     */
    public void index(String uuid, String text) throws IOException {
        Request request = new Request("POST", "/" + indexName + "/_doc");
        String jsonString = """
            {
               "id": "%s",
               "text": "%s"
            }
            """;

        request.setEntity(new StringEntity(String.format(jsonString, uuid, text), ContentType.APPLICATION_JSON));
        restClient().performRequest(request);
    }

    /**
     * Deletes document segments from the index if they exist, based on the provided UUID.
     *
     * @param uuid The UUID of the document to be deleted.
     * @throws IOException If an I/O exception occurs while interacting with OpenSearch.
     */
    public void deleteDocumentIfExists(String uuid) throws IOException {

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
        int totalValue = jsonResponse.path("hits").path("total").path("value").asInt();

        if (totalValue > 0) {

            request = new Request("POST", "/" + indexName + "/_delete_by_query");
            jsonString = """
                {
                  "query": {
                    "match": {
                      "id": "%s_*"
                    }
                  }
                }
                """;
            request.setEntity(new StringEntity(String.format(jsonString, uuid), ContentType.APPLICATION_JSON));
            restClient().performRequest(request);

        }

    }

}