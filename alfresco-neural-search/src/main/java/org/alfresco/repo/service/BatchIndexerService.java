package org.alfresco.repo.service;

import org.alfresco.opensearch.index.Index;
import org.alfresco.opensearch.ingest.Indexer;
import org.alfresco.search.handler.SearchApi;
import org.alfresco.search.model.*;
import org.alfresco.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service for batch indexing documents into OpenSearch.
 */
@Service
public class BatchIndexerService {

    private static final Logger LOG = LoggerFactory.getLogger(BatchIndexerService.class);

    @Value("${content.service.root.folder}")
    private String rootFolder;

    @Value("${batch.indexer.enabled}")
    private boolean enabled;

    @Autowired
    private SearchApi searchApi;

    @Autowired
    private RenditionService renditionService;

    @Autowired
    private Indexer indexer;

    @Autowired
    private Index index;

    private static final ReentrantLock lock = new ReentrantLock();

    /**
     * Indexes documents by retrieving them from the search API, creating text renditions if needed,
     * and indexing each document's segments.
     */
    @Scheduled(cron = "${batch.indexer.cron}")
    public void index() throws Exception {
        if (enabled) {
            internalIndex();
        }
    }

    /**
     * Indexes documents by retrieving them from the search API, creating text renditions if needed,
     * and indexing each document's segments.
     */
    private void internalIndex() throws Exception {

        if (lock.tryLock()) {

            try {

                RequestSortDefinition sortDefinition = new RequestSortDefinition();
                sortDefinition.add(new RequestSortDefinitionInner()
                        .type(RequestSortDefinitionInner.TypeEnum.FIELD)
                        .field("id")
                        .ascending(true));

                boolean hasMoreItems;
                int skipCount = 0;
                int maxItems = 20;

                String currentDate = getCurrentDate();
                String fromDate = index.getAlfrescoIndexField();

                LOG.info("INDEXING Retrieving existing documents from {} to {} ...", fromDate, currentDate);

                do {
                    ResponseEntity<ResultSetPaging> results = searchApi.search(
                            new SearchRequest()
                                    .query(new RequestQuery()
                                            .language(RequestQuery.LanguageEnum.AFTS)
                                            .query("PATH:\"" + rootFolder + "//*\" AND cm:modified:['" + fromDate + "' TO '" + currentDate + "']"))
                                    .sort(sortDefinition)
                                    .paging(new RequestPagination().maxItems(maxItems).skipCount(skipCount)));

                    LOG.info("Processing {} documents of a total of {}",
                            results.getBody().getList().getEntries().size(),
                            results.getBody().getList().getPagination().getTotalItems());

                    results.getBody().getList().getEntries().forEach((entry) -> {
                        String uuid = entry.getEntry().getId();

                        // Create text rendition if not already created
                        if (renditionService.textRenditionIsNotCreated(uuid)) {
                            renditionService.createTextRendition(uuid);
                            // Wait until the text rendition is created
                            while (renditionService.textRenditionIsNotCreated(uuid)) {
                                try {
                                    TimeUnit.MILLISECONDS.sleep(2000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        // Index the document segments
                        try {
                            indexer.deleteDocumentIfExists(uuid);
                            indexSegments(uuid, splitIntoSegments(JsonUtils.escape(renditionService.getTextRenditionContent(uuid))));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    });

                    hasMoreItems = results.getBody().getList().getPagination().isHasMoreItems();
                    skipCount = skipCount + maxItems;

                } while (hasMoreItems);

                index.updateAlfrescoIndex(currentDate);

            } finally {
                lock.unlock();
            }

        } else {
            LOG.info("Indexing is already running. Skipping this run...");
        }

    }

    /**
     * Indexes segments of a document.
     *
     * @param documentId the ID of the document
     * @param segments   the segments to index
     * @throws Exception if an error occurs during indexing
     */
    private void indexSegments(String documentId, List<String> segments) throws Exception {
        for (int i = 0; i < segments.size(); i++) {
            String segmentId = documentId + "_" + i;
            LOG.info("Indexing segment {} of {} for document {}", i + 1, segments.size(), documentId);
            indexer.index(segmentId, segments.get(i));
        }
    }

    /**
     * Retrieves the current date and time in UTC format as a string.
     *
     * @return A string representing the current date and time in the format "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'".
     */
    private String getCurrentDate() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return now.format(formatter);
    }

    private static final int MAX_TOKENS = 512;

    /**
     * Splits text into segments.
     *
     * @param text the text to split
     * @return list of text segments
     */
    private static List<String> splitIntoSegments(String text) {
        String[] tokens = text.split("\\s+");
        List<String> segments = new ArrayList<>();
        StringBuilder currentSegment = new StringBuilder();

        for (String token : tokens) {
            if (currentSegment.length() + token.length() + 1 > MAX_TOKENS) {
                segments.add(currentSegment.toString().trim());
                currentSegment = new StringBuilder();
            }
            currentSegment.append(token).append(" ");
        }

        if (!currentSegment.isEmpty()) {
            segments.add(currentSegment.toString().trim());
        }

        return segments;
    }
}
