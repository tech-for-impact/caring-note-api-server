package com.springboot.api.common.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.sentry.Sentry;

@RestController
@RequestMapping("/v1/test/sentry")
@Profile("staging")
public class SentryTestController {

    @GetMapping("/error")
    public ResponseEntity<String> triggerError() {
        throw new RuntimeException("Sentry 테스트용 500 에러");
    }

    @GetMapping("/message")
    public ResponseEntity<String> captureMessage() {
        Sentry.captureMessage("Sentry 테스트 메시지 - staging 환경");
        return ResponseEntity.ok("Sentry 메시지 전송 완료");
    }
}
