package br.com.movva

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform