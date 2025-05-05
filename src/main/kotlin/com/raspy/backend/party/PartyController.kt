package com.raspy.backend.party

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PartyController {

    @GetMapping("/test")
    fun test(): String  = "test good"

}