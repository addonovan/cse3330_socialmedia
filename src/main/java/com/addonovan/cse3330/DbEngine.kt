package com.addonovan.cse3330

import com.addonovan.cse3330.model.*
import com.addonovan.cse3330.sql.call
import com.addonovan.cse3330.sql.map
import com.addonovan.cse3330.sql.query
import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

/**
 * The main facade around JDBC.
 *
 * This singleton opens and maintains a connection to the PostgreSQL 10.5
 * database server running the SocialMedia database. The `DbEngine` instance
 * allows for access to the database through simple functions that invoke
 * functions (whose contents are available in
 * `/src/main/resources/db/sp_up.sql`) which are stored on the database.
 *
 * @see [FunctionCall][com.addonovan.cse3330.sql.FunctionCall]
 */
object DbEngine {

    /** A URL used to open a connection to the SocialMedia database.  */
    private const val CONNECTION_STRING = "jdbc:postgresql://localhost/SocialMedia"

    /** The actual database connection.  */
    private val CONNECTION: Connection

    init {
        val props = Properties()
        props.setProperty("user", "application")
        props.setProperty("password", "password1") // oh boy! check that security!

        // if there's a problem connecting to the database, then we'll immediately
        // throw an exception and die off
        try {
            CONNECTION = DriverManager.getConnection(CONNECTION_STRING, props)
        } catch (e: SQLException) {
            throw RuntimeException("Failed to open database connection!", e)
        }

        // make sure the connection is (properly) closed on shutdown, not that
        // it *really* matters all that much
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                CONNECTION.close()
            } catch (e: SQLException) {
                throw RuntimeException("Failed to close database connection!", e)
            }
        })
    }

    //
    // Account Finding
    //

    @Language("PostgreSQL")
    private val FIND_ACCOUNT_BY_ID: String =
            """
            SELECT * FROM "Account" a
            LEFT JOIN "Profile" prof ON PROF.accountid = a.accountid
            LEFT JOIN "Page" page ON page.accountid = a.accountid
            WHERE a.accountid = ?;
            """.trimIndent()

    /**
     * Generalization of the [getProfileById] and [getPageById], this will find
     * any [Account][com.addonovan.cse3330.model.Account] by the Id. This is
     * useful when you just need to display *some* of the information of the
     * account (i.e. its name and profile * picture but have no use for the
     * other information).
     *
     * @see [getProfileById]
     * @see [getProfileByUsername]
     * @see [getPageById]
     */
    fun getAccountById(id: Int) = query(FIND_ACCOUNT_BY_ID)
            .supply(id)
            .executeOn(CONNECTION) {
                if (!it.next())
                    null
                else if (it.getString("FirstName") != null)
                    Profile().apply { fromRow(it) }
                else if (it.getString("PageName") != null)
                    Page().apply { fromRow(it) }
                else
                    throw IllegalStateException("Inconsistent database state: Account(id=$id) is neither a Profile nor a Page!")
            }

    /**
     * Gets the [Profile][com.addonovan.cse3330.model.Profile] by the given `id`.
     *
     * @return `null` if no profile has the given `id`, which may mean the
     * account does not exist, is inactive, or is a page instead.
     *
     * @see [getAccountById]
     * @see [getProfileByUsername]
     */
    fun getProfileById(id: Int) = getAccountById(id) as? Profile?

    /**
     * Gets the [Page] with the given `id`. Note that there is not corresponding
     * function for finding a page by its name, as the page names are not
     * required to be unique.
     *
     * @return `null` if no page with the given `id` existed, which may mean the
     * account does not exist, is inactive, or is a page instead.
     *
     * @see [getAccountById]
     * @see [viewPage]
     */
    fun getPageById(id: Int) = getAccountById(id) as? Page?

    @Language("PostgreSQL")
    private val FIND_ACCOUNT_BY_USERNAME: String =
            """
            SELECT * FROM "Profile" prof
            LEFT JOIN "Account" a ON a.accountid = prof.accountid
            WHERE prof.username = ?;
            """.trimIndent()

    /**
     * Gets the [Profile] with the given `username`.
     *
     * @return `null` if no profile has the given `username`, which may mean
     * that the profile does not exist or is inactive.
     *
     * @see [getAccountById]
     * @see [getProfileById]
     */
    fun getProfileByUsername(username: String) = query(FIND_ACCOUNT_BY_USERNAME)
            .supply(username)
            .executeOn(CONNECTION) {
                if (it.next())
                    Profile().apply { fromRow(it) }
                else
                    null
            }

    /**
     * Updates the relationship between the [follower] and [followee]. If
     * [following] is set to `true`, then the `follower` will attempt to
     * follow the given `followee`; otherwise, it will unfollow them.
     *
     * If the `followee` is private and the `follower` is trying to follow them,
     * then a follow request will be added to the account.
     *
     * @see [getFollowers]
     */
    fun updateFollow(follower: Account, followee: Account, following: Boolean) =
            call("UpdateFollow")
                    .supply(follower.id)
                    .supply(followee.id)
                    .supply(following)
                    .executeOn(CONNECTION) {}

    /**
     * Gets a list of all accounts which follow the given [account]. This can
     * also return only the follow requests for the given account, should
     * [requests] be `true`.
     *
     * @return A list of all accounts which follow, or are requesting to follow,
     * the given [account].
     *
     * @see [getFollowing]
     * @see [updateFollow]
     * @see [getAccountById]
     */
    fun getFollowers(account: Account, requests: Boolean = false) = call("FindFollowers")
            .supply(account.id)
            .supply(requests)
            .executeOn(CONNECTION) {
                it.map {
                    getAccountById(it.getInt("FollowerId"))!!
                }
            }

    /**
     * Gets a list of all accounts which this [account] is following.
     *
     * @see [getFollowers]
     * @see [updateFollow]
     * @see [getAccountById]
     */
    fun getFollowing(account: Account) = call("FindFollowing")
            .supply(account.id)
            .executeOn(CONNECTION) {
                it.map {
                    getAccountById(it.getInt("FolloweeId"))!!
                }
            }

    /**
     * Views the page as to increment the number of page views.
     *
     * @see [getPageById]
     */
    fun viewPage(id: Int) = call("ViewPage")
            .supply(id)
            .executeOn(CONNECTION) {}

    /**
     * Generates the feed for the [account].
     *
     * The feed is a list of all of the account's activity along with the
     * activity from the accounts they follow. This is shown on the homepage.
     *
     * @see [createPost]
     * @see [wallOverview]
     */
    fun feedFor(account: Account) = call("FindFeedFor")
            .supply(account.id)
            .executeOn(CONNECTION) { set ->
                set.map { row ->
                    Post().apply { fromRow(row) }
                }
            }

    /**
     * Generates a list of calendar [events][Event] for the given [account].
     */
    fun calendarFor(account: Account) = call("FindCalendarFor")
            .supply(account.id)
            .executeOn(CONNECTION) { set ->
                set.map { row ->
                    Event().apply { fromRow(row) }
                }
            }

    /**
     * Constructs a wall overview (i.e. a list of
     * [Posts][com.addonovan.cse3330.model.Post]) of the given `account`. Note
     * that this is *anything* that the query deems relevant; therefore, the
     * posts returned might not even be on the *wall* for the given account.
     *
     * @see [createPost]
     * @see [feedFor]
     */
    fun wallOverview(account: Account) = call("FindWallOverviewFor")
            .supply(account.id)
            .executeOn(CONNECTION) {
                it.map {
                    Post().apply { fromRow(it) }
                }
            }

    /**
     * Creates a new `post` on the wall with the given `wallID`, authored by
     * the given `account`.
     */
    fun createPost(post: Post) = call("CreatePost")
            .supply(post.posterId)
            .supply(post.wallId)
            .supply(post.text?.body)
            .supply(post.media?.url)
            .supply(post.poll?.question)
            .supply(post.poll?.endTime)
            .supply(post.parentPostId)
            .executeOn(CONNECTION) {
                if (!it.next())
                    throw RuntimeException("No result from CreatePost call!")

                it.getInt(1)
            }

    /**
     * Inserts the new [profile][com.addonovan.cse3330.model.Profile] into the
     * database and returns the model built from querying the database with its
     * id after insertion.
     */
    fun createProfile(profile: Profile) = call("CreateProfile")
            .supply(profile.email)
            .supply(profile.phoneNumber)
            .supply(profile.firstName)
            .supply(profile.lastName)
            .supply(profile.username)
            .supply(profile.password)
            .executeOn(CONNECTION) {
                if (!it.next())
                    throw RuntimeException("No result from CreateProfile call!")

                getProfileById(it.getInt(1))!!
            }

    /**
     * Inserts the new [page][com.addonovan.cse3330.model.Page] into the
     * database and returns the model built from querying the database with its
     * id after insertion.
     */
    fun createPage(profileId: Int, page: Page) = call("CreatePage")
            .supply(profileId)
            .supply(page.email)
            .supply(page.phoneNumber)
            .supply(page.name)
            .supply(page.description)
            .executeOn(CONNECTION) {
                if (!it.next())
                    throw RuntimeException("No result from CreatePage call!")

                getPageById(it.getInt(1))!!
            }

    /**
     * Finds the event that has the given [id], if any exist.
     */
    fun getEventById(id: Int) = call("FindEvent")
            .supply(id)
            .executeOn(CONNECTION) {
                if (it.next())
                    Event().apply { fromRow(it) }
                else
                    null
            }

    /**
     * Inserts the provided [event] into the database, then returns the
     * newly created event object.
     */
    fun createEvent(event: Event) = call("CreateEvent")
            .supply(event.hostId)
            .supply(event.name)
            .supply(event.description)
            .supply(event.startTime)
            .supply(event.endTime)
            .supply(event.location)
            .executeOn(CONNECTION) {
                if (!it.next())
                    throw RuntimeException("No result from CreateEvent call!")

                getEventById(it.getInt(1))!!
            }

    fun deleteEvent(event: Event) = call("DeleteEvent")
            .supply(event.id)
            .executeOn(CONNECTION) {}

    fun markEventInterest(
            user: Profile,
            eventId: Int,
            onlyInterested: Boolean
    ) = call("MarkEventInterest")
            .supply(user.id)
            .supply(eventId)
            .supply(onlyInterested)
            .executeOn(CONNECTION) {}

    fun getAttendees(eventId: Int): List<Profile> = call("GetEventInterest")
            .supply(eventId)
            .supply(true)
            .executeOn(CONNECTION) { set ->
                set.map { row ->
                    val profileId = row.getInt(1)
                    getProfileById(profileId)!!
                }
            }

    fun getProspectiveAttendees(eventId: Int): List<Profile> = call("GetEventInterest")
            .supply(eventId)
            .supply(false)
            .executeOn(CONNECTION) { set ->
                set.map { row ->
                    val profileId = row.getInt(1)
                    getProfileById(profileId)!!
                }
            }

    fun updateProfile(user: Profile, newSettings: Profile) = call("UpdateProfile")
            .supply(user.id)
            .supply(newSettings.email)
            .supply(newSettings.phoneNumber)
            .supply(newSettings.firstName)
            .supply(newSettings.lastName)
            .supply(newSettings.username)
            .supply(newSettings.password)
            .executeOn(CONNECTION) {}

    fun getPagesByAdmin(id: Int) = call("FindAdminedPages")
            .supply(id)
            .executeOn(CONNECTION) { set ->
                set.map { row ->
                    val pageId = row.getInt(1)
                    getPageById(pageId)!!
                }
            }

    fun getRepliesTo(postId: Int) = call("FindRepliesToPost")
            .supply(postId)
            .executeOn(CONNECTION) { set ->
                set.map { row ->
                    Post().apply { fromRow(row) }
                }
            }

    fun getEmotionByName(emotionName: String) = call("FindEmotionByName")
            .supply(emotionName)
            .executeOn(CONNECTION) {
                if (!it.next())
                    throw RuntimeException("No emotion by name: $emotionName")

                Emotion().apply { fromRow(it) }
            }

    fun getReactionsTo(eventId: Int) = call("FindReactionsTo")
            .supply(eventId)
            .executeOn(CONNECTION) { set ->
                val mapEntries = set.map { row ->
                    val profile = getProfileById(row.getInt(1))!!
                    val emotion = Emotion[row.getInt(2)]
                    Pair(profile, emotion)
                }
                mapEntries.toMap()
            }

    fun addReaction(postId: Int, userId: Int, emotionId: Int) = call("AddReaction")
            .supply(postId)
            .supply(userId)
            .supply(emotionId)
            .executeOn(CONNECTION) {}

}
