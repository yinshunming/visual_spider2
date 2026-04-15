package com.visualspider.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface ListDiscoveryRunMapper {

    @Insert("""
            insert into list_discovery_run (rule_version_id, source_url)
            values (#{ruleVersionId}, #{sourceUrl})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ListDiscoveryRun run);
}

