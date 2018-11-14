package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import com.addonovan.cse3330.model.Page
import com.addonovan.cse3330.model.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@Controller
@RequestMapping("/register")
open class RegistrationController {

    /**
     * Provides the form to register a new profile.
     */
    @GetMapping("/profile")
    fun profileRegistrationForm(model: Model): String {
        model.addAttribute("profile", Profile())
        return "register/profile"
    }

    /**
     * Handles the submission of a profile registration request.
     */
    @PostMapping("/profile")
    fun profileRegistrationSubmission(
            response: HttpServletResponse,
            @ModelAttribute profile: Profile
    ) {
        val newId = DbEngine.createProfile(profile).id
        response.sendRedirect("/account/$newId")
    }

    /**
     * Provides the form to create a new page.
     */
    @GetMapping("/page")
    fun pageRegistrationForm(model: Model): String {
        model.addAttribute("page", Page())
        return "register/page"
    }

    /**
     * Handles the submission of a page creation request.
     */
    @PostMapping("/page")
    fun pageRegistartionSubmission(
            response: HttpServletResponse,
            @RequestParam adminId: Int,
            @ModelAttribute page: Page
    ) {
        val newId = DbEngine.createPage(adminId, page).id
        response.sendRedirect("/account/$newId")
    }
}
