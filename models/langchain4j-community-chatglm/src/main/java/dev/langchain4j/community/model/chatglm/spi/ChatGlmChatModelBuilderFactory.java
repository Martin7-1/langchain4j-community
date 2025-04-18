package dev.langchain4j.community.model.chatglm.spi;

import dev.langchain4j.community.model.chatglm.ChatGlmChatModel;

import java.util.function.Supplier;

/**
 * A factory for building {@link ChatGlmChatModel.ChatGlmChatModelBuilder} instances.
 */
public interface ChatGlmChatModelBuilderFactory extends Supplier<ChatGlmChatModel.ChatGlmChatModelBuilder> {
}
