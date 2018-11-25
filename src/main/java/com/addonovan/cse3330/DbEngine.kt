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
    // Account Actions
    //

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

                profile.apply {
                    id = it.getInt(1)
                }
            }

    /**
     * Inserts the new [page][com.addonovan.cse3330.model.Page] into the
     * database and returns the model built from querying the database with its
     * id after insertion.
     */
    fun createPage(admin: Profile, page: Page) = call("CreatePage")
            .supply(admin.id)
            .supply(page.email)
            .supply(page.phoneNumber)
            .supply(page.name)
            .supply(page.description)
            .executeOn(CONNECTION) {
                if (!it.next())
                    throw RuntimeException("No result from CreatePage call!")

                page.apply {
                    it.getInt(1)
                }
            }

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

    @Language("PostgreSQL")
    private val UPDATE_ACCOUNT: String =
            """
            UPDATE "Account"
            SET email = ?, phonenumber = ?, isprivate = ?
            WHERE accountid = ?;
            """.trimIndent()

    @Language("PostgreSQL")
    private val UPDATE_PROFILE: String =
            """
            UPDATE "Profile"
            SET firstname = ?, lastname = ?, username = ?, password = ?
            WHERE accountid = ?;
            """.trimIndent()

    fun updateProfile(user: Profile, newSettings: Profile) {
        query(UPDATE_ACCOUNT)
                .supply(newSettings.email)
                .supply(newSettings.phoneNumber)
                .supply(newSettings.isPrivate)
                .supply(user.id)
                .executeOn(CONNECTION)

        query(UPDATE_PROFILE)
                .supply(newSettings.firstName)
                .supply(newSettings.lastName)
                .supply(newSettings.username)
                .supply(newSettings.password)
                .supply(user.id)
                .executeOn(CONNECTION)
    }

    @Language("PostgreSQL")
    private val GET_PAGES_BY_ADMIN: String =
            """
            SELECT * FROM "PageAdmin" pa
            INNER JOIN "Account" a ON a.accountid = pa.pageid
            INNER JOIN "Page" p ON p.accountid = a.accountid
            WHERE pa.profileid = ?;
            """.trimIndent()

    fun getPagesByAdmin(admin: Profile) = query(GET_PAGES_BY_ADMIN)
            .supply(admin.id)
            .executeOn(CONNECTION) { set ->
                set.map {
                    Page().apply { fromRow(it) }
                }
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
                    .executeOn(CONNECTION)
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
                .executeOn(CONNECTION)
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

    @Language("PostgreSQL")
    private val DELETE_FOLLOW_REQUEST: String =
            """
            DELETE FROM "FollowRequest" fr
            WHERE fr.followerid = ? AND fr.followeeid = ?;
            """.trimIndent()

    fun deleteFollowRequest(followee: Account, follower: Account) =
            query(DELETE_FOLLOW_REQUEST)
                    .supply(follower.id)
                    .supply(followee.id)
                    .executeOn(CONNECTION)

    fun approveFollowRequest(followee: Account, follower: Account) {
        deleteFollowRequest(followee, follower)

        query(ADD_FOLLOW_FORMAT.format("Follow"))
                .supply(follower.id)
                .supply(followee.id)
                .executeOn(CONNECTION)
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
            .executeOn(CONNECTION)

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

    //
    // Posts & Events
    //

    @Language("PostgreSQL")
    private val CREATE_POST: String =
            """
            INSERT INTO "Post"(posterid, wallid, postmessage, postmediaurl, pollquestion, pollendtime, parentpostid)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING postid;
            """.trimIndent()

    /**
     * Creates a new `post` on the wall with the given `wallID`, authored by
     * the given `account`.
     */
    fun createPost(post: Post) = query(CREATE_POST)
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

                post.apply {
                    id = it.getInt(1)
                }
            }

    @Language("PostgreSQL")
    private val CREATE_EVENT: String =
            """
            INSERT INTO "Event"(hostid, eventname, eventdesc, starttime, endtime, location)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING eventid;
            """.trimIndent()

    /**
     * Inserts the provided [event] into the database, then returns the
     * newly created event object.
     */
    fun createEvent(event: Event) = query(CREATE_EVENT)
            .supply(event.hostId)
            .supply(event.name)
            .supply(event.description)
            .supply(event.startTime)
            .supply(event.endTime)
            .supply(event.location)
            .executeOn(CONNECTION) {
                if (!it.next())
                    throw RuntimeException("No result from CreateEvent call!")

                event.apply {
                    it.getInt(1)
                }
            }

    @Language("PostgreSQL")
    private val GET_EVENT: String =
            """
            SELECT * FROM "Event" e
            WHERE e.eventid = ?;
            """.trimIndent()

    /**
     * Finds the event that has the given [id], if any exist.
     */
    fun getEventById(id: Int) = query(GET_EVENT)
            .supply(id)
            .executeOn(CONNECTION) {
                if (it.next())
                    Event().apply { fromRow(it) }
                else
                    null
            }

    @Language("PostgreSQL")
    private val DELETE_EVENT: String =
            """
            DELETE FROM "Event" e
            WHERE e.eventid = ?;
            """.trimIndent()

    fun deleteEvent(event: Event) = query(DELETE_EVENT)
            .supply(event.id)
            .executeOn(CONNECTION)

    @Language("PostgreSQL")
    private val MARK_EVENT_INTEREST: String =
            """
            INSERT INTO "EventInterest"(eventid, profileid, isattending)
            VALUES (?, ?, ?);
            """.trimIndent()

    fun attendingEvent(user: Profile, event: Event) = query(MARK_EVENT_INTEREST)
            .supply(user.id)
            .supply(event.id)
            .supply(true)
            .executeOn(CONNECTION)

    fun interestedInEvent(user: Profile, event: Event) = query(MARK_EVENT_INTEREST)
            .supply(user.id)
            .supply(event.id)
            .supply(false)
            .executeOn(CONNECTION)

    @Language("PostgreSQL")
    private val GET_EVENT_ATTENDEES: String =
            """
            SELECT * FROM "EventInterest" ei
            INNER JOIN "Account" a ON a.accountid = ei.profileid
            INNER JOIN "Profile" p ON p.accountid = a.accountid
            WHERE ei.eventid = ? AND ei.isattending = ?;
            """.trimIndent()

    fun getAttendees(event: Event) = query(GET_EVENT_ATTENDEES)
            .supply(event.id)
            .supply(true)
            .executeOn(CONNECTION) { set ->
                set.map {
                    Profile().apply { fromRow(it) }
                }
            }

    fun getProspectiveAttendees(event: Event): List<Profile> = query(GET_EVENT_ATTENDEES)
            .supply(event.id)
            .supply(null as Boolean?)
            .executeOn(CONNECTION) { set ->
                set.map {
                    Profile().apply { fromRow(it) }
                }
            }

    @Language("PostgreSQL")
    private val GET_POST_REPLIES: String =
            """
            SELECT * FROM "Post" p
            WHERE
                p.parentpostid IS NOT NULL AND
                p.parentpostid = ?
            ORDER BY p.createdtime DESC;
            """.trimIndent()

    fun getRepliesTo(post: Post) = query(GET_POST_REPLIES)
            .supply(post.id)
            .executeOn(CONNECTION) { set ->
                set.map { row ->
                    Post().apply { fromRow(row) }
                }
            }

    //
    // Emotions & Reactions
    //

    @Language("PostgreSQL")
    private val GET_EMOTIONS: String =
            """
            SELECT * FROM "RefEmotion";
            """.trimIndent()

    fun getEmotions() = query(GET_EMOTIONS)
            .executeOn(CONNECTION) { set ->
                set.map {
                    Emotion().apply { fromRow(it) }
                }
            }

    @Language("PostgreSQL")
    private val GET_REACTIONS_TO_POST: String =
            """
            SELECT * FROM "PostReaction" pr
            INNER JOIN "Post" p ON p.postid = pr.postid
            INNER JOIN "Account" a ON a.accountid = pr.profileid
            INNER JOIN "Profile" prof ON prof.accountid = a.accountid
            WHERE pr.postid = ?;
            """.trimIndent()

    fun getReactionsTo(post: Post) = query(GET_REACTIONS_TO_POST)
            .supply(post.id)
            .executeOn(CONNECTION) { set ->
                val mapEntries = set.map {
                    val profile = Profile().apply { fromRow(it) }
                    val emotion = Emotion[it.getInt("EmotionId")]
                    profile to emotion
                }
                mapEntries.toMap()
            }

    @Language("PostgreSQL")
    private val ADD_REACTION: String =
            """
            INSERT INTO "PostReaction"(postid, profileid, emotionid)
            VALUES (?, ?, ?);
            """.trimIndent()

    fun addReaction(post: Post, user: Profile, emotion: Emotion) = query(ADD_REACTION)
            .supply(post.id)
            .supply(user.id)
            .supply(emotion.id)
            .executeOn(CONNECTION)

}
