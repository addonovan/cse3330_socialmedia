package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import com.addonovan.cse3330.model.Page
import com.addonovan.cse3330.model.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@Controller
@RequestMapping("/account")
open class AccountController {

    @GetMapping("/register")
    fun registrationForm(model: Model): String {
        model.addAttribute("profile", Profile())
        return "create_profile"
    }

    @PostMapping("/register")
    fun registrationSubmit(
            response: HttpServletResponse,
            @ModelAttribute profile: Profile
    ) {
        val newId = DbEngine.createProfile(profile).id
        response.sendRedirect("/profile/$newId")
    }

    @GetMapping("/createPage")
    fun pageCreationForm(model: Model): String {
        model.addAttribute("page", Page())
        return "create_page"
    }

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
