package com.addonovan.cse3330

import com.addonovan.cse3330.model.Profile
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/** The name of the login cookie. */
private const val LOGIN_PROFILE_COOKIE_NAME = "TotallyInsecureLoggedProfileId"

/**
 * The profile currently signed in, as per the cookies on the given
 * [HttpServletRequest]. Hopefully the end-user doesn't know how to edit their
 * cookies lol.
 */
val HttpServletRequest.profile: Profile?
    get() {
        val cookie = this.cookies.find {
            it.name == LOGIN_PROFILE_COOKIE_NAME
        } ?: return null

        val profileId = cookie.value.toIntOrNull() ?: return null
        return DbEngine.getProfileById(profileId)
    }

/**
 * Attaches a cookie that marks the given [profile] as signed in. yes, this is
 * hilariously insecure because the person can just change the number in the
 * cookies on their end, but I don't really care this is a school project.
 */
fun HttpServletResponse.signIn(profile: Profile) {
    val cookie = Cookie(LOGIN_PROFILE_COOKIE_NAME, profile.id.toString())
    this.addCookie(cookie)
}
