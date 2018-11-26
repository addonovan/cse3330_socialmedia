package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import com.addonovan.cse3330.Request
import com.addonovan.cse3330.model.Group
import com.addonovan.cse3330.model.GroupMessage
import com.addonovan.cse3330.model.Profile
import com.addonovan.cse3330.profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/chat")
open class ChatController {

    @GetMapping("/")
    fun index(request: Request, model: Model): String {
        val user = request.profile
                ?: errorPage(model, "You have to be logged in to do that!")

        return "chat/index"
    }

    //
    // API
    //

    @GetMapping("/api/group/{groupId:[0-9]+}")
    fun getGroup(
            request: Request,
            @RequestParam groupId: Int
    ): Group {
        val user = request.profile!!
        val group = DbEngine.getGroupById(groupId)!!

        if (user !in group.members) {
            throw RuntimeException("You don't have access to that group!")
        }

        return group
    }

    @GetMapping("/api/messages/{groupId:[0-9]+}")
    fun listMessages(
            request: Request,
            @RequestParam groupId: Int
    ): List<GroupMessage> {
        val user = request.profile!!
        val group = DbEngine.getGroupById(groupId)!!

        if (user !in group.members) {
            throw RuntimeException("You don't have access to that group!")
        }

        return group.messages
    }

    @GetMapping("/api/members/{groupId:[0-9]+}")
    fun listMembers(
            request: Request,
            @RequestParam groupId: Int
    ): Map<Int, Profile> {
        val user = request.profile!!
        val group = DbEngine.getGroupById(groupId)!!

        if (user !in group.members) {
            throw RuntimeException("You don't have access to that group!")
        }

        return group.members.map {
            it.id to it
        }.toMap()
    }

}
