package com.visualspider.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface RulePreviewRunMapper {

    @Insert("""
            insert into rule_preview_run (rule_version_id, preview_session_id, source_url)
            values (#{ruleVersionId}, #{previewSessionId}, #{sourceUrl})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RulePreviewRun run);
}

