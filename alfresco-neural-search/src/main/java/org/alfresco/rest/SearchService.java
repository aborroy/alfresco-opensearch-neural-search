package org.alfresco.rest;

import com.fasterxml.jackson.databind.JsonNode;
import org.alfresco.opensearch.search.Search;
import org.alfresco.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for handling search requests.
 */
@RestController
public class SearchService {

    @Autowired
    private Search search;

    /**
     * Endpoint for searching documents based on a query.
     *
     * @param query the search query
     * @return a list of DocumentBean objects matching the search criteria
     * @throws Exception if an error occurs during the search process
     */
    @GetMapping("/search")
    public List<DocumentBean> search(@RequestParam String query) throws Exception {

        // Perform the search
        JsonNode results = search.search(query);

        // Extract hits from search results
        JsonNode hitsNode = results.path("hits").path("hits");

        // Process hits and create DocumentBean objects
        List<DocumentBean> documents = new ArrayList<>();
        for (JsonNode hitNode : hitsNode) {
            JsonNode sourceNode = hitNode.path("_source");
            String id = sourceNode.path("id").asText();
            // Escape special characters in text content
            String text = JsonUtils.escape(sourceNode.path("text").asText());
            // Create a DocumentBean instance using Builder pattern
            documents.add(DocumentBean.builder().uuid(id).text(text).build());
        }

        return documents;
    }
}