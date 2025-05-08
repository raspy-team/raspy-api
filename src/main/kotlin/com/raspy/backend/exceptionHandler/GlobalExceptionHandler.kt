package com.raspy.backend.exceptionHandler

import com.raspy.backend.exception.DuplicateEmailException
import com.raspy.backend.exception.InvalidCredentialsException
import com.raspy.backend.exception.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ApiErrorResponse> {
        logger.warn("User not found exception: ${ex.message}")
        return buildResponse(HttpStatus.NOT_FOUND, ex.message ?: "User not found")
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<ApiErrorResponse> {
        logger.warn("Invalid credentials exception: ${ex.message}")
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.message ?: "Invalid credentials")
    }

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmail(ex: DuplicateEmailException): ResponseEntity<ApiErrorResponse> {
        logger.warn("Duplicate email exception: ${ex.message}")
        return buildResponse(HttpStatus.CONFLICT, ex.message ?: "This is a duplicate email.")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        logger.warn("Validation failed: $errors")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiErrorResponse(
                success = false,
                code = HttpStatus.BAD_REQUEST.value(),
                message = "Validation failed",
                data = mapOf("errors" to errors)
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<ApiErrorResponse> {
        logger.error("Unexpected error occurred", ex)
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.message ?: "An unexpected error occurred")
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleParseError(ex: HttpMessageNotReadableException): ResponseEntity<ApiErrorResponse> {
        val errorMsg = "Malformed JSON request: ${ex.localizedMessage}"
        return buildResponse(HttpStatus.BAD_REQUEST, errorMsg )
    }

    private fun buildResponse(status: HttpStatus, message: String): ResponseEntity<ApiErrorResponse> {
        logger.debug("Building error response - Status: ${status.value()}, Message: $message")
        return ResponseEntity.status(status).body(
            ApiErrorResponse(
                success = false,
                code = status.value(),
                message = message,
                data = null
            )
        )
    }
}

data class ApiErrorResponse(
    val success: Boolean,
    val code: Int,
    val message: String,
    val data: Any? = null
)
