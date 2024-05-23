package org.alfresco.opensearch.client;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;

/**
 * Factory class for creating and managing OpenSearch clients.
 */
@Component
public class OpenSearchClientFactory {

    @Value("${opensearch.host}")
    private String opensearchHost;

    @Value("${opensearch.port}")
    private Integer opensearchPort;

    @Value("${opensearch.protocol}")
    private String opensearchProtocol;

    @Value("${opensearch.user}")
    private String opensearchUser;

    @Value("${opensearch.password}")
    private String opensearchPassword;

    private OpenSearchClient openSearchClient;
    private RestClient restClient;

    /**
     * Initializes the OpenSearch client and REST client.
     */
    private synchronized void init() {
        if (openSearchClient != null && restClient != null) {
            return;
        }

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(opensearchUser, opensearchPassword));

        SSLContext sslContext;
        try {
            sslContext = SSLContextBuilder.create().loadTrustMaterial(null, (chains, authType) -> true).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSLContext", e);
        }

        restClient = RestClient.builder(new HttpHost(opensearchHost, opensearchPort, opensearchProtocol))
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setSSLContext(sslContext).setDefaultCredentialsProvider(credentialsProvider))
                .build();

        OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        openSearchClient = new OpenSearchClient(transport);
    }

    /**
     * Provides an instance of OpenSearchClient. Initializes the client if not already done.
     *
     * @return OpenSearchClient instance
     */
    public OpenSearchClient getOpenSearchClient() {
        if (openSearchClient == null) {
            init();
        }
        return openSearchClient;
    }

    /**
     * Provides an instance of RestClient. Initializes the client if not already done.
     *
     * @return RestClient instance
     */
    public RestClient getRestClient() {
        if (restClient == null) {
            init();
        }
        return restClient;
    }
}