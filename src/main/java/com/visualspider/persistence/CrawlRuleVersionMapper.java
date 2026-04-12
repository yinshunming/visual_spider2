package com.visualspider.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CrawlRuleVersionMapper {

    @Insert("""
            insert into crawl_rule_version (rule_id, version_no, status, source_preview_session_id, published_at)
            values (#{ruleId}, #{versionNo}, #{status}, #{sourcePreviewSessionId}, #{publishedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrawlRuleVersion version);

    @Select("""
            select id, rule_id, version_no, status, source_preview_session_id, created_at, published_at
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

    @Select("""
            select id, rule_id, version_no, status, source_preview_session_id, created_at, published_at
            from crawl_rule_version
            where id = #{id}
            """)
    @Results(id = "crawlRuleVersionByIdResult", value = {
            @Result(property = "ruleId", column = "rule_id"),
            @Result(property = "versionNo", column = "version_no"),
            @Result(property = "sourcePreviewSessionId", column = "source_preview_session_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "publishedAt", column = "published_at")
    })
    CrawlRuleVersion findById(Long id);

    @Select("""
            select id, rule_id, version_no, status, source_preview_session_id, created_at, published_at
            from crawl_rule_version
            where rule_id = #{ruleId}
            order by version_no desc
            """)
    @Results(id = "crawlRuleVersionListResult", value = {
            @Result(property = "ruleId", column = "rule_id"),
            @Result(property = "versionNo", column = "version_no"),
            @Result(property = "sourcePreviewSessionId", column = "source_preview_session_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "publishedAt", column = "published_at")
    })
    List<CrawlRuleVersion> findByRuleId(Long ruleId);

    @Select("""
            select max(version_no)
            from crawl_rule_version
            where rule_id = #{ruleId}
            """)
    Integer findMaxVersionNoByRuleId(Long ruleId);

    @Update("""
            update crawl_rule_version
            set status = 'ARCHIVED',
                published_at = null
            where rule_id = #{ruleId}
              and status = 'PUBLISHED'
            """)
    int clearPublishedStatus(Long ruleId);

    @Update("""
            update crawl_rule_version
            set status = 'PUBLISHED',
                published_at = current_timestamp
            where id = #{versionId}
            """)
    int markPublished(Long versionId);
}
