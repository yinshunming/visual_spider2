package com.visualspider.admin;

public record SelectableElementView(
        int elementIndex,
        String tagName,
        String text,
        String domPath,
        String elementIdValue,
        String classNames,
        String hrefValue,
        String titleValue,
        String dateTimeValue,
        double topPercent,
        double leftPercent,
        double widthPercent,
        double heightPercent
) {
}

