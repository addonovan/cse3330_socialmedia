package com.addonovan.cse3330.controller

import com.addonovan.cse3330.*
import com.addonovan.cse3330.model.Group
import com.addonovan.cse3330.model.GroupMessage
import com.addonovan.cse3330.model.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping("/chat")
open class ChatController {

    @GetMapping
    fun index(request: Request, model: Model): String {
        val user = request.profile
                ?: errorPage(model, "You have to be logged in to do that!")

        model.addAttribute("user", user)
        return "chat/index"
    }

    @GetMapping("/create")
    fun createChat(request: Request, model: Model): String {
        val user = request.profile
                ?: errorPage(model, "You have to be logged in to do that!")

        model.addAttribute("user", user)
        return "chat/create_chat"
    }

    @PostMapping("/create")
    @ResponseBody
    fun createChat(
            request: Request,
            @RequestParam name: String,
            @RequestParam description: String
    ) {
        val user = request.profile!!

        val group = DbEngine.createGroup(Group().apply {
            this.name = name
            this.description = description
        })

        DbEngine.addGroupMember(group, user)

        for (i in 0..Int.MAX_VALUE) {
            val profileId = request.getParameter("member$i")?.toInt() ?: break
            val profile = Profile().apply{ id = profileId }
            DbEngine.addGroupMember(group, profile)
        }
    }

    @PostMapping("/api/updateGroup")
    fun updateChatSettings(
            request: Request,
            response: Response,
            model: Model,
            newSettings: Group,
            @RequestParam groupImage: MultipartFile
    ): String {
        val user = request.profile
                ?: return errorPage(model, "You have to be logged in to do that!")
        val group = DbEngine.getGroupById(newSettings.id)
                ?: return errorPage(model, "That group does not exist :/")

        if (user !in group.members) {
            return errorPage(model, "You don't have access to that group!")
        }

        DbEngine.updateGroup(group.apply {
            name = newSettings.name
            description = newSettings.description
            pictureUrl =
                    if (groupImage.isEmpty)
                        pictureUrl
                    else
                        groupImage.writeAs(UploadType.GroupImage)
        })
        response.redirectToReferrer(request)
        return errorPage(model, "You really shouldn't be seeing this")
    }

    //
    // API
    //

    @GetMapping("/api/group/{groupId:[0-9]+}")
    @ResponseBody
    fun getGroup(
            request: Request,
            @PathVariable groupId: Int
    ): Group {
        val user = request.profile!!
        val group = DbEngine.getGroupById(groupId)!!

        if (user !in group.members) {
            throw RuntimeException("You don't have access to that group!")
        }

        return group
    }

    @GetMapping("/api/messages/{groupId:[0-9]+}")
    @ResponseBody
    fun listMessages(
            request: Request,
            @PathVariable groupId: Int
    ): List<GroupMessage> {
        val user = request.profile!!
        val group = DbEngine.getGroupById(groupId)!!

        if (user !in group.members) {
            throw RuntimeException("You don't have access to that group!")
        }

        return group.messages
    }

    @GetMapping("/api/members/{groupId:[0-9]+}")
    @ResponseBody
    fun listMembers(
            request: Request,
            @PathVariable groupId: Int
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

    @PostMapping("/api/send")
    @ResponseBody
    fun sendMessage(
            request: Request,
            response: Response,
            @RequestParam groupId: Int,
            @RequestParam messageText: String,
            @RequestParam mediaFile: MultipartFile
    ) {
        response.redirectToReferrer(request)

        val user = request.profile!!
        val group = DbEngine.getGroupById(groupId)!!

        if (user !in group.members) {
            throw RuntimeException("You don't have access to that group!")
        }

        val message = GroupMessage().apply {
            this.message = messageText

            // if we have a file, then we'll save it and add it to the post here
            if (!mediaFile.isEmpty) {
                mediaUrl = mediaFile.writeAs(UploadType.PostAttachment)
            }
        }

        DbEngine.sendGroupMessage(user, group, message)
    }

}
