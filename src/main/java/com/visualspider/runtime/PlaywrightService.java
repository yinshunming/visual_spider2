package com.visualspider.runtime;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class PlaywrightService {

    private final PlaywrightProperties properties;

    public PlaywrightService(PlaywrightProperties properties) {
        this.properties = properties;
    }

    public PageTitleProbeResult fetchPageTitle(String url) {
        Instant start = Instant.now();

        try (Playwright playwright = Playwright.create()) {
            BrowserType browserType = resolveBrowser(playwright);
            Browser browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(properties.isHeadless()));
            try (browser) {
                Page page = browser.newPage();
                page.navigate(url, new Page.NavigateOptions().setTimeout((double) properties.getTimeoutMs()));
                String title = page.title();
                String finalUrl = page.url();
                long durationMs = Duration.between(start, Instant.now()).toMillis();
                return new PageTitleProbeResult(url, finalUrl, title, durationMs);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Playwright 页面探测失败，请先安装浏览器或检查目标 URL 是否可访问: " + ex.getMessage(), ex);
        }
    }

    private BrowserType resolveBrowser(Playwright playwright) {
        return switch (properties.getBrowser().toLowerCase()) {
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> playwright.chromium();
        };
    }
}

