package com.visualspider.runtime;

import com.visualspider.persistence.CrawlSelectorCandidate;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.net.URI;

@Service
public class PlaywrightService {

    private static final int DEFAULT_ELEMENT_LIMIT = 80;

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

    public PageRuntimeSnapshot captureRuntimeSnapshot(String url) {
        try (Playwright playwright = Playwright.create()) {
            BrowserType browserType = resolveBrowser(playwright);
            Browser browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(properties.isHeadless()));
            try (browser) {
                Page page = browser.newPage();
                page.navigate(url, new Page.NavigateOptions().setTimeout((double) properties.getTimeoutMs()));
                String screenshotPath = saveScreenshot(page);
                return new PageRuntimeSnapshot(page.content(), screenshotPath);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("运行时快照采集失败: " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    public List<SelectableElement> inspectSelectableElements(String url) {
        try (Playwright playwright = Playwright.create()) {
            BrowserType browserType = resolveBrowser(playwright);
            Browser browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(properties.isHeadless()));
            try (browser) {
                Page page = browser.newPage();
                page.navigate(url, new Page.NavigateOptions().setTimeout((double) properties.getTimeoutMs()));
                Object raw = page.evaluate("""
                        () => {
                          const normalize = (value) => (value || '').replace(/\\s+/g, ' ').trim();
                          const docWidth = Math.max(document.documentElement.scrollWidth, window.innerWidth);
                          const docHeight = Math.max(document.documentElement.scrollHeight, window.innerHeight);
                          const domPath = (element) => {
                            const parts = [];
                            let current = element;
                            while (current && current.nodeType === 1 && current.tagName.toLowerCase() !== 'html') {
                              const tag = current.tagName.toLowerCase();
                              if (current.id) {
                                parts.unshift(`${tag}#${current.id}`);
                                break;
                              }
                              let index = 1;
                              let sibling = current.previousElementSibling;
                              while (sibling) {
                                if (sibling.tagName === current.tagName) {
                                  index++;
                                }
                                sibling = sibling.previousElementSibling;
                              }
                              parts.unshift(`${tag}:nth-of-type(${index})`);
                              current = current.parentElement;
                            }
                            return parts.join(' > ');
                          };

                          return Array.from(document.querySelectorAll('body *'))
                            .map((element, index) => {
                              const rect = element.getBoundingClientRect();
                              const text = normalize(element.innerText || element.textContent);
                              const childCount = element.children ? element.children.length : 0;
                              const top = rect.top + window.scrollY;
                              const left = rect.left + window.scrollX;
                              return {
                                elementIndex: index,
                                tagName: element.tagName.toLowerCase(),
                                text,
                                domPath: domPath(element),
                                elementIdValue: element.id || '',
                                classNames: typeof element.className === 'string' ? element.className.trim() : '',
                                hrefValue: element.getAttribute('href') || '',
                                titleValue: element.getAttribute('title') || '',
                                dateTimeValue: element.getAttribute('datetime') || '',
                                topPercent: top / docHeight * 100,
                                leftPercent: left / docWidth * 100,
                                widthPercent: rect.width / docWidth * 100,
                                heightPercent: rect.height / docHeight * 100,
                                area: rect.width * rect.height,
                                childCount
                              };
                            })
                            .filter(item => item.text.length >= 2)
                            .filter(item => item.widthPercent > 0.2 && item.heightPercent > 0.08)
                            .filter(item => ['h1','h2','h3','h4','p','span','a','time','div','article','section','li'].includes(item.tagName))
                            .filter(item => item.text.length <= 260)
                            .filter(item => item.area <= 400000)
                            .filter(item => item.tagName !== 'section' && item.tagName !== 'article')
                            .filter(item => item.tagName !== 'div' || (item.text.length >= 2 && item.text.length <= 200 && item.childCount <= 12))
                            .sort((a, b) => {
                              const score = (item) => {
                                const topBonus = Math.max(0, 120 - item.topPercent) * 100;
                                const lowerAreaBonus = item.topPercent > 35 ? 12000 : 0;
                                const tagBias =
                                  item.tagName === 'a' ? 120000 :
                                  item.tagName === 'li' ? 95000 :
                                  item.tagName === 'span' ? 90000 :
                                  item.tagName === 'time' ? 90000 :
                                  ['h1', 'h2', 'h3', 'h4'].includes(item.tagName) ? 85000 :
                                  item.tagName === 'p' ? 70000 :
                                  item.tagName === 'div' ? 12000 : 0;
                                const childPenalty = item.childCount * 3500;
                                const areaPenalty = item.area * 0.12;
                                return tagBias + topBonus + lowerAreaBonus - childPenalty - areaPenalty;
                              };
                              return a.topPercent - b.topPercent || score(b) - score(a);
                            })
                            .slice(0, 80);
                        }
                        """);

                List<Map<String, Object>> rawItems = (List<Map<String, Object>>) raw;
                List<SelectableElement> result = new ArrayList<>();
                for (Map<String, Object> item : rawItems) {
                    result.add(new SelectableElement(
                            ((Number) item.get("elementIndex")).intValue(),
                            asString(item.get("tagName")),
                            asString(item.get("text")),
                            asString(item.get("domPath")),
                            asString(item.get("elementIdValue")),
                            asString(item.get("classNames")),
                            asString(item.get("hrefValue")),
                            asString(item.get("titleValue")),
                            asString(item.get("dateTimeValue")),
                            asDouble(item.get("topPercent")),
                            asDouble(item.get("leftPercent")),
                            asDouble(item.get("widthPercent")),
                            asDouble(item.get("heightPercent"))
                    ));
                }
                return result.stream()
                        .sorted(Comparator.comparingDouble(SelectableElement::topPercent))
                        .limit(DEFAULT_ELEMENT_LIMIT)
                        .toList();
            }
        } catch (Exception ex) {
            throw new IllegalStateException("页面元素分析失败: " + ex.getMessage(), ex);
        }
    }

    public PreviewExtractionResult extractValue(String url,
                                                String fieldType,
                                                CrawlSelectorCandidate candidate) {
        try (Playwright playwright = Playwright.create()) {
            BrowserType browserType = resolveBrowser(playwright);
            Browser browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(properties.isHeadless()));
            try (browser) {
                Page page = browser.newPage();
                page.navigate(url, new Page.NavigateOptions().setTimeout((double) properties.getTimeoutMs()));
                String value = switch (candidate.getSelectorType()) {
                    case "css", "css_class", "dom_path", "attribute", "tag" ->
                            extractByCssLike(page, fieldType, candidate.getSelectorValue());
                    case "text" -> extractByText(page, fieldType, candidate.getSelectorValue());
                    default -> null;
                };

                if (value == null || value.isBlank()) {
                    return new PreviewExtractionResult(false, null, candidate, "未命中内容");
                }
                return new PreviewExtractionResult(true, value.trim(), candidate, null);
            }
        } catch (Exception ex) {
            return new PreviewExtractionResult(false, null, candidate, "抽取失败: " + ex.getMessage());
        }
    }

    public List<String> extractValues(String url,
                                      String fieldType,
                                      CrawlSelectorCandidate candidate) {
        try (Playwright playwright = Playwright.create()) {
            BrowserType browserType = resolveBrowser(playwright);
            Browser browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(properties.isHeadless()));
            try (browser) {
                Page page = browser.newPage();
                page.navigate(url, new Page.NavigateOptions().setTimeout((double) properties.getTimeoutMs()));
                return switch (candidate.getSelectorType()) {
                    case "css", "css_class", "dom_path", "attribute", "tag" ->
                            extractValuesByCssLike(page, fieldType, candidate.getSelectorValue());
                    case "text" -> extractValuesByText(page, fieldType, candidate.getSelectorValue());
                    default -> List.of();
                };
            }
        } catch (Exception ex) {
            throw new IllegalStateException("批量抽取失败: " + ex.getMessage(), ex);
        }
    }

    public String extractCanonicalUrl(String url) {
        try (Playwright playwright = Playwright.create()) {
            BrowserType browserType = resolveBrowser(playwright);
            Browser browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(properties.isHeadless()));
            try (browser) {
                Page page = browser.newPage();
                page.navigate(url, new Page.NavigateOptions().setTimeout((double) properties.getTimeoutMs()));
                String canonical = page.locator("link[rel='canonical']").first().getAttribute("href");
                if (canonical == null || canonical.isBlank()) {
                    canonical = page.locator("meta[property='og:url']").first().getAttribute("content");
                }
                if (canonical == null || canonical.isBlank()) {
                    return page.url();
                }
                return URI.create(page.url()).resolve(canonical).toString();
            }
        } catch (Exception ex) {
            return url;
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

    private String extractByCssLike(Page page, String fieldType, String selector) {
        var locator = page.locator(selector).first();
        if (locator.count() == 0) {
            return null;
        }
        return readFieldValue(locator, fieldType);
    }

    private String extractByText(Page page, String fieldType, String text) {
        var locator = page.getByText(text).first();
        if (locator.count() == 0) {
            return null;
        }
        return readFieldValue(locator, fieldType);
    }

    private List<String> extractValuesByCssLike(Page page, String fieldType, String selector) {
        var locator = page.locator(selector);
        int count = locator.count();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String value = readFieldValue(locator.nth(i), fieldType);
            if (value != null && !value.isBlank()) {
                values.add(value.trim());
            }
        }
        return values;
    }

    private List<String> extractValuesByText(Page page, String fieldType, String text) {
        var locator = page.getByText(text);
        int count = locator.count();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String value = readFieldValue(locator.nth(i), fieldType);
            if (value != null && !value.isBlank()) {
                values.add(value.trim());
            }
        }
        return values;
    }

    private String readFieldValue(com.microsoft.playwright.Locator locator, String fieldType) {
        return switch (fieldType) {
            case "HTML" -> locator.innerHTML();
            case "URL" -> firstNonBlank(locator.getAttribute("href"), locator.getAttribute("src"), locator.innerText());
            case "DATETIME" -> firstNonBlank(locator.getAttribute("datetime"), locator.innerText());
            default -> locator.innerText();
        };
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String asString(Object value) {
        return value == null ? "" : value.toString();
    }

    private double asDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0D;
    }
}
