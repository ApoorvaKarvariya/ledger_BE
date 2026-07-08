package com.nv.task1.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes the HR FAQ chatbot to any authenticated user (admin, manager or
 * employee). Backed by a locally running Ollama model -- see ChatService.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody ChatDTO.ChatRequest request) {
        String reply = chatService.ask(request.getMessage());
        return ResponseEntity.ok(new ChatDTO.ChatResponse(reply));
    }
}
