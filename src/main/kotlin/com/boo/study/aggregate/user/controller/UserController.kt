package com.boo.study.aggregate.user.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {

    @GetMapping("/api/ping")
    fun ping(): String{
        return "ok"
    }
}