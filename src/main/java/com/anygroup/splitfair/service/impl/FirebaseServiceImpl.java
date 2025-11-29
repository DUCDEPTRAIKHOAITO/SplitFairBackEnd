package com.anygroup.splitfair.service.impl;

import com.anygroup.splitfair.config.MessageConfig;
import com.anygroup.splitfair.dto.NotificationMessage;
import com.anygroup.splitfair.service.FirebaseService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseServiceImpl implements FirebaseService {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public String sendMessage(NotificationMessage notificationMessage) {

        Notification notification = Notification
                .builder()
                .setTitle(notificationMessage.getTitle())
                .setBody(notificationMessage.getBody())
                .setImage(notificationMessage.getImage())
                .build();

        Message message = Message.builder()
                .setTopic("test")
                .setNotification(notification)
                .putAllData(notificationMessage.getData())
                .build();

        try {
            String response = firebaseMessaging.send(message, true);
            System.out.println("FCM Response: " + response);
            return "Success Sending Notification";
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return "Error Sending Notification";
        }
        catch (Exception e) {
            e.printStackTrace();
            return "Error Sending Notification";
        }
    }
}
