package com.addonovan.cse3330

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    DbEngine.dumpData("output.log")

    SpringApplication.run(Application::class.java, *args)
}
