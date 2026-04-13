package com.visualspider.scheduler;

import com.visualspider.persistence.CrawlRunLog;
import com.visualspider.persistence.CrawlSnapshot;
import com.visualspider.persistence.CrawlSnapshotMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TaskSnapshotService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CrawlSnapshotMapper crawlSnapshotMapper;

    public TaskSnapshotService(CrawlSnapshotMapper crawlSnapshotMapper) {
        this.crawlSnapshotMapper = crawlSnapshotMapper;
    }

    public void writeSnapshot(CrawlRunLog runLog, String snapshotType, String content, String extension) {
        try {
            Path path = buildPath(runLog.getTaskId(), runLog.getId(), snapshotType, extension);
            Files.createDirectories(path.getParent());
            Files.writeString(path, content == null ? "" : content);

            CrawlSnapshot snapshot = new CrawlSnapshot();
            snapshot.setRunLogId(runLog.getId());
            snapshot.setSnapshotType(snapshotType);
            snapshot.setFilePath(path.toString());
            crawlSnapshotMapper.insert(snapshot);
        } catch (IOException ex) {
            throw new IllegalStateException("写入快照失败: " + ex.getMessage(), ex);
        }
    }

    public void copySnapshotFile(CrawlRunLog runLog, String snapshotType, String sourceFilePath, String extension) {
        try {
            Path source = Paths.get(sourceFilePath).toAbsolutePath().normalize();
            Path target = buildPath(runLog.getTaskId(), runLog.getId(), snapshotType, extension);
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

            CrawlSnapshot snapshot = new CrawlSnapshot();
            snapshot.setRunLogId(runLog.getId());
            snapshot.setSnapshotType(snapshotType);
            snapshot.setFilePath(target.toString());
            crawlSnapshotMapper.insert(snapshot);
        } catch (IOException ex) {
            throw new IllegalStateException("复制快照文件失败: " + ex.getMessage(), ex);
        }
    }

    private Path buildPath(Long taskId, Long runId, String snapshotType, String extension) {
        String timestamp = FORMATTER.format(LocalDateTime.now());
        return Paths.get("snapshots", "task-runs", String.valueOf(taskId), String.valueOf(runId),
                timestamp + "-" + snapshotType + "." + extension).toAbsolutePath().normalize();
    }
}
