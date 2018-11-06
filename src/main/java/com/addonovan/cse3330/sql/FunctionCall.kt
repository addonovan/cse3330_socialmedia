package com.addonovan.cse3330.sql

import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types
import kotlin.reflect.jvm.jvmName

class FunctionCall(private val name: String) {

    private val parameters = ArrayList<Any>()

    fun supply(parameter: Any): FunctionCall {
        parameters.add(parameter)
        return this
    }

    fun supplyNull(sqlType: Int): FunctionCall {
        parameters.add(NullParameter(sqlType))
        return this
    }

    inline fun <reified T> supplyNull() = supplyNull(
            when (T::class) {
                Int::class -> Types.INTEGER
                String::class -> Types.VARCHAR

                else ->
                    throw RuntimeException("Invalid type: ${T::class.jvmName}")
            }
    )

    fun <T> executeOn(connection: Connection, block: (ResultSet) -> T): T {
        try {
            val functionParameters = "?, ".repeat(parameters.size).removeSuffix(", ")
            val query = "SELECT * FROM $name($functionParameters)"

            connection.prepareStatement(query).use {
                for ((i, param) in parameters.withIndex()) {
                    if (param is NullParameter) {
                        it.setNull(i + 1, param.type)
                    } else {
                        it.set(i + 1, param)
                    }
                }
                it.execute()
                return block(it.resultSet)
            }
        } catch (e: SQLException) {
            throw RuntimeException("Failed to invoke function call: $name", e)
        }
    }

    private data class NullParameter(val type: Int)

}

fun call(name: String) = FunctionCall(name)
