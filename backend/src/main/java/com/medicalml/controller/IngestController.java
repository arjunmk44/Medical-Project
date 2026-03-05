package com.medicalml.controller;

import com.medicalml.dto.ApiResponse;
import com.medicalml.service.IngestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ingest")
@RequiredArgsConstructor
public class IngestController {

    private final IngestService ingestService;

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
