package io.ksetoue.login.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
class AccountHealthController {
    @GetMapping("/liveness")
    fun live(): ResponseEntity<String> {
        return ResponseEntity("OK", HttpStatus.OK)
    }
}