package com.scholarmatch.api.profile;

import com.scholarmatch.api.common.ApiException;
import com.scholarmatch.api.profile.dto.StudentProfileResponse;
import com.scholarmatch.api.profile.dto.StudentProfileUpdateRequest;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentProfileService {

  private final StudentProfileRepository studentProfileRepository;

  public StudentProfileService(StudentProfileRepository studentProfileRepository) {
    this.studentProfileRepository = studentProfileRepository;
  }

  public StudentProfileResponse get(UUID userId) {
    StudentProfile profile = studentProfileRepository.findByUserId(userId)
        .orElseThrow(() -> ApiException.notFound("Student profile not found"));
    return StudentProfileResponse.from(profile);
  }

  @Transactional
  public StudentProfileResponse upsert(UUID userId, StudentProfileUpdateRequest request) {
    if (request.isEmpty()) {
      throw ApiException.badRequest("At least one field is required");
    }
    StudentProfile profile = studentProfileRepository.findByUserId(userId).orElseGet(() -> new StudentProfile(userId));
    if (request.gpa() != null) {
      profile.setGpa(request.gpa());
    }
    if (request.fieldOfStudy() != null) {
      profile.setFieldOfStudy(request.fieldOfStudy());
    }
    if (request.academicLevel() != null) {
      profile.setAcademicLevel(request.academicLevel());
    }
    if (request.nationality() != null) {
      profile.setNationality(request.nationality());
    }
    if (request.financialNeed() != null) {
      profile.setFinancialNeed(request.financialNeed());
    }
    if (request.institution() != null) {
      profile.setInstitution(request.institution());
    }
    if (request.bio() != null) {
      profile.setBio(request.bio());
    }
    return StudentProfileResponse.from(studentProfileRepository.save(profile));
  }
}
