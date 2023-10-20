package com.chinosk.chieri.client.distributed.utils

import java.math.BigInteger
import java.security.MessageDigest


object ACalc {
    fun getMd5Hash(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val messageDigest = md.digest(input.toByteArray())
        val no = BigInteger(1, messageDigest)
        var hashText = no.toString(16)
        while (hashText.length < 32) {
            hashText = "0$hashText"
        }
        return hashText
    }

    fun calcA(string: String, ts: Long): String {
        return getMd5Hash("$string-$ts")
    }
}
