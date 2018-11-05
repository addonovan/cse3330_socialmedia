package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/profile")
open class ProfileController {

    @GetMapping(value = ["/{id}"])
    fun profileOverview(@PathVariable("id") id: Int, model: Model): String {
        val profile = DbEngine.getProfileById(id)

        return when (profile) {
            null -> "no_profile"

            else -> {
                model.addAttribute("profile", profile)
                "profile_overview"
            }
        }
    }

}
