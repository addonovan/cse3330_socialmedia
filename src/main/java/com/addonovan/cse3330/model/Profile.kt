package com.addonovan.cse3330.model

import com.addonovan.cse3330.DbEngine
import com.fasterxml.jackson.annotation.JsonIgnore
import java.sql.ResultSet

/**
 * The class model representing the SQL `Profile` entity. This is a
 * specialization of the [Account] entity.
 */
class Profile : Account() {

    /** The first name of this profile's user. */
    lateinit var firstName: String

    /** The last name of this profile's user. */
    lateinit var lastName: String

    /** The unique username of this profile. */
    lateinit var username: String

    /** This user's password (which will probably be hashed in the future) */
    lateinit var password: String

    //
    // Derived Properties
    //

    /** A list of pages that this user is an admin for. */
    @get:JsonIgnore
    val administeredPages: List<Page> by lazy {
        DbEngine.getPagesByAdmin(this)
    }

    @get:JsonIgnore
    val groups: List<Group> by lazy {
        DbEngine.getGroupsForUser(this)
    }

    //
    // Functions
    //

    override fun fromRow(row: ResultSet) {
        super.fromRow(row)
        if (isActive) {
            firstName = row.getString("FirstName")
            lastName = row.getString("LastName")
            username = row.getString("Username")
            password = row.getString("Password")
        } else {
            firstName = "[deleted]"
            lastName = "[deleted]"
            username = "[deleted]"
            password = "[deleted]"
        }
    }
}
