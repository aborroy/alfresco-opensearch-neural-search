# Sample integration of Alfresco with Opensearch Neural Search

This project integrates Alfresco with the neural search capabilities of OpenSearch. A Java Spring Boot service handles kNN indexing.

Docker Images from [quay.io](https://quay.io/organization/alfresco) are used, since this product is only available for Alfresco Enterprise customers. In addition, [Alfresco Nexus](https://nexus.alfresco.com) credentials may be required. If you are Enterprise Customer or Partner but you are still experimenting problems to download Docker Images or download artifacts from Nexus, contact [Alfresco Hyland Support](https://community.hyland.com) in order to get required credentials and permissions.

This project provides following folders:

* [alfresco-neural-search](alfresco-neural-search): Spring Boot application that indexes Alfresco documents in OpenSearch and provides a searching endpoing (like `http://localhost:8081/search?query=people playing a game`)
* [docker-alfresco](docker-alfresco): regular Alfresco deployment adding a `text` rendition service
* [docker-opensearch](docker-opensearch): regular 2 nodes OpenSearch deployment using TLS

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

Run the `alfresco-neural-app` app to create required model group, model, pipelines and indexes in OpenSearch.

```
cd alfresco-neural-search
mvn clean package
java -jar target/neural-search-0.8.0.jar --batch.indexer.enabled=false
```

Once OpenSearch is ready, stop the `alfresco-neural-app` app and run it again with Batch Indexer Enabled.

```
java -jar target/neural-search-0.8.0.jar --batch.indexer.enabled=true
```

Upload a number of files to Alfresco Shared Folder and wait until they are indexed in OpenSearch.

Try searching using the Search Endpoint available in 8081 port:

```
curl --location 'http://localhost:8081/search?query=people%20playing%20a%20game'
[
    {
        "uuid": "6e1c937a-12fa-48eb-83e9-3d8314219c6d_25",
        "text":"into her eyes; and once she remembered trying to box her own ears for having cheated herself in a game of croquet she was playing against herself, for this curious child was very fond of pretending to be two people. \“But it\’s no use now,\” thought poor Alice, \“to pretend to be two people! Why, there\’s hardly enough of me left to make _one_ respectable person!\”  Soon her eye fell on a little glass box that was lying under the table: she opened it, and"
    },
    {
        "uuid": "6e1c937a-12fa-48eb-83e9-3d8314219c6d_222",
        "text":"round.  \“I\’ll fetch the executioner myself,\” said the King eagerly, and he hurried off.  Alice thought she might as well go back, and see how the game was going on, as she heard the Queen\’s voice in the distance, screaming with passion. She had already heard her sentence three of the players to be executed for having missed their turns, and she did not like the look of things at all, as the game was in such confusion that she never knew whether it was her"
    }
]
```

>> Semantic search provides results not based in synonims or terms, but in the semantic meaning of the query.


## Resources

* https://opensearch.org/docs/latest/search-plugins/neural-search-tutorial/