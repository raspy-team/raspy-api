package com.raspy.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    log.info { "Starting the application..." }
    runApplication<DemoApplication>(*args)
    log.info { "Application started successfully!" }
}
