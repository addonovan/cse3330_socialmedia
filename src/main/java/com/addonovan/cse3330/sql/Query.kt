package com.addonovan.cse3330.sql

import org.intellij.lang.annotations.Language
import java.sql.*
import kotlin.reflect.jvm.jvmName

class Query(@Language("PostgreSQL") val queryText: String) {

    /** A list of all of the parameters to send to the function. */
    private val parameters = ArrayList<Any>()

    /**
     * Supplies a value to the function, then returns this [Query] so
     * that calls may be chained.
     *
     * @see [supply]
     */
    fun supplyValue(parameter: Any): Query {
        parameters.add(parameter)
        return this
    }

    /**
     * Supplies a value of type [T] as a parameter to the function. Values here
     * may be `null`, so long as the type is specified, they will be handled
     * correctly.
     */
    inline fun <reified T> supply(parameter: T) =
            if (parameter == null)
                supplyNull<T>()
            else
                supplyValue(parameter)

    /**
     * Supplies a `null` parameter with the given `sqlType` to the function.
     *
     * This function should never be directly called, as supported types may
     * be deduced automatically via the parameterized version of this function.
     *
     * @see [supply]
     */
    fun supplyNull(sqlType: Int): Query {
        parameters.add(NullParameter(sqlType))
        return this
    }

    /**
     * Deduces the corresponding `sqlType` from the given type, [T], and
     * supplies a `null` of that type.
     *
     * ## Note
     * When using unambiguous parameters, it's preferred to use [supply]
     * instead.
     *
     * @see [supplyNull]
     * @see [supply]
     */
    inline fun <reified T> supplyNull() = supplyNull(
            when (T::class) {
                Int::class -> Types.INTEGER
                String::class -> Types.VARCHAR
                Timestamp::class -> Types.TIMESTAMP

                else ->
                    throw RuntimeException("Invalid type: ${T::class.jvmName}")
            }
    )

    /**
     * Executes the built up function call over the given `connection`, then
     * runs the `block` on resultant [ResultSet], yielding type [T].
     */
    fun <T> executeOn(connection: Connection, block: (ResultSet) -> T): T {
        try {
            connection.prepareStatement(queryText).use {
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
            throw RuntimeException("Failed to invoke query:\n$queryText", e)
        }
    }

    private data class NullParameter(val type: Int)

}

/**
 * A nicer, more functional way to start off a call chain to specify a
 * [Query] fully.
 *
 * @see [Query]
 */
fun query(@Language("PostgreSQL") sqlText: String) = Query(sqlText)
