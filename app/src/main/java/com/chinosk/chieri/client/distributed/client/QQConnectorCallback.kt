package com.chinosk.chieri.client.distributed.client

import com.chinosk.chieri.client.distributed.connector.IWebSocketCallbacks
import com.chinosk.chieri.client.distributed.messageProcessor.MessageProcessor
import com.chinosk.chieri.client.distributed.utils.Logger
import okhttp3.WebSocket


object QQConnectorCallback: IWebSocketCallbacks {
    private var isConnect = false

    override fun onMessage(webSocket: WebSocket, message: String) {
        MessageProcessor.onQQMessage(webSocket, message)
    }

    override fun onClose(webSocket: WebSocket) {
        isConnect = false
        Logger.warning("Shamrock 连接已断开")
    }

    override fun onError(webSocket: WebSocket, error: Throwable) {
        isConnect = false
        Logger.error("Shamrock 连接出现错误: $error")
    }

    override fun onOpen(webSocket: WebSocket) {
        isConnect = true
        Logger.info("Shamrock 连接成功")
    }

    fun getIsConnect(): Boolean {
        return isConnect
    }

}
