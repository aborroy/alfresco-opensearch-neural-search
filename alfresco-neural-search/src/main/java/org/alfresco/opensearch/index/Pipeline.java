package org.alfresco.opensearch.index;

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

/**
 * Component for managing OpenSearch pipelines.
 */
@Component
public class Pipeline {

    static final Logger LOG = LoggerFactory.getLogger(Pipeline.class);

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
     * Applies the ingest and search pipelines for the given model ID.
     *
     * @param modelId the ID of the model to be used in the pipeline
     * @throws IOException if an I/O error occurs during the request
     */
    public void apply(String modelId) throws IOException {
        // Ingest pipeline
        Request request = new Request("PUT", "/_ingest/pipeline/" + ingestPipelineName);
        String ingestJsonString = """
                {
                  "description": "An NLP ingest pipeline",
                  "processors": [
                    {
                      "text_embedding": {
                        "model_id": "%s",
                        "field_map": {
                          "text": "passage_embedding"
                        }
                      }
                    }
                  ]
                }
                """;
        request.setEntity(new StringEntity(String.format(ingestJsonString, modelId), ContentType.APPLICATION_JSON));
        Response response = restClient().performRequest(request);

        // Search pipeline
        request = new Request("PUT", "/_search/pipeline/" + ingestPipelineName);
        String searchJsonString = """
                {
                  "description": "Post processor for hybrid search",
                  "phase_results_processors": [
                    {
                      "normalization-processor": {
                        "normalization": {
                          "technique": "min_max"
                        },
                        "combination": {
                          "technique": "arithmetic_mean",
                          "parameters": {
                            "weights": [
                              0.3,
                              0.7
                            ]
                          }
                        }
                      }
                    }
                  ]
                }
                """;
        request.setEntity(new StringEntity(searchJsonString, ContentType.APPLICATION_JSON));
        restClient().performRequest(request);

        LOG.info("Ingest and search pipeline {} has been configured", ingestPipelineName);
    }
}
