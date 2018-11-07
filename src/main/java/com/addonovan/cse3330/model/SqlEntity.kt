package com.addonovan.cse3330.model

import java.sql.ResultSet

/**
 * An entity in a SQL database.
 */
interface SqlEntity {

    /**
     * Parses this entity from the given [row] in a manner which will not affect
     * the row itself.
     */
    fun fromRow(row: ResultSet)

}
