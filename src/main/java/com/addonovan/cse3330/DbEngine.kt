package com.addonovan.cse3330

import com.addonovan.cse3330.model.Account
import com.addonovan.cse3330.model.Profile
import com.addonovan.cse3330.sql.set
import com.addonovan.cse3330.sql.setAll
import org.intellij.lang.annotations.Language

import java.sql.*
import java.util.Properties

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
        }
        catch (e: SQLException) {
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

    private inline fun <T> prepareCall(@Language("PostgreSQL") name: String, block: (PreparedStatement) -> T): T {
        try {
            CONNECTION.prepareCall(name).use {
                return block(it)
            }
        } catch (e: SQLException) {
            throw RuntimeException("Failed to execute query!", e)
        }
    }

    fun listAccounts() = query("""SELECT * FROM "Account" WHERE IsActive = TRUE;""") {
        Account().apply { fromRow(it) }
    }

    fun getProfileById(id: Int): Profile? {
        val profiles = query("""
            |SELECT *
            |FROM "Profile" p
            |INNER JOIN "Account" a
            |ON a.Id = p.AccountId
            |WHERE Id = $id
            """.trimMargin()) {
            Profile().apply { fromRow(it) }
        }
        return profiles.firstOrNull()
    }

    fun createProfile(profile: Profile) = prepareCall("SELECT CreateProfile(?, ?, ?, ?, ?, ?)") {
        it.setAll(
                profile.email,
                profile.phoneNumber,
                profile.firstName,
                profile.lastName,
                profile.username,
                profile.password
        )

        if (!it.execute())
            throw RuntimeException("No result returned from CreateProfile!")

        val newId = it.resultSet.getInt(1)
        getProfileById(newId)!!
    }

    fun getAccountById(id: Int) = query("""SELECT * FROM "Account" WHERE Id = $id""") {
        Account().apply { fromRow(it) }
    }

}
