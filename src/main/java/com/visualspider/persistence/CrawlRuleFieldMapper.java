package com.visualspider.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CrawlRuleFieldMapper {

    @Insert("""
            insert into crawl_rule_field (
                rule_version_id,
                field_name,
                field_type,
                selected_tag_name,
                selected_text,
                dom_path
            ) values (
                #{ruleVersionId},
                #{fieldName},
                #{fieldType},
                #{selectedTagName},
                #{selectedText},
                #{domPath}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrawlRuleField field);

    @Select("""
            select
                id,
                rule_version_id,
                field_name,
                field_type,
                selected_tag_name,
                selected_text,
                dom_path,
                created_at
            from crawl_rule_field
            where rule_version_id = #{ruleVersionId}
            order by id asc
            """)
    @Results(id = "crawlRuleFieldResult", value = {
            @Result(property = "ruleVersionId", column = "rule_version_id"),
            @Result(property = "fieldName", column = "field_name"),
            @Result(property = "fieldType", column = "field_type"),
            @Result(property = "selectedTagName", column = "selected_tag_name"),
            @Result(property = "selectedText", column = "selected_text"),
            @Result(property = "domPath", column = "dom_path"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<CrawlRuleField> findByRuleVersionId(Long ruleVersionId);
}

