package org.alfresco.opensearch.client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Factory class for creating and managing HTTP client connections to the Alfresco Solr API.
 */
@Component
public class AlfrescoSolrApiClientFactory {

    /**
     * Header name for Alfresco search secret.
     */
    public static final String X_ALFRESCO_SEARCH_SECRET = "X-Alfresco-Search-Secret";

    private static final Logger logger = LoggerFactory.getLogger(AlfrescoSolrApiClientFactory.class);

    /**
     * Base URL for the content service.
     */
    @Value("${content.service.url}")
    private String url;

    /**
     * API path for the Solr service.
     */
    @Value("${content.solr.path}")
    private String apiPath;

    /**
     * Secret key for accessing the Solr service.
     */
    @Value("${content.solr.secret}")
    private String secret;

    private CloseableHttpClient httpClient;

    /**
     * Initializes the HTTP client after the bean is constructed.
     */
    @PostConstruct
    public void init() {
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Closes the HTTP client before the bean is destroyed.
     */
    @PreDestroy
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            logger.error("Error closing HttpClient", e);
        }
    }

    /**
     * Creates an HttpGet request with the specified path and adds the required headers.
     *
     * @param path the path to the Solr endpoint.
     * @return a configured HttpGet request.
     */
    private HttpGet createHttpGetRequest(String path) {
        HttpGet request = new HttpGet(url + apiPath + path);
        request.addHeader(X_ALFRESCO_SEARCH_SECRET, secret);
        return request;
    }

    /**
     * Creates an HttpPost request with the specified path and payload, and adds the required headers.
     *
     * @param path the path to the Solr endpoint.
     * @param payload the payload to be sent in the POST request.
     * @return a configured HttpPost request.
     */
    private HttpPost createHttpPostRequest(String path, String payload) {
        HttpPost request = new HttpPost(url + apiPath + path);
        request.addHeader(X_ALFRESCO_SEARCH_SECRET, secret);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        return request;
    }

    /**
     * Executes the given HttpGet request and returns the response as a String.
     *
     * @param request the HttpGet request to be executed.
     * @return the response from the server as a String.
     * @throws IOException if an error occurs during request execution.
     */
    private String executeRequest(HttpGet request) throws IOException {
        try {
            return httpClient.execute(request, response -> response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            logger.error("Error executing GET request", e);
            throw e;
        }
    }

    /**
     * Executes the given HttpPost request and returns the response as a String.
     *
     * @param request the HttpPost request to be executed.
     * @return the response from the server as a String.
     * @throws IOException if an error occurs during request execution.
     */
    private String executeRequest(HttpPost request) throws IOException {
        try {
            return httpClient.execute(request, response -> response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            logger.error("Error executing POST request", e);
            throw e;
        }
    }

    /**
     * Executes a GET request to the specified path.
     *
     * @param path the path to the Solr endpoint.
     * @return the response from the server as a String.
     * @throws IOException if an error occurs during request execution.
     */
    public String executeGetRequest(String path) throws IOException {
        HttpGet request = createHttpGetRequest(path);
        return executeRequest(request);
    }

    /**
     * Executes a POST request to the specified path with the given payload.
     *
     * @param path the path to the Solr endpoint.
     * @param payload the payload to be sent in the POST request.
     * @return the response from the server as a String.
     * @throws IOException if an error occurs during request execution.
     */
    public String executePostRequest(String path, String payload) throws IOException {
        HttpPost request = createHttpPostRequest(path, payload);
        return executeRequest(request);
    }

}
