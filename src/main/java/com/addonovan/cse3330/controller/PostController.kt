package com.addonovan.cse3330.controller

import com.addonovan.cse3330.*
import com.addonovan.cse3330.model.Emotion
import com.addonovan.cse3330.model.Event
import com.addonovan.cse3330.model.Post
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
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
            model: Model,
            @RequestParam mediaFile: MultipartFile,
            @ModelAttribute post: Post
    ): String {
        val user = request.profile
                ?: return errorPage(model, "You must be logged in to do that")

        response.sendRedirect(request.getHeader("referer"))

        // if we have a file, then we'll save it and add it to the post here
        if (!mediaFile.isEmpty) {
            val url = mediaFile.writeAs(UploadType.PostAttachment)
            post.media = Post.MediaBody(url)
        }

        // default the post to be created by the current user, unless they
        // got to select the posting account on their end. Then just check to
        // make sure they're allowed to do that.
        if (post.posterId == 0) {
            post.posterId = user.id
        } else if (user.administeredPages.none { it.id == post.posterId }) {
            return errorPage(model, "You don't have permission to post from that account!")
        }

        DbEngine.createPost(post)

        return errorPage(model, "You shouldn't really see this")
    }

    @PostMapping("/react")
    fun reactTo(
            request: Request,
            @RequestParam postId: Int,
            @RequestParam emotionId: Int
    ) {
        val user = request.profile!!
        val post = Post().apply { id = postId }
        val emotion = Emotion[emotionId]

        DbEngine.addReaction(post, user, emotion)
    }

}
