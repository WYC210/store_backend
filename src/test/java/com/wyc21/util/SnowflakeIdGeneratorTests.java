package com.wyc21.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;

class SnowflakeIdGeneratorTests {

    @Test
    void testIdUniqueness() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator();
        Set<Long> ids = new HashSet<>();

        // 生成1000个ID并确保都是唯一的
        for (int i = 0; i < 1000; i++) {
            long id = generator.nextId();
            assertTrue(ids.add(id), "ID should be unique");
        }

        assertEquals(1000, ids.size());
    }
}