package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import com.addonovan.cse3330.signIn
import com.addonovan.cse3330.signOut
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletResponse

@Controller
open class SignInController {

    @GetMapping("/signin")
    fun signInForm() = "signin/login.pebble"

    @PostMapping("/signin")
    fun signIn(
            model: Model,
            response: HttpServletResponse,
            @RequestParam username: String,
            @RequestParam password: String
    ): String {
        val user = DbEngine.getProfileByUsername(username)

        // if the signin failed, just send 'em back to the login screen
        if (user == null || user.password != password) {
            model.addAttribute("errorReason", "Invalid username or password!")
            return "signin/login.pebble"
        }

        // otherwise, log the user in, then send them to the homepage
        response.signIn(user)
        response.sendRedirect("/")
        return "signin/success.pebble"
    }

    @PostMapping("/signout")
    fun signOut(response: HttpServletResponse) {
        // delete the cookie, then send them to the home page
        response.signOut()
        response.sendRedirect("/")
    }


}
