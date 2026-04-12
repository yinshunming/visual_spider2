package com.visualspider.admin;

import com.visualspider.persistence.DatabaseProbeMapper;
import com.visualspider.persistence.PagePreviewSession;
import com.visualspider.runtime.PagePreviewSessionService;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PagePreviewSessionService pagePreviewSessionService;
    private final DatabaseProbeMapper databaseProbeMapper;

    public AdminController(PagePreviewSessionService pagePreviewSessionService, DatabaseProbeMapper databaseProbeMapper) {
        this.pagePreviewSessionService = pagePreviewSessionService;
        this.databaseProbeMapper = databaseProbeMapper;
    }

    @GetMapping
    public String index(Model model) {
        if (!model.containsAttribute("probeForm")) {
            model.addAttribute("probeForm", new PageProbeForm());
        }
        if (!model.containsAttribute("validationError")) {
            model.addAttribute("validationError", null);
        }
        model.addAttribute("databaseOk", isDatabaseAvailable());
        return "admin/index";
    }

    @PostMapping("/playwright-demo")
    public String runPlaywrightDemo(@Valid @ModelAttribute("probeForm") PageProbeForm probeForm,
                                    BindingResult bindingResult,
                                    Model model) {
        model.addAttribute("databaseOk", isDatabaseAvailable());
        model.addAttribute("validationError", null);
        if (bindingResult.hasErrors()) {
            model.addAttribute("validationError", bindingResult.getFieldError("url") != null
                    ? bindingResult.getFieldError("url").getDefaultMessage()
                    : "表单校验失败");
            return "admin/index";
        }

        PreviewSessionView previewSession = pagePreviewSessionService.createPreview(probeForm.getUrl());
        model.addAttribute("previewSession", previewSession);
        model.addAttribute("probeError", previewSession.errorMessage());
        return "admin/index";
    }

    @GetMapping("/preview-sessions/{id}/screenshot")
    public ResponseEntity<Resource> screenshot(@PathVariable Long id) {
        PagePreviewSession session = pagePreviewSessionService.getSession(id);
        if (session == null || session.getScreenshotPath() == null) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(session.getScreenshotPath());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }

    private boolean isDatabaseAvailable() {
        try {
            return databaseProbeMapper.selectOne() == 1;
        } catch (Exception ignored) {
            return false;
        }
    }
}
