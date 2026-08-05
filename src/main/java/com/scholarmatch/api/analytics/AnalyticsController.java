package com.scholarmatch.api.analytics;

import com.scholarmatch.api.analytics.dto.AnalyticsOverviewResponse;
import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.common.Role;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@PreAuthorize("hasAnyRole('PROVIDER','ADMIN')")
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  public AnalyticsController(AnalyticsService analyticsService) {
    this.analyticsService = analyticsService;
  }

  @GetMapping("/overview")
  public AnalyticsOverviewResponse overview(@AuthenticationPrincipal AuthPrincipal principal) {
    return analyticsService.overview(principal.id(), principal.hasRole(Role.ADMIN));
  }
}
