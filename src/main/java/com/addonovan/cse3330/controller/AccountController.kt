package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
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
     * Provides the form to register a new profile.
     */
    @GetMapping("/register")
    fun registrationForm(model: Model): String {
        model.addAttribute("profile", Profile())
        return "create_profile"
    }

    /**
     * Handles the submission of a profile registration request.
     */
    @PostMapping("/register")
    fun registrationSubmit(
            response: HttpServletResponse,
            @ModelAttribute profile: Profile
    ) {
        val newId = DbEngine.createProfile(profile).id
        response.sendRedirect("/profile/$newId")
    }

    /**
     * Provides the form to create a new page.
     */
    @GetMapping("/createPage")
    fun pageCreationForm(model: Model): String {
        model.addAttribute("page", Page())
        return "create_page"
    }

    /**
     * Handles the submission of a page creation request.
     */
    @PostMapping("/createPage")
    fun pageCreationSubmit(
            response: HttpServletResponse,
            @RequestParam("adminId") adminId: Int,
            @ModelAttribute page: Page
    ) {
        val newId = DbEngine.createPage(adminId, page).id
        response.sendRedirect("/page/$newId")
    }

}
