package com.platform.analytics.repository;

import com.platform.analytics.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    Optional<Invitation> findByToken(String token);

    boolean existsByOrganizationIdAndInvitedEmailAndAcceptedAtIsNull(UUID organizationId, String email);
}
