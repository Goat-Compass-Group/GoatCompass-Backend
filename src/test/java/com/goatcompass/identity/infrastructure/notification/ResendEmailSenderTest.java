package com.goatcompass.identity.infrastructure.notification;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.goatcompass.shared.application.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ResendEmailSenderTest {
    private MockRestServiceServer server;
    private ResendEmailSender sender;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        sender = new ResendEmailSender(builder, "https://api.resend.com", "re_test_key",
                "GoatCompass <onboarding@resend.dev>");
    }

    @Test
    void postsEmailToResendApi() {
        server.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer re_test_key"))
                .andExpect(jsonPath("$.from").value("GoatCompass <onboarding@resend.dev>"))
                .andExpect(jsonPath("$.to[0]").value("user@example.com"))
                .andExpect(jsonPath("$.subject").value("Mã xác thực GoatCompass: 123456"))
                .andExpect(jsonPath("$.html").value("<p>123456</p>"))
                .andRespond(withSuccess("{\"id\":\"49a3999c-0ce1-4ea6-ab68-afcd6dc2e794\"}", MediaType.APPLICATION_JSON));

        sender.send("user@example.com", "Mã xác thực GoatCompass: 123456", "<p>123456</p>");
        server.verify();
    }

    @Test
    void rejectedEmailBecomesExternalServiceException() {
        server.expect(requestTo("https://api.resend.com/emails"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"statusCode\":403,\"name\":\"validation_error\",\"message\":\"You can only send testing emails to your own email address\"}"));

        assertThatThrownBy(() -> sender.send("other@example.com", "x", "<p>x</p>"))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("403");
    }
}
