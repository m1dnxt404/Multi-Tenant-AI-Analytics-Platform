-- ============================================================
-- V2: Tenant schema factory function
-- Creates a stored procedure that initialises a tenant schema
-- with all required tables. Called by TenantSchemaInitializer
-- at organisation registration time.
-- ============================================================

CREATE OR REPLACE FUNCTION public.create_tenant_schema(p_slug TEXT) RETURNS VOID AS $$
DECLARE
    v_schema TEXT := 'tenant_' || p_slug;
BEGIN
    -- Create schema
    EXECUTE format('CREATE SCHEMA IF NOT EXISTS %I', v_schema);

    -- Datasets table
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I.datasets (
            id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
            name         VARCHAR(255) NOT NULL,
            description  TEXT,
            file_name    VARCHAR(500) NOT NULL,
            row_count    INTEGER,
            status       VARCHAR(20)  NOT NULL DEFAULT ''PROCESSING''
                             CHECK (status IN (''PROCESSING'', ''READY'', ''ERROR'')),
            uploaded_by  UUID         NOT NULL,
            created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
            updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
        )', v_schema);

    -- Dataset columns (metadata about each CSV column)
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I.dataset_columns (
            id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
            dataset_id   UUID         NOT NULL,
            column_name  VARCHAR(255) NOT NULL,
            data_type    VARCHAR(50)  NOT NULL,
            sample_value TEXT,
            column_index INTEGER      NOT NULL,
            CONSTRAINT fk_dataset_columns_dataset
                FOREIGN KEY (dataset_id)
                REFERENCES %I.datasets(id)
                ON DELETE CASCADE
        )', v_schema, v_schema);

    -- AI insights
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I.ai_insights (
            id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
            dataset_id   UUID         NOT NULL,
            title        VARCHAR(500) NOT NULL,
            summary      TEXT         NOT NULL,
            details      JSONB,
            model_used   VARCHAR(100) NOT NULL DEFAULT ''mock-v1'',
            generated_by UUID         NOT NULL,
            created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
            CONSTRAINT fk_ai_insights_dataset
                FOREIGN KEY (dataset_id)
                REFERENCES %I.datasets(id)
                ON DELETE CASCADE
        )', v_schema, v_schema);

    -- Audit logs
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I.audit_logs (
            id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
            user_id     UUID         NOT NULL,
            action      VARCHAR(100) NOT NULL,
            resource    VARCHAR(100),
            resource_id UUID,
            metadata    JSONB,
            ip_address  VARCHAR(45),
            created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
        )', v_schema);

    -- Usage metrics
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I.usage_metrics (
            id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
            metric_name   VARCHAR(100)  NOT NULL,
            metric_value  NUMERIC       NOT NULL,
            recorded_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW()
        )', v_schema);

    -- Indexes per tenant schema
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_datasets_status      ON %I.datasets(status)',          v_schema);
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_datasets_uploaded_by ON %I.datasets(uploaded_by)',     v_schema);
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_columns_dataset_id   ON %I.dataset_columns(dataset_id)', v_schema);
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_insights_dataset_id  ON %I.ai_insights(dataset_id)',   v_schema);
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_audit_user_id        ON %I.audit_logs(user_id)',       v_schema);
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_audit_created_at     ON %I.audit_logs(created_at)',    v_schema);

END;
$$ LANGUAGE plpgsql;
