package com.platform.analytics.seeder;

import com.platform.analytics.ai.AiInsightEngine;
import com.platform.analytics.ai.AiInsightResult;
import com.platform.analytics.ai.ColumnInfo;
import com.platform.analytics.ai.InsightRequest;
import com.platform.analytics.model.*;
import com.platform.analytics.repository.*;
import com.platform.analytics.security.TenantContextHolder;
import com.platform.analytics.tenant.TenantSchemaInitializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds the database with test data on startup.
 * Only runs when app.seed.enabled=true (set in application-dev.yml).
 * Skips seeding if organisations already exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final OrganizationMemberRepository memberRepository;
    private final DatasetRepository datasetRepository;
    private final AiInsightRepository insightRepository;
    private final AuditLogRepository auditLogRepository;
    private final TenantSchemaInitializer schemaInitializer;
    private final PasswordEncoder passwordEncoder;
    private final AiInsightEngine aiInsightEngine;

    private static final String DEFAULT_PASSWORD = "Password123!";

    @Override
    public void run(String... args) {
        if (organizationRepository.count() > 0) {
            log.info("DataSeeder: organisations already exist — skipping seed.");
            return;
        }
        log.info("DataSeeder: seeding test data for 3 organisations...");
        try {
            seedAcme();
            seedTechStart();
            seedRetailCo();
            log.info("DataSeeder: seeding complete.");
        } catch (Exception e) {
            log.error("DataSeeder: seeding failed: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void seedAcme() {
        Organization org = createOrg("Acme Corp", "acme");

        User alice = createUser("alice@acme.com", "Alice Chen");
        User bob   = createUser("bob@acme.com",   "Bob Martinez");
        User carol = createUser("carol@acme.com",  "Carol Smith");
        User dave  = createUser("dave@acme.com",   "Dave Wilson");

        createMember(alice, org, Role.OWNER);
        createMember(bob,   org, Role.ADMIN);
        createMember(carol, org, Role.MEMBER);
        createMember(dave,  org, Role.VIEWER);

        withTenant("acme", () -> {
            Dataset sales = seedDataset(org, "Q4 Sales Data",
                "Quarterly revenue breakdown by region and product",
                "q4_sales.csv", 1250, alice.getId(),
                List.of(
                    new ColumnInfo("date",    "DATE",    "2023-10-01"),
                    new ColumnInfo("region",  "TEXT",    "North America"),
                    new ColumnInfo("product", "TEXT",    "Enterprise Suite"),
                    new ColumnInfo("revenue", "DECIMAL", "48250.00"),
                    new ColumnInfo("units",   "INTEGER", "12")
                ));

            Dataset customers = seedDataset(org, "Customer Segments",
                "Customer cohort and lifetime value analysis",
                "customers.csv", 3400, alice.getId(),
                List.of(
                    new ColumnInfo("customer_id",  "TEXT",    "CUST-0001"),
                    new ColumnInfo("cohort",       "TEXT",    "Enterprise"),
                    new ColumnInfo("ltv",          "DECIMAL", "12400.00"),
                    new ColumnInfo("churn_risk",   "DECIMAL", "0.12"),
                    new ColumnInfo("active",       "BOOLEAN", "true")
                ));

            Dataset marketing = seedDataset(org, "Marketing Spend",
                "Campaign performance and ROAS metrics",
                "marketing.csv", 890, bob.getId(),
                List.of(
                    new ColumnInfo("channel",     "TEXT",    "Paid Search"),
                    new ColumnInfo("campaign",    "TEXT",    "Q4 Enterprise Push"),
                    new ColumnInfo("spend",       "DECIMAL", "15200.00"),
                    new ColumnInfo("conversions", "INTEGER", "34"),
                    new ColumnInfo("roas",        "DECIMAL", "3.2")
                ));

            seedInsight(sales,     alice.getId());
            seedInsight(customers, alice.getId());
            seedInsight(marketing, bob.getId());

            seedAuditLogs(alice.getId());
        });
    }

    @Transactional
    public void seedTechStart() {
        Organization org = createOrg("TechStart Inc", "techstart");

        User david = createUser("david@techstart.io", "David Park");
        User emma  = createUser("emma@techstart.io",  "Emma Johnson");
        User frank = createUser("frank@techstart.io", "Frank Lee");

        createMember(david, org, Role.OWNER);
        createMember(emma,  org, Role.MEMBER);
        createMember(frank, org, Role.VIEWER);

        withTenant("techstart", () -> {
            Dataset product = seedDataset(org, "Product Analytics",
                "Feature usage and engagement metrics",
                "product.csv", 5600, david.getId(),
                List.of(
                    new ColumnInfo("event_date",  "DATE",    "2023-11-15"),
                    new ColumnInfo("feature",     "TEXT",    "Dashboard"),
                    new ColumnInfo("users",       "INTEGER", "1240"),
                    new ColumnInfo("sessions",    "INTEGER", "3890"),
                    new ColumnInfo("avg_duration","DECIMAL", "4.7")
                ));

            Dataset retention = seedDataset(org, "User Retention",
                "30-day and 90-day retention cohort data",
                "retention.csv", 2100, emma.getId(),
                List.of(
                    new ColumnInfo("cohort_date",  "DATE",    "2023-09-01"),
                    new ColumnInfo("cohort_size",  "INTEGER", "450"),
                    new ColumnInfo("day_30_rate",  "DECIMAL", "0.42"),
                    new ColumnInfo("day_90_rate",  "DECIMAL", "0.28"),
                    new ColumnInfo("plan",         "TEXT",    "Pro")
                ));

            seedInsight(product,   david.getId());
            seedInsight(retention, emma.getId());
        });
    }

    @Transactional
    public void seedRetailCo() {
        Organization org = createOrg("RetailCo", "retailco");

        User grace = createUser("grace@retailco.com", "Grace Kim");
        User henry = createUser("henry@retailco.com", "Henry Brown");
        User iris  = createUser("iris@retailco.com",  "Iris Chen");
        User jack  = createUser("jack@retailco.com",  "Jack Davis");

        createMember(grace, org, Role.OWNER);
        createMember(henry, org, Role.ADMIN);
        createMember(iris,  org, Role.MEMBER);
        createMember(jack,  org, Role.MEMBER);

        withTenant("retailco", () -> {
            Dataset inventory = seedDataset(org, "Inventory",
                "Current stock levels and reorder points",
                "inventory.csv", 8200, grace.getId(),
                List.of(
                    new ColumnInfo("sku",         "TEXT",    "SKU-00123"),
                    new ColumnInfo("category",    "TEXT",    "Electronics"),
                    new ColumnInfo("stock",       "INTEGER", "342"),
                    new ColumnInfo("reorder_pt",  "INTEGER", "50"),
                    new ColumnInfo("unit_cost",   "DECIMAL", "129.99")
                ));

            Dataset salesBySku = seedDataset(org, "Sales by SKU",
                "Weekly sales performance by product SKU",
                "sales_sku.csv", 4500, henry.getId(),
                List.of(
                    new ColumnInfo("week",        "DATE",    "2023-10-16"),
                    new ColumnInfo("sku",         "TEXT",    "SKU-00123"),
                    new ColumnInfo("units_sold",  "INTEGER", "87"),
                    new ColumnInfo("revenue",     "DECIMAL", "11309.13"),
                    new ColumnInfo("margin",      "DECIMAL", "0.34")
                ));

            Dataset suppliers = seedDataset(org, "Supplier Performance",
                "On-time delivery and quality scores by supplier",
                "suppliers.csv", 620, grace.getId(),
                List.of(
                    new ColumnInfo("supplier",       "TEXT",    "GlobalParts Ltd"),
                    new ColumnInfo("on_time_rate",   "DECIMAL", "0.91"),
                    new ColumnInfo("quality_score",  "DECIMAL", "4.2"),
                    new ColumnInfo("lead_days",      "INTEGER", "14"),
                    new ColumnInfo("active",         "BOOLEAN", "true")
                ));

            seedInsight(inventory, grace.getId());
            seedInsight(salesBySku, henry.getId());
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Organization createOrg(String name, String slug) {
        Organization org = organizationRepository.save(
            Organization.builder().name(name).slug(slug).build());
        schemaInitializer.initializeSchema(slug);
        return org;
    }

    private User createUser(String email, String fullName) {
        return userRepository.save(User.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
            .fullName(fullName)
            .isActive(true)
            .build());
    }

    private void createMember(User user, Organization org, Role role) {
        memberRepository.save(OrganizationMember.builder()
            .user(user).organization(org).role(role).build());
    }

    private Dataset seedDataset(Organization org, String name, String description,
                                 String fileName, int rowCount, java.util.UUID uploadedBy,
                                 List<ColumnInfo> cols) {
        Dataset dataset = Dataset.builder()
            .name(name)
            .description(description)
            .fileName(fileName)
            .rowCount(rowCount)
            .status(DatasetStatus.READY)
            .uploadedBy(uploadedBy)
            .build();

        List<DatasetColumn> columns = new java.util.ArrayList<>();
        for (int i = 0; i < cols.size(); i++) {
            ColumnInfo ci = cols.get(i);
            columns.add(DatasetColumn.builder()
                .dataset(dataset)
                .columnName(ci.name())
                .dataType(ci.dataType())
                .sampleValue(ci.sampleValue())
                .columnIndex(i)
                .build());
        }
        dataset.getColumns().addAll(columns);
        return datasetRepository.save(dataset);
    }

    private void seedInsight(Dataset dataset, java.util.UUID generatedBy) {
        List<ColumnInfo> cols = dataset.getColumns().stream()
            .map(c -> new ColumnInfo(c.getColumnName(), c.getDataType(), c.getSampleValue()))
            .toList();

        InsightRequest req = new InsightRequest(dataset.getName(),
            dataset.getRowCount() != null ? dataset.getRowCount() : 0, cols);
        AiInsightResult result = aiInsightEngine.generateInsight(req);

        insightRepository.save(AiInsight.builder()
            .datasetId(dataset.getId())
            .title(result.title())
            .summary(result.summary())
            .details(result.details())
            .modelUsed(result.modelUsed())
            .generatedBy(generatedBy)
            .build());
    }

    private void seedAuditLogs(java.util.UUID userId) {
        auditLogRepository.save(AuditLog.builder()
            .userId(userId).action("DATASET_UPLOAD").resource("dataset")
            .ipAddress("127.0.0.1").build());
        auditLogRepository.save(AuditLog.builder()
            .userId(userId).action("INSIGHT_GENERATE").resource("ai_insight")
            .ipAddress("127.0.0.1").build());
    }

    private void withTenant(String tenantId, Runnable action) {
        TenantContextHolder.setTenantId(tenantId);
        try {
            action.run();
        } finally {
            TenantContextHolder.clear();
        }
    }
}
