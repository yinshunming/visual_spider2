package com.visualspider.admin;

import com.visualspider.runtime.RuleDraftService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/rules/drafts")
public class RuleDraftController {

    private final RuleDraftService ruleDraftService;

    public RuleDraftController(RuleDraftService ruleDraftService) {
        this.ruleDraftService = ruleDraftService;
    }

    @GetMapping("/new")
    public String newDraft(@RequestParam Long previewSessionId,
                           @RequestParam(required = false) Long ruleId,
                           Model model) {
        model.addAttribute("pageView", ruleDraftService.buildDraftPage(previewSessionId, ruleId));
        if (!model.containsAttribute("fieldForm")) {
            RuleDraftFieldForm fieldForm = new RuleDraftFieldForm();
            fieldForm.setPreviewSessionId(previewSessionId);
            fieldForm.setRuleId(ruleId);
            model.addAttribute("fieldForm", fieldForm);
        }
        if (!model.containsAttribute("formError")) {
            model.addAttribute("formError", null);
        }
        return "admin/rule-draft";
    }

    @PostMapping("/fields")
    public String saveField(@Valid @ModelAttribute("fieldForm") RuleDraftFieldForm fieldForm,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formError", bindingResult.getAllErrors().getFirst().getDefaultMessage());
            model.addAttribute("pageView",
                    ruleDraftService.buildDraftPage(fieldForm.getPreviewSessionId(), fieldForm.getRuleId()));
            return "admin/rule-draft";
        }

        Long ruleId = ruleDraftService.saveDraftField(fieldForm);
        return "redirect:/admin/rules/drafts/new?previewSessionId="
                + fieldForm.getPreviewSessionId() + "&ruleId=" + ruleId;
    }
}
