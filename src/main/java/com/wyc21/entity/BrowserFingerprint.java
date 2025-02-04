package com.wyc21.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BrowserFingerprint {
    private String fingerprintId;
    private LocalDateTime firstSeenTime;
    private LocalDateTime lastSeenTime;
    private Long userId;
}