package com.unifor.br.leakwatch.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/api")
    public String hello() {
        return "API tá tudo ok Paizão! Pode usar";
    }
}
