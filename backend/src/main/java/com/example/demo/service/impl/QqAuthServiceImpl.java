package com.example.demo.service.impl;

import com.example.demo.config.QqOAuthProperties;
import com.example.demo.entity.User;
import com.example.demo.service.QqAuthService;
import com.example.demo.service.UserService;
import com.example.demo.support.QqOAuthStateStore;
import com.example.demo.utils.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
@Service
public class QqAuthServiceImpl implements QqAuthService {

    private static final Logger log = LoggerFactory.getLogger(QqAuthServiceImpl.class);

    private static final String AUTHORIZE_URL = "https://graph.qq.com/oauth2.0/authorize";
    private static final String TOKEN_URL = "https://graph.qq.com/oauth2.0/token";
    private static final String OPENID_URL = "https://graph.qq.com/oauth2.0/me";
    private static final String USER_INFO_URL = "https://graph.qq.com/user/get_user_info";

    @Autowired
    private QqOAuthProperties qqProps;

    @Autowired
    private QqOAuthStateStore stateStore;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public String buildAuthorizeUrl() {
        if (!qqProps.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "QQ 登录未配置");
        }
        String state = stateStore.createState();
        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", qqProps.getAppId())
                .queryParam("redirect_uri", qqProps.getRedirectUri())
                .queryParam("state", state)
                .queryParam("scope", "get_user_info")
                .build()
                .encode()
                .toUriString();
    }

    @Override
    public String handleCallback(String code, String state) {
        if (!qqProps.isEnabled()) {
            return errorRedirect("qq_disabled", "QQ 登录未启用");
        }
        if (code == null || code.isBlank()) {
            return errorRedirect("qq_no_code", "未收到授权码");
        }
        if (!stateStore.consumeState(state)) {
            return errorRedirect("qq_bad_state", "state 无效或已过期，请重新登录");
        }

        try {
            String accessToken = exchangeAccessToken(code.trim());
            String openid = fetchOpenId(accessToken);
            String nickname = fetchNickname(accessToken, openid);

            if (!qqProps.allowedAdminOpenidSet().contains(openid)) {
                log.warn("QQ 登录被拒绝：openid={} 不在 app.qq.admin-openids 中，请将该 openid 写入配置后重启后端", openid);
                return errorRedirect("qq_unauthorized", "该 QQ 未授权登录管理后台");
            }

            User user = userService.findOrCreateByQqOpenid(openid, nickname);
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            return successRedirect(token);
        } catch (ResponseStatusException e) {
            log.warn("QQ 登录失败: {}", e.getReason());
            return errorRedirect("qq_failed", e.getReason() != null ? e.getReason() : "QQ 登录失败");
        } catch (Exception e) {
            log.error("QQ 登录异常", e);
            return errorRedirect("qq_failed", "QQ 登录失败，请稍后重试");
        }
    }

    private String exchangeAccessToken(String code) throws Exception {
        String query = "grant_type=authorization_code"
                + "&client_id=" + urlEncode(qqProps.getAppId())
                + "&client_secret=" + urlEncode(qqProps.getAppKey())
                + "&code=" + urlEncode(code)
                + "&redirect_uri=" + urlEncode(qqProps.getRedirectUri())
                + "&fmt=json";
        JsonNode json = getJson(TOKEN_URL + "?" + query);
        if (json.has("error")) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    json.path("error_description").asText("换取 access_token 失败"));
        }
        String token = json.path("access_token").asText(null);
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "QQ 未返回 access_token");
        }
        return token;
    }

    private String fetchOpenId(String accessToken) throws Exception {
        String url = OPENID_URL + "?access_token=" + urlEncode(accessToken) + "&fmt=json";
        String body = getRaw(url);
        JsonNode json = parseQqWrappedJson(body);
        String openid = json.path("openid").asText(null);
        if (openid == null || openid.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "获取 openid 失败");
        }
        return openid;
    }

    private String fetchNickname(String accessToken, String openid) throws Exception {
        String url = USER_INFO_URL
                + "?access_token=" + urlEncode(accessToken)
                + "&oauth_consumer_key=" + urlEncode(qqProps.getAppId())
                + "&openid=" + urlEncode(openid);
        JsonNode json = getJson(url);
        if (json.path("ret").asInt(-1) != 0) {
            return "QQ用户";
        }
        String nickname = json.path("nickname").asText("");
        if (nickname == null || nickname.isBlank()) {
            return "QQ用户";
        }
        return nickname.trim();
    }

    private JsonNode getJson(String url) throws Exception {
        return parseQqWrappedJson(getRaw(url));
    }

    private String getRaw(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 400) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "QQ 接口 HTTP " + response.statusCode());
        }
        return response.body();
    }

    private JsonNode parseQqWrappedJson(String body) throws Exception {
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "QQ 返回为空");
        }
        String trimmed = body.trim();
        if (trimmed.startsWith("callback(") && trimmed.endsWith(");")) {
            trimmed = trimmed.substring("callback(".length(), trimmed.length() - 2);
        }
        return objectMapper.readTree(trimmed);
    }

    private String successRedirect(String token) {
        return UriComponentsBuilder.fromUriString(qqProps.getFrontendSuccessUrl())
                .queryParam("token", token)
                .queryParam("login", "qq")
                .build()
                .encode()
                .toUriString();
    }

    private String errorRedirect(String code, String message) {
        return UriComponentsBuilder.fromUriString(qqProps.getFrontendErrorUrl())
                .queryParam("error", code)
                .queryParam("message", message)
                .build()
                .encode()
                .toUriString();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
