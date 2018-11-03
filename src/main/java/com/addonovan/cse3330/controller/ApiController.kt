package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DatabaseDriver
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
open class ApiController {

    @GetMapping(value = ["/ping"], produces = ["application/json"])
    fun ping() = "pong"

    @GetMapping(value = ["/accounts"], produces = ["application/json"])
    fun accounts() = DatabaseDriver.listAccounts()

}
