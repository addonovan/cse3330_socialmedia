package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import com.addonovan.cse3330.model.Profile
import com.addonovan.cse3330.profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import javax.servlet.http.HttpServletRequest

@Controller
open class HomeController {

    @GetMapping("/")
    fun index(
            model: Model,
            request: HttpServletRequest
    ): String {
        val user = request.profile
        return if (user == null)
            defaultHome(model)
        else
            signedInHome(user, model)
    }

    private fun defaultHome(model: Model): String {
        return "home/welcome"
    }

    private fun signedInHome(
            user: Profile,
            model: Model
    ): String {
        model.addAttribute("user", user)
        DbEngine.feedFor(user).let {
            model.addAttribute("overview", it)
        }

        return "home/feed"
    }

}
