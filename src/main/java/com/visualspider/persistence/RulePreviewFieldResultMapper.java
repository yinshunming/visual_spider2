package com.visualspider.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface RulePreviewFieldResultMapper {

    @Insert("""
            insert into rule_preview_field_result (
                preview_run_id,
                field_id,
                selector_candidate_id,
                extracted_value,
                status,
                validation_message
            ) values (
                #{previewRunId},
                #{fieldId},
                #{selectorCandidateId},
                #{extractedValue},
                #{status},
                #{validationMessage}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RulePreviewFieldResult result);
}

