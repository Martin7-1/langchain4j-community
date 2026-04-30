package dev.langchain4j.community.model.zhipu.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.community.model.zhipu.ZhipuAiChatRequestParameters;
import dev.langchain4j.community.model.zhipu.ZhipuAiStreamingChatModel;
import dev.langchain4j.community.model.zhipu.chat.ChatCompletionModel;
import dev.langchain4j.community.model.zhipu.chat.Thinking;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.common.AbstractStreamingChatModelIT;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import java.util.List;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.InOrder;

@EnabledIfEnvironmentVariable(named = "ZHIPU_API_KEY", matches = ".+")
class ZhipuAiStreamingChatModelIT extends AbstractStreamingChatModelIT {

    private static final String ZHIPU_API_KEY = System.getenv("ZHIPU_API_KEY");

    @Override
    protected List<StreamingChatModel> models() {
        return List.of(
                //                ZhipuAiChatModel.builder()
                //                        .model(ChatCompletionModel.GLM_4_5)
                //                        .apiKey(ZHIPU_API_KEY)
                //                        .logRequests(true)
                //                        .logResponses(true)
                //                        .maxRetries(1)
                //                        .build(),
                //                ZhipuAiChatModel.builder()
                //                        .model(ChatCompletionModel.GLM_4_6)
                //                        .apiKey(ZHIPU_API_KEY)
                //                        .logRequests(true)
                //                        .logResponses(true)
                //                        .maxRetries(1)
                //                        .build(),
                ZhipuAiStreamingChatModel.builder()
                        .model(ChatCompletionModel.GLM_4_7)
                        .apiKey(ZHIPU_API_KEY)
                        .logRequests(true)
                        .logResponses(true)
                        .toolStream(true)
                        .thinking(Thinking.builder().type("disabled").build())
                        .build()
                //                ZhipuAiChatModel.builder()
                //                        .model(ChatCompletionModel.GLM_5)
                //                        .apiKey(ZHIPU_API_KEY)
                //                        .logRequests(true)
                //                        .logResponses(true)
                //                        .maxRetries(1)
                //                        .build(),
                //                ZhipuAiChatModel.builder()
                //                        .model(ChatCompletionModel.GLM_5_1)
                //                        .apiKey(ZHIPU_API_KEY)
                //                        .logRequests(true)
                //                        .logResponses(true)
                //                        .maxRetries(1)
                //                        .build()
                );
    }

    @Override
    protected boolean supportsToolChoiceRequired() {
        return false;
    }

    @Override
    protected boolean supportsToolChoiceRequiredWithSingleTool() {
        return false;
    }

    @Override
    protected boolean supportsToolChoiceRequiredWithMultipleTools() {
        return false;
    }

    @Override
    protected boolean supportsJsonResponseFormatWithSchema() {
        // Zhipu does not support response format with schema:
        // https://docs.bigmodel.cn/api-reference/%E6%A8%A1%E5%9E%8B-api/%E5%AF%B9%E8%AF%9D%E8%A1%A5%E5%85%A8#body-one-of-0-response-format
        return false;
    }

    @Override
    protected boolean supportsJsonResponseFormatWithRawSchema() {
        // Zhipu does not support response format with schema:
        // https://docs.bigmodel.cn/api-reference/%E6%A8%A1%E5%9E%8B-api/%E5%AF%B9%E8%AF%9D%E8%A1%A5%E5%85%A8#body-one-of-0-response-format
        return false;
    }

    @Override
    protected String customModelName() {
        return ChatCompletionModel.GLM_5.toString();
    }

    @Override
    protected StreamingChatModel createModelWith(ChatRequestParameters parameters) {
        ZhipuAiStreamingChatModel.ZhipuAiStreamingChatModelBuilder zhipuAiStreamingChatModelBuilder =
                ZhipuAiStreamingChatModel.builder()
                        .apiKey(ZHIPU_API_KEY)
                        .model(parameters.modelName())
                        .maxToken(parameters.maxOutputTokens())
                        .toolStream(true)
                        .thinking(Thinking.builder().type("disabled").build())
                        .logRequests(true)
                        .logResponses(true);
        if (parameters.modelName() == null) {
            zhipuAiStreamingChatModelBuilder.model(ChatCompletionModel.GLM_5);
        }
        return zhipuAiStreamingChatModelBuilder.build();
    }

    @Override
    protected List<StreamingChatModel> modelsSupportingImageInputs() {
        return List.of(
                ZhipuAiStreamingChatModel.builder()
                        .model(ChatCompletionModel.GLM_4_6V)
                        .apiKey(ZHIPU_API_KEY)
                        .logRequests(true)
                        .logResponses(true)
                        .toolStream(true)
                        .thinking(Thinking.builder().type("disabled").build())
                        .build()
                //                ZhipuAiStreamingChatModel.builder()
                //                        .model(ChatCompletionModel.GLM_5V_TURBO)
                //                        .apiKey(ZHIPU_API_KEY)
                //                        .logRequests(true)
                //                        .logResponses(true)
                //                        .thinking(Thinking.builder().type("disabled").build())
                //                        .build()
                );
    }

    @Override
    protected ChatRequestParameters createIntegrationSpecificParameters(int maxOutputTokens) {
        return ZhipuAiChatRequestParameters.builder()
                .maxOutputTokens(maxOutputTokens)
                .build();
    }

    @Override
    public StreamingChatModel createModelWith(ChatModelListener listener) {
        return ZhipuAiStreamingChatModel.builder()
                .apiKey(ZHIPU_API_KEY)
                .model(ChatCompletionModel.GLM_4_7)
                .toolStream(true)
                .thinking(Thinking.builder().type("disabled").build())
                .listeners(List.of(listener))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Override
    protected boolean supportsStreamingCancellation() {
        return false;
    }

    @Override
    protected String catImageUrl() {
        return "https://cdn.wanx.aliyuncs.com/upload/commons/Felis_silvestris_silvestris_small_gradual_decrease_of_quality.png";
    }

    @Override
    protected String diceImageUrl() {
        return "https://cdn.wanx.aliyuncs.com/upload/commons/PNG_transparency_demonstration_1.png";
    }

    @Override
    protected void verifyToolCallbacks(StreamingChatResponseHandler handler, InOrder io, String id) {
        io.verify(handler, atLeastOnce()).onPartialToolCall(any(), any());
        io.verify(handler).onCompleteToolCall(argThat(toolCall -> {
            ToolExecutionRequest request = toolCall.toolExecutionRequest();
            return toolCall.index() == 0
                    && request.id().equals(id)
                    && request.name().equals("getWeather")
                    && request.arguments().replace(" ", "").equals("{\"city\":\"Munich\"}");
        }));
    }

    @Override
    protected void verifyToolCallbacks(StreamingChatResponseHandler handler, InOrder io, String id1, String id2) {
        io.verify(handler, atLeastOnce())
                .onPartialToolCall(
                        argThat(toolCall -> toolCall.index() == 0
                                && toolCall.id().equals(id1)
                                && toolCall.name().equals("getWeather")
                                && !toolCall.partialArguments().isBlank()),
                        any());
        io.verify(handler).onCompleteToolCall(argThat(toolCall -> {
            ToolExecutionRequest request = toolCall.toolExecutionRequest();
            return toolCall.index() == 0
                    && request.id().equals(id1)
                    && request.name().equals("getWeather")
                    && request.arguments().replace(" ", "").equals("{\"city\":\"Munich\"}");
        }));

        io.verify(handler, atLeastOnce())
                .onPartialToolCall(
                        argThat(toolCall -> toolCall.index() == 1
                                && toolCall.id().equals(id2)
                                && toolCall.name().equals("getTime")
                                && !toolCall.partialArguments().isBlank()),
                        any());
        io.verify(handler).onCompleteToolCall(argThat(toolCall -> {
            ToolExecutionRequest request = toolCall.toolExecutionRequest();
            return toolCall.index() == 1
                    && request.id().equals(id2)
                    && request.name().equals("getTime")
                    && request.arguments().replace(" ", "").equals("{\"country\":\"France\"}");
        }));
    }
}
