package com.addonovan.cse3330

import com.addonovan.cse3330.model.Profile
import com.addonovan.cse3330.sql.call
import com.addonovan.cse3330.sql.executeWith
import com.addonovan.cse3330.sql.set
import org.intellij.lang.annotations.Language
import java.sql.*
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

    private inline fun <T> createStatement(crossinline action: (Statement) -> T): T {
        try {
            CONNECTION.createStatement().use {
                return action(it)
            }
        } catch (e: SQLException) {
            throw RuntimeException("Failed to execute query!", e)
        }
    }

    private inline fun <T> query(@Language("PostgreSQL") query: String, crossinline action: (ResultSet) -> T): List<T> {
        val list = arrayListOf<T>()
        createStatement {
            val resultSet = it.executeQuery(query)
            while (resultSet.next()) {
                list += action(resultSet)
            }
        }
        return list
    }

    fun getProfileById(id: Int) = call("FindProfileById")
            .supply(id)
            .executeOn(CONNECTION) {
                if (!it.next())
                    throw RuntimeException("No result from FindProfileById call!")

                Profile().apply {
                    fromRow(it)
                }
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

                getProfileById(it.getInt(1))
            }

}
