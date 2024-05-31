package org.alfresco.opensearch.client;

import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
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

    @Value("${opensearch.verify.hostname}")
    private Boolean verifyHostname;

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

        overrideTruststoreDefaults();
        SSLContext sslContext;
        try {
            sslContext = SSLContextBuilder.create().loadTrustMaterial((chain, authType) -> true).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSLContext", e);
        }

        RestClientBuilder builder = RestClient.builder(new HttpHost(opensearchHost, opensearchPort, opensearchProtocol));
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setSSLContext(sslContext);
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            if (!verifyHostname) {
                httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            }
            return httpClientBuilder;
        });
        restClient = builder.build();

        OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        openSearchClient = new OpenSearchClient(transport);
    }

    /**
     * Overrides the default truststore configuration by reading environment variables.
     * Environment variables:
     * - JAVAX_NET_SSL_TRUSTSTORE: Path to the truststore file.
     * - JAVAX_NET_SSL_TRUSTSTORETYPE: Type of the truststore (e.g., JKS, PKCS12).
     * - JAVAX_NET_SSL_TRUSTSTOREPASSWORD: Password for the truststore.
     */
    private void overrideTruststoreDefaults() {
        String trustStorePath = System.getenv("JAVAX_NET_SSL_TRUSTSTORE");
        if (trustStorePath != null) {
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        }
        String trustStoreType = System.getenv("JAVAX_NET_SSL_TRUSTSTORETYPE");
        if (trustStoreType != null) {
            System.setProperty("javax.net.ssl.trustStoreType", trustStoreType);
        }
        String trustStorePassword = System.getenv("JAVAX_NET_SSL_TRUSTSTOREPASSWORD");
        if (trustStorePassword != null) {
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        }
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