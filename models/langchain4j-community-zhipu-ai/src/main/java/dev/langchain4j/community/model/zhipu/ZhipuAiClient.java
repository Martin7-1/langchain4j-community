package dev.langchain4j.community.model.zhipu;

import static dev.langchain4j.community.model.zhipu.DefaultZhipuAiHelper.aiMessageFrom;
import static dev.langchain4j.community.model.zhipu.DefaultZhipuAiHelper.finishReasonFrom;
import static dev.langchain4j.community.model.zhipu.DefaultZhipuAiHelper.getFinishReason;
import static dev.langchain4j.community.model.zhipu.DefaultZhipuAiHelper.specificationsFrom;
import static dev.langchain4j.community.model.zhipu.DefaultZhipuAiHelper.toChatErrorResponse;
import static dev.langchain4j.community.model.zhipu.DefaultZhipuAiHelper.tokenUsageFrom;
import static dev.langchain4j.community.model.zhipu.Json.OBJECT_MAPPER;
import static dev.langchain4j.internal.Utils.isNullOrEmpty;
import static retrofit2.converter.jackson.JacksonConverterFactory.create;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.community.model.zhipu.chat.ChatCompletionChoice;
import dev.langchain4j.community.model.zhipu.chat.ChatCompletionRequest;
import dev.langchain4j.community.model.zhipu.chat.ChatCompletionResponse;
import dev.langchain4j.community.model.zhipu.chat.ToolCall;
import dev.langchain4j.community.model.zhipu.embedding.EmbeddingRequest;
import dev.langchain4j.community.model.zhipu.embedding.EmbeddingResponse;
import dev.langchain4j.community.model.zhipu.image.ImageRequest;
import dev.langchain4j.community.model.zhipu.image.ImageResponse;
import dev.langchain4j.community.model.zhipu.shared.ErrorResponse;
import dev.langchain4j.community.model.zhipu.shared.Usage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;

public class ZhipuAiClient {

    private static final Logger log = LoggerFactory.getLogger(ZhipuAiClient.class);

    private final ZhipuAiApi zhipuAiApi;
    private final OkHttpClient okHttpClient;
    private final Boolean logResponses;

    public ZhipuAiClient(Builder builder) {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .callTimeout(builder.callTimeout)
                .connectTimeout(builder.connectTimeout)
                .readTimeout(builder.readTimeout)
                .writeTimeout(builder.writeTimeout)
                .addInterceptor(new AuthorizationInterceptor(builder.apiKey));

        if (builder.logRequests) {
            okHttpClientBuilder.addInterceptor(new RequestLoggingInterceptor());
        }

        this.logResponses = builder.logResponses;
        if (builder.logResponses) {
            okHttpClientBuilder.addInterceptor(new ResponseLoggingInterceptor());
        }

        this.okHttpClient = okHttpClientBuilder.build();
        Retrofit retrofit = (new Retrofit.Builder())
                .baseUrl(Utils.ensureTrailingForwardSlash(builder.baseUrl))
                .client(this.okHttpClient)
                .addConverterFactory(create(OBJECT_MAPPER))
                .build();
        this.zhipuAiApi = retrofit.create(ZhipuAiApi.class);
    }

    public static Builder builder() {
        return new Builder();
    }

    public ChatCompletionResponse chatCompletion(ChatCompletionRequest request) {
        retrofit2.Response<ChatCompletionResponse> retrofitResponse;
        try {
            retrofitResponse = zhipuAiApi.chatCompletion(request).execute();
        } catch (IOException e) {
            return toChatErrorResponse(e);
        }
        if (retrofitResponse.isSuccessful()) {
            return retrofitResponse.body();
        }
        return toChatErrorResponse(retrofitResponse);
    }

    public EmbeddingResponse embedAll(EmbeddingRequest request) {
        try {
            retrofit2.Response<EmbeddingResponse> responseResponse =
                    zhipuAiApi.embeddings(request).execute();
            if (responseResponse.isSuccessful()) {
                return responseResponse.body();
            } else {
                throw toException(responseResponse);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void streamingChatCompletion(
            ChatCompletionRequest request,
            StreamingChatResponseHandler handler,
            List<ChatModelListener> listeners,
            ChatModelRequestContext requestContext,
            ModelProvider provider) {
        EventSourceListener eventSourceListener = new EventSourceListener() {
            final StringBuffer contentBuilder = new StringBuffer();
            List<ToolExecutionRequest> specifications;
            TokenUsage tokenUsage;
            FinishReason finishReason;
            ChatCompletionResponse chatCompletionResponse;

            @Override
            public void onOpen(@NotNull EventSource eventSource, @NotNull okhttp3.Response response) {
                if (logResponses) {
                    log.debug("onOpen()");
                }
            }

            @Override
            public void onEvent(@NotNull EventSource eventSource, String id, String type, @NotNull String data) {
                if (logResponses) {
                    log.debug("onEvent() {}", data);
                }
                if ("[DONE]".equals(data)) {
                    AiMessage aiMessage;
                    if (isNullOrEmpty(specifications)) {
                        aiMessage = AiMessage.from(contentBuilder.toString());
                    } else {
                        aiMessage = AiMessage.from(specifications);
                    }
                    ChatResponse response = ChatResponse.builder()
                            .aiMessage(aiMessage)
                            .tokenUsage(tokenUsage)
                            .finishReason(finishReason)
                            .build();

                    ChatModelResponseContext responseContext = new ChatModelResponseContext(
                            response, requestContext.chatRequest(), provider, requestContext.attributes());
                    for (ChatModelListener listener : listeners) {
                        try {
                            listener.onResponse(responseContext);
                        } catch (Exception e) {
                            log.warn("Exception while calling model listener", e);
                        }
                    }

                    handler.onCompleteResponse(response);
                } else {
                    try {
                        chatCompletionResponse = OBJECT_MAPPER.readValue(data, ChatCompletionResponse.class);
                        ChatCompletionChoice zhipuChatCompletionChoice =
                                chatCompletionResponse.getChoices().get(0);
                        String chunk = zhipuChatCompletionChoice.getDelta().getContent();
                        contentBuilder.append(chunk);
                        handler.onPartialResponse(chunk);
                        Usage zhipuUsageInfo = chatCompletionResponse.getUsage();
                        if (zhipuUsageInfo != null) {
                            this.tokenUsage = tokenUsageFrom(zhipuUsageInfo);
                        }

                        String finishReasonString = zhipuChatCompletionChoice.getFinishReason();
                        if (finishReasonString != null) {
                            this.finishReason = finishReasonFrom(finishReasonString);
                        }

                        List<ToolCall> toolCalls =
                                zhipuChatCompletionChoice.getDelta().getToolCalls();
                        if (!isNullOrEmpty(toolCalls)) {
                            this.specifications = specificationsFrom(toolCalls);
                        }
                    } catch (Exception exception) {
                        handleResponseException(exception, handler, requestContext, provider, listeners);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull EventSource eventSource, Throwable t, okhttp3.Response response) {
                if (logResponses) {
                    log.debug("onFailure()", t);
                }
                Throwable throwable;
                try {
                    ErrorResponse errorResponse = Json.fromJson(response.body().string(), ErrorResponse.class);
                    throwable = Utils.getOrDefault(
                            t,
                            new ZhipuAiException(
                                    errorResponse.getError().get("code"),
                                    errorResponse.getError().get("message")));
                } catch (IOException ignored) {
                    throwable = new ZhipuAiException(String.valueOf(response.code()), null);
                }
                handleResponseException(throwable, handler, requestContext, provider, listeners);
            }

            @Override
            public void onClosed(@NotNull EventSource eventSource) {
                if (logResponses) {
                    log.debug("onClosed()");
                }
            }
        };
        EventSources.createFactory(this.okHttpClient)
                .newEventSource(zhipuAiApi.streamingChatCompletion(request).request(), eventSourceListener);
    }

    private void handleResponseException(
            Throwable throwable,
            StreamingChatResponseHandler handler,
            ChatModelRequestContext requestContext,
            ModelProvider provider,
            List<ChatModelListener> listeners) {
        ChatModelErrorContext errorContext = new ChatModelErrorContext(
                throwable, requestContext.chatRequest(), provider, requestContext.attributes());

        listeners.forEach(listener -> {
            try {
                listener.onError(errorContext);
            } catch (Exception e2) {
                log.warn("Exception while calling model listener", e2);
            }
        });

        if (throwable instanceof ZhipuAiException) {
            ChatCompletionResponse errorResponse = toChatErrorResponse(throwable);
            ChatResponse response = ChatResponse.builder()
                    .aiMessage(aiMessageFrom(errorResponse))
                    .tokenUsage(tokenUsageFrom(errorResponse.getUsage()))
                    .finishReason(finishReasonFrom(getFinishReason(throwable)))
                    .build();
            handler.onCompleteResponse(response);
        } else {
            handler.onError(throwable);
        }
    }

    private RuntimeException toException(retrofit2.Response<?> retrofitResponse) throws IOException {
        int code = retrofitResponse.code();
        if (code >= 400) {
            try (ResponseBody errorBody = retrofitResponse.errorBody()) {
                if (errorBody != null) {
                    String errorBodyString = errorBody.string();
                    String errorMessage = String.format("status code: %s; body: %s", code, errorBodyString);
                    log.error("Error response: {}", errorMessage);
                    return new RuntimeException(errorMessage);
                }
            }
        }
        return new RuntimeException(retrofitResponse.message());
    }

    public ImageResponse imagesGeneration(ImageRequest request) {
        try {
            retrofit2.Response<ImageResponse> responseResponse =
                    zhipuAiApi.generations(request).execute();
            if (responseResponse.isSuccessful()) {
                return responseResponse.body();
            } else {
                throw toException(responseResponse);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder {

        private String baseUrl;
        private String apiKey;
        private Duration callTimeout;
        private Duration connectTimeout;
        private Duration readTimeout;
        private Duration writeTimeout;
        private boolean logRequests;
        private boolean logResponses;

        private Builder() {
            this.baseUrl = "https://open.bigmodel.cn/";
            this.callTimeout = Duration.ofSeconds(60L);
            this.connectTimeout = Duration.ofSeconds(60L);
            this.readTimeout = Duration.ofSeconds(60L);
            this.writeTimeout = Duration.ofSeconds(60L);
        }

        public Builder baseUrl(String baseUrl) {
            if (baseUrl != null && !baseUrl.trim().isEmpty()) {
                this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
                return this;
            } else {
                throw new IllegalArgumentException("baseUrl cannot be null or empty");
            }
        }

        public Builder apiKey(String apiKey) {
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                this.apiKey = apiKey;
                return this;
            } else {
                throw new IllegalArgumentException("apiKey cannot be null or empty. ");
            }
        }

        public Builder callTimeout(Duration callTimeout) {
            if (callTimeout == null) {
                throw new IllegalArgumentException("callTimeout cannot be null");
            } else {
                this.callTimeout = callTimeout;
                return this;
            }
        }

        public Builder connectTimeout(Duration connectTimeout) {
            if (connectTimeout == null) {
                throw new IllegalArgumentException("connectTimeout cannot be null");
            } else {
                this.connectTimeout = connectTimeout;
                return this;
            }
        }

        public Builder readTimeout(Duration readTimeout) {
            if (readTimeout == null) {
                throw new IllegalArgumentException("readTimeout cannot be null");
            } else {
                this.readTimeout = readTimeout;
                return this;
            }
        }

        public Builder writeTimeout(Duration writeTimeout) {
            if (writeTimeout == null) {
                throw new IllegalArgumentException("writeTimeout cannot be null");
            } else {
                this.writeTimeout = writeTimeout;
                return this;
            }
        }

        public Builder logRequests() {
            return this.logRequests(true);
        }

        public Builder logRequests(Boolean logRequests) {
            if (logRequests == null) {
                logRequests = false;
            }

            this.logRequests = logRequests;
            return this;
        }

        public Builder logResponses() {
            return this.logResponses(true);
        }

        public Builder logResponses(Boolean logResponses) {
            if (logResponses == null) {
                logResponses = false;
            }

            this.logResponses = logResponses;
            return this;
        }

        public ZhipuAiClient build() {
            return new ZhipuAiClient(this);
        }
    }
}
