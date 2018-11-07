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

    //
    // Registration
    //

    /**
     * Provides the form to register a new profile.
     */
    @GetMapping("/register/profile")
    fun registrationForm(model: Model): String {
        model.addAttribute("profile", Profile())
        return "create_profile"
    }

    /**
     * Handles the submission of a profile registration request.
     */
    @PostMapping("/register/profile")
    fun registrationSubmit(
            response: HttpServletResponse,
            @ModelAttribute profile: Profile
    ) {
        val newId = DbEngine.createProfile(profile).id
        response.sendRedirect("/account/$newId")
    }

    /**
     * Provides the form to create a new page.
     */
    @GetMapping("/register/page")
    fun pageCreationForm(model: Model): String {
        model.addAttribute("page", Page())
        return "create_page"
    }

    /**
     * Handles the submission of a page creation request.
     */
    @PostMapping("/register/page")
    fun pageCreationSubmit(
            response: HttpServletResponse,
            @RequestParam("adminId") adminId: Int,
            @ModelAttribute page: Page
    ) {
        val newId = DbEngine.createPage(adminId, page).id
        response.sendRedirect("/account/$newId")
    }

    //
    // Account Overviews
    //

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
     * Updates the [model] with the given [profile] information, then returns
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
