package org.alfresco.repo.service.beans;

/**
 * Represents a node involved in a transaction in the Alfresco repository.
 */
public class TransactionNode {
    private long id; // Unique identifier for the transaction node
    private String nodeRef; // Reference identifier of the node
    private int txnId; // Transaction identifier associated with the node
    private String status; // Status of the node within the transaction
    private int aclId; // Access Control List (ACL) identifier of the node
    private String tenant; // Tenant associated with the node

    /**
     * Retrieves the unique identifier of the transaction node.
     *
     * @return The ID of the transaction node.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the transaction node.
     *
     * @param id The ID of the transaction node.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Retrieves the reference identifier of the node.
     *
     * @return The node reference.
     */
    public String getNodeRef() {
        return nodeRef;
    }

    /**
     * Sets the reference identifier of the node.
     *
     * @param nodeRef The node reference.
     */
    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }

    /**
     * Retrieves the transaction identifier associated with the node.
     *
     * @return The transaction identifier.
     */
    public int getTxnId() {
        return txnId;
    }

    /**
     * Sets the transaction identifier associated with the node.
     *
     * @param txnId The transaction identifier.
     */
    public void setTxnId(int txnId) {
        this.txnId = txnId;
    }

    /**
     * Retrieves the status of the node within the transaction.
     *
     * @return The status of the node.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the node within the transaction.
     *
     * @param status The status of the node.
     */
    public void setStatus(String status) {
        this.status = status;
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
     * Retrieves the tenant associated with the node.
     *
     * @return The tenant associated with the node.
     */
    public String getTenant() {
        return tenant;
    }

    /**
     * Sets the tenant associated with the node.
     *
     * @param tenant The tenant associated with the node.
     */
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
}
