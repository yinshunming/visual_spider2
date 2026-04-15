package com.visualspider.admin;

import com.visualspider.runtime.ListDiscoveryService;
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

import java.util.List;

@Controller
@RequestMapping("/admin/rules/drafts")
public class RuleDraftController {

    private final RuleDraftService ruleDraftService;
    private final ListDiscoveryService listDiscoveryService;

    public RuleDraftController(RuleDraftService ruleDraftService, ListDiscoveryService listDiscoveryService) {
        this.ruleDraftService = ruleDraftService;
        this.listDiscoveryService = listDiscoveryService;
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

        try {
            Long ruleId = ruleDraftService.saveDraftField(fieldForm);
            return "redirect:/admin/rules/drafts/new?previewSessionId="
                    + fieldForm.getPreviewSessionId() + "&ruleId=" + ruleId;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("formError", ex.getMessage());
            model.addAttribute("pageView",
                    ruleDraftService.buildDraftPage(fieldForm.getPreviewSessionId(), fieldForm.getRuleId()));
            return "admin/rule-draft";
        }
    }

    @GetMapping("/list-discovery")
    public String listDiscovery(@RequestParam Long previewSessionId,
                                @RequestParam Long ruleId,
                                Model model) {
        try {
            model.addAttribute("discoveryPage", listDiscoveryService.preview(previewSessionId, ruleId));
            model.addAttribute("discoveryError", null);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("discoveryPage", new ListDiscoveryPageView(
                    previewSessionId,
                    ruleId,
                    "list-discovery",
                    "",
                    List.of()
            ));
            model.addAttribute("discoveryError", ex.getMessage());
        }
        return "admin/list-discovery";
    }
}
