package com.chinosk.chieri.client.distributed.ui.pokeSet


import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chinosk.chieri.client.distributed.MsgRuleActivity
import com.chinosk.chieri.client.distributed.R
import com.chinosk.chieri.client.distributed.databinding.FragmentPokeSetBinding
import com.chinosk.chieri.client.distributed.ui.PageViewModel
import com.chinosk.chieri.client.distributed.utils.*
import com.chinosk.chieri.client.distributed.utils.models.MsgRuleUIDataModel

/**
 * A placeholder fragment containing a simple view.
 */
class PokeSetPlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentPokeSetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var listViewMsgRule: ListView

    private var showData: List<MsgRuleUIDataModel> = listOf()

    private var isMultipleSelectMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPokeSetBinding.inflate(inflater, container, false)
        val root = binding.root

        listViewMsgRule = binding.listViewMsgRule
        listViewMsgRule.setOnItemClickListener { _, _, i, _ -> if (!isMultipleSelectMode) showAddDataFloatingDialog(i) }
        listViewMsgRule.setOnItemLongClickListener { _, _, i, _ -> if (!isMultipleSelectMode) onItemLongClick(i) else false }

        refreshListView()

        return root
    }

    private fun getListAdapter(isMulti: Boolean = false): MsgRuleArrayAdapter {
        showData = getShowData()
        return MsgRuleArrayAdapter(this.requireContext(),
            if (isMulti) android.R.layout.simple_list_item_multiple_choice else android.R.layout.simple_list_item_1,
            showData)
    }

    private fun changeMultipleSelectMode(isMulti: Boolean) {
        listViewMsgRule.adapter = getListAdapter(isMulti)
        isMultipleSelectMode = isMulti
        listViewMsgRule.choiceMode = if (isMulti) ListView.CHOICE_MODE_MULTIPLE else ListView.CHOICE_MODE_NONE
        MsgRuleActivity.cancelVisibilityLiveData.postValue(isMulti)
    }

    private fun getShowData(): List<MsgRuleUIDataModel> {
        val currentPageIndex = arguments?.getInt(ARG_SECTION_NUMBER) ?: return listOf()
        return when (currentPageIndex) {  // 0-拦截, 1-替换
            0 -> Globals.MsgRule.block.map {
                try {
                    val newList: MutableList<Int> = mutableListOf()
                    for (i in it[1] as MutableList<*>) {
                        newList.add(i as Int)
                    }
                    MsgRuleUIDataModel(it[0] as String, flags = newList)
                }
                catch (e: Exception) {
                    MsgRuleUIDataModel(null)
                }
            }
            1 -> Globals.MsgRule.convert.map {
                try {
                    val newList: MutableList<Int> = mutableListOf()
                    for (i in it[3] as MutableList<*>) {
                        newList.add(i as Int)
                    }
                    MsgRuleUIDataModel(it[0] as String, it[1] as String, newList)
                }
                catch (e: Exception) {
                    MsgRuleUIDataModel(null)
                }
            }
            else -> listOf()
        }
    }

    private fun refreshListView() {
        changeMultipleSelectMode(false)
    }

    fun showAddDataFloatingDialog(clickIndex: Int = -1) {
        val currentPageIndex = arguments?.getInt(ARG_SECTION_NUMBER) ?: return  // 0-拦截, 1-替换

        val dialogBuilder = AlertDialog.Builder(this.requireContext())
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.floating_dialog_layout, null)
        dialogBuilder.setView(dialogView)

        val inputEditTextPattern = dialogView.findViewById<EditText>(R.id.inputEditTextPattern)
        val textViewReplaceTo = dialogView.findViewById<TextView>(R.id.textViewReplaceTo)
        val inputEditTextReplaceString = dialogView.findViewById<EditText>(R.id.inputEditTextReplaceString)
        val textViewPriority = dialogView.findViewById<TextView>(R.id.textViewPriority)
        val inputEditTextPriority = dialogView.findViewById<EditText>(R.id.inputEditTextPriority)
        val checkBoxIgnoreCase = dialogView.findViewById<CheckBox>(R.id.checkBoxIgnoreCase)
        val checkBoxMultiline = dialogView.findViewById<CheckBox>(R.id.checkBoxMultiline)
        val checkBoxDotMatchesAll = dialogView.findViewById<CheckBox>(R.id.checkBoxDotMatchesAll)
        if (currentPageIndex == 0) {
            inputEditTextReplaceString.isEnabled = false
            inputEditTextPriority.isEnabled = false
            textViewReplaceTo.visibility = View.GONE
            textViewPriority.visibility = View.GONE
            inputEditTextReplaceString.visibility = View.GONE
            inputEditTextPriority.visibility = View.GONE
        }
        val currData: MsgRuleUIDataModel
        if (clickIndex >= 0) {
            currData = showData[clickIndex].copy()
            inputEditTextPattern.setText(currData.matchPattern ?: "")
            if (currData.newString != null) inputEditTextReplaceString.setText(currData.newString)
            if (currentPageIndex == 1) inputEditTextPriority.setText(clickIndex.toString())
            if (currData.flags.contains(2)) checkBoxIgnoreCase.isChecked = true
            if (currData.flags.contains(8)) checkBoxMultiline.isChecked = true
            if (currData.flags.contains(16)) checkBoxDotMatchesAll.isChecked = true
        }
        else {
            if (currentPageIndex == 1) inputEditTextPriority.setText(showData.size.toString())
            currData = MsgRuleUIDataModel(null)
        }

        dialogBuilder.setPositiveButton("确定") { dialog, _ ->
            currData.matchPattern = inputEditTextPattern.text.toString()
            currData.newString = inputEditTextReplaceString.text.toString()
            currData.flags.clear()
            if (checkBoxIgnoreCase.isChecked) currData.flags.add(2)
            if (checkBoxMultiline.isChecked) currData.flags.add(8)
            if (checkBoxDotMatchesAll.isChecked) currData.flags.add(16)
            val newPriority = try {inputEditTextPriority.text.toString().toInt()} catch (_: Exception) {showData.size}
            onDialogPositiveClick(currentPageIndex, clickIndex, newPriority, currData)
            dialog.dismiss()
        }

        dialogBuilder.setNegativeButton("取消") { dialog, _ ->
            dialog.cancel()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    fun deleteSelect() {
        val currentPageIndex = arguments?.getInt(ARG_SECTION_NUMBER) ?: return

        // val currData = showData as MutableList<MsgRuleUIDataModel>
        if (!isMultipleSelectMode) return
        val checkedItemPositions = listViewMsgRule.checkedItemPositions
        val rmList = mutableListOf<Int>()
        for (i in 0 until checkedItemPositions.size()) {
            val position = checkedItemPositions.keyAt(i)
            val isChecked = checkedItemPositions.valueAt(i)
            if (isChecked) {
                rmList.add(position)
            }
        }
        for (index in rmList.sortedDescending()) {
            when (currentPageIndex) {  // 0-拦截, 1-替换
                0 -> if (index in Globals.MsgRule.block.indices) Globals.MsgRule.block.removeAt(index)
                1 -> if (index in Globals.MsgRule.convert.indices) Globals.MsgRule.convert.removeAt(index)
            }
        }
        refreshListView()
    }

    private fun onItemLongClick(index: Int): Boolean {
        changeMultipleSelectMode(true)
        listViewMsgRule.setItemChecked(index, true)
        return true
    }

    fun onCancelClicked() {
        if (!isMultipleSelectMode) return
        for (i in 0..listViewMsgRule.adapter.count) {
            listViewMsgRule.setItemChecked(i, false)
        }
        changeMultipleSelectMode(false)
    }

    private fun onDialogPositiveClick(currentPageIndex: Int, origIndex: Int, newIndexIn: Int, data: MsgRuleUIDataModel) {
        try {
            var newIndex = newIndexIn
            when (currentPageIndex) {
                0 -> {  // 拦截
                    val addList = mutableListOf(data.matchPattern!!, data.flags)
                    if (origIndex < 0) {
                        Globals.MsgRule.block.add(addList)
                    }
                    else {
                        Globals.MsgRule.block[origIndex].replaceFromList(addList)
                    }
                }
                1 -> {  // 替换
                    val addList = mutableListOf(data.matchPattern!!, data.newString!!, 0, data.flags)
                    if (origIndex < 0) {
                        if (newIndex > Globals.MsgRule.convert.size) newIndex = Globals.MsgRule.convert.size
                        Globals.MsgRule.convert.add(newIndex, addList)
                    }
                    else {
                        if (origIndex == newIndex) {
                            Globals.MsgRule.convert[origIndex].replaceFromList(addList)
                        }
                        else {
                            Globals.MsgRule.convert.removeAt(origIndex)
                            if (newIndex > Globals.MsgRule.convert.size) newIndex = Globals.MsgRule.convert.size
                            Globals.MsgRule.convert.add(newIndex, addList)
                        }
                    }
                }
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
            Logger.error("保存消息规则时出错: $e")
        }
        ConfigManager.saveConfig()
        refreshListView()
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PokeSetPlaceholderFragment {
            return PokeSetPlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


class MsgRuleArrayAdapter(context: Context, resource: Int, objects: List<MsgRuleUIDataModel>) :
    ArrayAdapter<String>(context, resource, convertObjList(objects)) {

    companion object {
        private fun convertObjList(objects: List<MsgRuleUIDataModel>): List<String> {
            return objects.map { "${it.matchPattern ?: "[Invalid Data]"} ${if (it.newString != null) "→ ${it.newString}" else ""}" }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /*
    private var data: List<MsgRuleUIDataModel> = objects

    fun getMsgRuleData(index: Int): MsgRuleUIDataModel {
        return data[index]
    }

    fun getMsgRuleDataList(): List<MsgRuleUIDataModel> {
        return data
    }
     */

}
