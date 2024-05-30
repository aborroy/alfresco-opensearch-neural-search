package org.alfresco.repo.service.beans;

import java.util.List;

/**
 * Represents a container for a list of nodes in the Alfresco repository.
 */
public class NodeContainer {
    private List<Node> nodes; // List of nodes contained in the container

    /**
     * Retrieves the list of nodes contained in the container.
     *
     * @return The list of nodes.
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * Sets the list of nodes for the container.
     *
     * @param nodes The list of nodes to set.
     */
    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
}
