package com.scholarmatch.api.message;

import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.message.dto.MessageCreateRequest;
import com.scholarmatch.api.message.dto.MessageResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/applications/{applicationId}/messages")
public class MessageController {

  private final MessageService messageService;

  public MessageController(MessageService messageService) {
    this.messageService = messageService;
  }

  @GetMapping
  public List<MessageResponse> list(
      @AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID applicationId) {
    return messageService.listMessages(principal, applicationId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MessageResponse send(
      @AuthenticationPrincipal AuthPrincipal principal,
      @PathVariable UUID applicationId,
      @Valid @RequestBody MessageCreateRequest request) {
    return messageService.send(principal, applicationId, request);
  }

  @PostMapping("/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markRead(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID applicationId) {
    messageService.markRead(principal, applicationId);
  }
}
