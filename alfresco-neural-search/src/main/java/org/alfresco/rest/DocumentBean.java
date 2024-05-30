package org.alfresco.rest;

/**
 * Represents a document bean with UUID, name, and text content.
 */
public class DocumentBean {

    private String uuid;
    private String name;
    private String text;

    /**
     * Retrieves the UUID of the document.
     *
     * @return the UUID of the document
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the UUID of the document.
     *
     * @param uuid the UUID to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Retrieves the name of the document.
     *
     * @return the name of the document
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the document.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the text content of the document.
     *
     * @return the text content of the document
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text content of the document.
     *
     * @param text the text content to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Static nested Builder class to facilitate building DocumentBean instances.
     */
    public static class Builder {
        private String uuid;
        private String name;
        private String text;

        /**
         * Sets the UUID for the document being built.
         *
         * @param uuid the UUID to set
         * @return the Builder instance
         */
        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        /**
         * Sets the name for the document being built.
         *
         * @param name the name to set
         * @return the Builder instance
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the text content for the document being built.
         *
         * @param text the text content to set
         * @return the Builder instance
         */
        public Builder text(String text) {
            this.text = text;
            return this;
        }

        /**
         * Builds and returns a new DocumentBean instance based on the provided parameters.
         *
         * @return a new DocumentBean instance
         */
        public DocumentBean build() {
            DocumentBean documentBean = new DocumentBean();
            documentBean.setUuid(this.uuid);
            documentBean.setName(this.name);
            documentBean.setText(this.text);
            return documentBean;
        }
    }

    /**
     * Static method to obtain a new Builder instance for building DocumentBean objects.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}
