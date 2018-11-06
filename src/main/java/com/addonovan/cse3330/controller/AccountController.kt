package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import com.addonovan.cse3330.model.Page
import com.addonovan.cse3330.model.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
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
        model.addAttribute("accountId", 1) // TODO unhardcode this?
        return "create_page"
    }

    @PostMapping("/createPage")
    fun pageCreationSubmit(
            response: HttpServletResponse,
            @ModelAttribute accountId: Int,
            @ModelAttribute page: Page
    ) {
        val newId = DbEngine.createPage(accountId, page).id
        response.sendRedirect("/page/$newId")
    }

}
