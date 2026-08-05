package com.scholarmatch.api.analytics.dto;

import java.util.UUID;

public record PerScholarshipStat(UUID scholarshipId, String title, long applicationCount, long awardedCount) {
}
