package com.yousafdev.kidshield

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform