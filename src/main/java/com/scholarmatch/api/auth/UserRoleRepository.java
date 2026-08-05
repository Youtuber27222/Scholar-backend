package com.scholarmatch.api.auth;

import com.scholarmatch.api.common.Role;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

  List<UserRole> findByUserId(UUID userId);

  List<UserRole> findByRole(Role role);

  void deleteByUserIdAndRole(UUID userId, Role role);
}
