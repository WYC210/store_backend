package com.wyc21.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BrowseHistory {
    private Long historyId;
    private String fingerprintId;
    private Long userId;
    private Long productId;
    private LocalDateTime browseTime;
}