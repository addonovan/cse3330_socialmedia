package com.addonovan.cse3330

import com.addonovan.cse3330.model.Account

import java.sql.*
import java.util.Properties

object DatabaseDriver {

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

    fun listAccounts(): List<Account> {
        val accounts = arrayListOf<Account>()

        CONNECTION.createStatement().use {
            it.executeQuery("SELECT * FROM \"Account\" WHERE IsActive = TRUE;").run {
                while (next()) {
                    accounts += Account(
                            getInt("Id"),
                            getString("Email"),
                            getString("PhoneNumber"),
                            getString("ProfileImageURL"),
                            getString("HeaderImageURL"),
                            getBoolean("IsPrivate"),
                            getBoolean("IsActive"),
                            getTimestamp("CreatedTime")
                    )
                }
            }
        }

        return accounts
    }

}
