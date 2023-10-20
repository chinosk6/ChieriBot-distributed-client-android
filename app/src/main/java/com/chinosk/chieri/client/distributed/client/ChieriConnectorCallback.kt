package com.chinosk.chieri.client.distributed.client

import com.chinosk.chieri.client.distributed.connector.IWebSocketCallbacks
import com.chinosk.chieri.client.distributed.messageProcessor.MessageProcessor
import com.chinosk.chieri.client.distributed.utils.Api
import com.chinosk.chieri.client.distributed.utils.Logger
import com.chinosk.chieri.client.distributed.utils.MsgPack
import okhttp3.WebSocket
import okio.ByteString


object ChieriConnectorCallback: IWebSocketCallbacks {
    private var isConnect = false

    override fun onMessage(webSocket: WebSocket, message: String) {
        // Logger.debug("来自Chieri: $message")
        MessageProcessor.onChieriServerMessage(webSocket, message)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        val msg = MsgPack.msgpackToJson(bytes.toByteArray())
        // Logger.debug("来自Chieri M: $msg")
        MessageProcessor.onChieriServerMessage(webSocket, msg)
    }

    override fun onClose(webSocket: WebSocket) {
        isConnect = false
        Logger.warning("服务器连接已断开")
    }

    override fun onError(webSocket: WebSocket, error: Throwable) {
        isConnect = false
        Logger.error("服务器连接出现错误: $error")
        error.printStackTrace()
    }

    override fun onOpen(webSocket: WebSocket) {
        isConnect = true
        Logger.info("服务器连接成功")

        val userInfo = Api.getLoginInfo()
        if (userInfo == null) {
            Logger.error("登录信息获取失败, 请检查您的 HTTP 端口是否填写正确")
            webSocket.close(1008, "Invalid login data.")
            return
        }
        val friends = Api.getFriendListStr()
        val groups = Api.getGroupListStr()
        val sendPack = MsgPack.loginPack(userInfo.data.user_id, userInfo.data.nickname, friends, groups)
        webSocket.send(ByteString.of(*sendPack))
    }

    fun getIsConnect(): Boolean {
        return isConnect
    }
}