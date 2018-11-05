package com.addonovan.cse3330

import com.addonovan.cse3330.model.Account
import com.addonovan.cse3330.model.Profile
import org.intellij.lang.annotations.Language

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

    private inline fun <T> query(query: String, crossinline action: (ResultSet) -> T): List<T> {
        val list = arrayListOf<T>()
        createStatement {
            val resultSet = it.executeQuery(query)
            while (resultSet.next()) {
                list += action(resultSet)
            }
        }
        return list
    }

    private fun insert(@Language("PostgreSQL") query: String): ResultSet {
        var set: ResultSet? = null
        createStatement {
            set = it.executeQuery(query)
        }
        return set ?: throw RuntimeException("INSERT failed: $query")
    }

    fun listAccounts() = query("""SELECT * FROM "Account" WHERE IsActive = TRUE;""") {
        Account.fromRow(it)
    }

    fun getProfileById(id: Int): Profile? {
        val profiles = query("""
            |SELECT *
            |FROM "Profile" p
            |INNER JOIN "Account" a
            |ON a.Id = p.AccountId
            |WHERE Id = $id
            """.trimMargin()) {
            Profile.fromRow(it)
        }
        return profiles.firstOrNull()
    }

    fun getAccountById(id: Int) = query("""SELECT * FROM "Account" WHERE Id = $id""") {
        Account.fromRow(it)
    }

    fun insertProfile(profile: Profile): Profile {
        val newAccount = profile.account.copy(id = insert(profile.account.insert()).getInt(0))
        val newProfile = profile.copy(account = newAccount)
        insert(newProfile.insert())
        return newProfile
    }

}
