package br.com.movva.core.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.localTimeZone

actual fun dataDeHojeIso(): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd"
        locale = NSLocale(localeIdentifier = "en_US_POSIX")
        timeZone = NSTimeZone.localTimeZone
    }
    return formatter.stringFromDate(NSDate())
}
