package com.chinosk.chieri.client.distributed.utils.models

import com.chinosk.chieri.client.distributed.utils.Logger
import java.io.InvalidObjectException


data class ClientAdminCommand (
    var command_admin: MutableList<String> = mutableListOf(),
    var add_black_group_cmd: String = "//c add black group",
    var del_black_group_cmd: String = "//c del black group",
    var add_white_group_cmd: String = "//c add white group",
    var del_white_group_cmd: String = "//c del white group",
    var bw_group_white_type_cmd: String = "//c group white",
    var bw_group_black_type_cmd: String = "//c group black",

    var add_black_user_cmd: String = "//c add black private",
    var del_black_user_cmd: String = "//c del black private",
    var add_white_user_cmd: String = "//c add white private",
    var del_white_user_cmd: String = "//c del white private",
    var bw_user_white_type_cmd: String = "//c private white",
    var bw_user_black_type_cmd: String = "//c private black",
    var bw_user_black_group_also_enable_cmd: String = "//c pg enable",
    var bw_user_black_group_also_disable_cmd: String = "//c pg disable",

    var black_group_list_cmd: String = "//c black group list",
    var white_group_list_cmd: String = "//c white group list",
    var black_private_list_cmd: String = "//c black private list",
    var white_private_list_cmd: String = "//c white private list",

    var poke_enable_cmd: String = "//c poke enable",
    var poke_disable_cmd: String = "//c poke disable",
    var poke_angry_rate_cmd: String = "//c poke angry rate",
    var poke_angry_time_cmd: String = "//c poke angry time",

    var poke_corpus_angry_list_cmd: String = "//c poke corpus angry list",
    var poke_corpus_notangry_cmd: String = "//c poke corpus notangry list",
    var poke_corpus_angry_add_cmd: String = "//c poke corpus angry add",
    var poke_corpus_angry_del_cmd: String = "//c poke corpus angry del",
    var poke_corpus_notangry_add_cmd: String = "//c poke corpus notangry add",
    var poke_corpus_notangry_del_cmd: String = "//c poke corpus notangry del"
)

data class MsgRuleModel (
    var block: MutableList<MutableList<Any>> = mutableListOf(),
    var convert: MutableList<MutableList<Any>> = mutableListOf(),
    var whitelist_friends: MutableList<String> = mutableListOf(),
    var blacklist_friends: MutableList<String> = mutableListOf(),
    var blacklist_groups: MutableList<String> = mutableListOf(),
    var whitelist_groups: MutableList<String> = mutableListOf(),
    var bw_type_groups: Int = 0,  // 0 - 黑名单, 1 - 白名单
    var bw_type_friends: Int = 0,  // 0 - 黑名单, 1 - 白名单
    var enable_poke: Boolean = false,
    var poke_angry_rate: Int = 50,
    var poke_angry_keep_time: Int = 120,
    var poke_angry_lang: MutableList<String> = mutableListOf("哼! 不理你了! 赌气两分钟~"),
    var poke_lang: MutableList<String> = mutableListOf("咿呀~", "你干嘛！", "别戳啦！", "你再戳！", "你再戳我要生气了！",
        "不开心qwq", "咿呀！吓我一跳~", "嗯。嗯~嗯？嗯！", "睡觉zzz",
        "我家也没什么值钱的了，唯一能拿得出手的也就是我了"),
    var group_also: Boolean = true,
    var enable_client_admin_command: Boolean = true,
    var client_admin_command: ClientAdminCommand = ClientAdminCommand()
) {
    init {
        try {
            for ((index, item) in block.withIndex()) {
                var innerList = item[1]
                if (innerList is List<*>) {
                    innerList = innerList as MutableList<Any>
                    for ((inIndex, inItem) in innerList.withIndex()) {
                        if ((inItem is Double) or (inItem is Float)) innerList[inIndex] = inItem.toInt()
                        block[index][1] = innerList
                    }
                }
            }
            for ((index, item) in convert.withIndex()) {
                if ((item[2] is Float) or (item[2] is Double)) item[2] = item[2].toInt()
                var innerList = item[3]
                if (innerList is List<*>) {
                    innerList = innerList as MutableList<Any>
                    for ((inIndex, inItem) in innerList.withIndex()) {
                        if ((inItem is Double) or (inItem is Float)) innerList[inIndex] = inItem.toInt()
                        convert[index][3] = innerList
                    }
                }
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
            Logger.error("Init MsgRuleModel error: $e")
        }

    }
}

fun Any.toInt(): Int {
    return when (this) {
        is Int -> this
        is Float -> this.toInt()
        is Double -> this.toInt()
        is Long -> this.toInt()
        is Short -> this.toInt()
        is Byte -> this.toInt()
        else -> throw InvalidObjectException("Can't convert $this to Int.")
    }
}

fun Any.toLongEx(): Long? {
    return when (this) {
        is Long -> this
        is Float -> this.toLong()
        is Double -> this.toLong()
        is Int -> this.toLong()
        is Short -> this.toLong()
        is Byte -> this.toLong()
        else -> null
    }
}
