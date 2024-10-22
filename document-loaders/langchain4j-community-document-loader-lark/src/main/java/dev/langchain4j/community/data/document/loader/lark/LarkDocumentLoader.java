package dev.langchain4j.community.data.document.loader.lark;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a document loader to load a single document from Lark (i.e. Feishu)
 */
public class LarkDocumentLoader {

    private static final Logger log = LoggerFactory.getLogger(LarkDocumentLoader.class);

    private final LarkClient larkClient;

    /**
     * Load a single document from Lark (Feishu).
     *
     * @param baseUrl      Base URL for Lark (Feishu). For Lark, it's {@code https://open.larksuite.com/}. For Feishu, it's {@code https://open.feishu.cn/}.
     * @param timeout      Request timeout.
     * @param accessToken  Authorization access token, see <a href=https://open.larksuite.com/document/server-docs/getting-started/api-access-token/app-access-token-development-guide>access token development guide</a>.
     * @param logRequests  Whether to log request in debug log level or not.
     * @param logResponses Whether to log response in debug log level or not.
     */
    public LarkDocumentLoader(String baseUrl,
                              Duration timeout,
                              String accessToken,
                              boolean logRequests,
                              boolean logResponses) {
        larkClient = LarkClient.builder()
                .baseUrl(baseUrl)
                .timeout(timeout)
                .accessToken(accessToken)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }

    /**
     * Load document by documentId
     *
     * @param documentId document id
     * @return a document from Lark (Feishu)
     */
    public Document loadDocument(String documentId) {
        MetadataResponse metadataResponse = larkClient.getMetadata(documentId);
        DocumentResponse documentResponse = larkClient.getDocument(documentId);

        MetadataResponse.DocumentMetadata documentMetadata = metadataResponse.getDocument();
        Map<String, String> metadata = Optional.ofNullable(documentMetadata)
                .map(d -> Map.of(
                        "documentId", d.getDocumentId(),
                        "revisionId", d.getRevisionId(),
                        "title", d.getTitle()
                ))
                .orElse(Map.of());

        return Document.from(
                documentResponse.getContent(),
                Metadata.from(metadata)
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String baseUrl;
        private Duration timeout;
        private String accessToken;
        private boolean logRequests;
        private boolean logResponses;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder logRequests(boolean logRequests) {
            this.logRequests = logRequests;
            return this;
        }

        public Builder logResponses(boolean logResponses) {
            this.logResponses = logResponses;
            return this;
        }

        public LarkDocumentLoader build() {
            return new LarkDocumentLoader(baseUrl, timeout, accessToken, logRequests, logResponses);
        }
    }
}
