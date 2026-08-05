package com.scholarmatch.api.scholarship;

import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.common.ApiException;
import com.scholarmatch.api.common.Role;
import com.scholarmatch.api.profile.Profile;
import com.scholarmatch.api.profile.ProfileRepository;
import com.scholarmatch.api.profile.ProviderProfile;
import com.scholarmatch.api.profile.ProviderProfileRepository;
import com.scholarmatch.api.profile.StudentProfile;
import com.scholarmatch.api.profile.StudentProfileRepository;
import com.scholarmatch.api.scholarship.dto.ScholarshipCreateRequest;
import com.scholarmatch.api.scholarship.dto.ScholarshipResponse;
import com.scholarmatch.api.scholarship.dto.ScholarshipUpdateRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScholarshipService {

  private final ScholarshipRepository scholarshipRepository;
  private final ProfileRepository profileRepository;
  private final ProviderProfileRepository providerProfileRepository;
  private final StudentProfileRepository studentProfileRepository;
  private final MatchScoreService matchScoreService;

  public ScholarshipService(
      ScholarshipRepository scholarshipRepository,
      ProfileRepository profileRepository,
      ProviderProfileRepository providerProfileRepository,
      StudentProfileRepository studentProfileRepository,
      MatchScoreService matchScoreService) {
    this.scholarshipRepository = scholarshipRepository;
    this.profileRepository = profileRepository;
    this.providerProfileRepository = providerProfileRepository;
    this.studentProfileRepository = studentProfileRepository;
    this.matchScoreService = matchScoreService;
  }

  public List<ScholarshipResponse> list(
      String query, String fieldOfStudy, BigDecimal minGpa, Boolean financialNeed, AuthPrincipal principal) {
    Specification<Scholarship> spec = ScholarshipSpecifications.combine(
        ScholarshipSpecifications.isActive(),
        ScholarshipSpecifications.matchesQuery(query),
        ScholarshipSpecifications.fieldOfStudyEquals(fieldOfStudy),
        ScholarshipSpecifications.minGpaAtMost(minGpa),
        ScholarshipSpecifications.financialNeedEquals(financialNeed));
    Sort sort = Sort.by(Sort.Order.desc("isFeatured"), Sort.Order.asc("deadline"));

    Optional<StudentProfile> studentProfile = studentProfileFor(principal);
    return scholarshipRepository.findAll(spec, sort).stream()
        .map(scholarship -> toResponse(scholarship, studentProfile))
        .toList();
  }

  public ScholarshipResponse get(UUID id, AuthPrincipal principal) {
    Scholarship scholarship = findActiveOrThrow(id);
    return toResponse(scholarship, studentProfileFor(principal));
  }

  private Optional<StudentProfile> studentProfileFor(AuthPrincipal principal) {
    if (principal == null || !principal.hasRole(Role.STUDENT)) {
      return Optional.empty();
    }
    return studentProfileRepository.findByUserId(principal.id());
  }

  private ScholarshipResponse toResponse(Scholarship scholarship, Optional<StudentProfile> studentProfile) {
    Integer matchScore = studentProfile.map(profile -> matchScoreService.score(profile, scholarship)).orElse(null);
    return ScholarshipResponse.from(scholarship, matchScore);
  }

  @Transactional
  public ScholarshipResponse create(UUID providerId, ScholarshipCreateRequest request) {
    Scholarship scholarship = new Scholarship(providerId, resolveProviderName(providerId), request.title(), request.deadline());
    applyCreate(scholarship, request);
    return ScholarshipResponse.from(scholarshipRepository.save(scholarship));
  }

  @Transactional
  public ScholarshipResponse update(UUID currentUserId, boolean isAdmin, UUID scholarshipId, ScholarshipUpdateRequest request) {
    if (request.isEmpty()) {
      throw ApiException.badRequest("At least one field is required");
    }
    Scholarship scholarship = scholarshipRepository.findById(scholarshipId)
        .orElseThrow(() -> ApiException.notFound("Scholarship not found"));
    if (!isAdmin && !currentUserId.equals(scholarship.getProviderId())) {
      throw ApiException.forbidden("You do not own this scholarship");
    }
    applyUpdate(scholarship, request);
    return ScholarshipResponse.from(scholarshipRepository.save(scholarship));
  }

  @Transactional
  public void delete(UUID currentUserId, boolean isAdmin, UUID scholarshipId) {
    Scholarship scholarship = scholarshipRepository.findById(scholarshipId)
        .orElseThrow(() -> ApiException.notFound("Scholarship not found"));
    if (!isAdmin && !currentUserId.equals(scholarship.getProviderId())) {
      throw ApiException.notFound("Scholarship not found");
    }
    scholarshipRepository.delete(scholarship);
  }

  private Scholarship findActiveOrThrow(UUID id) {
    Scholarship scholarship = scholarshipRepository.findById(id)
        .orElseThrow(() -> ApiException.notFound("Scholarship not found"));
    if (!scholarship.isActive()) {
      throw ApiException.notFound("Scholarship not found");
    }
    return scholarship;
  }

  private String resolveProviderName(UUID providerId) {
    String organizationName = providerProfileRepository.findByUserId(providerId)
        .map(ProviderProfile::getOrganizationName)
        .filter(name -> name != null && !name.isBlank())
        .orElse(null);
    if (organizationName != null) {
      return organizationName;
    }
    String fullName = profileRepository.findById(providerId)
        .map(Profile::getFullName)
        .filter(name -> name != null && !name.isBlank())
        .orElse(null);
    return fullName != null ? fullName : "ScholarMatch Partner";
  }

  private void applyCreate(Scholarship scholarship, ScholarshipCreateRequest request) {
    scholarship.setDescription(request.description());
    scholarship.setFundingAmount(request.fundingAmount());
    if (request.currency() != null) {
      scholarship.setCurrency(request.currency().toUpperCase());
    }
    scholarship.setFieldOfStudy(request.fieldOfStudy());
    scholarship.setAcademicLevel(request.academicLevel());
    scholarship.setNationality(request.nationality());
    scholarship.setMinGpa(request.minGpa());
    if (request.financialNeed() != null) {
      scholarship.setFinancialNeed(request.financialNeed());
    }
    scholarship.setCountry(request.country());
    if (request.requirements() != null) {
      scholarship.setRequirements(request.requirements());
    }
    if (request.isActive() != null) {
      scholarship.setActive(request.isActive());
    }
  }

  private void applyUpdate(Scholarship scholarship, ScholarshipUpdateRequest request) {
    if (request.title() != null) {
      scholarship.setTitle(request.title());
    }
    if (request.description() != null) {
      scholarship.setDescription(request.description());
    }
    if (request.fundingAmount() != null) {
      scholarship.setFundingAmount(request.fundingAmount());
    }
    if (request.currency() != null) {
      scholarship.setCurrency(request.currency().toUpperCase());
    }
    if (request.deadline() != null) {
      scholarship.setDeadline(request.deadline());
    }
    if (request.fieldOfStudy() != null) {
      scholarship.setFieldOfStudy(request.fieldOfStudy());
    }
    if (request.academicLevel() != null) {
      scholarship.setAcademicLevel(request.academicLevel());
    }
    if (request.nationality() != null) {
      scholarship.setNationality(request.nationality());
    }
    if (request.minGpa() != null) {
      scholarship.setMinGpa(request.minGpa());
    }
    if (request.financialNeed() != null) {
      scholarship.setFinancialNeed(request.financialNeed());
    }
    if (request.country() != null) {
      scholarship.setCountry(request.country());
    }
    if (request.requirements() != null) {
      scholarship.setRequirements(request.requirements());
    }
    if (request.isActive() != null) {
      scholarship.setActive(request.isActive());
    }
  }
}
