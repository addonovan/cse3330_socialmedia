package com.addonovan.cse3330.controller

import org.springframework.ui.Model

fun errorPage(model: Model, message: String): String {
    model.addAttribute("errorReason", message)
    return "error_page"
}

