package com.visualspider.persistence;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RuleArticleMappingMapper {

    @Delete("""
            delete from rule_article_mapping
            where rule_version_id = #{ruleVersionId}
            """)
    int deleteByRuleVersionId(Long ruleVersionId);

    @Insert("""
            insert into rule_article_mapping (rule_version_id, field_id, article_column)
            values (#{ruleVersionId}, #{fieldId}, #{articleColumn})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RuleArticleMapping mapping);

    @Select("""
            select id, rule_version_id, field_id, article_column, created_at
            from rule_article_mapping
            where rule_version_id = #{ruleVersionId}
            order by id asc
            """)
    @Results(id = "ruleArticleMappingResult", value = {
            @Result(property = "ruleVersionId", column = "rule_version_id"),
            @Result(property = "fieldId", column = "field_id"),
            @Result(property = "articleColumn", column = "article_column"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<RuleArticleMapping> findByRuleVersionId(Long ruleVersionId);
}

