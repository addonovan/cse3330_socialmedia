package com.addonovan.cse3330.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/account")
open class AccountController {

    @GetMapping("createAccount")
    fun createAccount() = "create_account"

}
