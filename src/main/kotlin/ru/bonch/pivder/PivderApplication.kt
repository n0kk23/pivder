package ru.bonch.pivder

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PivderApplication

fun main(args: Array<String>) {
	runApplication<PivderApplication>(*args)
}
