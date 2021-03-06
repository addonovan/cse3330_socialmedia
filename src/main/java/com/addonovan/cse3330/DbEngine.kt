package com.addonovan.cse3330

import com.addonovan.cse3330.model.*
import com.addonovan.cse3330.sql.call
import com.addonovan.cse3330.sql.map
import com.addonovan.cse3330.sql.query
import com.fasterxml.jackson.databind.ObjectMapper
import org.intellij.lang.annotations.Language
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
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
    // Startup-only Actions
    //

    fun dumpData(outputFile: String) {
        File(outputFile).bufferedWriter().use { bw ->
            printData { text ->
                bw.write(text)
                bw.newLine()
            }
        }
    }

    /**
     * This was a project requirement.
     */
    private fun printData(println: (String) -> Unit) {
        val json = ObjectMapper().writerWithDefaultPrettyPrinter()

        fun <T> String.printResults(action: (ResultSet) -> T) {
            query(this).executeOn(CONNECTION) { set ->
                set.map {
                    action(it)
                }
            }.forEach {
                println(json.writeValueAsString(it))
            }
        }

        println("-- Profiles ------------------------------------------------")
        @Language("PostgreSQL")
        val getProfiles = """
            SELECT * FROM "Profile" p
            INNER JOIN "Account" a ON a.accountid = p.accountid
            ORDER BY p.accountid ASC;
        """.trimIndent()
        getProfiles.printResults { Profile().apply{ fromRow(it) } }
        println("------------------------------------------------------------")

        println("-- Pages ---------------------------------------------------")
        @Language("PostgreSQL")
        val getPages = """
            SELECT * FROM "Page" p
            INNER JOIN "Account" a ON a.accountid = p.accountid
            ORDER BY p.accountid ASC;
        """.trimIndent()
        getPages.printResults { Page().apply { fromRow(it) } }
        println("------------------------------------------------------------")

        println("-- Pages Administered --------------------------------------")
        getProfiles.printResults { row ->
            val profile = Profile().apply{ fromRow(row) }
            mapOf(profile.fullName to profile.administeredPages.map { it.name })
        }
        println("------------------------------------------------------------")

        println("-- Following -----------------------------------------------")
        getProfiles.printResults { row ->
            val profile = Profile().apply { fromRow(row) }
            mapOf(profile.fullName to profile.following.map { it.fullName })
        }
        println("------------------------------------------------------------")

        println("-- Follow Requests -----------------------------------------")
        getProfiles.printResults { row ->
            val profile = Profile().apply { fromRow(row) }
            mapOf(profile.fullName to profile.followRequests.map { it.fullName })
        }
        println("------------------------------------------------------------")

        println("-- Groups --------------------------------------------------")
        @Language("PostgreSQL")
        val getGroups = """
            SELECT * FROM "Group"
        """.trimIndent()
        getGroups.printResults { Group().apply { fromRow(it) } }
        println("------------------------------------------------------------")

        println("-- Group Members -------------------------------------------")
        getGroups.printResults { row ->
            val group = Group().apply { fromRow(row) }
            mapOf(group.name to group.members.map { it.fullName })
        }
        println("------------------------------------------------------------")

        println("-- Group Messages ------------------------------------------")
        getGroups.printResults { row ->
            val group = Group().apply { fromRow(row) }
            mapOf(group.name to group.messages)
        }
        println("------------------------------------------------------------")

        println("-- Events --------------------------------------------------")
        @Language("PostgreSQL")
        val getEvents = """
            SELECT * FROM "Event"
        """.trimIndent()
        getEvents.printResults { Event().apply { fromRow(it) } }
        println("------------------------------------------------------------")

        println("-- Event Attendees -----------------------------------------")
        getEvents.printResults { row ->
            val event = Event().apply { fromRow(row) }
            val attending = event.attendees.map { it.fullName }
            val interested = event.interested.map { it.fullName }

            mapOf(event.name to arrayOf(
                    mapOf("attending" to attending),
                    mapOf("interested" to interested)
            ))
        }
        println("------------------------------------------------------------")

        println("-- Posts ---------------------------------------------------")
        @Language("PostgreSQL")
        val getPosts = """
            SELECT * FROM "Post"
        """.trimIndent()
        getPosts.printResults { Post().apply { fromRow(it) } }
        println("------------------------------------------------------------")

        println("-- Post Reactions ------------------------------------------")
        getPosts.printResults { row ->
            val post = Post().apply { fromRow(row) }
            mapOf(post.id to post.reactions.map { (who, how) ->
                mapOf(who.fullName to how.name)
            })
        }
        println("------------------------------------------------------------")

        println("-- Poll Votes ----------------------------------------------")
        getPosts.printResults { row ->
            val post = Post().apply { fromRow(row) }
            mapOf(post.id to post.pollVotes.map { (answer, count) ->
                mapOf(answer.text to count)
            })
        }
        println("------------------------------------------------------------")
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
                    id = it.getInt(1)
                }
            }

    @Language("PostgreSQL")
    private val GET_ALL_ACCOUNTS: String =
            """
            SELECT * FROM "Account" a
            LEFT JOIN "Profile" prof ON PROF.accountid = a.accountid
            LEFT JOIN "Page" page ON page.accountid = a.accountid
            """.trimIndent()

    fun getAllAccounts() = query(GET_ALL_ACCOUNTS)
            .executeOn(CONNECTION) { set ->
                set.map {
                    Account.fromRow(it)
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

    @Language("PostgreSQL")
    private val TOGGLE_ACCOUNT: String =
            """
            UPDATE "Account"
            SET isactive = ?
            WHERE accountid = ?;
            """.trimIndent()

    /**
     * "Deletes" the [account].
     */
    fun deactivateAccount(account: Account) = query(TOGGLE_ACCOUNT)
            .supply(false)
            .supply(account.id)
            .executeOn(CONNECTION)

    fun activateAccount(account: Account) = query(TOGGLE_ACCOUNT)
            .supply(true)
            .supply(account.id)
            .executeOn(CONNECTION)

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
            WHERE prof.username = ? AND a.isactive;
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
            SET email = ?, phonenumber = ?, profileimageurl = ?, headerimageurl = ?, isprivate = ?, isactive = ?
            WHERE accountid = ?;
            """.trimIndent()

    @Language("PostgreSQL")
    private val UPDATE_PROFILE: String =
            """
            UPDATE "Profile"
            SET firstname = ?, lastname = ?, username = ?, password = ?
            WHERE accountid = ?;
            """.trimIndent()

    @Language("PostgreSQL")
    private val UPDATE_PAGE: String =
            """
            UPDATE "Page"
            SET pagename = ?, pagedesc = ?
            WHERE accountid = ?;
            """.trimIndent()

    fun updateProfile(user: Profile, newSettings: Profile) {
        query(UPDATE_ACCOUNT)
                .supply(newSettings.email)
                .supply(newSettings.phoneNumber)
                .supply(newSettings.profileImageURL)
                .supply(newSettings.headerImageURL)
                .supply(newSettings.isPrivate)
                .supply(newSettings.isActive)
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

    fun updatePage(page: Page, newSettings: Page) {
        query(UPDATE_ACCOUNT)
                .supply(newSettings.email)
                .supply(newSettings.phoneNumber)
                .supply(newSettings.profileImageURL)
                .supply(newSettings.headerImageURL)
                .supply(newSettings.isPrivate)
                .supply(newSettings.isActive)
                .supply(page.id)
                .executeOn(CONNECTION)

        query(UPDATE_PAGE)
                .supply(newSettings.name)
                .supply(newSettings.description)
                .supply(page.id)
                .executeOn(CONNECTION)
    }

    @Language("PostgreSQL")
    private val GET_PAGES_BY_ADMIN: String =
            """
            SELECT * FROM "PageAdmin" pa
            INNER JOIN "Account" a ON a.accountid = pa.pageid
            INNER JOIN "Page" p ON p.accountid = a.accountid
            WHERE pa.profileid = ? AND a.isactive;
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
            WHERE f.followeeid = ? AND a.isactive;
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
            WHERE f.followerid = ? AND a.isactive;
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
            WHERE p.wallid = ? AND p.parentpostid IS NULL
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
    private val ACCOUNT_OVERVIEW_FOR: String =
            """
            SELECT * FROM "Post" p
            WHERE p.posterid = ?
            ORDER BY p.createdtime DESC;
            """.trimIndent()

    fun accountOverview(account: Account) = query(ACCOUNT_OVERVIEW_FOR)
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
            WHERE p.parentpostid IS NULL AND
                (
                    p.wallid = ? OR
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
    private val ADD_POLL_ANSWER: String =
            """
            INSERT INTO "PollAnswer"(postid, pollanswertext)
            VALUES (?, ?);
            """.trimIndent()

    fun addPollAnswer(post: Post, answer: String) = query(ADD_POLL_ANSWER)
            .supply(post.id)
            .supply(answer)
            .executeOn(CONNECTION)


    @Language("PostgreSQL")
    private val GET_POST_COUNT_BY_DATE: String =
            """
            SELECT COUNT(postid) FROM "Post"
            WHERE wallid = ? AND createdtime::date = ?
            """.trimIndent()

    fun getPostCountByDate(wall: Account, date: Date) = query(GET_POST_COUNT_BY_DATE)
            .supply(wall.id)
            .supply(date)
            .executeOn(CONNECTION) {
                if (!it.next())
                    throw RuntimeException("No value returned from query!")

                it.getInt(1)
            }

    @Language("PostgreSQL")
    private val GET_POLL_ANSWERS: String =
            """
            SELECT * FROM "PollAnswer"
            WHERE postid = ?;
            """.trimIndent()

    fun getPollAnswers(post: Post) = query(GET_POLL_ANSWERS)
            .supply(post.id)
            .executeOn(CONNECTION) { set ->
                set.map {
                    PollAnswer().apply { fromRow(it) }
                }
            }

    @Language("PostgreSQL")
    private val VOTE_IN_POLL: String =
            """
            INSERT INTO "PollVote"(pollid, pollanswerid, profileid)
            VALUES (?, ?, ?);
            """.trimIndent()

    fun voteInPoll(voter: Profile, poll: Post, answer: PollAnswer) = query(VOTE_IN_POLL)
            .supply(poll.id)
            .supply(answer.id)
            .supply(voter.id)
            .executeOn(CONNECTION)

    @Language("PostgreSQL")
    private val GET_POLL_VOTERS: String =
            """
            SELECT * FROM "PollVote" pv
            INNER JOIN "Account" a ON a.accountid = pv.profileid
            INNER JOIN "Profile" p ON p.accountid = a.accountid
            WHERE pv.pollid = ?;
            """.trimIndent()

    fun getPollVoters(poll: Post) = query(GET_POLL_VOTERS)
            .supply(poll.id)
            .executeOn(CONNECTION) { set ->
                set.map {
                    Profile().apply { fromRow(it) }
                }
            }

    @Language("PostgreSQL")
    private val GET_POLL_VOTES: String = """
        SELECT pa.*, COUNT(pv.profileid) AS VoteCount
        FROM "PollVote" pv
        INNER JOIN "PollAnswer" pa ON pa.pollanswerid = pv.pollanswerid
        WHERE pv.pollid = ?
        GROUP BY pa.pollanswerid
    """.trimIndent()

    fun getPollVotes(poll: Post) = query(GET_POLL_VOTES)
            .supply(poll.id)
            .executeOn(CONNECTION) { set ->
                set.map {
                    val answer = PollAnswer().apply { fromRow(it) }
                    val count = it.getInt("VoteCount")
                    answer to count
                }.toMap()
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
            WHERE ei.eventid = ? AND ei.isattending = ? AND a.isactive;
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

    fun getInterestedPeople(event: Event) = query(GET_EVENT_ATTENDEES)
            .supply(event.id)
            .supply(false)
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
            WHERE pr.postid = ? AND a.isactive;
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

    //
    // Groups
    //

    @Language("PostgreSQL")
    private val CREATE_GROUP: String =
            """
            INSERT INTO "Group"(groupname, groupdesc)
            VALUES (?, ?)
            RETURNING groupid;
            """.trimIndent()

    fun createGroup(group: Group) = query(CREATE_GROUP)
            .supply(group.name)
            .supply(group.description)
            .executeOn(CONNECTION) {
                if (!it.next())
                    throw RuntimeException("Failed to create new Group!")

                group.apply {
                    id = it.getInt(1)
                }
            }


    @Language("PostgreSQL")
    private val ADD_GROUP_MEMBER: String =
            """
            INSERT INTO "GroupMember"(profileid, groupid)
            VALUES (?, ?);
            """.trimIndent()

    fun addGroupMember(group: Group, user: Profile) = query(ADD_GROUP_MEMBER)
            .supply(user.id)
            .supply(group.id)
            .executeOn(CONNECTION)

    @Language("PostgreSQL")
    private val GET_GROUP_BY_ID: String =
            """
            SELECT * FROM "Group"
            WHERE groupid = ?;
            """.trimIndent()

    fun getGroupById(id: Int) = query(GET_GROUP_BY_ID)
            .supply(id)
            .executeOn(CONNECTION) {
                if (!it.next())
                    null
                else
                    Group().apply { fromRow(it) }
            }

    @Language("PostgreSQL")
    private val GET_GROUPS_FOR_USER: String =
            """
            SELECT * FROM "GroupMember" gmem
            INNER JOIN "Group" g ON g.groupid = gmem.groupid
            WHERE gmem.profileid = ?;
            """.trimIndent()

    fun getGroupsForUser(user: Profile) = query(GET_GROUPS_FOR_USER)
            .supply(user.id)
            .executeOn(CONNECTION) { set ->
                set.map {
                    Group().apply { fromRow(it) }
                }
            }

    @Language("PostgreSQL")
    private val GET_GROUP_MEMBERS: String =
            """
            SELECT * FROM "GroupMember" gmem
            INNER JOIN "Account" a ON a.accountid = gmem.profileid
            INNER JOIN "Profile" p ON p.accountid = a.accountid
            WHERE gmem.groupid = ? AND a.isactive;
            """

    fun getGroupMembers(group: Group) = query(GET_GROUP_MEMBERS)
            .supply(group.id)
            .executeOn(CONNECTION) { set ->
                set.map {
                    Profile().apply { fromRow(it) }
                }
            }

    @Language("PostgreSQL")
    private val GET_GROUP_MESSAGE_HISTORY: String =
            """
            SELECT * FROM "GroupMessage" gm
            WHERE gm.groupid = ?
            ORDER BY gm.sendtime ASC;
            """.trimIndent()

    fun getGroupMessageHistory(group: Group) = query(GET_GROUP_MESSAGE_HISTORY)
            .supply(group.id)
            .executeOn(CONNECTION) { set ->
                set.map {
                    GroupMessage().apply{ fromRow(it) }
                }
            }

    @Language("PostgreSQL")
    private val ADD_NEW_GROUP_MESSAGE: String =
            """
            INSERT INTO "GroupMessage"(senderid, groupid, messagetext, mediaurl)
            VALUES (?, ?, ?, ?);
            """.trimIndent()

    fun sendGroupMessage(sender: Profile, group: Group, message: GroupMessage) =
            query(ADD_NEW_GROUP_MESSAGE)
                    .supply(sender.id)
                    .supply(group.id)
                    .supply(message.message)
                    .supply(message.mediaUrl)
                    .executeOn(CONNECTION)

    @Language("PostgreSQL")
    private val UPDATE_GROUP_SETTINGS: String =
            """
            UPDATE "Group"
            SET groupname = ?, groupdesc = ?, grouppictureurl = ?
            WHERE groupid = ?;
            """.trimIndent()

    fun updateGroup(group: Group) = query(UPDATE_GROUP_SETTINGS)
            .supply(group.name)
            .supply(group.description)
            .supply(group.pictureUrl)
            .supply(group.id)
            .executeOn(CONNECTION)

}
