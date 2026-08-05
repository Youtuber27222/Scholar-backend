package com.scholarmatch.api.admin;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderInviteRepository extends JpaRepository<ProviderInvite, UUID> {

  Optional<ProviderInvite> findByTokenHash(String tokenHash);

  boolean existsByEmailAndAcceptedAtIsNull(String email);
}
