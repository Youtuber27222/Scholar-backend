package com.scholarmatch.api.analytics.dto;

import java.util.List;

public record AnalyticsOverviewResponse(
    ScholarshipCounts scholarships, ApplicationCounts applications, List<PerScholarshipStat> perScholarship) {
}
