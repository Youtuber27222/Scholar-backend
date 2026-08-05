package com.scholarmatch.api.scholarship;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScholarshipRepository extends JpaRepository<Scholarship, UUID>, JpaSpecificationExecutor<Scholarship> {

  List<Scholarship> findByProviderId(UUID providerId);

  @Query("SELECT s.providerId, COUNT(s) FROM Scholarship s WHERE s.providerId IN :providerIds GROUP BY s.providerId")
  List<Object[]> countByProviderIdIn(@Param("providerIds") Collection<UUID> providerIds);
}
