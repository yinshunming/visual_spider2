package com.visualspider.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CrawlRuleMapper {

    @Insert("""
            insert into crawl_rule (rule_name, source_preview_session_id)
            values (#{ruleName}, #{sourcePreviewSessionId})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrawlRule rule);

    @Select("""
            select id, rule_name, source_preview_session_id, created_at, updated_at
            from crawl_rule
            where id = #{id}
            """)
    @Results(id = "crawlRuleResult", value = {
            @Result(property = "ruleName", column = "rule_name"),
            @Result(property = "sourcePreviewSessionId", column = "source_preview_session_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    CrawlRule findById(Long id);

    @Select("""
            select id, rule_name, source_preview_session_id, created_at, updated_at
            from crawl_rule
            order by id desc
            """)
    @Results(id = "crawlRuleListResult", value = {
            @Result(property = "ruleName", column = "rule_name"),
            @Result(property = "sourcePreviewSessionId", column = "source_preview_session_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<CrawlRule> findAll();
}
