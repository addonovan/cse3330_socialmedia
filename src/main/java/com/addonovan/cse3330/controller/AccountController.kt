package com.addonovan.cse3330.controller

import com.addonovan.cse3330.*
import com.addonovan.cse3330.model.Account
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

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
    fun overviewById(
            request: Request,
            model: Model,
            @PathVariable id: Int
    ) = pageOverview(request, model, DbEngine.getAccountById(id))

    /**
     * Generates a profile over by the profile's [username].
     */
    @GetMapping(value = ["/{username:[a-zA-Z_][a-zA-Z0-9_-]*}"])
    fun overviewByUsername(
            request: Request,
            model: Model,
            @PathVariable username: String
    ) = pageOverview(request, model, DbEngine.getProfileByUsername(username))

    @PostMapping(value = ["/{id:[0-9+]}/follow"])
    fun followAccount(
            request: Request,
            response: Response,
            model: Model,
            @PathVariable id: Int
    ) {
        response.redirectToReferrer(request)

        val user = request.profile!!
        val followee = DbEngine.getAccountById(id)!!
        DbEngine.updateFollow(user, followee, following = true)
    }

    @PostMapping(value = ["/{id:[0-9]+}/unfollow"])
    fun unfollowAccount(
            request: Request,
            response: Response,
            model: Model,
            @PathVariable id: Int
    ) {
        response.redirectToReferrer(request)

        val user = request.profile!!
        val followee = DbEngine.getAccountById(id)!!
        DbEngine.updateFollow(user, followee, following = false)
    }


    /**
     * Updates the [model] with the given [account] information, then returns
     * the name of the relevant template file.
     */
    private fun pageOverview(
            request: Request,
            model: Model,
            account: Account?
    ) = when (account) {
        null -> "account/none"

        else -> {
            val user = request.profile
            val overview = DbEngine.wallOverview(account)

            model.addAttribute("account", account)
            model.addAttribute("overview", overview)
            model.addAttribute("user", user)
            "account/overview"
        }
    }


}
