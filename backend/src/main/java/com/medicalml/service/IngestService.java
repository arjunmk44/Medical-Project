package com.medicalml.service;

import com.medicalml.entity.*;
import com.medicalml.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Data ingestion service — Ref-1 Layer 1.
 * Parses CSV/Excel/JSON/HL7/FHIR files and extracts biomarkers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IngestService {

    private final PatientRepository patientRepository;
    private final CheckupRecordRepository recordRepository;
    private final BiomarkerRepository biomarkerRepository;

    @Transactional
    public Map<String, Object> uploadFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String format = detectFormat(filename);

        log.info("Ingesting file: {} (format: {})", filename, format);

        try {
            List<Map<String, String>> rows = parseFile(file, format);
            List<UUID> recordIds = new ArrayList<>();

            for (Map<String, String> row : rows) {
                // Find or create patient
                String mrn = row.getOrDefault("mrn", "MRN-" + UUID.randomUUID().toString().substring(0, 8));
                Patient patient = patientRepository.findByMrn(mrn)
                        .orElseGet(() -> {
                            Patient p = Patient.builder()
                                    .mrn(mrn)
                                    .firstName(row.getOrDefault("first_name", "Unknown"))
                                    .lastName(row.getOrDefault("last_name", "Patient"))
                                    .age(parseIntSafe(row.get("age")))
                                    .sex(row.get("sex"))
                                    .build();
                            return patientRepository.save(p);
                        });

                // Create checkup record
                CheckupRecord record = CheckupRecord.builder()
                        .patientId(patient.getId())
                        .recordDate(LocalDate.now())
                        .sourceFormat(format)
                        .rawJson(new HashMap<>(row))
                        .status("PARSED")
                        .build();
                record = recordRepository.save(record);
                recordIds.add(record.getId());

                // Extract biomarkers
                Biomarker biomarker = Biomarker.builder()
                        .recordId(record.getId())
                        .systolicBp(parseBigDecimal(row.get("systolic_bp")))
                        .diastolicBp(parseBigDecimal(row.get("diastolic_bp")))
                        .heartRate(parseBigDecimal(row.get("heart_rate")))
                        .glucose(parseBigDecimal(row.get("glucose")))
                        .hba1c(parseBigDecimal(row.get("hba1c")))
                        .totalCholesterol(parseBigDecimal(row.get("total_cholesterol")))
                        .ldl(parseBigDecimal(row.get("ldl")))
                        .hdl(parseBigDecimal(row.get("hdl")))
                        .triglycerides(parseBigDecimal(row.get("triglycerides")))
                        .alt(parseBigDecimal(row.get("alt")))
                        .ast(parseBigDecimal(row.get("ast")))
                        .alp(parseBigDecimal(row.get("alp")))
                        .creatinine(parseBigDecimal(row.get("creatinine")))
                        .bun(parseBigDecimal(row.get("bun")))
                        .egfr(parseBigDecimal(row.get("egfr")))
                        .heightCm(parseBigDecimal(row.get("height_cm")))
                        .weightKg(parseBigDecimal(row.get("weight_kg")))
                        .bmi(parseBigDecimal(row.get("bmi")))
                        .waistCm(parseBigDecimal(row.get("waist_cm")))
                        .hipCm(parseBigDecimal(row.get("hip_cm")))
                        .smokingStatus(row.get("smoking_status"))
                        .alcoholUnits(parseBigDecimal(row.get("alcohol_units")))
                        .activityLevel(row.get("activity_level"))
                        .sleepHours(parseBigDecimal(row.get("sleep_hours")))
                        .build();
                biomarkerRepository.save(biomarker);
            }

            log.info("Ingestion complete: {} records from {}", rows.size(), filename);

            return Map.of(
                    "recordsProcessed", rows.size(),
                    "recordIds", recordIds,
                    "format", format,
                    "filename", filename);

        } catch (Exception e) {
            log.error("Ingestion failed for file: {}", filename, e);
            throw new RuntimeException("Failed to ingest file: " + e.getMessage());
        }
    }

    private List<Map<String, String>> parseFile(MultipartFile file, String format) throws Exception {
        return switch (format) {
            case "CSV" -> parseCsv(file);
            case "EXCEL" -> parseExcel(file);
            case "JSON" -> parseJson(file);
            default -> throw new RuntimeException("Unsupported format: " + format);
        };
    }

    private List<Map<String, String>> parseCsv(MultipartFile file) throws Exception {
        String content = new String(file.getBytes());
        String[] lines = content.split("\n");
        if (lines.length < 2)
            return List.of();

        String[] headers = lines[0].trim().split(",");
        List<Map<String, String>> rows = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty())
                continue;
            String[] values = line.split(",", -1);
            Map<String, String> row = new HashMap<>();
            for (int j = 0; j < headers.length && j < values.length; j++) {
                String key = headers[j].trim().toLowerCase().replaceAll("[^a-z0-9_]", "_");
                row.put(key, values[j].trim());
            }
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, String>> parseExcel(MultipartFile file) throws Exception {
        try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(file.getInputStream())) {
            var sheet = workbook.getSheetAt(0);
            var headerRow = sheet.getRow(0);
            if (headerRow == null)
                return List.of();

            String[] headers = new String[headerRow.getLastCellNum()];
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headerRow.getCell(i).getStringCellValue().trim().toLowerCase().replaceAll("[^a-z0-9_]",
                        "_");
            }

            List<Map<String, String>> rows = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                var row = sheet.getRow(i);
                if (row == null)
                    continue;
                Map<String, String> rowMap = new HashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    var cell = row.getCell(j);
                    if (cell != null) {
                        rowMap.put(headers[j], switch (cell.getCellType()) {
                            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
                            case STRING -> cell.getStringCellValue();
                            default -> "";
                        });
                    }
                }
                rows.add(rowMap);
            }
            return rows;
        }
    }

    private List<Map<String, String>> parseJson(MultipartFile file) throws Exception {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var tree = mapper.readTree(file.getBytes());
        List<Map<String, String>> rows = new ArrayList<>();

        if (tree.isArray()) {
            for (var node : tree) {
                Map<String, String> row = new HashMap<>();
                node.fields()
                        .forEachRemaining(entry -> row.put(entry.getKey().toLowerCase(), entry.getValue().asText()));
                rows.add(row);
            }
        }
        return rows;
    }

    private String detectFormat(String filename) {
        if (filename == null)
            return "CSV";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".xlsx") || lower.endsWith(".xls"))
            return "EXCEL";
        if (lower.endsWith(".json"))
            return "JSON";
        return "CSV";
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank())
            return null;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseIntSafe(String value) {
        if (value == null || value.isBlank())
            return null;
        try {
            return (int) Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
