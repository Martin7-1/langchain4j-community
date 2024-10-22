package dev.langchain4j.community.data.document.loader.lark;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.internal.Utils;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.time.Duration;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static dev.langchain4j.community.data.document.loader.lark.LarkApi.OK;

class LarkClient {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(INDENT_OUTPUT);

    private final LarkApi larkApi;

    public LarkClient(String baseUrl,
                      Duration timeout,
                      String accessToken,
                      boolean logRequests,
                      boolean logResponses) {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .callTimeout(timeout)
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout);
        if (logRequests) {
            okHttpClientBuilder.addInterceptor(new LarkRequestLoggingInterceptor());
        }
        if (logResponses) {
            okHttpClientBuilder.addInterceptor(new LarkResponseLoggingInterceptor());
        }
        okHttpClientBuilder.addInterceptor(new LarkAuthorizationInterceptor(accessToken));
        OkHttpClient okHttpClient = okHttpClientBuilder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Utils.ensureTrailingForwardSlash(baseUrl))
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create(OBJECT_MAPPER))
                .build();

        larkApi = retrofit.create(LarkApi.class);
    }

    MetadataResponse getMetadata(String documentId) {
        try {
            Response<ResponseWrapper<MetadataResponse>> response = larkApi.getDocumentMetadata(documentId).execute();

            return postprocessResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    DocumentResponse getDocument(String documentId) {
        try {
            Response<ResponseWrapper<DocumentResponse>> response = larkApi.getDocument(documentId).execute();

            return postprocessResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    <T> T postprocessResponse(Response<ResponseWrapper<T>> response) throws IOException {
        if (response.isSuccessful() && response.body() != null) {
            ResponseWrapper<T> wrapper = response.body();
            if (OK != wrapper.getCode()) {
                throw toException(wrapper);
            }
            return wrapper.getData();
        } else {
            throw toException(response);
        }
    }

    private RuntimeException toException(Response<?> response) throws IOException {
        int code = response.code();
        String body = response.errorBody().string();

        String errorMessage = String.format("status code: %s; body: %s", code, body);
        return new RuntimeException(errorMessage);
    }

    private RuntimeException toException(ResponseWrapper<?> responseWrapper) {
        return toException(responseWrapper.getCode(), responseWrapper.getMsg());
    }

    private RuntimeException toException(int code, String msg) {
        String errorMessage = String.format("code: %s; message: %s", code, msg);

        return new RuntimeException(errorMessage);
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder {

        private String baseUrl;
        private Duration timeout;
        private String accessToken;
        private boolean logRequests;
        private boolean logResponses;

        Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        Builder logRequests(boolean logRequests) {
            this.logRequests = logRequests;
            return this;
        }

        Builder logResponses(boolean logResponses) {
            this.logResponses = logResponses;
            return this;
        }

        LarkClient build() {
            return new LarkClient(baseUrl, timeout, accessToken, logRequests, logResponses);
        }
    }
}
