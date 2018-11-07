package com.addonovan.cse3330.sql

import java.sql.*

fun <T> PreparedStatement.set(index: Int, value: T) {
    when (value) {
        is Int -> setInt(index, value)
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
