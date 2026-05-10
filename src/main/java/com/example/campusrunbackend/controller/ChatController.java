package com.example.campusrunbackend.controller;

import com.example.campusrunbackend.model.ChatMessage;
import com.example.campusrunbackend.model.User;
import com.example.campusrunbackend.repository.ChatMessageRepository;
import com.example.campusrunbackend.repository.UserRepository;
import com.example.campusrunbackend.service.CustomUserDetailsService;
import com.example.campusrunbackend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatController(ChatMessageRepository messageRepository,
                          UserRepository userRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // REST: Get History
    @GetMapping("/history/{otherUserId}")
    public ResponseEntity<Map<String, Object>> getChatHistory(@PathVariable Long otherUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByIndexNumber(auth.getName())
                .orElseThrow(() -> new java.util.NoSuchElementException("Current user not found: " + auth.getName()));

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Other user not found: " + otherUserId));

        List<ChatMessage> history = messageRepository.findConversation(currentUser.getId(), otherUser.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "History loaded");
        response.put("data", history);

        return ResponseEntity.ok(response);
    }

    // STOMP: Send Message
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());

        // Persist to DB
        ChatMessage saved = messageRepository.save(message);

        // Send to personal queue of receiver
        // Path: /user/{receiverId}/queue/messages
        messagingTemplate.convertAndSendToUser(
                String.valueOf(message.getReceiverId()),
                "/queue/messages",
                saved
        );
    }
}
