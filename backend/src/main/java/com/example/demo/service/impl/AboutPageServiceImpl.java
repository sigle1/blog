package com.example.demo.service.impl;

import com.example.demo.dto.AboutPageDto;
import com.example.demo.entity.AboutPageSettings;
import com.example.demo.mapper.AboutPageMapper;
import com.example.demo.service.AboutPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AboutPageServiceImpl implements AboutPageService {

    private static final int INTRO_MAX_LEN = 8000;

    private static final String DEFAULT_PUBLIC_INTRO =
            "你好，欢迎来访我的博客。\n\n"
                    + "这里是我的个人站点，我会记录技术笔记、生活随笔与爱好相关的内容。你可以在后台「关于页」随时修改这段自我介绍。\n\n"
                    + "若你愿意交流，欢迎留言。感谢你的阅读。";

    @Autowired
    private AboutPageMapper aboutPageMapper;

    @Override
    public AboutPageDto getPublic() {
        AboutPageDto out = new AboutPageDto();
        AboutPageSettings row = loadRowOrNull();
        String raw = row != null ? row.getIntro() : null;
        if (raw == null || raw.isBlank()) {
            out.setIntro(DEFAULT_PUBLIC_INTRO);
        } else {
            out.setIntro(raw);
        }
        return out;
    }

    @Override
    public AboutPageDto getAdmin() {
        AboutPageDto out = new AboutPageDto();
        AboutPageSettings row = loadRowOrNull();
        out.setIntro(row != null && row.getIntro() != null ? row.getIntro() : "");
        return out;
    }

    @Override
    public void update(AboutPageDto dto) {
        String intro = dto.getIntro() != null ? dto.getIntro() : "";
        if (intro.length() > INTRO_MAX_LEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "自我介绍过长（最多 " + INTRO_MAX_LEN + " 字）");
        }
        AboutPageSettings row = new AboutPageSettings();
        row.setId(1);
        row.setIntro(intro.isBlank() ? null : intro);
        aboutPageMapper.upsert(row);
    }

    private AboutPageSettings loadRowOrNull() {
        try {
            return aboutPageMapper.selectSingleton();
        } catch (Exception e) {
            return null;
        }
    }
}
