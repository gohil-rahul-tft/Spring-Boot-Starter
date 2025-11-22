package com.world.spring.features.csv.service

import com.world.spring.features.csv.batch.CsvBatchProcessor
import com.world.spring.features.csv.entity.CsvRecord
import com.world.spring.features.csv.repository.CsvRecordRepository
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files

/**
 * Service class for handling CSV operations including parsing and database operations
 */
@Service
class CsvService(
    val csvRecordRepository: CsvRecordRepository,  // Made public so controller can access it
    private val jobLauncher: JobLauncher,
    private val csvBatchJob: Job
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Process a CSV file and save records to the database with single record processing
     */
    fun processCsvFileSingle(multipartFile: MultipartFile): Int {
        var recordCount = 0
        val inputStream = multipartFile.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))

        // Skip header line
        reader.readLine()

        // Process each line one by one
        reader.useLines { lines ->
            lines.forEach { line ->
                val fields = line.split(",")

                if (fields.size >= 4) { // Ensure we have enough fields
                    val csvRecord = CsvRecord(
                        name = fields[0].trim(),
                        email = fields[1].trim(),
                        age = fields[2].trim().toIntOrNull() ?: 0,
                        city = fields[3].trim()
                    )

                    // Save single record to database
                    csvRecordRepository.save(csvRecord)
                    recordCount++
                }
            }
        }

        return recordCount
    }

    /**
     * Process a CSV file and save records to the database in batches for better performance
     */
    fun processCsvFileBatch(multipartFile: MultipartFile, batchSize: Int = 1000): Int {
        var recordCount = 0
        val inputStream = multipartFile.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))

        // Skip header line
        reader.readLine()

        val batch = mutableListOf<CsvRecord>()

        reader.useLines { lines ->
            lines.forEach { line ->
                val fields = line.split(",")

                if (fields.size >= 4) { // Ensure we have enough fields
                    val csvRecord = CsvRecord(
                        name = fields[0].trim(),
                        email = fields[1].trim(),
                        age = fields[2].trim().toIntOrNull() ?: 0,
                        city = fields[3].trim()
                    )

                    batch.add(csvRecord)
                    recordCount++

                    // Save batch when it reaches the specified size
                    if (batch.size >= batchSize) {
                        csvRecordRepository.saveAll(batch)
                        batch.clear() // Clear the batch after saving
                    }
                }
            }

            // Save remaining records if any
            if (batch.isNotEmpty()) {
                csvRecordRepository.saveAll(batch)
                batch.clear()
            }
        }

        return recordCount
    }

    /**
     * Process a CSV file using Spring Boot Batch for maximum performance with large files
     */
    fun processCsvFileWithBatch(multipartFile: MultipartFile): Int {
        val tempFile = Files.createTempFile("csv_upload_", ".csv")
        multipartFile.transferTo(tempFile.toFile())

        try {
            val jobParameters = JobParametersBuilder()
                .addString("filePath", tempFile.toAbsolutePath().toString())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters()

            log.info("Starting Spring Batch job for file: {}", tempFile.fileName)
            val jobExecution = jobLauncher.run(csvBatchJob, jobParameters)
            log.info("Spring Batch job finished with status: {}", jobExecution.status)

            return jobExecution.stepExecutions.sumOf { it.writeCount }.toInt()
        } finally {
            Files.deleteIfExists(tempFile)
            log.info("Temporary file {} deleted", tempFile.fileName)
        }
    }

    /**
     * count of the records in the DB
     */
    fun fetchCountOfRecordsInTheDB(): Int {
         return csvRecordRepository.count().toInt()
    }
}