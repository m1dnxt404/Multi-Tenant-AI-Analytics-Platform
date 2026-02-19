package com.platform.analytics.service;

import com.platform.analytics.config.RedisConfig;
import com.platform.analytics.dto.request.UpdateOrganizationRequest;
import com.platform.analytics.dto.response.OrganizationResponse;
import com.platform.analytics.exception.ResourceNotFoundException;
import com.platform.analytics.model.Organization;
import com.platform.analytics.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Cacheable(value = RedisConfig.ORGANIZATIONS, key = "#slug")
    @Transactional(readOnly = true)
    public OrganizationResponse getBySlug(String slug) {
        Organization org = organizationRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + slug));
        return toResponse(org);
    }

    @CacheEvict(value = RedisConfig.ORGANIZATIONS, key = "#slug")
    @Transactional
    public OrganizationResponse updateName(String slug, UpdateOrganizationRequest request) {
        Organization org = organizationRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + slug));
        org.setName(request.name());
        return toResponse(organizationRepository.save(org));
    }

    private OrganizationResponse toResponse(Organization org) {
        return new OrganizationResponse(org.getId(), org.getName(), org.getSlug(), org.getCreatedAt());
    }
}
