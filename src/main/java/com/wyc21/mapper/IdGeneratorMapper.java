package com.wyc21.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IdGeneratorMapper {
    Integer initIdGenerator(@Param("idType") String idType, 
                          @Param("currentMaxId") Long currentMaxId);
    
    Long getCurrentMaxId(@Param("idType") String idType);
    
    Integer updateMaxId(@Param("idType") String idType, 
                       @Param("newMaxId") Long newMaxId, 
                       @Param("version") Integer version);
} 