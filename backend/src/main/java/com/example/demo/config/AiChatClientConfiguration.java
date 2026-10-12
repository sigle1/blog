package com.example.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatClientConfiguration {

    @Bean
    public ChatMemory chatMemory(
            @Value("${app.ai.chat-memory-max-messages:20}") int maxMessages) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(maxMessages)
                .build();
    }

    /** 无 Advisor：无 conversationId 时配合前端 history */
    @Bean(name = "chatClientPlain")
    public ChatClient chatClientPlain(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    /** 服务端多轮记忆：需 conversationId */
    @Bean(name = "chatClientMemoryOnly")
    public ChatClient chatClientMemoryOnly(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
