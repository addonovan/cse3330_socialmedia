package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import com.addonovan.cse3330.model.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/account")
open class AccountController {

    @GetMapping("/register")
    fun registrationForm(model: Model): String {
        model.addAttribute("profile", Profile())
        return "create_account"
    }

    @PostMapping("/register")
    fun registrationSubmit(model: Model, @ModelAttribute profile: Profile): String {
        val newProfile = DbEngine.createProfile(profile)
        model.addAttribute("profile", newProfile)
        return "profile_overview"
    }

}
