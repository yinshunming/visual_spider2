package com.visualspider.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DatabaseProbeMapper {

    @Select("select 1")
    int selectOne();
}

