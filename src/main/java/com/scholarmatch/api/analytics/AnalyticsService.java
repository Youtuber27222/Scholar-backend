package com.scholarmatch.api.analytics;

import com.scholarmatch.api.analytics.dto.AnalyticsOverviewResponse;
import com.scholarmatch.api.analytics.dto.ApplicationCounts;
import com.scholarmatch.api.analytics.dto.PerScholarshipStat;
import com.scholarmatch.api.analytics.dto.ScholarshipCounts;
import com.scholarmatch.api.application.ApplicationRepository;
import com.scholarmatch.api.application.ApplicationStatus;
import com.scholarmatch.api.scholarship.Scholarship;
import com.scholarmatch.api.scholarship.ScholarshipRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

  private final ScholarshipRepository scholarshipRepository;
  private final ApplicationRepository applicationRepository;

  public AnalyticsService(ScholarshipRepository scholarshipRepository, ApplicationRepository applicationRepository) {
    this.scholarshipRepository = scholarshipRepository;
    this.applicationRepository = applicationRepository;
  }

  public AnalyticsOverviewResponse overview(UUID providerId, boolean isAdmin) {
    List<Scholarship> scope = isAdmin ? scholarshipRepository.findAll() : scholarshipRepository.findByProviderId(providerId);
    Map<UUID, String> titlesById = new LinkedHashMap<>();
    for (Scholarship scholarship : scope) {
      titlesById.put(scholarship.getId(), scholarship.getTitle());
    }

    ScholarshipCounts scholarshipCounts = new ScholarshipCounts(
        scope.size(),
        scope.stream().filter(Scholarship::isActive).count(),
        scope.stream().filter(Scholarship::isFeatured).count());

    List<Object[]> statusRows = isAdmin
        ? applicationRepository.countByStatusGlobal()
        : applicationRepository.countByStatusForProvider(providerId);
    Map<String, Long> byStatus = new LinkedHashMap<>();
    long totalApplications = 0;
    long awarded = 0;
    long rejected = 0;
    for (Object[] row : statusRows) {
      ApplicationStatus status = (ApplicationStatus) row[0];
      long count = (Long) row[1];
      byStatus.put(status.getValue(), count);
      totalApplications += count;
      if (status == ApplicationStatus.AWARDED) awarded = count;
      if (status == ApplicationStatus.REJECTED) rejected = count;
    }
    long decided = awarded + rejected;
    Double awardRate = decided == 0 ? null : (double) awarded / decided;
    ApplicationCounts applicationCounts = new ApplicationCounts(totalApplications, byStatus, awardRate);

    List<Object[]> perScholarshipRows = isAdmin
        ? applicationRepository.perScholarshipCountsGlobal(ApplicationStatus.AWARDED)
        : applicationRepository.perScholarshipCountsForProvider(providerId, ApplicationStatus.AWARDED);

    // GROUP BY only returns scholarships with at least one application; fill in the
    // rest of the scope at zero so a provider can also see what's getting no interest.
    Map<UUID, PerScholarshipStat> byScholarshipId = new LinkedHashMap<>();
    for (Map.Entry<UUID, String> entry : titlesById.entrySet()) {
      byScholarshipId.put(entry.getKey(), new PerScholarshipStat(entry.getKey(), entry.getValue(), 0, 0));
    }
    for (Object[] row : perScholarshipRows) {
      UUID scholarshipId = (UUID) row[0];
      long applicationCount = (Long) row[1];
      long awardedCount = row[2] == null ? 0 : (Long) row[2];
      String title = titlesById.getOrDefault(scholarshipId, "Unknown scholarship");
      byScholarshipId.put(scholarshipId, new PerScholarshipStat(scholarshipId, title, applicationCount, awardedCount));
    }
    List<PerScholarshipStat> perScholarship = List.copyOf(byScholarshipId.values());

    return new AnalyticsOverviewResponse(scholarshipCounts, applicationCounts, perScholarship);
  }
}
