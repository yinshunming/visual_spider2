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
public interface CrawlTaskMapper {

    @Insert("""
            insert into crawl_task (task_name, url_template, rule_version_id, cron_expression, status)
            values (#{taskName}, #{urlTemplate}, #{ruleVersionId}, #{cronExpression}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrawlTask task);

    @Update("""
            update crawl_task
            set task_name = #{taskName},
                url_template = #{urlTemplate},
                rule_version_id = #{ruleVersionId},
                cron_expression = #{cronExpression},
                status = #{status},
                updated_at = current_timestamp
            where id = #{id}
            """)
    int update(CrawlTask task);

    @Select("""
            select id, task_name, url_template, rule_version_id, cron_expression, status, created_at, updated_at
            from crawl_task
            where id = #{id}
            """)
    @Results(id = "crawlTaskResult", value = {
            @Result(property = "taskName", column = "task_name"),
            @Result(property = "urlTemplate", column = "url_template"),
            @Result(property = "ruleVersionId", column = "rule_version_id"),
            @Result(property = "cronExpression", column = "cron_expression"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    CrawlTask findById(Long id);

    @Select("""
            select id, task_name, url_template, rule_version_id, cron_expression, status, created_at, updated_at
            from crawl_task
            order by id desc
            """)
    @Results(id = "crawlTaskListResult", value = {
            @Result(property = "taskName", column = "task_name"),
            @Result(property = "urlTemplate", column = "url_template"),
            @Result(property = "ruleVersionId", column = "rule_version_id"),
            @Result(property = "cronExpression", column = "cron_expression"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<CrawlTask> findAll();
}

