package com.chinosk.chieri.client.distributed.utils


import com.daveanthonythomas.moshipack.MoshiPack
import okio.ByteString
import java.util.Date


object MsgPack {

    fun mapToMsgPack(jsonMap: Map<String, Any>): ByteArray {
        return MoshiPack.pack(jsonMap).readByteArray()
    }

    fun jsonToMsgPack(json: String): ByteArray {
        return MoshiPack.jsonToMsgpack(json).readByteArray()
    }

    fun msgpackToJson(bytes: ByteArray): String {
        return MoshiPack.msgpackToJson(bytes)
    }

    fun getSendPack(msg: Map<String, Any>): ByteArray {
        val ts = Date().time
        return mapToMsgPack(mapOf(
            "t" to Globals.Config.token,
            "a" to ACalc.calcA(Globals.Config.token, ts),
            "ts" to ts,
            "m" to msg
        ))
    }

    fun loginPack(qq: Long, nick: String, friends: String, groups: String): ByteArray {
        return getSendPack(mapOf(
            "type" to "login",
            "qq" to qq,
            "nick" to nick,
            "friends" to friends,
            "groups" to groups,
            "version" to Globals.VERSION,
        ))
    }

    fun gocqPack(msg: Map<String, Any>): ByteArray {
        return getSendPack(mapOf(
            "type" to "gocq",
            "msg" to mapToMsgPack(msg)
        ))
    }

    fun callAPIPack(id: String, errString: String): ByteArray {
        return getSendPack(mapOf(
            "type" to "api_resp",
            "data" to mapOf(
                "error" to true,
                "id" to id,
                "response" to errString
            )
        ))
    }

    fun callAPIPack(id: String, url: String, statusCode: Int, content: ByteArray?, headers: Map<String, String>? = null): ByteArray {
        return getSendPack(mapOf(
            "type" to "api_resp",
            "data" to mapOf(
                "error" to false,
                "id" to id,
                "response" to mapOf(
                    "url" to url,
                    "statusCode" to statusCode,
                    "content" to content,
                    "headers" to headers
                )
            )
        ))
    }

}