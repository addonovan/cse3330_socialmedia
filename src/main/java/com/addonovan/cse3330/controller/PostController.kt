package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import com.addonovan.cse3330.model.Account
import com.addonovan.cse3330.model.Post
import com.addonovan.cse3330.profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Controller which handles actions specific to the
 * [Post][com.addonovan.cse3330.model.Post] class.
 */
@Controller
@RequestMapping("/post")
open class PostController {

    /**
     * Handles the process of creating a new [post], posted by account with the
     * given [posterId] onto the account's wall with the given [wallId].
     */
    @PostMapping("/submit")
    fun submitPost(
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam wallId: Int,
            @ModelAttribute post: Post
    ) {
        response.sendRedirect(request.requestURL.toString())
        val account = request.profile ?: return
        DbEngine.createPost(account, wallId, post)
    }

}
