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
     * Handles search requests and returns a list of {@link DocumentBean} objects based on the specified search type.
     *
     * <p>This method performs a search using the specified query and search type (neural, text, or hybrid). It extracts
     * the hits from the search results, processes each hit to create a {@link DocumentBean} object, and returns a list
     * of these objects.</p>
     *
     * @param query the search query string
     * @param searchType the type of search to perform; can be "neural", "text", or "hybrid"
     * @return a list of {@link DocumentBean} objects representing the search results
     * @throws Exception if an error occurs during the search or processing of results
     * @throws IllegalArgumentException if the provided search type is invalid
     */
    @GetMapping("/search")
    public List<DocumentBean> search(@RequestParam String query, @RequestParam(defaultValue = "neural") String searchType) throws Exception {

        // Perform the search based on searchType
        JsonNode results = switch (searchType.toLowerCase()) {
            case "keyword" -> search.keywordSearch(query);
            case "hybrid" -> search.hybridSearch(query);
            default -> search.neuralSearch(query);
        };

        // Extract hits from search results
        JsonNode hitsNode = results.path("hits").path("hits");

        // Process hits and create DocumentBean objects
        List<DocumentBean> documents = new ArrayList<>();
        for (JsonNode hitNode : hitsNode) {
            JsonNode sourceNode = hitNode.path("_source");
            String id = sourceNode.path("id").asText();
            String name = sourceNode.path("name").asText();
            // Escape special characters in text content
            String text = JsonUtils.escape(sourceNode.path("text").asText());
            documents.add(DocumentBean.builder().uuid(id).name(name).text(text).build());
        }

        return documents;
    }

}