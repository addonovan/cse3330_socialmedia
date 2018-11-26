package com.addonovan.cse3330.model

import com.addonovan.cse3330.DbEngine
import java.sql.ResultSet

class Group : SqlEntity {

    var id: Int = 0

    lateinit var name: String

    lateinit var description: String

    lateinit var pictureUrl: String

    //
    // Derived Properties
    //

    val members: List<Profile> by lazy {
        DbEngine.getGroupMembers(this)
    }

    val messages: List<GroupMessage> by lazy {
        DbEngine.getGroupMessageHistory(this)
    }

    //
    // Functions
    //

    override fun fromRow(row: ResultSet) {
        id = row.getInt("GroupId")
        name = row.getString("GroupName")
        description = row.getString("GroupDesc")
        pictureUrl = row.getString("GroupPictureUrl")
    }
}
