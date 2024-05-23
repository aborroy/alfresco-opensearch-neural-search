package org.alfresco.opensearch.index;

import org.alfresco.opensearch.client.OpenSearchClientFactory;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.cluster.PutClusterSettingsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for applying cluster settings to the OpenSearch cluster.
 */
@Component
public class ClusterSettings {

    static final Logger LOG = LoggerFactory.getLogger(ClusterSettings.class);

    @Autowired
    private OpenSearchClientFactory openSearchClientFactory;

    /**
     * Retrieves an instance of OpenSearchClient from the factory.
     *
     * @return OpenSearchClient instance
     */
    private OpenSearchClient openSearchClient() {
        return openSearchClientFactory.getOpenSearchClient();
    }

    /**
     * Applies the specified cluster settings to the OpenSearch cluster.
     *
     * @throws IOException if an I/O error occurs
     */
    public void apply() throws IOException {
        Map<String, Object> mlCommonsSettings = createMlCommonsSettings();
        Map<String, Object> pluginsSettings = createPluginsSettings(mlCommonsSettings);
        Map<String, JsonData> settingsMap = createSettingsMap(pluginsSettings);

        PutClusterSettingsRequest putSettingsRequest = new PutClusterSettingsRequest.Builder()
                .persistent(settingsMap)
                .build();

        openSearchClient().cluster().putSettings(putSettingsRequest);

        LOG.info("OpenSearch cluster settings for ML Commons set");
    }

    /**
     * Creates the ML Commons settings map.
     *
     * @return a map containing ML Commons settings
     */
    private Map<String, Object> createMlCommonsSettings() {
        Map<String, Object> mlCommonsSettings = new HashMap<>();
        mlCommonsSettings.put("only_run_on_ml_node", "false");
        mlCommonsSettings.put("model_access_control_enabled", "true");
        mlCommonsSettings.put("native_memory_threshold", "99");
        return mlCommonsSettings;
    }

    /**
     * Creates the plugins settings map containing the ML Commons settings.
     *
     * @param mlCommonsSettings the ML Commons settings map
     * @return a map containing plugins settings
     */
    private Map<String, Object> createPluginsSettings(Map<String, Object> mlCommonsSettings) {
        Map<String, Object> pluginsSettings = new HashMap<>();
        pluginsSettings.put("ml_commons", mlCommonsSettings);
        return pluginsSettings;
    }

    /**
     * Creates the settings map to be applied to the cluster.
     *
     * @param pluginsSettings the plugins settings map
     * @return a map containing the settings to be applied
     */
    private Map<String, JsonData> createSettingsMap(Map<String, Object> pluginsSettings) {
        Map<String, JsonData> settingsMap = new HashMap<>();
        settingsMap.put("plugins", JsonData.of(pluginsSettings));
        return settingsMap;
    }
}