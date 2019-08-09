package com.mitchtalmadge.uofu_cs_bot.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingRestController {

  @RequestMapping("/ping")
  public String onPing() {
    return "pong";
  }
}
