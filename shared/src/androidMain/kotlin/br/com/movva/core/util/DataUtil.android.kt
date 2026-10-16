package br.com.movva.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun dataDeHojeIso(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

actual fun horaAtual(): Int = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
