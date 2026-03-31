package com.lg.gulimail.ai.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ai_chat_message")
public class AiChatMessageEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private Integer tokenUsage;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
