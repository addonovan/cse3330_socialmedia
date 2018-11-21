package com.addonovan.cse3330.controller

import com.addonovan.cse3330.*
import com.addonovan.cse3330.model.Event
import com.addonovan.cse3330.model.Post
import org.springframework.stereotype.Controller
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
            @RequestParam mediaFile: MultipartFile,
            @ModelAttribute post: Post
    ) {
        response.sendRedirect(request.getHeader("referer"))

        // if we have a file, then we'll save it and add it to the post here
        if (!mediaFile.isEmpty) {
            val url = mediaFile.writeAs(UploadType.PostAttachment)
            post.media = Post.MediaBody(url)
        }

        val user = request.profile ?: return
        post.posterId = user.id
        DbEngine.createPost(post)
    }

    @PostMapping("/event")
    fun submitEvent(
            request: Request,
            response: Response,
            @RequestParam name: String,
            @RequestParam description: String,
            @RequestParam location: String,
            @RequestParam startTime: String,
            @RequestParam startDate: String,
            @RequestParam endTime: String,
            @RequestParam endDate: String
    ) {
        val user = request.profile ?: return
        val start = Timestamp.valueOf("$startDate $startTime:00")
        val end = Timestamp.valueOf("$endDate $endTime:00")

        DbEngine.createEvent(Event().apply {
            this.hostId = user.id
            this.name = name
            this.description = description
            this.location = location
            this.startTime = start
            this.endTime = end
        })
    }
}
