package com.platform.analytics.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.platform.analytics.dto.response.DatasetColumnResponse;
import com.platform.analytics.dto.response.DatasetDetailResponse;
import com.platform.analytics.dto.response.DatasetResponse;
import com.platform.analytics.dto.response.PagedResponse;
import com.platform.analytics.exception.DatasetProcessingException;
import com.platform.analytics.exception.ResourceNotFoundException;
import com.platform.analytics.model.Dataset;
import com.platform.analytics.model.DatasetColumn;
import com.platform.analytics.model.DatasetStatus;
import com.platform.analytics.repository.DatasetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetService {

    private static final int SAMPLE_ROW_LIMIT = 1000;
    private static final String[] ALLOWED_CONTENT_TYPES = {
        "text/csv", "application/csv", "application/vnd.ms-excel", "text/plain"
    };

    private final DatasetRepository datasetRepository;

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    @Transactional
    public DatasetDetailResponse upload(MultipartFile file, String name, UUID uploadedBy) {
        validateFile(file);

        String effectiveName = (name != null && !name.isBlank()) ? name
                : stripExtension(file.getOriginalFilename());

        // Parse CSV to extract column metadata
        List<String[]> sampleRows = new ArrayList<>();
        String[] headers;

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            headers = reader.readNext();
            if (headers == null || headers.length == 0) {
                throw new DatasetProcessingException("CSV file has no headers");
            }
            String[] row;
            int rowsRead = 0;
            while ((row = reader.readNext()) != null && rowsRead < SAMPLE_ROW_LIMIT) {
                sampleRows.add(row);
                rowsRead++;
            }
        } catch (IOException | CsvValidationException e) {
            throw new DatasetProcessingException("Failed to parse CSV file: " + e.getMessage(), e);
        }

        // Build column metadata
        List<DatasetColumn> columns = buildColumns(headers, sampleRows);

        // Save file to disk
        String storedFileName = saveFile(file, uploadedBy);

        // Estimate total row count
        int totalRows = estimateRowCount(file, sampleRows.size(), headers.length);

        // Persist dataset
        Dataset dataset = Dataset.builder()
                .name(effectiveName)
                .fileName(storedFileName)
                .rowCount(totalRows)
                .status(DatasetStatus.READY)
                .uploadedBy(uploadedBy)
                .build();

        columns.forEach(c -> c.setDataset(dataset));
        dataset.getColumns().addAll(columns);

        Dataset saved = datasetRepository.save(dataset);
        log.info("Dataset [{}] uploaded by user [{}], {} rows", saved.getId(), uploadedBy, totalRows);

        return toDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DatasetResponse> findAll(int page, int size) {
        return PagedResponse.from(
            datasetRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)),
            this::toResponse
        );
    }

    @Transactional(readOnly = true)
    public DatasetDetailResponse findById(UUID id) {
        return toDetailResponse(getDataset(id));
    }

    @Transactional
    public void delete(UUID id) {
        Dataset dataset = getDataset(id);
        datasetRepository.delete(dataset);
        log.info("Dataset [{}] deleted", id);
    }

    private Dataset getDataset(UUID id) {
        return datasetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset", id));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DatasetProcessingException("No file provided");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new DatasetProcessingException("Only CSV files are accepted");
        }
    }

    private List<DatasetColumn> buildColumns(String[] headers, List<String[]> sampleRows) {
        List<DatasetColumn> columns = new ArrayList<>();
        for (int i = 0; i < headers.length; i++) {
            List<String> values = new ArrayList<>();
            for (String[] row : sampleRows) {
                if (i < row.length && row[i] != null && !row[i].isBlank()) {
                    values.add(row[i].trim());
                }
            }
            String dataType = inferType(values);
            String sample = values.isEmpty() ? null : values.get(0);

            columns.add(DatasetColumn.builder()
                    .columnName(headers[i].trim())
                    .dataType(dataType)
                    .sampleValue(sample)
                    .columnIndex(i)
                    .build());
        }
        return columns;
    }

    private String inferType(List<String> values) {
        if (values.isEmpty()) return "TEXT";
        if (values.stream().allMatch(v -> v.matches("^-?\\d+$"))) return "INTEGER";
        if (values.stream().allMatch(v -> v.matches("^-?\\d+(\\.\\d+)?$"))) return "DECIMAL";
        if (values.stream().allMatch(this::isDate)) return "DATE";
        if (values.stream().allMatch(v ->
                v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false") ||
                v.equalsIgnoreCase("yes")  || v.equalsIgnoreCase("no"))) return "BOOLEAN";
        return "TEXT";
    }

    private boolean isDate(String value) {
        try {
            LocalDate.parse(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String saveFile(MultipartFile file, UUID uploadedBy) {
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(dir);
            String stored = uploadedBy + "_" + System.currentTimeMillis() + "_"
                    + sanitize(file.getOriginalFilename());
            file.transferTo(dir.resolve(stored));
            return stored;
        } catch (IOException e) {
            log.warn("Could not save file to disk: {}. Continuing without file storage.", e.getMessage());
            return file.getOriginalFilename();
        }
    }

    private int estimateRowCount(MultipartFile file, int sampleSize, int columnCount) {
        return sampleSize;
    }

    private String sanitize(String filename) {
        if (filename == null) return "upload.csv";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String stripExtension(String filename) {
        if (filename == null) return "Untitled Dataset";
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private DatasetResponse toResponse(Dataset d) {
        return new DatasetResponse(d.getId(), d.getName(), d.getDescription(), d.getFileName(),
                d.getRowCount(), d.getStatus(), d.getUploadedBy(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private DatasetDetailResponse toDetailResponse(Dataset d) {
        List<DatasetColumnResponse> cols = d.getColumns().stream()
                .map(c -> new DatasetColumnResponse(c.getId(), c.getColumnName(),
                        c.getDataType(), c.getSampleValue(), c.getColumnIndex()))
                .toList();
        return new DatasetDetailResponse(d.getId(), d.getName(), d.getDescription(), d.getFileName(),
                d.getRowCount(), d.getStatus(), d.getUploadedBy(), d.getCreatedAt(), d.getUpdatedAt(), cols);
    }
}
