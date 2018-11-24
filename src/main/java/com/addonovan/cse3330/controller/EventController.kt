package com.addonovan.cse3330.controller

import com.addonovan.cse3330.*
import com.addonovan.cse3330.model.Event
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.sql.Timestamp

@Controller
@RequestMapping("/event")
open class EventController {

    @PostMapping("/submit")
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
        response.redirectToReferrer(request)

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
