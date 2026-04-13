package com.visualspider.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CrawlSnapshotMapper {

    @Insert("""
            insert into crawl_snapshot (run_log_id, snapshot_type, file_path)
            values (#{runLogId}, #{snapshotType}, #{filePath})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrawlSnapshot snapshot);

    @Select("""
            select id, run_log_id, snapshot_type, file_path, created_at
            from crawl_snapshot
            where run_log_id = #{runLogId}
            order by id asc
            """)
    @Results(id = "crawlSnapshotResult", value = {
            @Result(property = "runLogId", column = "run_log_id"),
            @Result(property = "snapshotType", column = "snapshot_type"),
            @Result(property = "filePath", column = "file_path"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<CrawlSnapshot> findByRunLogId(Long runLogId);
}

