package com.addonovan.cse3330.controller

import com.addonovan.cse3330.Request
import com.addonovan.cse3330.profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/chat")
open class ChatController {

    @GetMapping("/")
    fun index(request: Request, model: Model): String {
        val user = request.profile
                ?: errorPage(model, "You have to be logged in to do that!")

        return "chat/index"
    }

}
