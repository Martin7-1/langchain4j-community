package dev.langchain4j.community.data.document.loader.lark;

import dev.langchain4j.data.document.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariables;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariables({
        @EnabledIfEnvironmentVariable(named = "LARK_BASE_URL", matches = ".+"),
        @EnabledIfEnvironmentVariable(named = "LARK_ACCESS_TOKEN", matches = ".+")
})
class LarkDocumentLoaderIT {

    LarkDocumentLoader documentLoader = LarkDocumentLoader.builder()
            .baseUrl(System.getenv("LARK_BASE_URL"))
            .timeout(Duration.ofSeconds(60))
            .accessToken(System.getenv("LARK_ACCESS_TOKEN"))
            .logRequests(true)
            .logResponses(true)
            .build();

    @Test
    void should_load_single_document() {

        // when
        Document document = documentLoader.loadDocument("IvbNdGeHjoHBm2xQDrNcI5l4nLf");

        // then
        assertThat(document.text()).isNotNull();
        assertThat(document.metadata().toMap()).containsKey("documentId");
        assertThat(document.metadata().toMap()).containsKey("revisionId");
        assertThat(document.metadata().toMap()).containsKey("title");
    }
}
