package com.scholarmatch.api.application;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

  List<Application> findByStudentIdOrderByUpdatedAtDesc(UUID studentId);

  List<Application> findAllByOrderByUpdatedAtDesc();

  @Query("SELECT a FROM Application a WHERE a.scholarshipId IN "
      + "(SELECT s.id FROM Scholarship s WHERE s.providerId = :providerId) ORDER BY a.updatedAt DESC")
  List<Application> findByScholarshipProviderId(@Param("providerId") UUID providerId);

  boolean existsByStudentIdAndScholarshipId(UUID studentId, UUID scholarshipId);

  @Query("SELECT a.status, COUNT(a) FROM Application a "
      + "WHERE a.scholarshipId IN (SELECT s.id FROM Scholarship s WHERE s.providerId = :providerId) "
      + "GROUP BY a.status")
  List<Object[]> countByStatusForProvider(@Param("providerId") UUID providerId);

  @Query("SELECT a.status, COUNT(a) FROM Application a GROUP BY a.status")
  List<Object[]> countByStatusGlobal();

  @Query("SELECT a.scholarshipId, COUNT(a), SUM(CASE WHEN a.status = :awarded THEN 1L ELSE 0L END) FROM Application a "
      + "WHERE a.scholarshipId IN (SELECT s.id FROM Scholarship s WHERE s.providerId = :providerId) "
      + "GROUP BY a.scholarshipId")
  List<Object[]> perScholarshipCountsForProvider(@Param("providerId") UUID providerId, @Param("awarded") ApplicationStatus awarded);

  @Query("SELECT a.scholarshipId, COUNT(a), SUM(CASE WHEN a.status = :awarded THEN 1L ELSE 0L END) FROM Application a "
      + "GROUP BY a.scholarshipId")
  List<Object[]> perScholarshipCountsGlobal(@Param("awarded") ApplicationStatus awarded);
}
