package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GuestMessage {
    private Long id;
    private String nickname;
    private String content;
    private Integer likes;
    private Integer favorites;
    /** 评论条数（列表/详情查询时填充） */
    private Integer commentCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;
}
