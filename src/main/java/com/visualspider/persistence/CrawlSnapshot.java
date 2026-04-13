package com.visualspider.persistence;

import java.time.LocalDateTime;

public class CrawlSnapshot {

    private Long id;
    private Long runLogId;
    private String snapshotType;
    private String filePath;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRunLogId() {
        return runLogId;
    }

    public void setRunLogId(Long runLogId) {
        this.runLogId = runLogId;
    }

    public String getSnapshotType() {
        return snapshotType;
    }

    public void setSnapshotType(String snapshotType) {
        this.snapshotType = snapshotType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

