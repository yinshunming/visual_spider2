package com.visualspider.admin;

import jakarta.validation.constraints.NotBlank;

public class PageProbeForm {

    @NotBlank(message = "URL 不能为空")
    private String url = "https://www.sina.com.cn";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
