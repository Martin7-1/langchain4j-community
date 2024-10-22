package dev.langchain4j.community.data.document.loader.lark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
class MetadataResponse {

    private DocumentMetadata document;

    MetadataResponse() {
    }

    MetadataResponse(DocumentMetadata document) {
        this.document = document;
    }

    public DocumentMetadata getDocument() {
        return document;
    }

    public void setDocument(DocumentMetadata document) {
        this.document = document;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(NON_NULL)
    @JsonNaming(SnakeCaseStrategy.class)
    static class DocumentMetadata {

        private String documentId;
        private String revisionId;
        private String title;

        DocumentMetadata() {
        }

        DocumentMetadata(String documentId, String revisionId, String title) {
            this.documentId = documentId;
            this.revisionId = revisionId;
            this.title = title;
        }

        public String getDocumentId() {
            return documentId;
        }

        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public String getRevisionId() {
            return revisionId;
        }

        public void setRevisionId(String revisionId) {
            this.revisionId = revisionId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
