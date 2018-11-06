package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import com.addonovan.cse3330.model.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/profile")
open class ProfileController {

    @GetMapping(value = ["/{id:[0-9]+}"])
    fun overviewById(@PathVariable("id") id: Int, model: Model) =
            pageOverview(model, DbEngine.getProfileById(id))

    @GetMapping(value = ["/{username:[a-zA-Z_][a-zA-Z0-9_-]*}"])
    fun overviewByUsername(@PathVariable("username") username: String, model: Model) =
            pageOverview(model, DbEngine.getProfileByUsername(username))

    private fun pageOverview(model: Model, profile: Profile?) = when (profile) {
        null -> "no_profile"

        else -> {
            model.addAttribute("profile", profile)
            "profile_overview"
        }
    }

}
