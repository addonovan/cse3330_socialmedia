package com.addonovan.cse3330.sql

import java.sql.ResultSet

/**
 * A convenience function to map each row of this [ResultSet] to a specific
 * type, [T], by using the provided `action`.
 */
inline fun <T> ResultSet.map(action: (ResultSet) -> T): List<T> {
    val list = ArrayList<T>()

    while (next()) {
        list += action(this)
    }

    return list
}
