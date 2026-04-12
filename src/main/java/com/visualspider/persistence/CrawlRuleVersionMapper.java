package com.visualspider.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CrawlRuleVersionMapper {

    @Insert("""
            insert into crawl_rule_version (rule_id, version_no, status, source_preview_session_id)
            values (#{ruleId}, #{versionNo}, #{status}, #{sourcePreviewSessionId})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrawlRuleVersion version);

    @Select("""
            select id, rule_id, version_no, status, source_preview_session_id, created_at
            from crawl_rule_version
            where rule_id = #{ruleId}
              and status = 'DRAFT'
            order by version_no desc
            limit 1
            """)
    @Results(id = "crawlRuleVersionResult", value = {
            @Result(property = "ruleId", column = "rule_id"),
            @Result(property = "versionNo", column = "version_no"),
            @Result(property = "sourcePreviewSessionId", column = "source_preview_session_id"),
            @Result(property = "createdAt", column = "created_at")
    })
    CrawlRuleVersion findLatestDraftByRuleId(Long ruleId);
}

