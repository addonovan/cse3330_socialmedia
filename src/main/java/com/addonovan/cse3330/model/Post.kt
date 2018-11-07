package com.addonovan.cse3330.model

import java.lang.IllegalStateException
import java.sql.ResultSet
import java.sql.Timestamp

class Post : SqlEntity {

    var id: Int = 0

    var posterId: Int = 0

    var text: TextBody? = null

    var media: MediaBody? = null

    var poll: PollBody? = null

    var parentPostId: Int? = null

    lateinit var createTime: Timestamp

    //
    // Implementation
    //

    override fun fromRow(row: ResultSet) {
        id = row.getInt("Id")
        posterId = row.getInt("PosterId")

        // the different body types have a little bit of preprocessing to do
        // because at least one needs to be present, but all of them could be
        // there, so we have to use nulls to represent those

        row.getString("Message")?.let {
            text = TextBody(body = it)
        }

        row.getString("MediaURL")?.let {
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

        createTime = row.getTimestamp("CreateTime")
    }

    //
    // Body classes
    //

    data class TextBody(val body: String)

    data class MediaBody(val url: String)

    data class PollBody(val question: String, val endTime: Timestamp)

}
