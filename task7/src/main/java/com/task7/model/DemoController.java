package com.task7.model;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DemoController {

    @PostMapping("/api/demo-action")
    @ResponseBody
    public String demoAction(String testData, String manualData) {
        return "CSRF protected action executed successfully! Data: " +
                (testData != null ? testData : manualData);
    }
}