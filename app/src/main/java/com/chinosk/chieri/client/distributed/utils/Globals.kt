package com.chinosk.chieri.client.distributed.utils

import com.chinosk.chieri.client.distributed.utils.models.ConfigModel
import com.chinosk.chieri.client.distributed.utils.models.GroupInfo
import com.chinosk.chieri.client.distributed.utils.models.MsgRuleModel
import com.chinosk.chieri.client.distributed.utils.models.UserInfo
import java.io.File



object Globals {
    const val VERSION = 1.3
    var filesDir: File? = null

    val Config = ConfigModel()
    val MsgRule = MsgRuleModel()

    var friendsList: MutableList<UserInfo> = mutableListOf()
    var groupList: MutableList<GroupInfo> = mutableListOf()

}