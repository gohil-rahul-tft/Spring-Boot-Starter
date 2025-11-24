package com.world.spring.features.new_csv.controller

import com.world.spring.features.new_csv.service.CsvService
import com.world.spring.shared.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * Controller for handling all CSV file uploads and processing
 */
@RestController
@RequestMapping("/api/csv")
@Validated
@Tag(name = "CSV Processing", description = "CSV file upload and processing endpoints with different processing strategies")
@SecurityRequirement(name = "bearerAuth")
class CsvController(
    private val csvService: CsvService
) {

    /**
     * Upload and process CSV file with single record processing
     * This method processes each record individually, suitable for smaller files
     */
    @PostMapping("/upload-single")
    @Operation(
        summary = "Upload CSV with single record processing",
        description = "Processes each CSV record individually. Suitable for smaller files."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "CSV processed successfully"),
            SwaggerApiResponse(responseCode = "415", description = "Unsupported media type - File must be CSV"),
            SwaggerApiResponse(responseCode = "500", description = "Error processing CSV file"),
            SwaggerApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun uploadCsvSingle(
        @Parameter(description = "CSV file to upload", required = true)
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ApiResponse<Unit>> {
        // Validate file type
        if (!isValidCsvFile(file)) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error("File must be a CSV"))
        }

        try {
            val recordCount = csvService.processCsvFileSingle(file)
            return ResponseEntity.ok(
                ApiResponse.success("Successfully processed $recordCount records using single processing")
            )
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error processing CSV file: ${e.message}"))
        }
    }

    /**
     * Upload and process CSV file with batch processing
     * This method processes records in batches, suitable for large files for better performance
     */
    @PostMapping("/upload-batch")
    @Operation(
        summary = "Upload CSV with batch processing",
        description = "Processes CSV records in batches for better performance. Suitable for larger files."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "CSV processed successfully in batches"),
            SwaggerApiResponse(responseCode = "415", description = "Unsupported media type - File must be CSV"),
            SwaggerApiResponse(responseCode = "500", description = "Error processing CSV file"),
            SwaggerApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun uploadCsvBatch(
        @Parameter(description = "CSV file to upload", required = true)
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ApiResponse<Unit>> {
        // Validate file type
        if (!isValidCsvFile(file)) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error("File must be a CSV"))
        }

        try {
            val recordCount = csvService.processCsvFileBatch(file)
            return ResponseEntity.ok(
                ApiResponse.success("Successfully processed $recordCount records using batch processing")
            )
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error processing CSV file: ${e.message}"))
        }
    }

    /**
     * Upload and process CSV file using Spring Boot Batch
     * This method uses Spring Boot Batch framework for processing large files efficiently
     */
    @PostMapping("/upload-and-run", consumes = ["multipart/form-data"])
    @Operation(
        summary = "Upload CSV with Spring Batch processing",
        description = "Uses Spring Batch framework for efficient processing of large CSV files with job execution tracking."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "202", description = "CSV processing job accepted and started"),
            SwaggerApiResponse(responseCode = "500", description = "Error processing CSV file with batch"),
            SwaggerApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun uploadCsvWithSpringBatch(
        @Parameter(description = "CSV file to upload and process with Spring Batch", required = true)
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ApiResponse<Any>> {
        try {
            val result = csvService.processCsvWithBatch(file)
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(data = result))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error processing CSV file with batch: ${e.message}"))
        }
    }

    /**
     * Get the total count of CSV records in the database
     */
    @GetMapping("/count")
    @Operation(
        summary = "Get CSV record count",
        description = "Retrieves the total number of CSV person records stored in the database."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Record count retrieved successfully"),
            SwaggerApiResponse(responseCode = "500", description = "Error retrieving record count"),
            SwaggerApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun getCsvRecordCount(): ResponseEntity<ApiResponse<Int>> {
        try {
            val count = csvService.fetchCountOfRecordsInTheDB()
            return ResponseEntity.ok(
                ApiResponse.success(count, "Total CSV records retrieved successfully")
            )
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving record count: ${e.message}"))
        }
    }

    /**
     * Helper function to validate if the uploaded file is a CSV
     */
    private fun isValidCsvFile(file: MultipartFile): Boolean {
        return file.contentType == "text/csv" ||
               file.contentType == "application/vnd.ms-excel" ||
               file.originalFilename?.endsWith(".csv") == true
    }
}