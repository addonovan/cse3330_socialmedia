package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import com.addonovan.cse3330.model.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

/**
 * The controller responsible for showing data and performing actions specific
 * to the [Profile][com.addonovan.cse3330.model.Profile].
 */
@Controller
@RequestMapping("/profile")
open class ProfileController {

    /**
     * Generates a profile overview by the profile's [id].
     */
    @GetMapping(value = ["/{id:[0-9]+}"])
    fun overviewById(@PathVariable("id") id: Int, model: Model) =
            pageOverview(model, DbEngine.getProfileById(id))

    /**
     * Generates a profile over by the profile's [username].
     */
    @GetMapping(value = ["/{username:[a-zA-Z_][a-zA-Z0-9_-]*}"])
    fun overviewByUsername(@PathVariable("username") username: String, model: Model) =
            pageOverview(model, DbEngine.getProfileByUsername(username))

    /**
     * Updates the [model] with the given [profile] information, then returns
     * the name of the relevant template file.
     */
    private fun pageOverview(model: Model, profile: Profile?) = when (profile) {
        null -> "no_profile"

        else -> {
            val overview = DbEngine.wallOverview(profile)

            model.addAttribute("profile", profile)
            model.addAttribute("overview", overview)
            "profile_overview"
        }
    }

}
