package com.chinosk.chieri.client.distributed.utils

import com.chinosk.chieri.client.distributed.utils.models.GroupInfoList
import com.chinosk.chieri.client.distributed.utils.models.LoginInfo
import com.chinosk.chieri.client.distributed.utils.models.UserInfoList
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class Api {
    companion object {
        fun getLoginInfo(): LoginInfo? {
            val reqStr = HttpReq.requestGetStr("http://127.0.0.1:${Globals.Config.httpPort}/get_login_info")
            return JsonParser.parseJson(reqStr)
        }

        fun getFriendListStr(): String {
            val origStr = HttpReq.requestGetStr("http://127.0.0.1:${Globals.Config.httpPort}/get_friend_list")
            val parsed: MutableMap<String, Any> = JsonParser.parseJson(origStr) ?: return ""
            val data = (parsed["data"] ?: return "") as List<*>
            val newData = mutableListOf<Map<*, *>>()
            for (i in data) {
                val iData = (i as Map<*, *>).toMutableMap()
                iData["nickname"] = iData["user_name"]
                iData["remark"] = iData["user_remark"]
                newData.add(iData)
            }
            parsed["data"] = newData
            return JsonParser.dumpJson(parsed) ?: ""
        }

        fun getGroupListStr(): String {
            val origStr = HttpReq.requestGetStr("http://127.0.0.1:${Globals.Config.httpPort}/get_group_list")

            val parsed: MutableMap<String, Any> = JsonParser.parseJson(origStr) ?: return ""
            val data = (parsed["data"] ?: return "") as List<*>
            val newData = mutableListOf<Map<*, *>>()
            for (i in data) {
                val iData = (i as Map<*, *>).toMutableMap()
                iData["group_memo"] = iData["group_remark"]
                iData["group_create_time"] = 0
                iData["group_level"] = 0
                newData.add(iData)
            }
            parsed["data"] = newData
            return JsonParser.dumpJson(parsed) ?: ""
        }

        fun getGroupList(): GroupInfoList? {
            return JsonParser.parseJson(getGroupListStr())
        }

        fun getFriendList(): UserInfoList? {
            return JsonParser.parseJson(getFriendListStr())
        }

        fun sendGroupMessage(groupId: String, message: String): String {
            val urlBuilder = "http://127.0.0.1:${Globals.Config.httpPort}/send_group_msg".toHttpUrlOrNull()?.newBuilder()
            urlBuilder?.addQueryParameter("group_id", groupId)
            urlBuilder?.addQueryParameter("message", message)
            return HttpReq.requestGetStr(urlBuilder.toString())
        }

        fun sendPrivateMessage(userId: String, message: String): String {
            val urlBuilder = "http://127.0.0.1:${Globals.Config.httpPort}/send_private_msg".toHttpUrlOrNull()?.newBuilder()
            urlBuilder?.addQueryParameter("user_id", userId)
            urlBuilder?.addQueryParameter("message", message)
            return HttpReq.requestGetStr(urlBuilder.toString())
        }

    }
}