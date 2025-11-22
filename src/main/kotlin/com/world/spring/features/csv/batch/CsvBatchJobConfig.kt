package com.world.spring.features.csv.batch

import com.world.spring.features.csv.entity.CsvRecord
import com.world.spring.features.csv.repository.CsvRecordRepository
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.data.RepositoryItemWriter
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.FileSystemResource
import org.springframework.transaction.PlatformTransactionManager

@Configuration
@Profile("!test") // Only load this configuration when not in test profile
class CsvBatchJobConfig {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun csvBatchJob(
        jobRepository: JobRepository,
        csvProcessingStep: Step,
        csvBatchJobListener: CsvBatchJobListener
    ): Job {
        return JobBuilder("csvBatchJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .listener(csvBatchJobListener)
            .start(csvProcessingStep)
            .build()
    }

    @Bean
    fun csvProcessingStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        csvItemReader: FlatFileItemReader<CsvRecord>,
        csvBatchProcessor: CsvBatchProcessor,
        csvItemWriter: RepositoryItemWriter<CsvRecord>
    ): Step {
        return StepBuilder("csvProcessingStep", jobRepository)
            .chunk<CsvRecord, CsvRecord>(1000, transactionManager)
            .reader(csvItemReader)
            .processor(csvBatchProcessor)
            .writer(csvItemWriter)
            .build()
    }

    @Bean
    @StepScope
    fun csvItemReader(@Value("#{jobParameters[filePath]}") filePath: String?): FlatFileItemReader<CsvRecord> {
        if (filePath.isNullOrEmpty()) {
            log.warn("File path is null or empty, creating a no-op reader.")
            return FlatFileItemReaderBuilder<CsvRecord>()
                .name("csvItemReader")
                .resource(FileSystemResource(createTempEmptyFile()))
                .delimited()
                .names("dummy")
                .build()
        }

        return FlatFileItemReaderBuilder<CsvRecord>()
            .name("csvItemReader")
            .resource(FileSystemResource(filePath))
            .delimited()
            .names("name", "email", "age", "city")
            .fieldSetMapper { fieldSet ->
                val ageStr = fieldSet.readString("age")
                val age = try {
                    ageStr.toInt()
                } catch (e: NumberFormatException) {
                    log.warn("Could not parse age value '{}', using default 0", ageStr)
                    0
                }
                CsvRecord(
                    name = fieldSet.readString("name"),
                    email = fieldSet.readString("email"),
                    age = age,
                    city = fieldSet.readString("city")
                )
            }
            .linesToSkip(1) // Skip the header line
            .build()
    }

    private fun createTempEmptyFile(): java.io.File {
        return java.nio.file.Files.createTempFile("empty_", ".csv").toFile().also { it.deleteOnExit() }
    }

    @Bean
    fun csvItemWriter(csvRecordRepository: CsvRecordRepository): RepositoryItemWriter<CsvRecord> {
        return RepositoryItemWriterBuilder<CsvRecord>()
            .repository(csvRecordRepository)
            .methodName("save")
            .build()
    }
}
