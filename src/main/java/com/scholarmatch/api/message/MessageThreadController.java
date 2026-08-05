package com.scholarmatch.api.message;

import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.message.dto.ThreadSummaryResponse;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
public class MessageThreadController {

  private final MessageService messageService;

  public MessageThreadController(MessageService messageService) {
    this.messageService = messageService;
  }

  @GetMapping("/threads")
  public List<ThreadSummaryResponse> threads(@AuthenticationPrincipal AuthPrincipal principal) {
    return messageService.listThreads(principal);
  }

  @GetMapping("/unread-count")
  public Map<String, Integer> unreadCount(@AuthenticationPrincipal AuthPrincipal principal) {
    return Map.of("count", messageService.unreadCount(principal.id()));
  }
}
