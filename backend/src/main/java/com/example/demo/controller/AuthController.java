package com.example.demo.controller;

import com.example.demo.config.QqOAuthProperties;
import com.example.demo.entity.User;
import com.example.demo.service.QqAuthService;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private UserService userService;

    @Resource
    private JwtUtil jwtUtil;

    @Autowired
    private QqAuthService qqAuthService;

    @Autowired
    private QqOAuthProperties qqOAuthProperties;

    @GetMapping("/qq/config")
    public Map<String, Object> qqConfig() {
        Map<String, Object> body = new HashMap<>();
        body.put("enabled", qqOAuthProperties.isEnabled());
        return body;
    }

    /** 跳转 QQ 授权页 */
    @GetMapping("/qq")
    public RedirectView qqAuthorize() {
        String url = qqAuthService.buildAuthorizeUrl();
        RedirectView view = new RedirectView(url);
        view.setContextRelative(false);
        return view;
    }

    /** QQ 互联回调（地址须与后台登记一致） */
    @GetMapping("/qq/callback")
    public RedirectView qqCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state
    ) {
        String target = qqAuthService.handleCallback(code, state);
        RedirectView view = new RedirectView(target);
        view.setContextRelative(false);
        return view;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        User user = userService.findByUsername(username);
        Map<String, Object> response = new HashMap<>();

        if (user != null && userService.validatePassword(user, password)) {
            // 生成JWT令牌
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            response.put("success", true);
            response.put("token", token);
            response.put("user", user);
        } else {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
        }

        return response;
    }
}
