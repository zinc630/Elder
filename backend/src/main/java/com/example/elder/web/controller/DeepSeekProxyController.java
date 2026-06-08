package com.example.elder.web.controller;

import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * 将首页智慧助手的对话请求转发到 DeepSeek，密钥只留在服务端（环境变量或配置）。
 */
@RestController
@RequestMapping("/api")
public class DeepSeekProxyController {

    private static final RestClient CLIENT =
            RestClient.builder().baseUrl("https://api.deepseek.com").build();

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @PostMapping(
            value = "/deepseek/v1/chat/completions",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> chatCompletions(@RequestBody String body) {
        if (apiKey == null || apiKey.isBlank()) {
            String json =
                    "{\"error\":{\"message\":\"未配置 DeepSeek 密钥：在 backend 目录复制 local-deepseek.yml.example 为 local-deepseek.yml 并填写 deepseek.api-key，或设置环境变量 DEEPSEEK_API_KEY，然后重启后端（须在 backend 目录启动以保证能读到该文件）。\"}}";
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        }

        try {
            String upstreamBody = CLIENT.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(upstreamBody == null ? "" : upstreamBody);
        } catch (RestClientResponseException ex) {
            String errBody = ex.getResponseBodyAsString(StandardCharsets.UTF_8);
            if (errBody != null && !errBody.isBlank()) {
                return ResponseEntity.status(ex.getStatusCode())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errBody);
            }
            String fallback =
                    "{\"error\":{\"message\":\"DeepSeek 返回错误 HTTP "
                            + ex.getStatusCode().value()
                            + "\"}}";
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(fallback);
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage().replace("\"", "'") : "unknown";
            String json = "{\"error\":{\"message\":\"调用 DeepSeek 失败: " + msg + "\"}}";
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        }
    }
}
