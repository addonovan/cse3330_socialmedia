package com.addonovan.cse3330

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    // force the database connection open before so any failure is immediate
    // and not delayed until the first time the class is
    forceInit(DbEngine::class.java)

    SpringApplication.run(Application::class.java, *args)
}

private fun <T> forceInit(clazz: Class<T>) {
    try {
        Class.forName(clazz.name, true, clazz.classLoader)
    } catch (e: ClassNotFoundException) {
        throw AssertionError("Failed to force class initialization!")
    }

}
