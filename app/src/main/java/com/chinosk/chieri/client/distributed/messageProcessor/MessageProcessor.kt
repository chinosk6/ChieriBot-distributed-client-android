package com.chinosk.chieri.client.distributed.messageProcessor

import com.chinosk.chieri.client.distributed.client.ServerRun
import com.chinosk.chieri.client.distributed.utils.*
import com.chinosk.chieri.client.distributed.utils.models.ApiCall
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Response
import okhttp3.WebSocket
import okio.ByteString


object MessageProcessor {
    fun onQQMessage(webSocket: WebSocket, jsonStr: String) {
        val msg: Map<String, Any> = JsonParser.parseJson(jsonStr) ?: return
        val newMsg = MessageConverter.msgParser(msg) ?: return
        ServerRun.chieriConnector.sendMessage(ByteString.of(*MsgPack.gocqPack(newMsg)))
    }

    fun onChieriServerMessage(webSocket: WebSocket, jsonStr: String) {
        val data: Map<String, Any> = JsonParser.parseJson(jsonStr) ?: return
        val msgType = (data["type"] ?: return) as String
        when (msgType) {
            "login" -> {
                if ((data["pass"] ?: false) as Boolean) {
                    Logger.info("身份验证通过: ${data["msg"]}")
                }
                else {
                    Logger.error("身份验证失败: ${data["msg"]}")
                }
            }
            "callapi" -> {
                val callData: ApiCall = JsonParser.parseJson(jsonStr) ?: return
                callData.args?.remove("chieriapparg_qq")
                if (callData.url == "") {
                    callData.url = "http://127.0.0.1:${Globals.Config.httpPort}/${callData.path}"
                }
                callData.method = callData.method.uppercase()

                val urlBuilder = callData.url.toHttpUrlOrNull()?.newBuilder()
                if (urlBuilder != null) {
                    if (callData.args != null) {
                        for (i in callData.args) {
                            urlBuilder.addQueryParameter(i.key, i.value)
                        }
                    }
                    callData.url = urlBuilder.build().toString()
                }
                var respPack: ByteArray

                try {
                    val response: Response

                    when (callData.method) {
                        "GET" -> {
                            response = HttpReq.requestGet(callData.url, callData.header)
                        }
                        "POST" -> {
                            response = when (callData.body) {
                                is String -> HttpReq.requestPost(callData.url, callData.body, callData.header)
                                is Map<*, *> -> HttpReq.requestPost(callData.url, callData.body, callData.header)
                                else -> HttpReq.requestPost(callData.url, null, callData.header)
                            }
                        }
                        else -> {
                            Logger.warning("收到服务器未知的请求方法: ${callData.method}")
                            response = HttpReq.requestGet(callData.url, callData.header)
                        }
                    }
                    respPack = MsgPack.callAPIPack(callData.id, callData.url, response.code, response.body?.bytes(), response.headers.toMap())
                }
                catch (e: Exception) {
                    respPack = MsgPack.callAPIPack(callData.id, e.toString())
                }
                webSocket.send(ByteString.of(*respPack))
            }
        }

    }

}