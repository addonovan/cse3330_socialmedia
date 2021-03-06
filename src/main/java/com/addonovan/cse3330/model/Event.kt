package com.addonovan.cse3330.model

import com.addonovan.cse3330.DbEngine
import com.fasterxml.jackson.annotation.JsonIgnore
import java.sql.ResultSet
import java.sql.Timestamp

class Event : SqlEntity {

    var id: Int = 0

    var hostId: Int = 0

    lateinit var name: String

    lateinit var description: String

    var location: String? = null

    lateinit var startTime: Timestamp

    lateinit var endTime: Timestamp

    //
    // Derived Properties
    //

    @get:JsonIgnore
    val host: Account by lazy {
        DbEngine.getAccountById(hostId)!!
    }

    @get:JsonIgnore
    val attendees: List<Profile> by lazy {
        DbEngine.getAttendees(this)
    }

    @get:JsonIgnore
    val interested: List<Profile> by lazy {
        DbEngine.getInterestedPeople(this)
    }

    @get:JsonIgnore
    val prospectiveAttendees: List<Profile> by lazy {
        DbEngine.getProspectiveAttendees(this)
    }

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
