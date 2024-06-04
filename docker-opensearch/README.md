# Dockerized OpenSearch

## Overview

The **Dockerized OpenSearch** project provides a Docker-based setup for deploying OpenSearch.

## Prerequisites

Ensure the following prerequisites are met before setting up the Dockerized OpenSearch environment:

* Docker installed
* Docker Compose installed

## Installation

### Clone the Repository

```bash
git clone https://github.com/aborroy/alfresco-opensearch-neural-search.git
cd alfresco-opensearch-neural-search/docker-opensearch
```

### Build and Start the OpenSearch Service

Use Docker Compose to build and start the OpenSearch service:

```bash
docker-compose up --build
```

This command will pull the necessary Docker images, build the custom images, and start the services defined in the `compose.yaml` file.

>> Note that [certs](certs) folder includes all the certificates required for TLS configuration (ca, opensearch nodes and opensearch dashboards).

### Verify the Setup

Once the service is up and running, verify the setup by accessing the OpenSearch dashboard:

* Credentials: admin / Alfresco.org.2024
* OpenSearch Dashboards: https://localhost:5601