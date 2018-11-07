package com.addonovan.cse3330.controller

import com.addonovan.cse3330.DbEngine
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Controller which handles all of the requests specific to a
 * [Page][com.addonovan.cse3330.model.Page].
 */
@Controller
@RequestMapping("/page")
open class PageController {

    /**
     * Shows a page overview for the page with the given [id].
     */
    @GetMapping("{id:[0-9]+}")
    fun pageOverview(@PathVariable("id") id: Int, model: Model): String {
        val page = DbEngine.getPageById(id) ?: return "no_page"
        val overview = DbEngine.wallOverview(page)
        DbEngine.viewPage(page.id)

        model.addAttribute("page", page)
        model.addAttribute("overview", overview)
        return "page_overview"
    }

}
