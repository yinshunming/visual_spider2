package com.visualspider.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectableElementPriorityTest {

    @Test
    void shouldPreferFineGrainedElementsOverContainerDivs() {
        SelectableElement anchor = new SelectableElement(
                1, "a", "热点新闻", "body > div:nth-of-type(2) > a:nth-of-type(1)",
                "", "", "https://news.example.com", "", "", 60, 10, 12, 2
        );
        SelectableElement span = new SelectableElement(
                2, "span", "更多资讯", "body > div:nth-of-type(2) > span:nth-of-type(1)",
                "", "", "", "", "", 61, 10, 8, 2
        );
        SelectableElement div = new SelectableElement(
                3, "div", "热点新闻 更多资讯 今日头条 新浪视频 财经 体育 娱乐 教育 房产 汽车",
                "body > div:nth-of-type(2)", "", "", "", "", "", 59, 10, 80, 22
        );

        assertTrue(anchor.text().length() < div.text().length());
        assertTrue(span.text().length() < div.text().length());
        assertTrue(anchor.widthPercent() < div.widthPercent());
    }
}
