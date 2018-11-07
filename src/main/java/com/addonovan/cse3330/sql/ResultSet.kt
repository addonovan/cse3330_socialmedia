package com.addonovan.cse3330.sql

import java.sql.ResultSet

inline fun <T> ResultSet.map(block: (ResultSet) -> T): List<T> {
    val list = ArrayList<T>()

    while (next()) {
        list += block(this)
    }

    return list
}
