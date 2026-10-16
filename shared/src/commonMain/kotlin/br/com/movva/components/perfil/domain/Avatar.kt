package br.com.movva.components.perfil.domain

import kotlin.random.Random

object Avatar {
    private const val BASE_URL = "https://api.dicebear.com/9.x/dylan/png"
    private const val TAMANHO = 256

    fun novoAvatarId(): String =
        (1..16).joinToString("") { Random.nextInt(16).toString(16) }

    fun url(avatarId: String): String =
        "$BASE_URL?seed=${encode(avatarId)}&size=$TAMANHO"

    private fun encode(valor: String): String = buildString {
        valor.encodeToByteArray().forEach { byte ->
            val c = byte.toInt().toChar()
            if (c.isLetterOrDigit() && c.code < 128 || c in "-_.~") append(c)
            else append('%').append((byte.toInt() and 0xFF).toString(16).uppercase().padStart(2, '0'))
        }
    }
}
