package com.goatcompass.identity.infrastructure.notification;

import com.goatcompass.shared.application.exception.ExternalServiceException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
class ResendEmailSender {
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);
    private static final String SEND_FAILED = "Không gửi được email, vui lòng thử lại sau.";

    private final RestClient restClient;
    private final String from;

    @Autowired
    ResendEmailSender(
            @Value("${app.mail.resend.base-url}") String baseUrl,
            @Value("${app.mail.resend.api-key}") String apiKey,
            @Value("${app.mail.from}") String from
    ) {
        this(RestClient.builder().requestFactory(requestFactory()), baseUrl, apiKey, from);
    }

    ResendEmailSender(RestClient.Builder builder, String baseUrl, String apiKey, String from) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
        this.from = from;
    }

    void send(String to, String subject, String html) {
        try {
            restClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ResendEmail(from, List.of(to), subject, html))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.error("Resend từ chối email ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalServiceException(SEND_FAILED, "Resend trả lỗi " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            throw new ExternalServiceException(SEND_FAILED, "Không gọi được Resend: " + e.getMessage(), e);
        }
    }

    private static JdkClientHttpRequestFactory requestFactory() {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
        factory.setReadTimeout(READ_TIMEOUT);
        return factory;
    }

    record ResendEmail(String from, List<String> to, String subject, String html) {
    }
}