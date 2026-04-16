package com.visualspider.runtime;

public record ArticleIngestionResult(
        ArticleIngestionAction action,
        String sourceUrl,
        String message
) {

    public static ArticleIngestionResult inserted(String sourceUrl, String message) {
        return new ArticleIngestionResult(ArticleIngestionAction.INSERTED, sourceUrl, message);
    }

    public static ArticleIngestionResult updated(String sourceUrl, String message) {
        return new ArticleIngestionResult(ArticleIngestionAction.UPDATED, sourceUrl, message);
    }

    public static ArticleIngestionResult skipped(String sourceUrl, String message) {
        return new ArticleIngestionResult(ArticleIngestionAction.SKIPPED, sourceUrl, message);
    }
}
