package com.wyc21.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.wyc21.mapper.OrderMapper;

@Slf4j
@Service
public class ArchiveService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    /**
     * 每月1号凌晨2点执行归档操作
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional
    public void archiveData() {
        try {
            log.info("开始执行数据归档...");
            
            // 归档订单和订单项
            int archivedOrders = orderMapper.archiveOrders();
            int archivedOrderItems = orderMapper.archiveOrderItems();
            
            // 删除已归档的数据
            orderMapper.deleteArchivedOrders();
            orderMapper.deleteArchivedOrderItems();
            
            log.info("数据归档完成. 归档订单数: {}, 归档订单项数: {}", 
                    archivedOrders, archivedOrderItems);
        } catch (Exception e) {
            log.error("数据归档失败", e);
            throw e;
        }
    }
} 