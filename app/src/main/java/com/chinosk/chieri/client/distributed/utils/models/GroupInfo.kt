package com.chinosk.chieri.client.distributed.utils.models

import com.chinosk.chieri.client.distributed.utils.Globals


data class GroupInfo (
    var group_id: Long? = 0,
    var group_name: String? = null,
    var group_memo: String? = null,  // group_remark
    var group_uin: Long? = 0,
    var admins: List<Long>? = null,
    var class_text: String? = null,
    var is_frozen: Boolean? = false,
    var max_member: Int? = 0,
    var member_num: Int? = 0,
    var member_count: Int? = 0,
    var max_member_count: Int? = 0
)


data class GroupInfoList (
    var status: String? = null,
    var retcode: Int? = 0,
    var data: List<GroupInfo>? = null,
    var echo: String? = null
) {
    companion object {
        fun getGroupName(groupId: Long): String {
            val result = Globals.groupList.find { it.group_id == groupId }
            return result?.group_name ?: "[Unknown]"
        }

        fun getGroupName(userId: String): String {
            return try {
                getGroupName(userId.toLong())
            } catch (_: Exception) {
                "[Unknown]"
            }
        }
    }
}
