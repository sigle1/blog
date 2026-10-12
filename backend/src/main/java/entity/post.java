package entity;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 博客文章实体类
 * 这个类映射到数据库中的 'posts' 表。
 */
@Data // Lombok 注解，自动生成 Getters, Setters, toString() 等方法
@Component// 告诉 JPA 这是一个实体类

public class post {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverTitle;
    private String coverColor;

    // 存储标签，例如 "Vue,前端,JavaScript"
    private String tags;

    private Integer views;

    private Integer likes;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }


    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}