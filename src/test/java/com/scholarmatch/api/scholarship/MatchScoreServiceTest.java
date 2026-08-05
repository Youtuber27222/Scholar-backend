package com.scholarmatch.api.scholarship;

import static org.assertj.core.api.Assertions.assertThat;

import com.scholarmatch.api.profile.StudentProfile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MatchScoreServiceTest {

  private final MatchScoreService service = new MatchScoreService();

  @Test
  void bareProfileAgainstBareScholarshipMatchesOnFinancialNeedOnly() {
    // GPA/field/nationality are nullable "not yet filled in" fields, so they're
    // skipped when either side is null. financial_need is a non-nullable boolean
    // on both sides, so it's always comparable — "no need required" vs "no need
    // declared" is a genuine (if thin) match, not an absence of signal.
    StudentProfile profile = new StudentProfile(UUID.randomUUID());
    Scholarship scholarship = newScholarship();

    assertThat(service.score(profile, scholarship)).isEqualTo(100);
  }

  @Test
  void fullMatchScoresOneHundred() {
    StudentProfile profile = new StudentProfile(UUID.randomUUID());
    profile.setGpa(new BigDecimal("3.80"));
    profile.setFieldOfStudy("Engineering");
    profile.setNationality("Ghanaian");
    profile.setFinancialNeed(true);

    Scholarship scholarship = newScholarship();
    scholarship.setMinGpa(new BigDecimal("3.50"));
    scholarship.setFieldOfStudy("Engineering");
    scholarship.setNationality("Ghanaian");
    scholarship.setFinancialNeed(true);

    assertThat(service.score(profile, scholarship)).isEqualTo(100);
  }

  @Test
  void financialNeedNotRequiredByScholarshipAlwaysMatches() {
    StudentProfile profile = new StudentProfile(UUID.randomUUID());
    profile.setFinancialNeed(false);

    Scholarship scholarship = newScholarship();
    scholarship.setFinancialNeed(false);

    assertThat(service.score(profile, scholarship)).isEqualTo(100);
  }

  @Test
  void scholarshipRequiresFinancialNeedButStudentHasNone() {
    StudentProfile profile = new StudentProfile(UUID.randomUUID());
    profile.setFinancialNeed(false);

    Scholarship scholarship = newScholarship();
    scholarship.setFinancialNeed(true);

    assertThat(service.score(profile, scholarship)).isEqualTo(0);
  }

  @Test
  void gpaExactlyAtThresholdIsFullCredit() {
    StudentProfile profile = new StudentProfile(UUID.randomUUID());
    profile.setGpa(new BigDecimal("3.50"));

    Scholarship scholarship = newScholarship();
    scholarship.setMinGpa(new BigDecimal("3.50"));
    scholarship.setFinancialNeed(false);

    assertThat(service.score(profile, scholarship)).isEqualTo(100);
  }

  @Test
  void gpaHalfPointBelowThresholdGetsPartialCredit() {
    StudentProfile profile = new StudentProfile(UUID.randomUUID());
    profile.setGpa(new BigDecimal("3.00"));

    Scholarship scholarship = newScholarship();
    scholarship.setMinGpa(new BigDecimal("3.50"));
    scholarship.setFinancialNeed(false);

    // GPA weight 35, financial-need weight 15, total 50; GPA credit is 50% of 35 = 17.5.
    // (17.5 + 15) / 50 * 100 = 65
    assertThat(service.score(profile, scholarship)).isEqualTo(65);
  }

  @Test
  void gpaFullPointOrMoreBelowThresholdGetsZeroCredit() {
    StudentProfile profile = new StudentProfile(UUID.randomUUID());
    profile.setGpa(new BigDecimal("2.00"));

    Scholarship scholarship = newScholarship();
    scholarship.setMinGpa(new BigDecimal("3.50"));
    scholarship.setFinancialNeed(false);

    // GPA credit 0; only financial-need weight (15) counted; (0 + 15) / 50 * 100 = 30
    assertThat(service.score(profile, scholarship)).isEqualTo(30);
  }

  @Test
  void fieldOfStudyAnyWildcardMatchesAnything() {
    StudentProfile profile = new StudentProfile(UUID.randomUUID());
    profile.setFieldOfStudy("Fine Art");

    Scholarship scholarship = newScholarship();
    scholarship.setFieldOfStudy("Any");
    scholarship.setFinancialNeed(false);

    assertThat(service.score(profile, scholarship)).isEqualTo(100);
  }

  @Test
  void broadNationalityCategoryGetsPartialCreditForNonExactMatch() {
    StudentProfile profile = new StudentProfile(UUID.randomUUID());
    profile.setNationality("Kenyan");

    Scholarship scholarship = newScholarship();
    scholarship.setNationality("African");
    scholarship.setFinancialNeed(false);

    // Nationality weight 20 * 0.6 = 12; financial-need weight 15; total weight 35.
    // (12 + 15) / 35 * 100 = 77.14... -> rounds to 77
    assertThat(service.score(profile, scholarship)).isEqualTo(77);
  }

  @Test
  void unrelatedNationalityGetsNoCredit() {
    StudentProfile profile = new StudentProfile(UUID.randomUUID());
    profile.setNationality("Kenyan");

    Scholarship scholarship = newScholarship();
    scholarship.setNationality("United Kingdom");
    scholarship.setFinancialNeed(false);

    // Nationality weight 20 (0 earned) + financial-need weight 15 (earned) = 15/35 * 100 ~= 43
    assertThat(service.score(profile, scholarship)).isEqualTo(43);
  }

  private Scholarship newScholarship() {
    return new Scholarship(UUID.randomUUID(), "Test Provider", "Test Scholarship", LocalDate.now().plusDays(30));
  }
}
