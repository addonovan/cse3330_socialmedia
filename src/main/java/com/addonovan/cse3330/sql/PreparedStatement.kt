package com.addonovan.cse3330.sql

import java.sql.*

/**
 * Sets the parameter at the given `index` to the given `value`. This will
 * deduce the correct function to call from the given type, [T].
 */
fun <T> PreparedStatement.set(index: Int, value: T) {
    when (value) {
        is Int -> setInt(index, value)
        is String -> setString(index, value)
        is Boolean -> setBoolean(index, value)
        is Timestamp -> setTimestamp(index, value)
        is Date -> setDate(index, value)
        else -> throw RuntimeException("Unsupported type!")
    }
}
