package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties(prefix = "app.qq")
public class QqOAuthProperties {

    private boolean enabled = false;
    private String appId = "";
    private String appKey = "";
    /** 与 QQ 互联后台登记的回调地址完全一致 */
    private String redirectUri = "http://localhost:8080/auth/qq/callback";
    /** 登录成功后浏览器跳转的前端地址（不含 query） */
    private String frontendSuccessUrl = "http://localhost:5173/admin/login";
    private String frontendErrorUrl = "http://localhost:5173/admin/login";
    /** 允许登录管理后台的 QQ openid，逗号分隔；留空则拒绝（见日志提示如何配置） */
    private String adminOpenids = "";

    public boolean isEnabled() {
        return enabled && appId != null && !appId.isBlank() && appKey != null && !appKey.isBlank();
    }

    public Set<String> allowedAdminOpenidSet() {
        if (adminOpenids == null || adminOpenids.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(adminOpenids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public boolean isEnabledFlag() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getFrontendSuccessUrl() {
        return frontendSuccessUrl;
    }

    public void setFrontendSuccessUrl(String frontendSuccessUrl) {
        this.frontendSuccessUrl = frontendSuccessUrl;
    }

    public String getFrontendErrorUrl() {
        return frontendErrorUrl;
    }

    public void setFrontendErrorUrl(String frontendErrorUrl) {
        this.frontendErrorUrl = frontendErrorUrl;
    }

    public String getAdminOpenids() {
        return adminOpenids;
    }

    public void setAdminOpenids(String adminOpenids) {
        this.adminOpenids = adminOpenids;
    }
}
