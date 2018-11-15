package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import com.addonovan.cse3330.UploadType
import com.addonovan.cse3330.model.Post
import com.addonovan.cse3330.profile
import com.addonovan.cse3330.writeAs
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
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
            @RequestParam mediaFile: MultipartFile?,
            @ModelAttribute post: Post
    ) {
        response.sendRedirect(request.getHeader("referer"))

        // if we have a file, then we'll save it and add it to the post here
        mediaFile?.writeAs(UploadType.PostAttachment)?.let {
            post.media = Post.MediaBody(it)
        }

        val user = request.profile ?: return
        post.posterId = user.id
        DbEngine.createPost(post)
    }
}
