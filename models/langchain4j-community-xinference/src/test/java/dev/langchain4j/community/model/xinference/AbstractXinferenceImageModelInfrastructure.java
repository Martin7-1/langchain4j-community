package dev.langchain4j.community.model.xinference;

import static dev.langchain4j.community.model.xinference.XinferenceUtils.IMAGE_MODEL_NAME;
import static dev.langchain4j.community.model.xinference.XinferenceUtils.XINFERENCE_API_KEY;
import static dev.langchain4j.community.model.xinference.XinferenceUtils.XINFERENCE_BASE_URL;
import static dev.langchain4j.community.model.xinference.XinferenceUtils.XINFERENCE_IMAGE;
import static dev.langchain4j.community.model.xinference.XinferenceUtils.resolve;
import static dev.langchain4j.internal.Utils.isNullOrEmpty;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

class AbstractXinferenceImageModelInfrastructure {

    private static final String LOCAL_IMAGE = String.format("tc-%s-%s", XINFERENCE_IMAGE, modelName());
    static XinferenceContainer container;

    @BeforeAll
    static void beforeAll() {
        if (isNullOrEmpty(XINFERENCE_BASE_URL)) {
            container = new XinferenceContainer(resolve(XINFERENCE_IMAGE, LOCAL_IMAGE)).withModel(modelName());
            container.start();
        }
    }

    @AfterAll
    static void afterAll() {
        if (container != null) {
            container.stop();
        }
    }

    public static String baseUrl() {
        if (isNullOrEmpty(XINFERENCE_BASE_URL)) {
            return container.getEndpoint();
        } else {
            return XINFERENCE_BASE_URL;
        }
    }

    public static String apiKey() {
        return XINFERENCE_API_KEY;
    }

    public static String modelName() {
        return IMAGE_MODEL_NAME;
    }
}
