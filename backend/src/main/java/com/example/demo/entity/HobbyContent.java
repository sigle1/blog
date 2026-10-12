package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HobbyContent {
    private Long id;
    private Long hobbyId;
    private String content;
    private LocalDateTime createdAt;
}

