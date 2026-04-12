package com.visualspider.runtime;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PlaywrightService {

    private final PlaywrightProperties properties;
    private final SnapshotProperties snapshotProperties;

    public PlaywrightService(PlaywrightProperties properties, SnapshotProperties snapshotProperties) {
        this.properties = properties;
        this.snapshotProperties = snapshotProperties;
    }

    public PageTitleProbeResult fetchPageTitle(String url) {
        PagePreviewResult preview = capturePagePreview(url);
        return new PageTitleProbeResult(preview.requestedUrl(), preview.finalUrl(), preview.title(), preview.durationMs());
    }

    public PagePreviewResult capturePagePreview(String url) {
        Instant start = Instant.now();

        try (Playwright playwright = Playwright.create()) {
            BrowserType browserType = resolveBrowser(playwright);
            Browser browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(properties.isHeadless()));
            try (browser) {
                Page page = browser.newPage();
                page.navigate(url, new Page.NavigateOptions().setTimeout((double) properties.getTimeoutMs()));
                String title = page.title();
                String finalUrl = page.url();
                String screenshotPath = saveScreenshot(page);
                long durationMs = Duration.between(start, Instant.now()).toMillis();
                return new PagePreviewResult(url, finalUrl, title, durationMs, screenshotPath);
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

    private String saveScreenshot(Page page) throws IOException {
        Path root = Paths.get(snapshotProperties.getStorageRoot()).toAbsolutePath().normalize();
        Path previewDir = root.resolve("page-preview");
        Files.createDirectories(previewDir);

        String fileName = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(java.time.LocalDateTime.now())
                + "-" + UUID.randomUUID() + ".png";
        Path screenshotFile = previewDir.resolve(fileName);
        page.screenshot(new Page.ScreenshotOptions().setPath(screenshotFile).setFullPage(true));
        return screenshotFile.toString();
    }
}
