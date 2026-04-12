package com.visualspider.admin;

import com.visualspider.persistence.DatabaseProbeMapper;
import com.visualspider.runtime.PageTitleProbeResult;
import com.visualspider.runtime.PlaywrightService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PlaywrightService playwrightService;
    private final DatabaseProbeMapper databaseProbeMapper;

    public AdminController(PlaywrightService playwrightService, DatabaseProbeMapper databaseProbeMapper) {
        this.playwrightService = playwrightService;
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
        if (bindingResult.hasErrors()) {
            model.addAttribute("validationError", bindingResult.getFieldError("url") != null
                    ? bindingResult.getFieldError("url").getDefaultMessage()
                    : "表单校验失败");
            return "admin/index";
        }

        try {
            PageTitleProbeResult result = playwrightService.fetchPageTitle(probeForm.getUrl());
            model.addAttribute("probeResult", result);
        } catch (Exception ex) {
            model.addAttribute("probeError", ex.getMessage());
        }
        return "admin/index";
    }

    private boolean isDatabaseAvailable() {
        try {
            return databaseProbeMapper.selectOne() == 1;
        } catch (Exception ignored) {
            return false;
        }
    }
}
