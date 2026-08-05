package com.scholarmatch.api.analytics.dto;

import java.util.Map;

public record ApplicationCounts(long total, Map<String, Long> byStatus, Double awardRate) {
}
