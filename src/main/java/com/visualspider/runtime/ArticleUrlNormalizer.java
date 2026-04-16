package com.visualspider.runtime;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class ArticleUrlNormalizer {

    public String normalize(String extractedUrl, String canonicalUrl, String sourcePageUrl) {
        String preferred = firstNonBlank(canonicalUrl, extractedUrl, sourcePageUrl);
        if (preferred == null) {
            return null;
        }
        try {
            URI raw = new URI(preferred.trim());
            String scheme = raw.getScheme() == null ? "https" : raw.getScheme().toLowerCase();
            String host = raw.getHost() == null ? null : raw.getHost().toLowerCase();
            String path = raw.getPath();
            if (path == null || path.isBlank()) {
                path = "/";
            }
            URI normalized = new URI(scheme, raw.getUserInfo(), host, raw.getPort(), path, null, null);
            return normalized.toString();
        } catch (URISyntaxException ex) {
            return preferred.trim();
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
