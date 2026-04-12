package com.visualspider.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CrawlSelectorCandidateMapper {

    @Insert("""
            insert into crawl_selector_candidate (
                field_id,
                selector_type,
                selector_value,
                priority
            ) values (
                #{fieldId},
                #{selectorType},
                #{selectorValue},
                #{priority}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrawlSelectorCandidate candidate);

    @Select("""
            select
                id,
                field_id,
                selector_type,
                selector_value,
                priority,
                created_at
            from crawl_selector_candidate
            where field_id = #{fieldId}
            order by priority asc, id asc
            """)
    @Results(id = "crawlSelectorCandidateResult", value = {
            @Result(property = "fieldId", column = "field_id"),
            @Result(property = "selectorType", column = "selector_type"),
            @Result(property = "selectorValue", column = "selector_value"),
            @Result(property = "createdAt", column = "created_at")
    })
    java.util.List<CrawlSelectorCandidate> findByFieldId(Long fieldId);

    @Select("""
            select
                id,
                field_id,
                selector_type,
                selector_value,
                priority,
                created_at
            from crawl_selector_candidate
            where id = #{id}
            """)
    @Results(id = "crawlSelectorCandidateByIdResult", value = {
            @Result(property = "fieldId", column = "field_id"),
            @Result(property = "selectorType", column = "selector_type"),
            @Result(property = "selectorValue", column = "selector_value"),
            @Result(property = "createdAt", column = "created_at")
    })
    CrawlSelectorCandidate findById(Long id);
}
