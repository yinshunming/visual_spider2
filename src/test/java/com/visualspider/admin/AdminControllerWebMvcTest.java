package com.visualspider.admin;

import com.visualspider.persistence.DatabaseProbeMapper;
import com.visualspider.persistence.PagePreviewSessionMapper;
import com.visualspider.runtime.PagePreviewSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminController.class)
class AdminControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PagePreviewSessionService pagePreviewSessionService;

    @MockBean
    private DatabaseProbeMapper databaseProbeMapper;

    @MockBean
    private PagePreviewSessionMapper pagePreviewSessionMapper;

    @Test
    void shouldRenderAdminPage() throws Exception {
        given(databaseProbeMapper.selectOne()).willReturn(1);

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"))
                .andExpect(model().attributeExists("probeForm"));
    }

    @Test
    void shouldShowPreviewSessionWhenProbeSucceeds() throws Exception {
        given(databaseProbeMapper.selectOne()).willReturn(1);
        given(pagePreviewSessionService.createPreview(eq("https://www.sina.com.cn"))).willReturn(
                new PreviewSessionView(
                        12L,
                        "https://www.sina.com.cn",
                        "https://www.sina.com.cn/",
                        "新浪首页",
                        200L,
                        "SUCCESS",
                        null,
                        "/admin/preview-sessions/12/screenshot"
                )
        );

        mockMvc.perform(post("/admin/playwright-demo")
                        .param("url", "https://www.sina.com.cn"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"))
                .andExpect(model().attributeExists("previewSession"))
                .andExpect(model().attribute("probeError", (Object) null));
    }

    @Test
    void shouldShowValidationErrorForBlankUrl() throws Exception {
        given(databaseProbeMapper.selectOne()).willReturn(1);

        mockMvc.perform(post("/admin/playwright-demo")
                        .param("url", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"))
                .andExpect(model().attributeExists("validationError"));
    }

    @Test
    void shouldReturn404WhenScreenshotIsMissing() throws Exception {
        given(pagePreviewSessionService.getSession(anyLong())).willReturn(null);

        mockMvc.perform(get("/admin/preview-sessions/99/screenshot"))
                .andExpect(status().isNotFound());
    }
}
