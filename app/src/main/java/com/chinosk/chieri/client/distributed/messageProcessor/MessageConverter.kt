package com.chinosk.chieri.client.distributed.messageProcessor

import com.chinosk.chieri.client.distributed.utils.*
import com.chinosk.chieri.client.distributed.utils.models.MessageModel
import com.chinosk.chieri.client.distributed.utils.models.toInt
import com.chinosk.chieri.client.distributed.utils.models.toLongEx
import java.util.Date
import kotlin.random.Random


fun <T> List<T>.selectRandomOne(): T {
    return this[Random.nextInt(this.size)]
}

inline fun <reified T> setFieldValue(self: T, propertyName: String, propertyValue: Any) {
    try {
        val property = T::class.java.getDeclaredField(propertyName)
        property.set(self, propertyValue)
    }
    catch (e: Exception) {
        Logger.error("setFieldValue failed (this=${T::class.java.name}, name=$propertyName, value=$propertyValue) error: $e")
    }
}


object MessageConverter {
    private val pokeBanUsers: MutableMap<String, Long> = mutableMapOf()
    private val pyReFlag: Map<Int, RegexOption> = mapOf(
        2 to RegexOption.IGNORE_CASE,
        // 4 to RegexOption.LOCALE,
        8 to RegexOption.MULTILINE,
        16 to RegexOption.DOT_MATCHES_ALL
        // 32 to UNICODE
        // 64 to VERBOSE
    )

    fun msgParser(msgDict: Map<String, Any>): Map<String, Any>? {
        val msgChain = msgDict as MutableMap<String, Any>
        val metaType = (msgChain["meta_event_type"] ?: "") as String
        if (metaType == "heartbeat") return null  // 忽略心跳信息
        if (metaType == "lifecycle") return null  // connect

        if (msgChain.containsKey("message")) {
            if ((msgChain["message"] !is String) and msgChain.containsKey("raw_message")) {
                val rawMsg = msgChain["raw_message"]
                if (rawMsg != null) msgChain["message"] = rawMsg
            }
        }

        if (msgChain.containsKey("user_id")) {
            if (checkPokeBan((msgChain["user_id"]?.toLongEx() ?: "").toString())) {
                return null
            }
        }

        if (msgChain.containsKey("message_type")) {
            if (!permissionCheck(msgChain)) return null
        }
        else if (msgChain.containsKey("notice_type")) {
            if (msgChain["notice_type"] as String? == "notify") {
                if (msgChain["sub_type"] as String? == "poke") {
                    val groupId = msgChain["group_id"]?.toLongEx()
                    val userId = msgChain["user_id"]?.toLongEx() ?: 0
                    if (groupId != null) {
                        if (!checkBWList(groupId.toString(), "group")) {
                            Logger.debug("群 $groupId 消息被拦截")
                            return null
                        }
                        if (Globals.MsgRule.group_also) {
                            if (!checkBWList(userId.toString(), "private")) {
                                Logger.debug("群 $groupId 内用户 $userId 消息被拦截")
                                return null
                            }
                        }
                    }
                    else {
                        if (!checkBWList(userId.toString(), "private")) {
                            Logger.debug("用户 $userId 消息被拦截")
                            return null
                        }
                    }
                    onPoke(userId.toString(), groupId, msgChain["target_id"]?.toLongEx().toString(), msgChain["self_id"]?.toLongEx().toString())
                }
            }
        }

        if (msgChain.containsKey("message")) {
            val msgStr = msgChain["message"] as String?
            if (msgStr != null) {
                if (checkMessageBlock(msgStr)) return null
                msgChain["message"] = messageReplace(msgStr)
            }
        }

        return msgChain
    }

    private fun checkMessageBlock(msg: String): Boolean {
        for (i in Globals.MsgRule.block) {
            if (i.size != 2) continue
            val pattern = i[0] as String
            val flags = i[1] as List<*>
            if (msg.matches(pattern.toRegex(pyFlagsToKt(flags)))) return true
        }
        return false
    }

    private fun pyFlagToKt(pyFlag: Int): RegexOption? {
        return pyReFlag[pyFlag]
    }

    private fun pyFlagsToKt(pyFlag: List<*>): Set<RegexOption> {
        val ret: MutableSet<RegexOption> = mutableSetOf()
        for (i in pyFlag) {
            if (i is Int) {
                val add = pyFlagToKt(i)
                if (add != null) ret.add(add)
            }
        }
        return ret
    }

    private fun messageReplace(msgIn: String): String {
        var msg = msgIn
        for (i in Globals.MsgRule.convert) {
            if (i.size != 4) continue
            val pattern = i[0] as String
            val replace = i[1] as String
            // val count = i[2] as Int
            val flags = i[3] as List<*>
            msg = msg.replace(pattern.toRegex(pyFlagsToKt(flags)), replace)
        }
        return msg
    }

    private fun permissionCheck(msgMap: Map<String, Any>): Boolean {
        val msgChain = JsonParser.parseJson<MessageModel>(msgMap) ?: return true

        when (msgChain.message_type) {
            "group" -> {
                if (msgChain.sub_type == "normal") {  // 普通群消息
                    clientAdminCommandParser(msgChain.user_id.toString(), msgChain.message, "group", msgChain.group_id.toString())
                    if (!checkBWList(msgChain.group_id.toString(), "group")) {
                        Logger.debug("群 ${msgChain.group_id} 消息被拦截")
                        return false
                    }
                    if (Globals.MsgRule.group_also) {
                        if (!checkBWList(msgChain.user_id.toString(), "private")) {
                            Logger.debug("群 ${msgChain.group_id} 内用户 ${msgChain.user_id} 消息被拦截")
                            return false
                        }
                    }
                }
            }
            "private" -> {
                when (msgChain.sub_type) {
                    "friend" -> {
                        clientAdminCommandParser(msgChain.user_id.toString(), msgChain.message, "private")
                        if (!checkBWList(msgChain.user_id.toString(), "private")) {
                            Logger.debug("私聊用户 ${msgChain.user_id} 消息被拦截")
                            return false
                        }
                    }
                    "group" -> {
                        if (!checkBWList(msgChain.user_id.toString(), "private")) {
                            Logger.debug("群私聊临时用户 ${msgChain.user_id} 消息被拦截")
                            return false
                        }
                    }
                }
            }
            // "guild" -> {}
        }
        return true
    }

    private fun clientAdminCommandParser(userId: String, message: String, messageType: String, groupId: String? = null) {
        if (!Globals.MsgRule.enable_client_admin_command) return

        fun sendMsg(msg: String) {
            if (messageType == "group") {
                Api.sendGroupMessage(groupId!!, msg)
            } else if (messageType == "private") {
                Api.sendPrivateMessage(userId, msg)
            }
        }

        fun getEnd(m: String): String {
            return message.substring(m.length)
        }

        fun endAppend(cmd: String, addList: MutableList<String>, corpus: String, remove: Boolean = false) {
            if (message.startsWith(cmd)) {
                val id = getEnd(cmd).trim()
                if (id == "") {
                    sendMsg("请输入值")
                    return
                }
                if (remove) {
                    if (id in addList) {
                        addList.remove(id)
                    }
                } else {
                    if (id !in addList) {
                        addList.add(id)
                    }
                }
                ConfigManager.saveConfig()
                sendMsg(corpus.format(id))
            }
        }

        fun endSetInt(cmd: String, setAttr: String, corpus: String) {
            if (message.startsWith(cmd)) {
                val id = getEnd(cmd).trim()
                try {
                    if (id == "") {
                        sendMsg("请输入值")
                        return
                    }

                    setFieldValue(Globals.MsgRule, setAttr, id.toInt())
                    ConfigManager.saveConfig()
                    sendMsg(corpus.format(id))
                } catch (e: Exception) {
                    sendMsg("设置失败")
                    Logger.error("设置属性出现异常: 消息: $message, 匹配到指令: $cmd, 错误: $e")
                }
            }
        }

        fun matchSet(cmd: String, setAttr: String, setValue: Any, corpus: String) {
            if (message == cmd) {
                setFieldValue(Globals.MsgRule, setAttr, setValue)
                ConfigManager.saveConfig()
                sendMsg(corpus)
            }
        }

        fun matchSend(cmd: String, corpus: String) {
            if (message == cmd) {
                sendMsg(corpus)
            }
        }

        if (userId !in Globals.MsgRule.client_admin_command.command_admin) {
            return
        }

        endAppend(Globals.MsgRule.client_admin_command.add_black_group_cmd, Globals.MsgRule.blacklist_groups, "群 {} 已添加至黑名单")
        endAppend(Globals.MsgRule.client_admin_command.del_black_group_cmd, Globals.MsgRule.blacklist_groups, "群 {} 已从黑名单移除", true)
        endAppend(Globals.MsgRule.client_admin_command.add_white_group_cmd, Globals.MsgRule.blacklist_groups, "群 {} 已添加至白名单")
        endAppend(Globals.MsgRule.client_admin_command.del_white_group_cmd, Globals.MsgRule.blacklist_groups, "群 {} 已从白名单移除", true)
        matchSet(Globals.MsgRule.client_admin_command.bw_group_white_type_cmd, "bw_type_groups", 1, "群聊工作模式已设置为: 白名单模式")
        matchSet(Globals.MsgRule.client_admin_command.bw_group_black_type_cmd, "bw_type_groups", 0, "群聊工作模式已设置为: 黑名单模式")
        endAppend(Globals.MsgRule.client_admin_command.add_black_user_cmd, Globals.MsgRule.blacklist_friends, "用户 {} 已添加至黑名单")
        endAppend(Globals.MsgRule.client_admin_command.del_black_user_cmd, Globals.MsgRule.blacklist_friends, "用户 {} 已从黑名单移除", true)
        endAppend(Globals.MsgRule.client_admin_command.add_white_user_cmd, Globals.MsgRule.whitelist_friends, "用户 {} 已添加至白名单")
        endAppend(Globals.MsgRule.client_admin_command.del_white_user_cmd, Globals.MsgRule.whitelist_friends, "用户 {} 已从白名单移除", true)
        matchSet(Globals.MsgRule.client_admin_command.bw_user_white_type_cmd, "bw_type_friends", 1, "私聊工作模式已设置为: 白名单模式")
        matchSet(Globals.MsgRule.client_admin_command.bw_user_black_type_cmd, "bw_type_friends", 0, "私聊工作模式已设置为: 黑名单模式")
        matchSet(Globals.MsgRule.client_admin_command.bw_user_black_group_also_enable_cmd, "group_also", true, "私聊工作模式已设置为: 对群聊生效")
        matchSet(Globals.MsgRule.client_admin_command.bw_user_black_group_also_disable_cmd, "group_also", false, "私聊工作模式已设置为: 不对群聊生效")
        matchSet(Globals.MsgRule.client_admin_command.poke_enable_cmd, "enable_poke", true, "已打开戳一戳")
        matchSet(Globals.MsgRule.client_admin_command.poke_disable_cmd, "enable_poke", false, "已关闭戳一戳")
        endSetInt(Globals.MsgRule.client_admin_command.poke_angry_rate_cmd, "poke_angry_rate", "戳一戳生气概率已设置为: {}%")
        endSetInt(Globals.MsgRule.client_admin_command.poke_angry_time_cmd, "poke_angry_keep_time", "戳一戳生气持续时间已设置为: {}s")
        matchSend(Globals.MsgRule.client_admin_command.poke_corpus_angry_list_cmd, "[戳一戳生气语料列表]\n" + Globals.MsgRule.poke_angry_lang.joinToString("\n"))
        matchSend(Globals.MsgRule.client_admin_command.poke_corpus_notangry_cmd, "[戳一戳不生气语料列表]\n" + Globals.MsgRule.poke_lang.joinToString("\n"))
        endAppend(Globals.MsgRule.client_admin_command.poke_corpus_angry_add_cmd, Globals.MsgRule.poke_angry_lang, "已添加戳一戳生气语料: {}")
        endAppend(Globals.MsgRule.client_admin_command.poke_corpus_angry_del_cmd, Globals.MsgRule.poke_angry_lang, "已移除戳一戳生气语料: {}", true)
        endAppend(Globals.MsgRule.client_admin_command.poke_corpus_notangry_add_cmd, Globals.MsgRule.poke_lang, "已添加戳一戳不生气语料: {}")
        endAppend(Globals.MsgRule.client_admin_command.poke_corpus_notangry_del_cmd, Globals.MsgRule.poke_lang, "已移除戳一戳不生气语料: {}", true)
        matchSend(Globals.MsgRule.client_admin_command.black_group_list_cmd, "[群聊黑名单列表]\n" + Globals.MsgRule.blacklist_groups.joinToString("\n"))
        matchSend(Globals.MsgRule.client_admin_command.white_group_list_cmd, "[群聊白名单列表]\n" + Globals.MsgRule.whitelist_groups.joinToString("\n"))
        matchSend(Globals.MsgRule.client_admin_command.black_private_list_cmd, "[私聊黑名单列表]\n" + Globals.MsgRule.blacklist_friends.joinToString("\n"))
        matchSend(Globals.MsgRule.client_admin_command.white_private_list_cmd, "[私聊白名单列表]\n" + Globals.MsgRule.whitelist_friends.joinToString("\n"))
    }


    private fun checkBWList(idStr: String, typeStr: String): Boolean {
        when (typeStr) {
            "group" -> {
                when (Globals.MsgRule.bw_type_groups) {
                    0 -> return !Globals.MsgRule.blacklist_groups.contains(idStr)  // 黑名单
                    1 -> return Globals.MsgRule.whitelist_groups.contains(idStr)  // 白名单
                }
            }
            "private" -> {
                when (Globals.MsgRule.bw_type_friends) {
                    0 -> !Globals.MsgRule.blacklist_friends.contains(idStr)
                    1 -> Globals.MsgRule.whitelist_friends.contains(idStr)
                }
            }
        }
        return true
    }

    private fun onPoke(userId: String, groupIdL: Long?, targetId: String, selfId: String) {
        if (!Globals.MsgRule.enable_poke) return
        if (targetId != selfId) return
        if (Random.nextInt(101) < Globals.MsgRule.poke_angry_rate) {
            pokeBanUsers[userId] = Date().time / 1000
            if (groupIdL == null)
                Api.sendPrivateMessage(userId, Globals.MsgRule.poke_angry_lang.selectRandomOne())
            else
                Api.sendGroupMessage(groupIdL.toString(), Globals.MsgRule.poke_angry_lang.selectRandomOne())
        }
        else {
            if (groupIdL == null)
                Api.sendPrivateMessage(userId, Globals.MsgRule.poke_lang.selectRandomOne())
            else
                Api.sendGroupMessage(groupIdL.toString(), Globals.MsgRule.poke_lang.selectRandomOne())
        }
    }

    private fun checkPokeBan(userId: String): Boolean {
        val userBanTime = pokeBanUsers[userId] ?: return false
        val currentTime = Date().time / 1000
        if (currentTime - userBanTime <= Globals.MsgRule.poke_angry_keep_time) {
            Logger.info("$userId - 赌气拦截")
            return true
        }
        return false
    }

}