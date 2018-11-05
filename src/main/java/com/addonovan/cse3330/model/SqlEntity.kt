package com.addonovan.cse3330.model

import java.sql.Connection
import java.sql.ResultSet

interface SqlEntity {

    fun fromRow(row: ResultSet)

    fun insertInto(connection: Connection): Boolean

}
