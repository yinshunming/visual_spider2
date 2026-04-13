package com.visualspider.admin;

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

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin/tasks")
public class CrawlTaskController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CrawlTaskService crawlTaskService;
    private final RuleVersionService ruleVersionService;

    public CrawlTaskController(CrawlTaskService crawlTaskService, RuleVersionService ruleVersionService) {
        this.crawlTaskService = crawlTaskService;
        this.ruleVersionService = ruleVersionService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("tasks", crawlTaskService.findAllTasks().stream()
                .map(task -> new TaskSummaryView(
                        task.getId(),
                        task.getTaskName(),
                        task.getUrlTemplate(),
                        task.getRuleVersionId(),
                        task.getCronExpression(),
                        task.getStatus()
                )).toList());
        return "admin/task-list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        if (!model.containsAttribute("taskForm")) {
            model.addAttribute("taskForm", new CrawlTaskForm());
        }
        model.addAttribute("publishedVersions", ruleVersionService.findPublishedVersionOptions());
        return "admin/task-form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("taskForm") CrawlTaskForm taskForm,
                       BindingResult bindingResult,
                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("publishedVersions", ruleVersionService.findPublishedVersionOptions());
            return "admin/task-form";
        }

        CrawlTask task = new CrawlTask();
        task.setId(taskForm.getId());
        task.setTaskName(taskForm.getTaskName().trim());
        task.setUrlTemplate(taskForm.getUrlTemplate().trim());
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
        return "admin/task-runs";
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
                snapshots
        ));
        return "admin/task-run-detail";
    }
}
