package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import com.addonovan.cse3330.model.Post
import com.addonovan.cse3330.profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
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
     * Handles the process of creating a new [post] on a different wall.
     */
    @PostMapping("/submit")
    fun submitPost(
            request: HttpServletRequest,
            response: HttpServletResponse,
            @ModelAttribute post: Post
    ) {
        response.sendRedirect(request.requestURL.toString())

        val user = request.profile ?: return
        post.posterId = user.id
        DbEngine.createPost(post)
    }

    /**
     * Handles the process of creating a new [post] on the user's own profile.
     */
    @PostMapping("/selfpost")
    fun submitSelfPost(
            request: HttpServletRequest,
            response: HttpServletResponse,
            @ModelAttribute post: Post
    ) {
        response.sendRedirect(request.requestURL.toString())

        val user = request.profile ?: return
        post.posterId = user.id
        post.wallId = user.id
        DbEngine.createPost(post)
    }
}
