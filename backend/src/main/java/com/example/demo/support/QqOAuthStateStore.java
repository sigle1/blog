package com.example.demo.support;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class QqOAuthStateStore {

    private static final long TTL_MS = 10 * 60 * 1000L;

    private final Map<String, Long> states = new ConcurrentHashMap<>();

    public String createState() {
        purgeExpired();
        String state = UUID.randomUUID().toString().replace("-", "");
        states.put(state, System.currentTimeMillis() + TTL_MS);
        return state;
    }

    public boolean consumeState(String state) {
        if (state == null || state.isBlank()) {
            return false;
        }
        purgeExpired();
        Long exp = states.remove(state.trim());
        return exp != null && exp >= System.currentTimeMillis();
    }

    private void purgeExpired() {
        long now = System.currentTimeMillis();
        states.entrySet().removeIf(e -> e.getValue() < now);
    }
}
