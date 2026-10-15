package br.com.movva.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun dataDeHojeIso(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
