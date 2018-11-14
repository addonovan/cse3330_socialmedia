package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import com.addonovan.cse3330.model.Page
import com.addonovan.cse3330.model.Profile
import com.addonovan.cse3330.profile
import com.addonovan.cse3330.signIn
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
@RequestMapping("/register")
open class RegistrationController {

    /**
     * Provides the form to register a new profile.
     */
    @GetMapping("/profile")
    fun profileRegistrationForm(model: Model) = "register/profile"

    /**
     * Handles the submission of a profile registration request.
     */
    @PostMapping("/profile")
    fun profileRegistrationSubmission(
            response: HttpServletResponse,
            @ModelAttribute profile: Profile
    ) {
        val newProfile = DbEngine.createProfile(profile)
        response.signIn(newProfile)
        response.sendRedirect("/")
    }

    /**
     * Provides the form to create a new page.
     */
    @GetMapping("/page")
    fun pageRegistrationForm(model: Model) = "register/page"

    /**
     * Handles the submission of a page creation request.
     */
    @PostMapping("/page")
    fun pageRegistrationSubmission(
            request: HttpServletRequest,
            response: HttpServletResponse,
            @ModelAttribute page: Page
    ) {
        val adminId = request.profile?.id
        if (adminId == null) {
            response.sendRedirect("/")
            return
        }

        val newId = DbEngine.createPage(adminId, page).id
        response.sendRedirect("/account/$newId")
    }
}
