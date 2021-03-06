package com.addonovan.cse3330.controller

import com.addonovan.cse3330.*
import com.addonovan.cse3330.model.Account
import com.addonovan.cse3330.model.Emotion
import com.addonovan.cse3330.model.Page
import com.addonovan.cse3330.model.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.sql.Date

/**
 * The controller for general account activities, such as creation and deletion
 * of both pages and profiles.
 */
@Controller
@RequestMapping("/account")
open class AccountController {

    /**
     * Generates a profile overview by the profile's [id].
     */
    @GetMapping(value = ["/{id:[0-9]+}"])
    fun overviewById(
            request: Request,
            model: Model,
            @PathVariable id: Int
    ) = pageOverview(request, model, DbEngine.getAccountById(id))

    /**
     * Generates a profile over by the profile's [username].
     */
    @GetMapping(value = ["/{username:[a-zA-Z_][a-zA-Z0-9_-]*}"])
    fun overviewByUsername(
            request: Request,
            model: Model,
            @PathVariable username: String
    ) = pageOverview(request, model, DbEngine.getProfileByUsername(username))

    @GetMapping("/settings")
    fun settings(
            request: Request,
            model: Model
    ): String {
        val user = request.profile
                ?: return errorPage(model, "You have to be logged in to do that")

        model.addAttribute("user", user)
        return "account/settings"
    }

    @GetMapping("/settings/{pageId:[0-9]+}")
    fun pageSettings(
            request: Request,
            model: Model,
            @PathVariable pageId: Int
    ): String {
        val user = request.profile
                ?: return errorPage(model, "You have to be logged in to do that")

        val page = user.administeredPages.firstOrNull {
            it.id == pageId
        } ?: return errorPage(model, "You don't have access to that page!")

        model.addAttribute("user", user)
        model.addAttribute("page", page)
        return "account/page_settings"
    }

    private fun updateSettings(
            request: Request,
            oldSettings: Account,
            newSettings: Account,
            profileImage: MultipartFile,
            headerImage: MultipartFile
    ) {
        newSettings.isPrivate = request.getParameter("isPrivate") == "on"
        newSettings.isActive = request.getParameter("isActive") == "on"

        // update images
        newSettings.profileImageURL =
                if (profileImage.isEmpty)
                    oldSettings.profileImageURL
                else
                    profileImage.writeAs(UploadType.ProfileImage)

        newSettings.headerImageURL =
                if (headerImage.isEmpty)
                    oldSettings.headerImageURL
                else
                    headerImage.writeAs(UploadType.HeaderImage)
    }

    @PostMapping("/updateSettings/{pageId:[0-9]+}")
    fun updatePageSettings(
            request: Request,
            response: Response,
            newSettings: Page,
            @PathVariable pageId: Int,
            @RequestParam profileImage: MultipartFile,
            @RequestParam headerImage: MultipartFile
    ) {
        response.redirectToReferrer(request)
        val user = request.profile!!
        val page = user.administeredPages.first {
            it.id == pageId
        }
        updateSettings(request, page, newSettings, profileImage, headerImage)
        DbEngine.updatePage(page, newSettings)
    }

    @PostMapping("/updateSettings")
    fun updateSettings(
            request: Request,
            response: Response,
            newSettings: Profile,
            @RequestParam profileImage: MultipartFile,
            @RequestParam headerImage: MultipartFile
    ) {
        response.redirectToReferrer(request)
        val user = request.profile!!
        updateSettings(request, user, newSettings, profileImage, headerImage)
        DbEngine.updateProfile(user, newSettings)
    }

    @PostMapping(value = ["/{id:[0-9+]}/follow"])
    fun followAccount(
            request: Request,
            response: Response,
            @PathVariable id: Int
    ) {
        response.redirectToReferrer(request)

        val user = request.profile!!
        val followee = DbEngine.getAccountById(id)!!
        DbEngine.addFollow(user, followee)
    }

    @PostMapping(value = ["/{id:[0-9]+}/unfollow"])
    fun unfollowAccount(
            request: Request,
            response: Response,
            @PathVariable id: Int
    ) {
        response.redirectToReferrer(request)

        val user = request.profile!!
        val followee = DbEngine.getAccountById(id)!!
        DbEngine.removeFollow(user, followee)
    }

    @PostMapping("/follow/approve/{id:[0-9]+}")
    fun acceptFollowRequest(
            request: Request,
            response: Response,
            @PathVariable id: Int
    ) {
        response.redirectToReferrer(request)

        val user = request.profile!!
        val follower = Account().apply { this.id = id }
        DbEngine.approveFollowRequest(user, follower)
    }

    @PostMapping("/follow/reject/{id:[0-9]+}")
    fun rejectFollowRequest(
            request: Request,
            response: Response,
            @PathVariable id: Int
    ) {
        response.redirectToReferrer(request)

        val user = request.profile!!
        val follower = Account().apply { this.id = id }
        DbEngine.deleteFollowRequest(user, follower)
    }


    /**
     * Updates the [model] with the given [account] information, then returns
     * the name of the relevant template file.
     */
    private fun pageOverview(
            request: Request,
            model: Model,
            account: Account?
    ) = when (account) {
        null -> "account/none"

        else -> {
            val user = request.profile

            val (wallActivity, accountActivity) = when {
                !account.isPrivate
                || user == account
                || (user as Account?) in account.followers
                    -> DbEngine.wallOverview(account) to DbEngine.accountOverview(account)

                else -> listOf<Account>() to listOf<Account>()
            }

            if (account is Page) {
                DbEngine.viewPage(account)
            }

            model.addAttribute("account", account)
            model.addAttribute("wallActivity", wallActivity.reversed())
            model.addAttribute("accountActivity", accountActivity.reversed())
            model.addAttribute("user", user)
            model.addAttribute("emotions", Emotion.values)
            "account/overview"
        }
    }

    //
    // API Stuff
    //

    @GetMapping("/api/search")
    @ResponseBody
    fun searchAccounts(@RequestParam text: String): List<Account> =
            DbEngine.getAllAccounts()
                    .filter {
                        val regex = ".*$text.*".toRegex()

                        if (it.fullName.matches(regex)) true
                        else it is Profile && it.username.matches(regex)
                    }

    @GetMapping("/api/postCountByDate")
    @ResponseBody
    fun getPostCountByDate(
            @RequestParam wallId: Int,
            @RequestParam date: String
    ): Int {
        val wall = Account().apply { id = wallId }
        return DbEngine.getPostCountByDate(wall, Date.valueOf(date))
    }

}
