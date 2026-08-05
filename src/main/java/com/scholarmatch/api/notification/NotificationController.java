package com.scholarmatch.api.notification;

import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.notification.dto.NotificationResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping
  public List<NotificationResponse> list(@AuthenticationPrincipal AuthPrincipal principal) {
    return notificationService.list(principal.id());
  }

  @GetMapping("/unread-count")
  public Map<String, Integer> unreadCount(@AuthenticationPrincipal AuthPrincipal principal) {
    return Map.of("count", notificationService.unreadCount(principal.id()));
  }

  @PostMapping("/{id}/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markRead(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
    notificationService.markRead(id, principal.id());
  }

  @PostMapping("/read-all")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markAllRead(@AuthenticationPrincipal AuthPrincipal principal) {
    notificationService.markAllRead(principal.id());
  }
}
