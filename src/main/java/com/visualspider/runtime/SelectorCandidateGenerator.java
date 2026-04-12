package com.visualspider.runtime;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Component
public class SelectorCandidateGenerator {

    public List<SelectorCandidateDraft> generate(SelectableElement element) {
        List<SelectorCandidateDraft> candidates = new ArrayList<>();
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        int priority = 1;

        if (hasText(element.elementIdValue())) {
            priority = addCandidate(candidates, seen, priority, "css", "#" + element.elementIdValue().trim());
        }

        if (hasText(element.domPath())) {
            priority = addCandidate(candidates, seen, priority, "dom_path", element.domPath().trim());
        }

        if (hasText(element.text())) {
            priority = addCandidate(candidates, seen, priority, "text", normalizeText(element.text()));
        }

        if (hasText(element.classNames())) {
            String cssByClass = buildClassSelector(element.tagName(), element.classNames());
            if (hasText(cssByClass)) {
                priority = addCandidate(candidates, seen, priority, "css_class", cssByClass);
            }
        }

        if (hasText(element.dateTimeValue())) {
            priority = addCandidate(candidates, seen, priority, "attribute",
                    element.tagName() + "[datetime=\"" + element.dateTimeValue().trim() + "\"]");
        } else if (hasText(element.titleValue())) {
            priority = addCandidate(candidates, seen, priority, "attribute",
                    element.tagName() + "[title=\"" + element.titleValue().trim() + "\"]");
        } else if (hasText(element.hrefValue())) {
            priority = addCandidate(candidates, seen, priority, "attribute",
                    element.tagName() + "[href=\"" + element.hrefValue().trim() + "\"]");
        }

        if (candidates.size() < 2) {
            addCandidate(candidates, seen, priority, "tag", element.tagName());
        }
        return candidates;
    }

    private int addCandidate(List<SelectorCandidateDraft> candidates,
                             LinkedHashSet<String> seen,
                             int priority,
                             String type,
                             String value) {
        String normalized = type + "::" + value;
        if (seen.add(normalized)) {
            candidates.add(new SelectorCandidateDraft(type, value, priority));
            return priority + 1;
        }
        return priority;
    }

    private String buildClassSelector(String tagName, String classNames) {
        String[] parts = classNames.trim().split("\\s+");
        if (parts.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder(tagName);
        int count = 0;
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            builder.append('.').append(part);
            count++;
            if (count >= 2) {
                break;
            }
        }
        return count == 0 ? null : builder.toString();
    }

    private String normalizeText(String text) {
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 80 ? normalized : normalized.substring(0, 80);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
