package dev.langchain4j.community.model.zhipu.spi;

import dev.langchain4j.community.model.zhipu.ZhipuAiStreamingChatModel;

import java.util.function.Supplier;

/**
 * A factory for building {@link ZhipuAiStreamingChatModel.ZhipuAiStreamingChatModelBuilder} instances.
 */
public interface ZhipuAiStreamingChatModelBuilderFactory extends Supplier<ZhipuAiStreamingChatModel.ZhipuAiStreamingChatModelBuilder> {
}
