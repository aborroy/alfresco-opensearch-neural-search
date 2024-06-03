# Sample integration of Alfresco with Opensearch Neural Search

This project integrates Alfresco with the neural search capabilities of OpenSearch. A Java Spring Boot service handles kNN indexing.

Docker Images from [quay.io](https://quay.io/organization/alfresco) are used, since this product is only available for Alfresco Enterprise customers. In addition, [Alfresco Nexus](https://nexus.alfresco.com) credentials may be required. If you are Enterprise Customer or Partner but you are still experiencing problems to download Docker Images or download artifacts from Nexus, contact [Alfresco Hyland Support](https://community.hyland.com) in order to get required credentials and permissions.

This project provides following folders:

* [alfresco-neural-search](alfresco-neural-search): Spring Boot application that indexes Alfresco documents in OpenSearch and provides a searching endpoint (like `http://localhost:8081/search?query=people playing a game`)
* [docker-alfresco](docker-alfresco): regular Alfresco deployment
* [docker-opensearch](docker-opensearch): regular 2 nodes OpenSearch deployment using TLS
* [neural-search-ui](neural-search-ui): Sample ADF UI to test semantic, keyword or hybrid search with link to document detail in Alfresco Share

## Running

Start Alfresco Repository

```
cd docker-alfresco
docker compose up
```

Start OpenSearch cluster

```
cd docker-opensearch
docker compose up
```

Run the `alfresco-neural-app` app to create required model group, model, pipelines and indexes in OpenSearch. One OpenSearch is configured, Batch Indexer will ingest documents in Alfresco Repository.

```
cd alfresco-neural-search
mvn clean package
java -jar target/neural-search-0.8.0.jar
```

Upload a number of files to Alfresco and wait until they are indexed in OpenSearch.

Try searching using the Search Endpoint available in 8081 port with default settings (that means `neural` mode, using NLP):

```
curl --location 'http://localhost:8081/search?query=people%20screaming'
[
    {
        "uuid": "270c6206-b8be-4dd8-829f-133b89ed1a17_560",
        "name": "adventures-huckleberry-finn.txt",
        "text":"staggers back, clawing at the air--bang! goes the second one, and he tumbles backwards on to the ground, heavy and solid, with his arms spread out. That young girl screamed out and comes rushing, and down she throws herself on her father, crying, and saying, \“Oh, he's killed him, he's killed him!\” The crowd closed up around them, and shouldered and jammed one another, with their necks stretched, trying to see, and people on the inside trying to shove them back and shouting, \“Back, back! give him air, give"
    },
    {
        "uuid": "c933b120-5ce8-4743-89b9-8ceaa066c605_379",
        "name": "crime-and-punishment.txt",
        "text":"The shout ended in a shriek; the last sounds came from the yard; all was still. But at the same instant several men talking loud and fast began noisily mounting the stairs. There were three or four of them. He distinguished the ringing voice of the young man. \“Hey!\” Filled with despair he went straight to meet them, feeling \“come what must!\” If they stopped him--all was lost; if they let him pass--all was lost too; they would remember him. They were approaching; they were only a flight from him--and"
    }
]
```

>> Semantic (`neural`) search provides results not based on synonyms or terms, but in the semantic meaning of the query.

Using keyword search (based in BM25 algorithm) is also available by adding parameter `searchType=keyword`

```
curl --location 'http://localhost:8081/search?query=people%20screaming&searchType=keyword'
[
    {
        "uuid": "4eda9949-57a0-4d21-aaf5-b9d9053d92c3_1611",
        "name": "anna-karenina.txt",
        "text":"good-humoredly. \“Oh, no!\” said Levin with annoyance; \“that method of doctoring I merely meant as a simile for doctoring the people with schools. The people are poor and ignorant\—that we see as surely as the peasant woman sees the baby is ill because it screams. But in what way this trouble of poverty and ignorance is to be cured by schools is as incomprehensible as how the hen-roost affects the screaming. What has to be cured is what makes him poor.\” \“Well, in that, at least, you\’re in agreement with"
    },
    {
        "uuid": "4eda9949-57a0-4d21-aaf5-b9d9053d92c3_1610",
        "name": "anna-karenina.txt",
        "text":"yesterday, I met a peasant woman in the evening with a little baby, and asked her where she was going. She said she was going to the wise woman; her boy had screaming fits, so she was taking him to be doctored. I asked, \‘Why, how does the wise woman cure screaming fits?\’ \‘She puts the child on the hen-roost and repeats some charm....\’\” \“Well, you\’re saying it yourself! What\’s wanted to prevent her taking her child to the hen-roost to cure it of screaming fits is just....\” Sviazhsky said, smiling"
    }
]
```

Finally, hybrid search, that combines both neural and keyword search by weighting results, can be invoked using `searchType=hybrid` parameter:

```
curl --location 'http://localhost:8081/search?query=people%20screaming&searchType=hybrid'
[
    {
        "uuid": "c933b120-5ce8-4743-89b9-8ceaa066c605_109",
        "name": "crime-and-punishment.txt",
        "text":"if she does begin pulling it, that\’s not what I am afraid of... it\’s her eyes I am afraid of... yes, her eyes... the red on her cheeks, too, frightens me... and her breathing too.... Have you noticed how people in that disease breathe... when they are excited? I am frightened of the children\’s crying, too.... For if Sonia has not taken them food... I don\’t know what\’s happened! I don\’t know! But blows I am not afraid of.... Know, sir, that such blows are not a pain to me, but even an enjoyment. In fact I"
    },
    {
        "uuid": "270c6206-b8be-4dd8-829f-133b89ed1a17_878",
        "name": "adventures-huckleberry-finn.txt",
        "text":"would.\” \“_They_ told you I would. Whoever told you's _another_ lunatic. I never heard the beat of it. Who's _they_?\” \“Why, everybody. They all said so, m'am.\” It was all she could do to hold in; and her eyes snapped, and her fingers worked like she wanted to scratch him; and she says: \“Who's 'everybody'? Out with their names, or ther'll be an idiot short.\” He got up and looked distressed, and fumbled his hat, and says: \“I'm sorry, and I warn't expecting it. They told me to. They all told me to. They all"
    }
]
```


## Running as a container

Build the Docker Image using following command (JAR file for `alfresco-neural-search` needs to be built before building the Docker Image):

```
cd alfresco-neural-search
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

## Running all services using Docker Compose

Build the Docker Image using following command:

```
cd alfresco-neural-search
mvn clean package
docker build . -t alfresco-neural-search
```

Go back to project root folder and start Docker Compose:

```
cd ..
docker compose up
```

After a while, services will be available.

## Running PoC for the UI

Sample Angular UI (based in https://github.com/AlfrescoLabs/app-starter-kit) is available in folder [neural-search-ui](neural-search-ui).

It can be started as a regular Angular 18 app.

```
cd neural-search-ui
nvm use 18
npm start
```

Open the browser and use http://locahost:4200 to test the backend using `Semantic`, `Keyword` or `Hybrid` methods. 

## Additional Resources

* https://opensearch.org/docs/latest/search-plugins/neural-search-tutorial/
