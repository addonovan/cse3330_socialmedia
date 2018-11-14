package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import com.addonovan.cse3330.model.Account
import com.addonovan.cse3330.model.Page
import com.addonovan.cse3330.model.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

/**
 * The controller for general account activities, such as creation and deletion
 * of both pages and profiles.
 */
@Controller
@RequestMapping("/account")
open class AccountController {

    /**
     * Generates a profile overview by the profile's [id].
     */
    @GetMapping(value = ["/{id:[0-9]+}"])
    fun overviewById(@PathVariable("id") id: Int, model: Model) =
            pageOverview(model, DbEngine.getAccountById(id))

    /**
     * Generates a profile over by the profile's [username].
     */
    @GetMapping(value = ["/{username:[a-zA-Z_][a-zA-Z0-9_-]*}"])
    fun overviewByUsername(@PathVariable("username") username: String, model: Model) =
            pageOverview(model, DbEngine.getProfileByUsername(username))

    /**
     * Updates the [model] with the given [account] information, then returns
     * the name of the relevant template file.
     */
    private fun pageOverview(model: Model, account: Account?) = when (account) {
        null -> "no_profile"

        else -> {
            val overview = DbEngine.wallOverview(account)

            model.addAttribute("account", account)
            model.addAttribute("overview", overview)
            "account_overview"
        }
    }


}
