package com.visualspider.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visualspider.persistence.CrawlTask;
import com.visualspider.scheduler.CrawlTaskService;
import com.visualspider.runtime.RuleVersionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin/tasks")
public class CrawlTaskController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CrawlTaskService crawlTaskService;
    private final RuleVersionService ruleVersionService;
    private final ObjectMapper objectMapper;

    public CrawlTaskController(CrawlTaskService crawlTaskService,
                               RuleVersionService ruleVersionService,
                               ObjectMapper objectMapper) {
        this.crawlTaskService = crawlTaskService;
        this.ruleVersionService = ruleVersionService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("tasks", crawlTaskService.findAllTasks().stream()
                .map(task -> new TaskSummaryView(
                        task.getId(),
                        task.getTaskName(),
                        task.getUrlTemplate(),
                        task.getListRuleVersionId(),
                        task.getRuleVersionId(),
                        task.getCronExpression(),
                        task.getStatus()
                )).toList());
        return "admin/task-list-v2";
    }

    @GetMapping("/new")
    public String form(Model model) {
        if (!model.containsAttribute("taskForm")) {
            model.addAttribute("taskForm", new CrawlTaskForm());
        }
        model.addAttribute("publishedVersions", ruleVersionService.findPublishedVersionOptions());
        return "admin/task-form-v2";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("taskForm") CrawlTaskForm taskForm,
                       BindingResult bindingResult,
                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("publishedVersions", ruleVersionService.findPublishedVersionOptions());
            return "admin/task-form-v2";
        }

        CrawlTask task = new CrawlTask();
        task.setId(taskForm.getId());
        task.setTaskName(taskForm.getTaskName().trim());
        task.setUrlTemplate(taskForm.getUrlTemplate().trim());
        task.setListRuleVersionId(taskForm.getListRuleVersionId());
        task.setRuleVersionId(taskForm.getRuleVersionId());
        task.setCronExpression(taskForm.getCronExpression().trim());
        task.setStatus(taskForm.getStatus());
        crawlTaskService.saveTask(task);
        return "redirect:/admin/tasks";
    }

    @PostMapping("/{taskId}/pause")
    public String pause(@PathVariable Long taskId) {
        crawlTaskService.pauseTask(taskId);
        return "redirect:/admin/tasks";
    }

    @PostMapping("/{taskId}/activate")
    public String activate(@PathVariable Long taskId) {
        crawlTaskService.activateTask(taskId);
        return "redirect:/admin/tasks";
    }

    @GetMapping("/{taskId}/runs")
    public String runs(@PathVariable Long taskId, Model model) {
        model.addAttribute("task", crawlTaskService.findTask(taskId));
        model.addAttribute("runs", crawlTaskService.findRuns(taskId));
        return "admin/task-runs-v2";
    }

    @GetMapping("/runs/{runId}")
    public String runDetail(@PathVariable Long runId, Model model) {
        var run = crawlTaskService.findRun(runId);
        var snapshots = crawlTaskService.findSnapshots(runId).stream()
                .map(snapshot -> new TaskSnapshotView(snapshot.getSnapshotType(), snapshot.getFilePath()))
                .toList();
        model.addAttribute("runDetail", new TaskRunDetailView(
                run.getId(),
                run.getStatus(),
                run.getSourceUrl(),
                run.getDurationMs(),
                run.getErrorMessage(),
                run.getStartedAt() == null ? "-" : run.getStartedAt().format(FORMATTER),
                run.getFinishedAt() == null ? "-" : run.getFinishedAt().format(FORMATTER),
                parseStats(snapshots),
                snapshots
        ));
        return "admin/task-run-detail-v2";
    }

    private TaskRunStatsView parseStats(List<TaskSnapshotView> snapshots) {
        return snapshots.stream()
                .filter(snapshot -> "extract-result".equals(snapshot.snapshotType()))
                .findFirst()
                .map(snapshot -> readStats(Path.of(snapshot.filePath())))
                .orElse(null);
    }

    private TaskRunStatsView readStats(Path path) {
        try {
            JsonNode node = objectMapper.readTree(Files.readString(path));
            return new TaskRunStatsView(
                    node.path("mode").asText("SINGLE_DETAIL"),
                    node.path("listDiscoveryCount").asInt(0),
                    node.path("detailCount").asInt(0),
                    node.path("articleInsertedCount").asInt(0),
                    node.path("articleUpdatedCount").asInt(0),
                    node.path("articleSkippedCount").asInt(0),
                    node.path("failureCount").asInt(0),
                    node.path("failureReasons").isArray()
                            ? java.util.stream.StreamSupport.stream(node.path("failureReasons").spliterator(), false)
                            .map(JsonNode::asText)
                            .toList()
                            : List.of()
            );
        } catch (IOException ex) {
            return null;
        }
    }
}
