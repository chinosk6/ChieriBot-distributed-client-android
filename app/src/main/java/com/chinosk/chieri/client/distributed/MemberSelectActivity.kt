package com.chinosk.chieri.client.distributed

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.chinosk.chieri.client.distributed.databinding.ActivityMemberSelectBinding
import com.chinosk.chieri.client.distributed.utils.ConfigManager
import com.chinosk.chieri.client.distributed.utils.Globals
import com.chinosk.chieri.client.distributed.utils.models.GroupInfo
import com.chinosk.chieri.client.distributed.utils.models.UserInfo
import com.chinosk.chieri.client.distributed.utils.updateFromList

class MemberSelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMemberSelectBinding
    private lateinit var listViewMemberSelect: ListView
    private lateinit var textViewSelectMode: TextView
    private lateinit var checkBoxSelectAll: CheckBox

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMemberSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listViewMemberSelect = binding.listViewMemberSelect
        textViewSelectMode = binding.textViewSelectMode
        checkBoxSelectAll = binding.checkBoxSelectAll
        checkBoxSelectAll.setOnClickListener { onCheckBoxSelectAllClicked() }
        listViewMemberSelect.setOnItemClickListener { _, _, _, _ -> onItemClick() }

        val currentTab = intent.getIntExtra("currentTab", 0)

        val bwTypeName: Int
        val tabName = when (currentTab) {  // 0-群, 1-私
            0 -> {
                bwTypeName = if (Globals.MsgRule.bw_type_groups == 0) R.string.blackListMode else R.string.whiteListMode
                R.string.tabTextGroupMode
            }
            1 -> {
                bwTypeName = if (Globals.MsgRule.bw_type_friends == 0) R.string.blackListMode else R.string.whiteListMode
                R.string.tabTextPrivateMode
            }
            else -> {
                bwTypeName = R.string.unknownMode
                R.string.unknownMode
            }
        }

        textViewSelectMode.text = "${getString(tabName)} - ${getString(bwTypeName)}"
        binding.fab.setOnClickListener { onButtonClickedSubmitList(currentTab) }

        renderListView(currentTab)
    }

    private fun renderListView(currentTab: Int) {  // 0-群, 1-私
        val adapter = when (currentTab) {
            0 -> GroupInfoArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, Globals.groupList)
            1 -> UserInfoArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, Globals.friendsList)
            else -> return
        }

        listViewMemberSelect.adapter = adapter
        listViewMemberSelect.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        val checkedList = when (currentTab) {
            0 -> {
                when (Globals.MsgRule.bw_type_groups) {
                    0 -> Globals.MsgRule.blacklist_groups  // 群, 黑
                    1 -> Globals.MsgRule.whitelist_groups  // 群, 白
                    else -> return
                }

            }
            1 -> {
                when (Globals.MsgRule.bw_type_friends) {
                    0 -> Globals.MsgRule.blacklist_friends  // 友, 黑
                    1 -> Globals.MsgRule.whitelist_friends  // 友, 白
                    else -> return
                }
            }
            else -> return
        }

        for (i in 0 until adapter.count) {
            val itemId = listViewMemberSelect.getItemIdAtPosition(i).toString()
            if (checkedList.contains(itemId)) {
                listViewMemberSelect.setItemChecked(i, true)
            }
        }
        refreshCheckBoxSelectAll()
    }

    private fun onButtonClickedSubmitList(currentTab: Int) {
        val checkedIds: MutableList<String> = mutableListOf()
        val checkedItemPositions = listViewMemberSelect.checkedItemPositions
        for (i in 0 until checkedItemPositions.size()) {
            val position = checkedItemPositions.keyAt(i)
            val isChecked = checkedItemPositions.valueAt(i)

            if (isChecked) {
                checkedIds.add(listViewMemberSelect.getItemIdAtPosition(position).toString())
            }
        }

        when (currentTab) {
            0 -> {
                when (Globals.MsgRule.bw_type_groups) {
                    0 -> Globals.MsgRule.blacklist_groups.updateFromList(checkedIds) // 群, 黑
                    1 -> Globals.MsgRule.whitelist_groups.updateFromList(checkedIds) // 群, 白
                }

            }
            1 -> {
                when (Globals.MsgRule.bw_type_friends) {
                    0 -> Globals.MsgRule.blacklist_friends.updateFromList(checkedIds) // 友, 黑
                    1 -> Globals.MsgRule.whitelist_friends.updateFromList(checkedIds) // 友, 白
                }
            }

        }
        ConfigManager.saveConfig()
        normalEnd()
    }

    private fun onCheckBoxSelectAllClicked() {
        for (i in 0 until listViewMemberSelect.adapter.count) {
            listViewMemberSelect.setItemChecked(i, checkBoxSelectAll.isChecked)
        }
    }

    private fun refreshCheckBoxSelectAll() {
        var isCheckAll = true
        for (index in 0 until listViewMemberSelect.adapter.count) {
            if (!listViewMemberSelect.isItemChecked(index)) {
                isCheckAll = false
                break
            }
        }
        checkBoxSelectAll.isChecked = isCheckAll
    }

    private fun onItemClick() {
        refreshCheckBoxSelectAll()
    }

    private fun normalEnd() {
        val intent = Intent()
        intent.putExtra("isNormalEnd", true)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}


class GroupInfoArrayAdapter(context: Context, resource: Int, objects: List<GroupInfo>) :
    ArrayAdapter<String>(context, resource, convertObjList(objects)) {

    companion object {
        private fun convertObjList(objects: List<GroupInfo>): List<String> {
            return objects.map { "${it.group_name} (${it.group_id})" }
        }
    }

    private var data: List<GroupInfo> = objects

    override fun getItemId(position: Int): Long {
        return try {
            data[position].group_id ?: 0
        } catch (_: Exception) {
            0
        }
    }
}

class UserInfoArrayAdapter(context: Context, resource: Int, objects: List<UserInfo>) :
    ArrayAdapter<String>(context, resource, convertObjList(objects)) {

    companion object {
        private fun convertObjList(objects: List<UserInfo>): List<String> {
            return objects.map { "${it.nickname} (${it.user_id})" }
        }
    }

    private var data: List<UserInfo> = objects

    override fun getItemId(position: Int): Long {

        return try {
            data[position].user_id
        } catch (_: Exception) {
            0
        }
    }
}
