package com.example.restopass.common

// Usar el unicode y reemplazar U+ por 0x. Ejemplo: U+1F601 -> 0x1F601
// https://apps.timwhitlock.info/emoji/tables/unicode
object EmojisHelper {
    val happy = 0x1F601.emoji()
    val leftHand = 	0x1F448.emoji()

    private fun Int.emoji() = StringBuilder(String(Character.toChars(this)))
}