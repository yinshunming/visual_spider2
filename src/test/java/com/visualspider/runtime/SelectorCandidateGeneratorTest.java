package com.visualspider.runtime;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectorCandidateGeneratorTest {

    private final SelectorCandidateGenerator generator = new SelectorCandidateGenerator();

    @Test
    void shouldGenerateAtLeastTwoSelectorCandidates() {
        SelectableElement element = new SelectableElement(
                1,
                "h1",
                "新浪首页",
                "body > div:nth-of-type(1) > h1:nth-of-type(1)",
                "main_title",
                "news-title headline",
                "",
                "",
                "",
                10,
                10,
                20,
                5
        );

        List<SelectorCandidateDraft> candidates = generator.generate(element);

        assertTrue(candidates.size() >= 2);
        assertTrue(candidates.stream().noneMatch(candidate ->
                "xpath".equalsIgnoreCase(candidate.selectorType()) || candidate.selectorValue().startsWith("//")));
    }

    @Test
    void shouldUseTextFallbackWhenAttributesAreSparse() {
        SelectableElement element = new SelectableElement(
                2,
                "p",
                "这是一段正文摘要文本",
                "body > article:nth-of-type(1) > p:nth-of-type(2)",
                "",
                "",
                "",
                "",
                "",
                12,
                15,
                40,
                8
        );

        List<SelectorCandidateDraft> candidates = generator.generate(element);

        assertTrue(candidates.stream().anyMatch(candidate -> "text".equals(candidate.selectorType())));
        assertFalse(candidates.isEmpty());
    }
}
