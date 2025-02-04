package com.wyc21.mapper;

import com.wyc21.entity.BrowseHistory;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BrowseHistoryMapper {
    void insert(BrowseHistory history);

    List<BrowseHistory> findByFingerprintId(String fingerprintId);

    List<BrowseHistory> findByUserId(Long userId);

    void updateUserIdByFingerprintId(String fingerprintId, Long userId);
}