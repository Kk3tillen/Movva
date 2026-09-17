package br.com.movva.core.clipboard

import platform.UIKit.UIPasteboard

actual class ClipboardManager {
    actual fun copy(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}
