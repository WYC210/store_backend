package com.wyc21.util;

import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdGenerator {
    // 开始时间戳（2024-01-01 00:00:00）
    private final long START_TIMESTAMP = 1704038400000L;
    
    // 每部分占用的位数
    private final long SEQUENCE_BIT = 12;   // 序列号占用的位数
    private final long MACHINE_BIT = 5;     // 机器标识占用的位数
    private final long DATACENTER_BIT = 5;  // 数据中心占用的位数
    
    // 每部分的最大值
    private final long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);
    private final long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    
    // 每部分向左的位移
    private final long MACHINE_LEFT = SEQUENCE_BIT;
    private final long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;
    
    private long datacenterId = 1;  // 数据中心ID
    private long machineId = 1;     // 机器标识ID
    private long sequence = 0L;     // 序列号
    private long lastTimestamp = -1L;// 上一次时间戳
    
    public synchronized long nextId() {
        long currTimestamp = System.currentTimeMillis();
        
        if (currTimestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }
        
        if (currTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0L) {
                currTimestamp = getNextMillis();
            }
        } else {
            sequence = 0L;
        }
        
        lastTimestamp = currTimestamp;
        
        return ((currTimestamp - START_TIMESTAMP) << TIMESTAMP_LEFT) 
                | (datacenterId << DATACENTER_LEFT) 
                | (machineId << MACHINE_LEFT) 
                | sequence;
    }
    
    private long getNextMillis() {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
} 