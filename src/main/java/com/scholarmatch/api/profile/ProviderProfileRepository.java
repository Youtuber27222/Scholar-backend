package com.scholarmatch.api.profile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, UUID> {

  Optional<ProviderProfile> findByUserId(UUID userId);

  List<ProviderProfile> findByUserIdIn(Collection<UUID> userIds);
}
