# Dockerized Alfresco

## Overview

The **Dockerized Alfresco** project provides a Docker-based setup for running Alfresco Community.

## Prerequisites

Ensure the following prerequisites are met before setting up the Dockerized Alfresco Neural Search environment:

* Docker installed
* Docker Compose installed

## Installation

### Clone the Repository

```bash
git clone https://github.com/aborroy/alfresco-opensearch-neural-search.git
cd alfresco-opensearch-neural-search/docker-alfresco
```

### Build and Start the Services

Use Docker Compose to build and start the Alfresco and OpenSearch services:

```bash
docker-compose up --build
```

This command will pull the necessary Docker images, build the custom images, and start the services defined in the `docker-compose.yml` file.

### Verify the Setup

Once the services are up and running, verify the setup by accessing the Alfresco web interface and the OpenSearch dashboard:

* Credentials: admin / admin
* Repository: http://localhost:8080/alfresco
* Share UI: http://localhost:8080/share
* ACA UI: http://localhost:8080/