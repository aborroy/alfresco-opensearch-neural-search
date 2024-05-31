package org.alfresco.opensearch.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

import org.alfresco.opensearch.ingest.Indexer;

/**
 * Component for managing OpenSearch configuration settings, including cluster settings, model groups, models, pipelines, and indices.
 */
@Component
public class OpenSearchConfiguration {

    static final Logger LOG = LoggerFactory.getLogger(OpenSearchConfiguration.class);

    @Autowired
    private ClusterSettings clusterSettings;

    @Autowired
    private ModelGroups modelGroups;

    @Autowired
    private Model model;

    @Autowired
    private Pipeline pipeline;

    @Autowired
    private Index index;

    @Autowired
    private Indexer indexer;

    private String modelGroupId;
    private String modelId;

    // Prevent indexer to be executed till OpenSearch is properly configured
    private final CountDownLatch latch = new CountDownLatch(1);

    /**
     * Applies OpenSearch configuration settings.
     * If the index exists, retrieves existing model group ID and model ID.
     * Otherwise, applies cluster settings, registers model group, model, pipeline, and creates index.
     *
     * @throws Exception if an error occurs during the application of configuration settings
     */
    public void apply() throws Exception {
        LOG.info("-- CONFIG --");
        if (index.existIndex()) {
            // If index exists, retrieve existing model group ID and model ID
            modelGroupId = modelGroups.getModelGroupId();
            modelId = model.getModelId();
            LOG.info("CONFIG: Index ready and model with id {} already available!", modelId);
        } else {
            // Apply cluster settings, register model group, model, pipeline, and create index
            clusterSettings.apply();
            modelGroupId = modelGroups.apply();
            modelId = model.apply(modelGroupId);
            pipeline.apply(modelId);
            index.createKnnIndex();
            index.createAlfrescoIndex();
            indexer.verifyIndexStatus();
            LOG.info("CONFIG: Index configured and model with id {} deployed!", modelId);
        }
        LOG.info("-- CONFIG --");
        latch.countDown();
    }

    /**
     * Gets the ID of the model group.
     *
     * @return the ID of the model group
     */
    public String getModelGroupId() {
        return modelGroupId;
    }

    /**
     * Gets the ID of the model.
     *
     * @return the ID of the model
     */
    public String getModelId() {
        return modelId;
    }

    /**
     * Gets the status of the latch
     *
     * @return concurrent latch status
     */
    public CountDownLatch getLatch() {
        return latch;
    }

}