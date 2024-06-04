# Alfresco Neural Search

## Overview

The **Alfresco Neural Search** Spring Boot app adds neural search capabilities to the Alfresco Content Services (ACS) Community Edition. It integrates [machine learning models](https://opensearch.org/docs/latest/ml-commons-plugin/pretrained-models/) to provide more accurate and context-aware search results within the Alfresco platform.

This repository contains the source code for the neural search Spring Boot app, which utilizes OpenSearch as the search engine and Alfresco as the content repository. By leveraging neural networks, the search extension offers improved search relevancy and performance compared to traditional keyword-based search methods.

## Prerequisites

Before setting up the Alfresco Neural Search Spring Boot app, ensure you have the following prerequisites:

- Alfresco Content Services Community Edition up and running
- OpenSearch set up and running
- Java 17 or later
- Maven 3.6.0 or later

## Installation

### Clone the Repository

```bash
git clone https://github.com/aborroy/alfresco-opensearch-neural-search.git
cd alfresco-opensearch-neural-search/alfresco-neural-search
```

### Build the Project

Use Maven to build the project:

```bash
mvn clean install
```

## Usage

Once installed and configured, the neural search app will enhance the standard search functionality in Alfresco. Users can perform searches as usual, and the neural search will provide more relevant and context-aware results.

## Configuration

Configuration options are available in the [application.properties](src/main/resources/application.properties) file.

### Alfresco Neural Search App

```
# Port for the Neural Search App
server.port=8081
# Cron expression to execute synchronization of Alfresco Repository to OpenSearch index
batch.indexer.cron=0/12 * * * * ?
# Maximum number of documents to handle in a single loop
batch.indexer.transaction.maxResults=100
# Alfresco Content Types that will be indexed (add comma separated values if required)
batch.indexer.indexableTypes=cm:content
```

### Alfresco Repository

```
# Basic authentication credentials for Alfresco Repository
content.service.security.basicAuth.username=admin
content.service.security.basicAuth.password=admin
# URL and path for Alfresco Server API
content.service.url=http://localhost:8080
content.service.path=/alfresco/api/-default-/public/alfresco/versions/1

# Alfresco SOLR Services Configuration
content.solr.path=/alfresco/service/api/solr/
content.solr.secret=ker0dxaln2b
```

### OpenSearch

```
# Hostname of the OpenSearch server
opensearch.host=localhost
# Port of the OpenSearch server
opensearch.port=9200
# Protocol for communication with OpenSearch server
opensearch.protocol=https
# Username for authentication with OpenSearch server
opensearch.user=admin
# Password for authentication with OpenSearch server
opensearch.password=Alfresco.org.2024
# Enable if TLS certificate subject needs to be verified to the host name
opensearch.verify.hostname=false
# Truststore configuration for TLS connection to OpenSearch
# Overwrite from Docker using JAVAX_NET_SSL_TRUSTSTORE env variable
javax.net.ssl.trustStore=alfresco.truststore
# Overwrite from Docker using JAVAX_NET_SSL_TRUSTSTORETYPE env variable
javax.net.ssl.trustStoreType=PKCS12
# Overwrite from Docker using JAVAX_NET_SSL_TRUSTSTOREPASSWORD env variable
javax.net.ssl.trustStorePassword=truststore

# OpenSearch ML plugin configuration
# Name of the OpenSearch index
opensearch.index.name=alfresco-nlp-index
# Name of the model group in OpenSearch ML
opensearch.model.group.name=Alfresco_NLP_model_group
# Name of the model used for NLP tasks (https://opensearch.org/docs/latest/ml-commons-plugin/pretrained-models/)
opensearch.model.name=huggingface/sentence-transformers/msmarco-distilbert-base-tas-b
# Name of the ingest pipeline for NLP tasks
opensearch.ingest.pipeline.name=alfresco-nlp-ingest-pipeline
# Number of results for neural queries
opensearch.results.count=10
```

## Running

Run the `alfresco-neural-app` app to create required model group, model, pipelines and indexes in OpenSearch. One OpenSearch is configured, Batch Indexer will ingest documents in Alfresco Repository.

```
cd alfresco-neural-search
mvn clean package
java -jar target/neural-search-0.8.0.jar
```

Once ready, endpoint will be available as in http://localhost:8081/search?query=(query)&searchType=(searchType)

* `query` parameter accepts the sentence for searching
* `searchType` accepts following values:
  * `Semantic` uses the neural searching engine, using the kNN index to provide results
  * `Keyword` uses the traditional searching engine, using the BM25 index to provide results
  * `Hybrid` combines neural and traditional searching engine, mixing results from both sources according to a weighting schema


## Running as a container

Build the Docker Image using following command (JAR file for `alfresco-neural-search` needs to be built before building the Docker Image):

```
docker build . -t alfresco-neural-search
```

Run the Docker Container:

```
docker run -p 8081:8081 \
-e CONTENT_SERVICE_URL=http://<ALFRESCO_HOST_NAME>:8080 \
-e OPENSEARCH_HOST=<OPENSEARCH_HOST_NAME> \
-e JAVAX_NET_SSL_TRUSTSTORE=/opt/app/alfresco.truststore \
-e JAVAX_NET_SSL_TRUSTSTORETYPE=PKCS12 \
-e JAVAX_NET_SSL_TRUSTSTOREPASSWORD=truststore \
-v ./src/main/resources/alfresco.truststore:/opt/app/alfresco.truststore \
alfresco-neural-search
```

Use the Docker Image as a Docker Compose service:


```
services:
  alfresco-neural-search:
    image: alfresco-neural-search
    ports:
      - "8081:8081"
    environment:
      CONTENT_SERVICE_URL: "http://<ALFRESCO_HOST_NAME>:8080"
      OPENSEARCH_HOST: "<OPENSEARCH_HOST_NAME>"
      JAVAX_NET_SSL_TRUSTSTORE: "/opt/app/alfresco.truststore"
      JAVAX_NET_SSL_TRUSTSTORETYPE: "PKCS12"
      JAVAX_NET_SSL_TRUSTSTOREPASSWORD: "truststore"
    volumes:
      - "./src/main/resources/alfresco.truststore:/opt/app/alfresco.truststore"
```
