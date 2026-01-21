package com.example.spring_spa.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
  @GetMapping("app1")
  String app1() {
    return "app1/index";
  }

  @GetMapping("app1/{path}")
  String app1Paths() {
    return "forward:/app1";
  }

  @GetMapping
  String defaultGet() {
    return "redirect:/app1";
  }

  @GetMapping("app2")
  String app2() {
    return "app2/index";
  }

  @GetMapping("app2/{path}")
  String app2Paths() {
    return "forward:/app2";
  }
}
