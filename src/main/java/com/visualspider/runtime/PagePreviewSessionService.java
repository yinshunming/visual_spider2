package com.visualspider.runtime;

import com.visualspider.admin.PreviewSessionView;
import com.visualspider.persistence.PagePreviewSession;
import com.visualspider.persistence.PagePreviewSessionMapper;
import org.springframework.stereotype.Service;

@Service
public class PagePreviewSessionService {

    private final PlaywrightService playwrightService;
    private final PagePreviewSessionMapper pagePreviewSessionMapper;

    public PagePreviewSessionService(PlaywrightService playwrightService,
                                     PagePreviewSessionMapper pagePreviewSessionMapper) {
        this.playwrightService = playwrightService;
        this.pagePreviewSessionMapper = pagePreviewSessionMapper;
    }

    public PreviewSessionView createPreview(String url) {
        PagePreviewSession session = new PagePreviewSession();
        session.setRequestedUrl(url);

        try {
            PagePreviewResult result = playwrightService.capturePagePreview(url);
            session.setFinalUrl(result.finalUrl());
            session.setPageTitle(result.title());
            session.setLoadDurationMs(result.durationMs());
            session.setScreenshotPath(result.screenshotPath());
            session.setStatus("SUCCESS");
        } catch (Exception ex) {
            session.setStatus("FAILED");
            session.setErrorMessage(ex.getMessage());
        }

        pagePreviewSessionMapper.insert(session);
        return toView(session);
    }

    public PagePreviewSession getSession(Long id) {
        return pagePreviewSessionMapper.findById(id);
    }

    private PreviewSessionView toView(PagePreviewSession session) {
        String screenshotUrl = session.getScreenshotPath() == null || session.getId() == null
                ? null
                : "/admin/preview-sessions/" + session.getId() + "/screenshot";

        return new PreviewSessionView(
                session.getId(),
                session.getRequestedUrl(),
                session.getFinalUrl(),
                session.getPageTitle(),
                session.getLoadDurationMs(),
                session.getStatus(),
                session.getErrorMessage(),
                screenshotUrl
        );
    }
}

