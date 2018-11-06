package com.addonovan.cse3330

import com.addonovan.cse3330.model.Profile
import com.addonovan.cse3330.sql.call
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

object DbEngine {

    /** A URL used to open a connection to the SocialMedia database.  */
    private val CONNECTION_STRING = "jdbc:postgresql://localhost/SocialMedia"

    /** The actual database connection.  */
    private val CONNECTION: Connection

    init {
        val props = Properties()
        props.setProperty("user", "application")
        props.setProperty("password", "password1") // oh boy! check that security!

        // if there's a problem connecting to the database, then we'll immediately
        // throw an exception and die off
        try {
            CONNECTION = DriverManager.getConnection(CONNECTION_STRING, props)
        } catch (e: SQLException) {
            throw RuntimeException("Failed to open database connection!", e)
        }

        // make sure the connection is (properly) closed on shutdown, not that
        // it *really* matters all that much
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                CONNECTION.close()
            } catch (e: SQLException) {
                throw RuntimeException("Failed to close database connection!", e)
            }
        })
    }

    fun getProfileById(id: Int) = call("FindProfileById")
            .supply(id)
            .executeOn(CONNECTION) {
                if (it.next())
                    Profile().apply { fromRow(it) }
                else
                    null
            }

    fun createProfile(profile: Profile) = call("CreateProfile")
            .supply(profile.email)
            .supply(profile.phoneNumber)
            .supply(profile.firstName)
            .supply(profile.lastName)
            .supply(profile.username)
            .supply(profile.password)
            .executeOn(CONNECTION) {
                if (!it.next())
                    throw RuntimeException("No result from CreateProfile call!")

                getProfileById(it.getInt(1))!!
            }

}
