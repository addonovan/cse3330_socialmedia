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
}
