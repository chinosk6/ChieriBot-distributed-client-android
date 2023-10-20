package com.chinosk.chieri.client.distributed.utils.models

import com.chinosk.chieri.client.distributed.utils.Globals


data class UserInfo (
    var user_id: Long = 0,
    var nickname: String? = null,
    var user_displayname: String? = null,
    var remark: String? = null,
    var age: Int? = 0,
    var gender: Int? = 0,
    var group_id: Int? = 0,
    var platform: String? = null,
    var term_type: Long? = 0
)

class UserInfoList (
    var status: String? = null,
    var retcode: Int = 0,
    var data: List<UserInfo>? = null,
    var echo: String? = null
) {
    companion object {
        fun getUserName(userId: Long): String {
            val result = Globals.friendsList.find { it.user_id == userId }
            return result?.nickname ?: "[Unknown]"
        }

        fun getUserName(userId: String): String {
            return try {
                getUserName(userId.toLong())
            } catch (_: Exception) {
                "[Unknown]"
            }
        }
    }
}
