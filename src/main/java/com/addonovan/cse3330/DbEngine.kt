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
                else
                    Account.fromRow(it)
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

    //
    // Following Actions
    //

    @Suppress("SqlResolve")
    @Language("PostgreSQL")
    private val REMOVE_FOLLOW_FORMAT: String =
            """
            DELETE FROM "%s" f
            WHERE f.followerid = ?
              AND f.followeeid = ?;
            """.trimIndent()

    fun removeFollow(follower: Account, followee: Account) {
        val queries = arrayOf(
                query(REMOVE_FOLLOW_FORMAT.format("Follow")),
                query(REMOVE_FOLLOW_FORMAT.format("FollowRequest"))
        )

        for (query in queries) {
            query.supply(follower.id)
                    .supply(followee.id)
                    .executeOn(CONNECTION) {}
        }
    }

    @Suppress("SqlResolve")
    @Language("PostgreSQL")
    private val ADD_FOLLOW_FORMAT: String =
            """
            INSERT INTO "%s" (followerid, followeeid)
            VALUES (?, ?);
            """.trimIndent()

    /**
     * @see [getFollowers]
     */
    fun addFollow(follower: Account, followee: Account) {
        val tableName = when (followee.isPrivate) {
            true -> "FollowRequest"
            false -> "Follow"
        }

        query(ADD_FOLLOW_FORMAT.format(tableName))
                .supply(follower.id)
                .supply(followee.id)
                .executeOn(CONNECTION) {}
    }

    @Language("PostgreSQL")
    private val GET_FOLLOWERS_FORMAT: String =
            """
            SELECT * FROM "%s" f
            INNER JOIN "Account" a ON a.accountid = f.followerid
            LEFT JOIN "Profile" prof ON prof.accountid = a.accountid
            LEFT JOIN "Page" page ON page.accountid = a.accountid
            WHERE f.followeeid = ?;
            """

    fun getFollowers(account: Account) = query(GET_FOLLOWERS_FORMAT.format("Follow"))
            .supply(account.id)
            .executeOn(CONNECTION) { set ->
                set.map {
                    Account.fromRow(it)
                }
            }

    fun getFollowRequests(account: Account) = query(GET_FOLLOWERS_FORMAT.format("FollowRequest"))
            .supply(account.id)
            .executeOn(CONNECTION) { set ->
                set.map {
                    Account.fromRow(it)
                }
            }

    @Language("PostgreSQL")
    private val GET_FOLLOWING: String =
            """
            SELECT * FROM "Follow" f
            INNER JOIN "Account" a ON a.accountid = f.followeeid
            LEFT JOIN "Profile" prof ON prof.accountid = a.accountid
            LEFT JOIN "Page" page ON page.accountid = a.accountid
            WHERE f.followerid = ?;
            """.trimIndent()

    /**
     * Gets a list of all accounts which this [account] is following.
     *
     * @see [getFollowers]
     * @see [getAccountById]
     */
    fun getFollowing(account: Account) = query(GET_FOLLOWING)
            .supply(account.id)
            .executeOn(CONNECTION) { set ->
                set.map {
                    Account.fromRow(it)
                }
            }

    //
    // Account Overviews
    //

    @Language("PostgreSQL")
    private val WALL_OVERVIEW_FOR: String =
            """
            SELECT * FROM "Post" p
            WHERE p.wallid = ?
            ORDER BY p.createdtime DESC;
            """.trimIndent()

    /**
     * Constructs a wall overview (i.e. a list of
     * [Posts][com.addonovan.cse3330.model.Post]) of the given `account`. Note
     * that this is *anything* that the query deems relevant; therefore, the
     * posts returned might not even be on the *wall* for the given account.
     *
     * @see [createPost]
     * @see [feedFor]
     */
    fun wallOverview(account: Account) = query(WALL_OVERVIEW_FOR)
            .supply(account.id)
            .executeOn(CONNECTION) { set ->
                set.map {
                    Post().apply { fromRow(it) }
                }
            }

    @Language("PostgreSQL")
    private val VIEW_PAGE: String =
            """
            UPDATE "Page" p
            SET viewcount = p.viewcount + 1
            WHERE p.accountid = ?;
            """.trimIndent()

    /**
     * Views the page as to increment the number of page views.
     *
     * @see [getPageById]
     */
    fun viewPage(page: Page) = query(VIEW_PAGE)
            .supply(page.id)
            .executeOn(CONNECTION) {}

    //
    // User's Display
    //

    @Language("PostgreSQL")
    private val GET_ACCOUNT_FEED: String =
            """
            SELECT * FROM "Post" p
            WHERE p.wallid = ?
               OR (
                    p.parentpostid IS NULL AND
                    p.posterid IN (
                        SELECT followeeid FROM "Follow" f
                        WHERE f.followerid = ?
                    )
               )
            ORDER BY p.createdtime DESC;
            """.trimIndent()

    /**
     * Generates the feed for the [account].
     *
     * The feed is a list of all of the account's activity along with the
     * activity from the accounts they follow. This is shown on the homepage.
     *
     * @see [createPost]
     * @see [wallOverview]
     */
    fun feedFor(account: Account) = query(GET_ACCOUNT_FEED)
            .supply(account.id)
            .supply(account.id)
            .executeOn(CONNECTION) { set ->
                set.map { row ->
                    Post().apply { fromRow(row) }
                }
            }

    @Language("PostgreSQL")
    private val GET_ACCOUNT_CALENDAR: String =
            """
            SELECT * FROM "Event" e
            WHERE e.hostid = ?
               OR e.hostid IN (
                    SELECT followeeid FROM "Follow" f
                    WHERE f.followerid = ?
               )
            """.trimIndent()

    /**
     * Generates a list of calendar [events][Event] for the given [account].
     */
    fun calendarFor(account: Account) = query(GET_ACCOUNT_CALENDAR)
            .supply(account.id)
            .supply(account.id)
            .executeOn(CONNECTION) { set ->
                set.map { row ->
                    Event().apply { fromRow(row) }
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
