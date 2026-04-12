package com.visualspider.admin;

import com.visualspider.runtime.ArticleIngestionService;
import com.visualspider.runtime.ArticleMappingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/rules/{ruleId}/article-mappings")
public class ArticleMappingController {

    private final ArticleMappingService articleMappingService;
    private final ArticleIngestionService articleIngestionService;

    public ArticleMappingController(ArticleMappingService articleMappingService,
                                    ArticleIngestionService articleIngestionService) {
        this.articleMappingService = articleMappingService;
        this.articleIngestionService = articleIngestionService;
    }

    @GetMapping
    public ModelAndView page(@PathVariable Long ruleId) {
        ModelAndView mav = new ModelAndView("admin/article-mapping");
        mav.addObject("mappingPage", articleMappingService.buildPage(ruleId, null));
        mav.addObject("mappingForm", articleMappingService.buildForm(ruleId));
        return mav;
    }

    @PostMapping
    public String save(@PathVariable Long ruleId,
                       @ModelAttribute("mappingForm") ArticleMappingForm form,
                       RedirectAttributes redirectAttributes) {
        articleMappingService.saveMappings(ruleId, form);
        redirectAttributes.addFlashAttribute("runMessage", "字段映射已保存");
        return "redirect:/admin/rules/" + ruleId + "/article-mappings";
    }

    @PostMapping("/run")
    public String run(@PathVariable Long ruleId, RedirectAttributes redirectAttributes) {
        String result = articleIngestionService.ingest(ruleId);
        redirectAttributes.addFlashAttribute("runMessage", result);
        return "redirect:/admin/rules/" + ruleId + "/article-mappings";
    }
}

