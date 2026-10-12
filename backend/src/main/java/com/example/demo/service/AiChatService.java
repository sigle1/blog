package com.example.demo.service;

import java.util.List;
import java.util.Map;

public interface AiChatService {

    /**
     * @param conversationId 非空时使用服务端临时记忆；请为每个对话窗口固定一个 UUID
     */
    String chat(String message, List<Map<String, String>> history, String conversationId);
}
