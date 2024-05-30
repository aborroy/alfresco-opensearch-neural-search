package org.alfresco.repo.service.beans;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Represents a node in the Alfresco repository.
 */
public class Node {
    private long id; // Unique identifier for the node
    private String tenantDomain; // Domain of the tenant to which the node belongs
    private String nodeRef; // Node reference identifier
    private String type; // Type of the node
    private int aclId; // Access Control List (ACL) identifier
    private int txnId; // Transaction identifier
    private Map<String, Serializable> properties; // Properties associated with the node
    private List<String> aspects; // Aspects associated with the node
    private List<String> paths; // Paths to the node
    private List<String> namePaths; // Paths to the node by name

    // Getters and setters

    /**
     * Retrieves the unique identifier of the node.
     *
     * @return The ID of the node.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the node.
     *
     * @param id The ID of the node.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Retrieves the domain of the tenant to which the node belongs.
     *
     * @return The tenant domain.
     */
    public String getTenantDomain() {
        return tenantDomain;
    }

    /**
     * Sets the domain of the tenant to which the node belongs.
     *
     * @param tenantDomain The tenant domain.
     */
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    /**
     * Retrieves the node reference identifier.
     *
     * @return The node reference.
     */
    public String getNodeRef() {
        return nodeRef;
    }

    /**
     * Sets the node reference identifier.
     *
     * @param nodeRef The node reference.
     */
    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }

    /**
     * Retrieves the type of the node.
     *
     * @return The node type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the node.
     *
     * @param type The node type.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Retrieves the Access Control List (ACL) identifier of the node.
     *
     * @return The ACL identifier.
     */
    public int getAclId() {
        return aclId;
    }

    /**
     * Sets the Access Control List (ACL) identifier of the node.
     *
     * @param aclId The ACL identifier.
     */
    public void setAclId(int aclId) {
        this.aclId = aclId;
    }

    /**
     * Retrieves the transaction identifier of the node.
     *
     * @return The transaction identifier.
     */
    public int getTxnId() {
        return txnId;
    }

    /**
     * Sets the transaction identifier of the node.
     *
     * @param txnId The transaction identifier.
     */
    public void setTxnId(int txnId) {
        this.txnId = txnId;
    }

    /**
     * Retrieves the properties associated with the node.
     *
     * @return The node properties.
     */
    public Map<String, Serializable> getProperties() {
        return properties;
    }

    /**
     * Sets the properties associated with the node.
     *
     * @param properties The node properties.
     */
    public void setProperties(Map<String, Serializable> properties) {
        this.properties = properties;
    }

    /**
     * Retrieves the aspects associated with the node.
     *
     * @return The node aspects.
     */
    public List<String> getAspects() {
        return aspects;
    }

    /**
     * Sets the aspects associated with the node.
     *
     * @param aspects The node aspects.
     */
    public void setAspects(List<String> aspects) {
        this.aspects = aspects;
    }

    /**
     * Retrieves the paths to the node.
     *
     * @return The paths to the node.
     */
    public List<String> getPaths() {
        return paths;
    }

    /**
     * Sets the paths to the node.
     *
     * @param paths The paths to the node.
     */
    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    /**
     * Retrieves the paths to the node by name.
     *
     * @return The paths to the node by name.
     */
    public List<String> getNamePaths() {
        return namePaths;
    }

    /**
     * Sets the paths to the node by name.
     *
     * @param namePaths The paths to the node by name.
     */
    public void setNamePaths(List<String> namePaths) {
        this.namePaths = namePaths;
    }
}
