package com.addonovan.cse3330

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

typealias Request = HttpServletRequest
typealias Response = HttpServletResponse

fun Response.redirectToReferrer(request: Request) =
    sendRedirect(request.getHeader("referer").toString())
