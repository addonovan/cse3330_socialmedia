package com.addonovan.cse3330.model

import java.sql.ResultSet
import java.sql.Timestamp

class GroupMessage : SqlEntity {

    var id: Int = 0

    var senderId: Int = 0

    var groupId: Int = 0

    var message: String? = null

    var mediaUrl: String? = null

    lateinit var sendTime: Timestamp

    //
    // Functions
    //

    override fun fromRow(row: ResultSet) {
        id = row.getInt("MessageId")
        senderId = row.getInt("SenderId")
        groupId = row.getInt("GroupId")
        message = row.getString("MessageText")
        mediaUrl = row.getString("MediaURL")
        sendTime = row.getTimestamp("SendTime")
    }

}
