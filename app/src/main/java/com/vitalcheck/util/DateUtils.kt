package com.vitalcheck.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE // yyyy-MM-dd

    fun today(): String = LocalDate.now().format(formatter)

    fun yesterday(): String = LocalDate.now().minusDays(1).format(formatter)

    fun parse(date: String): LocalDate = LocalDate.parse(date, formatter)

    fun format(date: LocalDate): String = date.format(formatter)
}
