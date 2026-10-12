package com.example.demo.controller;

import com.example.demo.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> payload) {
        String message = payload.get("message") == null ? "" : String.valueOf(payload.get("message")).trim();
        if (message.isEmpty()) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{
                put("message", "问题不能为空");
            }});
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) payload.get("history");

        String conversationId = payload.get("conversationId") == null
                ? null
                : String.valueOf(payload.get("conversationId")).trim();
        if (conversationId != null && conversationId.isEmpty()) {
            conversationId = null;
        }

        try {
            String answer = aiChatService.chat(message, history, conversationId);
            Map<String, Object> result = new HashMap<>();
            result.put("answer", answer);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", e.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }
}
