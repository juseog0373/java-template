package com.nexacode.template.infrastructure.client;

import com.google.firebase.messaging.*;
import com.nexacode.template.infrastructure.client.exception.FcmClientException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class FcmClient {

    public void sendMessage(Request request) {
        var message = createMessage(request);

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            var errorResponse = e.getHttpResponse();
            log.error("fail to send fcm message", e);

            throw new FcmClientException(errorResponse.getStatusCode(), e.getMessage(), e.getMessagingErrorCode());
        }

    }

    private Message createMessage(Request request) {
        var notification = Notification.builder()
            .setTitle(request.getTitle())
            .setBody(request.getContent())
            .build();

        return Message.builder()
            .setNotification(notification)
            .setToken(request.getToken())
            .putData("deepLink", request.getDeepLink())
            .build();
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String token;
        private String title;
        private String content;
        private String deepLink;
    }
}