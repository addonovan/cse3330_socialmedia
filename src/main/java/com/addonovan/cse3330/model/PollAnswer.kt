package com.addonovan.cse3330.model

import java.sql.ResultSet

class PollAnswer : SqlEntity {

    var id: Int = 0

    lateinit var text: String

    var pollId: Int = 0

    //
    // Functions
    //

    override fun fromRow(row: ResultSet) {
        id = row.getInt("PollAnswerId")
        pollId = row.getInt("PostId")
        text = row.getString("PollAnswerText")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PollAnswer

        if (id != other.id) return false
        if (pollId != other.pollId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + pollId
        return result
    }


}
