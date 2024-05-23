package org.alfresco.repo.service;

import org.alfresco.core.handler.RenditionsApi;
import org.alfresco.core.model.Rendition;
import org.alfresco.core.model.RenditionBodyCreate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Service for managing document renditions.
 */
@Service
public class RenditionService {

    @Autowired
    private RenditionsApi renditionsApi;

    /**
     * Retrieves the content of the text rendition for a given document UUID.
     *
     * @param uuid the UUID of the document
     * @return the content of the text rendition
     * @throws IOException if an I/O error occurs
     */
    public String getTextRenditionContent(String uuid) throws IOException {
        byte[] renditionContent = renditionsApi.getRenditionContent(uuid, "text", false, null, null, null).getBody().getContentAsByteArray();
        return new String(renditionContent, StandardCharsets.UTF_8);
    }

    /**
     * Checks if the text rendition for a given document UUID has been created.
     *
     * @param uuid the UUID of the document
     * @return true if the text rendition is created, false otherwise
     */
    public boolean textRenditionIsNotCreated(String uuid) {
        Rendition rendition = renditionsApi.getRendition(uuid, "text").getBody().getEntry();
        return rendition.getStatus() != Rendition.StatusEnum.CREATED;
    }

    /**
     * Creates a text rendition for a given document UUID.
     * <p>
     * Note: This method requires the repository to be configured with a rendition configuration file named "0200-enableText.json",
     * mounted as a volume for Docker.
     * <p>
     * The content of 0200-enableText.json:
     * {
     *   "renditions": [
     *     {
     *       "renditionName": "text",
     *       "targetMediaType": "text/plain"
     *     }
     *   ]
     * }
     * <p>
     * Mounted as volume for Docker as:
     * volumes:
     *   - ./0200-enableText.json:/usr/local/tomcat/shared/classes/alfresco/extension/transform/renditions/0200-enableText.json
     *
     * @param uuid the UUID of the document
     */
    public void createTextRendition(String uuid) {
        renditionsApi.createRendition(uuid, new RenditionBodyCreate().id("text"));
    }
}
