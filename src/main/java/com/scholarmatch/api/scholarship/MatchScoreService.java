package com.scholarmatch.api.scholarship;

import com.scholarmatch.api.profile.StudentProfile;
import java.math.BigDecimal;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Scores how well a scholarship fits a student's profile (0-100), weighted across
 * GPA proximity, field-of-study match, nationality/region match, and financial need.
 * Normalized by the weight actually considered so an incomplete profile isn't
 * unfairly penalized for fields it hasn't filled in yet — only genuinely
 * comparable fields count toward the score.
 */
@Service
public class MatchScoreService {

  private static final int GPA_WEIGHT = 35;
  private static final int FIELD_WEIGHT = 30;
  private static final int NATIONALITY_WEIGHT = 20;
  private static final int FINANCIAL_NEED_WEIGHT = 15;

  // Scholarships open to a broad region/category still partially fit a student
  // outside that exact nationality (e.g. a Ghanaian applying to an "African" award).
  private static final Set<String> BROAD_ELIGIBILITY_CATEGORIES = Set.of("african", "commonwealth", "any");
  private static final double BROAD_ELIGIBILITY_CREDIT = 0.6;

  public Integer score(StudentProfile profile, Scholarship scholarship) {
    double earned = 0;
    double totalWeight = 0;

    if (scholarship.getMinGpa() != null && profile.getGpa() != null) {
      totalWeight += GPA_WEIGHT;
      earned += GPA_WEIGHT * gpaCredit(profile.getGpa(), scholarship.getMinGpa());
    }

    if (scholarship.getFieldOfStudy() != null && profile.getFieldOfStudy() != null) {
      totalWeight += FIELD_WEIGHT;
      if ("any".equalsIgnoreCase(scholarship.getFieldOfStudy())
          || scholarship.getFieldOfStudy().equalsIgnoreCase(profile.getFieldOfStudy())) {
        earned += FIELD_WEIGHT;
      }
    }

    if (scholarship.getNationality() != null && profile.getNationality() != null) {
      totalWeight += NATIONALITY_WEIGHT;
      if (scholarship.getNationality().equalsIgnoreCase(profile.getNationality())) {
        earned += NATIONALITY_WEIGHT;
      } else if (BROAD_ELIGIBILITY_CATEGORIES.contains(scholarship.getNationality().toLowerCase())) {
        earned += NATIONALITY_WEIGHT * BROAD_ELIGIBILITY_CREDIT;
      }
    }

    totalWeight += FINANCIAL_NEED_WEIGHT;
    if (!scholarship.isFinancialNeed() || profile.isFinancialNeed()) {
      earned += FINANCIAL_NEED_WEIGHT;
    }

    if (totalWeight == 0) {
      return null;
    }
    return (int) Math.round(100 * earned / totalWeight);
  }

  /** Full credit at or above the requirement, tapering to zero a full GPA point below it. */
  private double gpaCredit(BigDecimal studentGpa, BigDecimal minGpa) {
    BigDecimal gap = minGpa.subtract(studentGpa);
    if (gap.signum() <= 0) {
      return 1.0;
    }
    double gapValue = gap.doubleValue();
    return Math.max(0, 1 - gapValue);
  }
}
