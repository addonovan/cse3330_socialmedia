package com.addonovan.cse3330.model

import com.addonovan.cse3330.DbEngine
import java.lang.IllegalStateException
import java.sql.ResultSet
import java.sql.Timestamp

/**
 * The class model representing the SQL `Post` entity.
 */
class Post : SqlEntity {

    /** The unique identifier for this post. */
    var id: Int = 0

    /** The account ID of the account which posted this. */
    var posterId: Int = 0

    /** The unique identifier for the account whose wall this was posted to. */
    var wallId: Int = 0

    /** The text of this post, if any. */
    var text: TextBody? = null

    /** The media of this post, if any. */
    var media: MediaBody? = null

    /** The poll of this post, if any. */
    var poll: PollBody? = null

    /** The ID of the post this is a reply to (or `null` if it's a top-level comment) */
    var parentPostId: Int? = null

    /** When this post was written. */
    lateinit var createTime: Timestamp

    //
    // Derived Properties
    //

    val poster: Account by lazy {
        DbEngine.getAccountById(posterId)!!
    }

    val wall: Account by lazy {
        DbEngine.getAccountById(wallId)!!
    }

    val replies: List<Post> by lazy {
        DbEngine.getRepliesTo(id)
    }

    val reactions: Map<Profile, Emotion> by lazy {
        DbEngine.getReactionsTo(id)
    }

    //
    // Implementation
    //

    override fun fromRow(row: ResultSet) {
        id = row.getInt("PostId")
        posterId = row.getInt("PosterId")
        wallId = row.getInt("WallId")

        // the different body types have a little bit of preprocessing to do
        // because at least one needs to be present, but all of them could be
        // there, so we have to use nulls to represent those

        row.getString("PostMessage")?.let {
            text = TextBody(body = it)
        }

        row.getString("PostMediaURL")?.let {
            media = MediaBody(url = it)
        }

        row.getString("PollQuestion")?.let {
            val endTime = row.getTimestamp("PollEndTime")
            poll = PollBody(question = it, endTime = endTime)
        }

        // ensure database integrity (even though the constraints are written
        // into the database as well)
        if (text == null && media == null && poll == null) {
            throw IllegalStateException("Post (id=$id) somehow has no body!")
        }

        // don't let the NULL value be coalesced into a valid value here,
        // use the type system to our advantage!
        parentPostId = row.getInt("ParentPostId").let {
            if (it == 0)
                null
            else
                it
        }

        createTime = row.getTimestamp("CreatedTime")
    }

    //
    // Body classes
    //

    /**
     * A wrapper around the [body] text of this post.
     */
    data class TextBody(val body: String)

    /**
     * A wrapper around the media [url] of this post.
     */
    data class MediaBody(val url: String)

    /**
     * A wrapper around the poll-related columns ([question], [endTime]) of this
     * post.
     */
    data class PollBody(val question: String, val endTime: Timestamp)

}
