package com.addonovan.cse3330.sql

import org.intellij.lang.annotations.Language
import java.lang.RuntimeException
import java.sql.*

fun Connection.execute(@Language("PostgreSQL") sql: String, vararg params: Any?): ResultSet {
    try {
        val statement = this.prepareStatement(sql)
        for ((i, value) in params.withIndex()) {
            statement.set(i + 1, value)
        }
        statement.executeQuery()
        return statement.resultSet
    } catch (e: SQLException) {
        throw RuntimeException("Failed to execute query: $sql", e)
    }
}

fun <T> PreparedStatement.set(index: Int, value: T) {
    when (value) {
        is String -> setString(index, value)
        is Boolean -> setBoolean(index, value)
        is Timestamp -> setTimestamp(index, value)
        else -> throw RuntimeException("Unsupported type!")
    }
}

fun PreparedStatement.setAll(vararg values: Any) {
    for ((i, param) in values.withIndex()) {
        set(i + 1, param)
    }
}
