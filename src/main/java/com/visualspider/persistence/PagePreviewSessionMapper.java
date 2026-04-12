package com.visualspider.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PagePreviewSessionMapper {

    @Insert("""
            insert into page_preview_session (
                requested_url,
                final_url,
                page_title,
                load_duration_ms,
                screenshot_path,
                status,
                error_message
            ) values (
                #{requestedUrl},
                #{finalUrl},
                #{pageTitle},
                #{loadDurationMs},
                #{screenshotPath},
                #{status},
                #{errorMessage}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PagePreviewSession session);

    @Select("""
            select
                id,
                requested_url,
                final_url,
                page_title,
                load_duration_ms,
                screenshot_path,
                status,
                error_message,
                created_at
            from page_preview_session
            where id = #{id}
            """)
    @Results(id = "pagePreviewSessionResult", value = {
            @Result(property = "requestedUrl", column = "requested_url"),
            @Result(property = "finalUrl", column = "final_url"),
            @Result(property = "pageTitle", column = "page_title"),
            @Result(property = "loadDurationMs", column = "load_duration_ms"),
            @Result(property = "screenshotPath", column = "screenshot_path"),
            @Result(property = "errorMessage", column = "error_message"),
            @Result(property = "createdAt", column = "created_at")
    })
    PagePreviewSession findById(Long id);
}

