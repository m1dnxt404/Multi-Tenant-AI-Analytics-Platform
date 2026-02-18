package com.platform.analytics.repository;

import com.platform.analytics.model.OrganizationMember;
import com.platform.analytics.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {

    Optional<OrganizationMember> findByUserId(UUID userId);

    Optional<OrganizationMember> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    @Query("SELECT om FROM OrganizationMember om JOIN FETCH om.user WHERE om.organization.id = :organizationId")
    List<OrganizationMember> findByOrganizationIdWithUser(UUID organizationId);

    boolean existsByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    long countByOrganizationIdAndRole(UUID organizationId, Role role);

    void deleteByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}
