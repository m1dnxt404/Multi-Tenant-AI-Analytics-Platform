-- ============================================================
-- V1: Global public schema
-- Contains cross-tenant identity and membership tables.
-- These tables are never schema-switched via search_path.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Organizations (tenants)
CREATE TABLE IF NOT EXISTS public.organizations (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Users (global identity, no org reference here)
CREATE TABLE IF NOT EXISTS public.users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Membership: user <-> org + role
CREATE TABLE IF NOT EXISTS public.organization_members (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    organization_id UUID        NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    role            VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER')),
    joined_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_org UNIQUE (user_id, organization_id)
);

-- Pending invitations
CREATE TABLE IF NOT EXISTS public.invitations (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID         NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    invited_email   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'MEMBER', 'VIEWER')),
    token           VARCHAR(255) NOT NULL UNIQUE,
    invited_by      UUID         NOT NULL REFERENCES public.users(id),
    expires_at      TIMESTAMPTZ  NOT NULL,
    accepted_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_org_members_user_id         ON public.organization_members(user_id);
CREATE INDEX IF NOT EXISTS idx_org_members_organization_id ON public.organization_members(organization_id);
CREATE INDEX IF NOT EXISTS idx_invitations_token           ON public.invitations(token);
CREATE INDEX IF NOT EXISTS idx_invitations_email           ON public.invitations(invited_email);
CREATE INDEX IF NOT EXISTS idx_users_email                 ON public.users(email);
CREATE INDEX IF NOT EXISTS idx_organizations_slug          ON public.organizations(slug);
