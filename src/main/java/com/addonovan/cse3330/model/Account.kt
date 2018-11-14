package com.addonovan.cse3330.model

import com.addonovan.cse3330.DbEngine
import java.lang.IllegalStateException
import java.sql.ResultSet
import java.sql.Timestamp

/**
 * The class model for the SQL `Account` entity.
 */
open class Account : SqlEntity {

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
    val fullName: String by lazy {
        when (this) {
            is Profile -> "$firstName $lastName"
            is Page -> name
            else -> throw IllegalStateException("Account does not have enough information to find its name")
        }
    }

    val isProfile: Boolean by lazy {
        when (this) {
            is Profile -> true
            is Page -> false
            else -> throw IllegalStateException("Account does not have enough information to determine type")
        }
    }

    /** A list of accounts who are currently following this one. */
    val followers: List<Account> by lazy {
        DbEngine.getFollowers(this)
    }

    /** A list of accounts who are requesting ot follow this one. */
    val followRequests: List<Account> by lazy {
        DbEngine.getFollowers(this, requests = true)
    }

    //
    // Functions
    //

    override fun fromRow(row: ResultSet) {
        id = row.getInt("AccountId")
        email = row.getString("Email")
        phoneNumber = row.getString("PhoneNumber")
        profileImageURL = row.getString("ProfileImageURL")
        headerImageURL = row.getString("HeaderImageURL")
        isPrivate = row.getBoolean("IsPrivate")
        isActive = row.getBoolean("IsActive")
        createdTime = row.getTimestamp("CreatedTime")
    }

}
