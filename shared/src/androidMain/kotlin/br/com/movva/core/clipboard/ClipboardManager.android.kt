package br.com.movva.core.clipboard

import android.content.ClipData
import android.content.Context

actual class ClipboardManager(private val context: Context) {
    actual fun copy(text: String) {
        val systemClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                as android.content.ClipboardManager
        systemClipboard.setPrimaryClip(ClipData.newPlainText("user_tag", text))
    }
}
