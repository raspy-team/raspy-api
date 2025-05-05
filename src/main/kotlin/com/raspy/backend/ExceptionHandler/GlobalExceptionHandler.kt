package com.raspy.backend.ExceptionHandler

import com.raspy.backend.Exception.DuplicateEmailException
import com.raspy.backend.Exception.InvalidCredentialsException
import com.raspy.backend.Exception.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf("message" to (ex.message ?: "User not found")))
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(mapOf("message" to (ex.message ?: "Invalid credentials")))
    }

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmail(ex: DuplicateEmailException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(mapOf("message" to (ex.message?:"this is duplicate email.")))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf("message" to (ex.message?:"An unexpected error occurred")))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }

            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Validation failed", "errors" to errors.toString()))
        }
    }