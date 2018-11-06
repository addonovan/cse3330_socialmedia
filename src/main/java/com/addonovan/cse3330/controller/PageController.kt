package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/page")
open class PageController {

    @GetMapping("{id:[0-9]+}")
    fun pageOverview(@PathVariable("id") id: Int, model: Model): String {
        val page = DbEngine.getPageById(id) ?: return "no_page"
        model.addAttribute("page", page)
        return "page_overview"
    }

}
