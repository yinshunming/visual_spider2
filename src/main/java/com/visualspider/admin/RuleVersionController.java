package com.visualspider.admin;

import com.visualspider.runtime.RuleVersionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin/rules")
public class RuleVersionController {

    private final RuleVersionService ruleVersionService;

    public RuleVersionController(RuleVersionService ruleVersionService) {
        this.ruleVersionService = ruleVersionService;
    }

    @GetMapping("/{ruleId}/versions")
    public ModelAndView versions(@PathVariable Long ruleId) {
        ModelAndView mav = new ModelAndView("admin/rule-versions");
        mav.addObject("versionPage", ruleVersionService.buildVersionPage(ruleId));
        return mav;
    }

    @PostMapping("/{ruleId}/versions/{versionId}/publish")
    public String publish(@PathVariable Long ruleId, @PathVariable Long versionId) {
        ruleVersionService.publishVersion(ruleId, versionId);
        return "redirect:/admin/rules/" + ruleId + "/versions";
    }

    @PostMapping("/{ruleId}/versions/{versionId}/fork-draft")
    public String forkDraft(@PathVariable Long ruleId, @PathVariable Long versionId) {
        ruleVersionService.createDraftFromVersion(ruleId, versionId);
        return "redirect:/admin/rules/" + ruleId + "/versions";
    }
}

