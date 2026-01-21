package com.example.spring_spa.api;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("resources")
public class ResourceController {
  @GetMapping("me")
  Map<String, Object> me(Authentication authentication) {
    return Map.of(
        "username", authentication.getName(),
        "authorities", authentication.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .toList()
    );
  }

  @PostMapping("books")
  Map<String, String> addBook(@RequestBody Map<String, String> book) {
    return book;
  }
}
