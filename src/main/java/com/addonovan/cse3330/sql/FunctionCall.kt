package com.addonovan.cse3330.sql

import java.lang.RuntimeException
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

class FunctionCall(private val name: String) {

    private val parameters = ArrayList<Any>()

    fun supply(parameter: Any): FunctionCall {
        parameters.add(parameter)
        return this
    }

    fun <T> executeOn(connection: Connection, block: (ResultSet) -> T): T {
        try {
            val functionParameters = "?, ".repeat(parameters.size).removeSuffix(", ")
            val query = "SELECT * FROM $name($functionParameters)"

            connection.prepareStatement(query).use {
                for ((i, param) in parameters.withIndex()) {
                    it.set(i + 1, param)
                }
                it.execute()
                return block(it.resultSet)
            }
        } catch (e: SQLException) {
            throw RuntimeException("Failed to invoke function call: $name", e)
        }
    }

}

fun call(name: String) = FunctionCall(name)
