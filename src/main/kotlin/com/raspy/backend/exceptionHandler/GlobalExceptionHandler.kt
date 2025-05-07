package com.raspy.backend.exceptionHandler

import com.raspy.backend.auth.AuthController
import com.raspy.backend.exception.DuplicateEmailException
import com.raspy.backend.exception.InvalidCredentialsException
import com.raspy.backend.exception.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ApiErrorResponse> {
        return buildResponse(HttpStatus.NOT_FOUND, ex.message ?: "User not found")
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<ApiErrorResponse> {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.message ?: "Invalid credentials")
    }

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmail(ex: DuplicateEmailException): ResponseEntity<ApiErrorResponse> {
        return buildResponse(HttpStatus.CONFLICT, ex.message ?: "This is a duplicate email.")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiErrorResponse(
                success = false,
                code = HttpStatus.BAD_REQUEST.value(),
                message = "Validation failed",
                data = mapOf("errors" to errors)
            )
        )
    }

    /**
     * 기타 에러에 대한 default handler
     */
    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<ApiErrorResponse> {
        logger.error("Unexpected error occurred", ex)
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.message ?: "An unexpected error occurred")
    }

    private fun buildResponse(status: HttpStatus, message: String): ResponseEntity<ApiErrorResponse> {
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

// 공통 응답 DTO
data class ApiErrorResponse(
    val success: Boolean,
    val code: Int,
    val message: String,
    val data: Any? = null
)
