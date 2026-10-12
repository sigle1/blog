package com.example.demo.service.impl;

import com.example.demo.service.AiChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AiChatServiceImpl implements AiChatService {

    private final ChatClient chatClientPlain;
    private final ChatClient chatClientMemoryOnly;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    public AiChatServiceImpl(
            @Qualifier("chatClientPlain") ChatClient chatClientPlain,
            @Qualifier("chatClientMemoryOnly") ChatClient chatClientMemoryOnly) {
        this.chatClientPlain = chatClientPlain;
        this.chatClientMemoryOnly = chatClientMemoryOnly;
    }

    @Override
    public String chat(String message, List<Map<String, String>> history, String conversationId) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException(
                    "未配置大模型 API Key，请设置环境变量 DASHSCOPE_API_KEY（或 OPENAI_API_KEY），或在配置中填写 spring.ai.openai.api-key");
        }

        if (StringUtils.hasText(conversationId)) {
            String id = conversationId.trim();
            String out = chatClientMemoryOnly.prompt()
                    .user(message.trim())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, id))
                    .call()
                    .content();
            return out == null ? "" : out.trim();
        }

        List<Message> messages = buildMessagesFromHistory(history);
        messages.add(new UserMessage(message.trim()));
        Prompt prompt = new Prompt(messages);
        String reply = chatClientPlain.prompt(prompt).call().content();
        return reply == null ? "" : reply.trim();
    }

    private static List<Message> buildMessagesFromHistory(List<Map<String, String>> history) {
        List<Message> messages = new ArrayList<>();
        if (history == null) {
            return messages;
        }
        for (Map<String, String> item : history) {
            if (item == null) {
                continue;
            }
            String role = item.get("role");
            String content = item.get("content");
            if (role == null || content == null || content.trim().isEmpty()) {
                continue;
            }
            String r = role.trim().toLowerCase(Locale.ROOT);
            switch (r) {
                case "system" -> messages.add(new SystemMessage(content.trim()));
                case "assistant" -> messages.add(new AssistantMessage(content.trim()));
                case "user" -> messages.add(new UserMessage(content.trim()));
                default -> { /* ignore */ }
            }
        }
        return messages;
    }
}
