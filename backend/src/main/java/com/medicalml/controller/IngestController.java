package com.medicalml.controller;

import com.medicalml.dto.ApiResponse;
import com.medicalml.service.IngestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for data ingestion — uploads medical data files (CSV, Excel,
 * JSON).
 *
 * Base path: /api/ingest
 * Requires authentication (JWT token in Authorization header).
 *
 * This controller handles the first step in the ML pipeline: getting raw
 * patient
 * data into the system. It accepts file uploads and delegates parsing to
 * IngestService.
 */
@RestController
@RequestMapping("/api/ingest")
@RequiredArgsConstructor
public class IngestController {

    /**
     * Service responsible for parsing uploaded files and creating patient/biomarker
     * records.
     */
    private final IngestService ingestService;

    /**
     * Upload and ingest a medical data file.
     *
     * Supported formats:
     * - CSV (.csv) — Comma-separated biomarker data
     * - Excel (.xlsx/.xls) — Spreadsheet biomarker data
     * - JSON (.json) — Array of patient biomarker objects
     *
     * Processing pipeline (handled by IngestService):
     * 1. Detects the file format from the extension.
     * 2. Parses rows from the file.
     * 3. Creates/updates Patient records in the database.
     * 4. Creates CheckupRecord entries for each data row.
     * 5. Extracts all biomarker values into Biomarker entities.
     *
     * @param file The uploaded file (sent as multipart/form-data with key "file")
     * @return 200 OK with record count, record IDs, format, and filename on
     *         success.
     *         400 Bad Request with error code "INGEST_FAILED" on failure.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            var result = ingestService.uploadFile(file);
            return ResponseEntity.ok(ApiResponse.Success.builder()
                    .data(result).message("File uploaded and parsed successfully").build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.Error.builder()
                    .error(ApiResponse.ErrorDetail.builder()
                            .code("INGEST_FAILED").message(e.getMessage()).build())
                    .build());
        }
    }
}
