package com.addonovan.cse3330.sql

import java.sql.*

fun PreparedStatement.executeWith(vararg params: Any): ResultSet {
    setAll(*params)
    if (!execute() || !resultSet.next())
        throw RuntimeException("PreparedStatement returned no values!")

    return resultSet
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
