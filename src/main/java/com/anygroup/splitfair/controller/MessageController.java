package com.anygroup.splitfair.controller;

import com.anygroup.splitfair.dto.NotificationMessage;
import com.anygroup.splitfair.service.FirebaseService;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/noti")
@RequiredArgsConstructor
public class MessageController {
    private final FirebaseService firebaseService;

    @PostMapping
    public String sendMessage(@RequestBody NotificationMessage message) throws FirebaseMessagingException {
        return firebaseService.sendMessage(message);
    }
}
