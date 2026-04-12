package com.visualspider.admin;

import com.visualspider.runtime.RulePreviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/rules/previews")
public class RulePreviewController {

    private final RulePreviewService rulePreviewService;

    public RulePreviewController(RulePreviewService rulePreviewService) {
        this.rulePreviewService = rulePreviewService;
    }

    @GetMapping
    public String preview(@RequestParam Long previewSessionId,
                          @RequestParam Long ruleId,
                          @RequestParam Map<String, String> params,
                          Model model) {
        model.addAttribute("previewPage", rulePreviewService.preview(previewSessionId, ruleId, parseOverrides(params)));
        return "admin/rule-preview";
    }

    private Map<Long, Long> parseOverrides(Map<String, String> params) {
        Map<Long, Long> overrides = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!entry.getKey().startsWith("candidateField_")) {
                continue;
            }
            String fieldIdText = entry.getKey().substring("candidateField_".length());
            if (fieldIdText.isBlank() || entry.getValue().isBlank()) {
                continue;
            }
            overrides.put(Long.parseLong(fieldIdText), Long.parseLong(entry.getValue()));
        }
        return overrides;
    }
}
