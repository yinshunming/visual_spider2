package com.visualspider.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface ListDiscoveryItemMapper {

    @Insert("""
            insert into list_discovery_item (run_id, item_index, title_text, detail_url, time_text)
            values (#{runId}, #{itemIndex}, #{titleText}, #{detailUrl}, #{timeText})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ListDiscoveryItem item);
}

