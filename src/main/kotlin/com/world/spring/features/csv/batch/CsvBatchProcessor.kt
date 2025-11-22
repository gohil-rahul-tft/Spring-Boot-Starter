package com.world.spring.features.csv.batch

import com.world.spring.features.csv.entity.CsvRecord
import org.slf4j.LoggerFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class CsvBatchProcessor : ItemProcessor<CsvRecord, CsvRecord> {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun process(item: CsvRecord): CsvRecord? {
        // Perform any transformation or validation on the item
        // Return the processed item, or null to exclude it from the output

        // Example: Skip records with empty names
        if (item.name.isEmpty()) {
            log.warn("Skipping record with empty name: {}", item)
            return null
        }

        // Create a new instance with processed values
        // For example, trim whitespace or normalize data
        return CsvRecord(
            id = item.id,
            name = item.name.trim(),
            email = item.email.trim().lowercase(),
            age = item.age,
            city = item.city,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt
        )
    }
}