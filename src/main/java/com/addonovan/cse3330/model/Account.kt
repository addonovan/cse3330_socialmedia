package com.addonovan.cse3330.model

import com.addonovan.cse3330.DbEngine
import com.fasterxml.jackson.annotation.JsonIgnore
import java.lang.IllegalStateException
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

/**
 * The class model for the SQL `Account` entity.
 */
open class Account : SqlEntity {

    companion object {

        fun fromRow(row: ResultSet): Account =
                when {
                    row.getString("FirstName") != null -> Profile().apply { fromRow(row) }
                    row.getString("PageName") != null -> Page().apply { fromRow(row) }
                    else -> throw IllegalStateException("Inconsistent database state: Account is neither a Profile nor a Page!")
                }

    }

    var id: Int = 0

    /** An (unverified) email account registered to this user. */
    lateinit var email: String

    /** This account's phone number, which may or may not have been provided. */
    var phoneNumber: String? = null

    /** The profile image for this account. */
    lateinit var profileImageURL: String

    /** The header image for this account when their page is accessed. */
    lateinit var headerImageURL: String

    /**
     * If this account is private, and thus inaccessible to users who are
     * not already following this person.
     */
    var isPrivate: Boolean = true

    /**
     * If this account is active. If not, then the data for this user should
     * be redacted with a `[deleted]` tag a la reddit.
     */
    var isActive: Boolean = true

    /** When the user was created. */
    lateinit var createdTime: Timestamp

    //
    // Derived Properties
    //

    /**
     * Computes the display name for this account, if this account has that
     * information.
     */
    @get:JsonIgnore
    val fullName: String by lazy {
        when (this) {
            is Profile -> "$firstName $lastName"
            is Page -> name
            else -> throw IllegalStateException("Account does not have enough information to find its name")
        }
    }

    @get:JsonIgnore
    val isProfile: Boolean by lazy {
        when (this) {
            is Profile -> true
            is Page -> false
            else -> throw IllegalStateException("Account does not have enough information to determine type")
        }
    }

    /** A list of accounts this one is currently following */
    @get:JsonIgnore
    val following: List<Account> by lazy {
        DbEngine.getFollowing(this)
    }

    /** A list of accounts who are currently following this one. */
    @get:JsonIgnore
    val followers: List<Account> by lazy {
        DbEngine.getFollowers(this)
    }

    /** A list of accounts who are requesting ot follow this one. */
    @get:JsonIgnore
    val followRequests: List<Account> by lazy {
        DbEngine.getFollowRequests(this)
    }

    @get:JsonIgnore
    val joinYear: Int by lazy {
        createdTime.toLocalDateTime().year
    }

    //
    // Functions
    //

    override fun fromRow(row: ResultSet) {
        isActive = row.getBoolean("IsActive")
        id = row.getInt("AccountId")

        if (isActive) {
            email = row.getString("Email")
            phoneNumber = row.getString("PhoneNumber")
            profileImageURL = row.getString("ProfileImageURL")
            headerImageURL = row.getString("HeaderImageURL")
            isPrivate = row.getBoolean("IsPrivate")
            createdTime = row.getTimestamp("CreatedTime")
        } else {
            email = "[deleted]"
            phoneNumber = "[deleted]"
            profileImageURL = "/media/profiles/default.png"
            headerImageURL = "/media/headers/default.png"
            isPrivate = false
            createdTime = Timestamp.from(Instant.now())
        }
    }

    override fun equals(other: Any?): Boolean =
            other is Account && other.id == id

}
