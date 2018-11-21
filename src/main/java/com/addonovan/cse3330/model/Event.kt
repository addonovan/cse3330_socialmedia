package com.addonovan.cse3330.model

import java.sql.ResultSet
import java.sql.Timestamp

class Event : SqlEntity {

    var id: Int = 0

    var hostId: Int = 0

    lateinit var name: String

    lateinit var description: String

    lateinit var location: String

    lateinit var startTime: Timestamp

    lateinit var endTime: Timestamp

    //
    // Functions
    //

    override fun fromRow(row: ResultSet) {
        id = row.getInt("EventId")
        hostId = row.getInt("HostId")
        name = row.getString("EventName")
        description = row.getString("EventDesc")
        startTime = row.getTimestamp("StartTime")
        endTime = row.getTimestamp("EndTime")
        location = row.getString("Location")
    }

}
