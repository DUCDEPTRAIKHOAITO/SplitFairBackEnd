package com.anygroup.splitfair.service;

import com.anygroup.splitfair.dto.NotificationMessage;
import org.springframework.stereotype.Service;

@Service
public interface FirebaseService {
    String sendMessage(NotificationMessage notificationMessage);
}
