package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import com.addonovan.cse3330.model.Account
import com.addonovan.cse3330.model.Post
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/post")
open class PostController {

    @PostMapping("/submit")
    fun submitPost(
            @RequestParam("posterId") posterId: Int,
            @ModelAttribute post: Post
    ) {
        val account = Account().apply {
            id = posterId
        }
        DbEngine.createPost(account, post)
    }

}
