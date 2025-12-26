package com.wsd.blogapi.spa;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    /**
     * SPA(Single Page Application) 루트 경로 처리
     * React 앱의 index.html을 반환
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}
