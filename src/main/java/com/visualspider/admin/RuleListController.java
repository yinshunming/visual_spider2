package com.visualspider.admin;

import com.visualspider.persistence.CrawlRuleMapper;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/rules")
public class RuleListController {

    private final CrawlRuleMapper crawlRuleMapper;
    private final CrawlRuleVersionMapper crawlRuleVersionMapper;

    public RuleListController(CrawlRuleMapper crawlRuleMapper, CrawlRuleVersionMapper crawlRuleVersionMapper) {
        this.crawlRuleMapper = crawlRuleMapper;
        this.crawlRuleVersionMapper = crawlRuleVersionMapper;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("rules", crawlRuleMapper.findAll().stream().map(rule -> {
            var draft = crawlRuleVersionMapper.findLatestDraftByRuleId(rule.getId());
            return new RuleListItemView(
                    rule.getId(),
                    rule.getRuleName(),
                    rule.getSourcePreviewSessionId(),
                    draft == null ? null : draft.getId(),
                    draft == null ? null : draft.getVersionNo(),
                    draft == null ? null : draft.getStatus()
            );
        }).toList());
        return "admin/rule-list";
    }
}
