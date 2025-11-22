package com.world.spring.features.csv.batch

import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.StepExecution
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CsvBatchJobListener : JobExecutionListener {
    
    private val logger: Logger = LoggerFactory.getLogger(CsvBatchJobListener::class.java)
    
    override fun beforeJob(jobExecution: JobExecution) {
        logger.info("Starting CSV batch job: ${jobExecution.jobInstance.jobName}")
    }
    
    override fun afterJob(jobExecution: JobExecution) {
        if (jobExecution.status.isUnsuccessful) {
            logger.error("CSV batch job failed: ${jobExecution.jobInstance.jobName}")
        } else {
            logger.info("CSV batch job completed successfully: ${jobExecution.jobInstance.jobName}")
            
            for (stepExecution in jobExecution.stepExecutions) {
                logger.info("Step ${stepExecution.stepName}: read=${stepExecution.readCount}, " +
                           "written=${stepExecution.writeCount}, failed=${stepExecution.filterCount}")
            }
        }
    }
}