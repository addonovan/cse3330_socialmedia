package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DatabaseDriver
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
open class ProfileController {

    @GetMapping(value = ["/accounts"], produces = ["application/json"])
    fun accounts() = DatabaseDriver.listAccounts()

    @GetMapping(value = ["/profile/{id}"])
    fun profileOverview(@PathVariable("id") id: Int, model: Model): String {
        val profile = DatabaseDriver.getProfileById(id)

        return when (profile) {
            null -> "no_profile"

            else -> {
                model.addAttribute("profile", profile)
                "profile_overview"
            }
        }
    }

}
