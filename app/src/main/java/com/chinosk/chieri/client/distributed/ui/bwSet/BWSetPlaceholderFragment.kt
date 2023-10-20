package com.chinosk.chieri.client.distributed.ui.bwSet


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chinosk.chieri.client.distributed.databinding.FragmentBWSetBinding
import com.chinosk.chieri.client.distributed.ui.PageViewModel
import com.chinosk.chieri.client.distributed.utils.ConfigManager
import com.chinosk.chieri.client.distributed.utils.Globals
import com.chinosk.chieri.client.distributed.utils.models.GroupInfoList
import com.chinosk.chieri.client.distributed.utils.models.UserInfoList

/**
 * A placeholder fragment containing a simple view.
 */
class BWSetPlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentBWSetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var adapter: ArrayAdapter<String>? = null
    private var showData: List<String> = listOf()
    private lateinit var radioButtonBlackMode: RadioButton
    private lateinit var radioButtonWhiteMode: RadioButton
    private lateinit var checkBoxGroupAlso: CheckBox

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
        _binding = FragmentBWSetBinding.inflate(inflater, container, false)
        val root = binding.root

        adapter = ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_list_item_1)
        radioButtonBlackMode = binding.radioButtonBlackMode
        radioButtonWhiteMode = binding.radioButtonWhiteMode
        checkBoxGroupAlso = binding.checkBoxGroupAlso

        radioButtonBlackMode.setOnCheckedChangeListener { _, _ -> onBWRadioButtonCheckedChange() }
        checkBoxGroupAlso.setOnCheckedChangeListener { _, _ -> onBWGroupAlsoCheckedChange() }
        // radioButtonWhiteMode.setOnCheckedChangeListener { _, _ -> onBWRadioButtonClick() }
        // val currentPageIndex = arguments?.getInt(ARG_SECTION_NUMBER) ?: return root  // 0-群, 1-私

        refreshBWList()

        return root
    }

    private fun renderListView() {
        if (adapter == null) return
        val listView = binding.listViewBW
        adapter!!.clear()
        adapter!!.addAll(showData)
        listView.adapter = adapter
    }

    private fun updateShowDataAndRadioButton() {
        val currentPageIndex = arguments?.getInt(ARG_SECTION_NUMBER) ?: return  // 0-群, 1-私

        when (currentPageIndex) {
            0 -> {  // 群
                checkBoxGroupAlso.visibility = View.GONE
                if (Globals.MsgRule.bw_type_groups == 0) {  // 0-黑, 1-白
                    radioButtonBlackMode.isChecked = true
                    showData = Globals.MsgRule.blacklist_groups.map { "${GroupInfoList.getGroupName(it)} ($it)" }
                }
                else {
                    radioButtonWhiteMode.isChecked = true
                    showData = Globals.MsgRule.whitelist_groups.map { "${GroupInfoList.getGroupName(it)} ($it)" }
                }
            }
            1 -> {
                checkBoxGroupAlso.visibility = View.VISIBLE
                if (Globals.MsgRule.bw_type_friends == 0) {  // 0-黑, 1-白
                    radioButtonBlackMode.isChecked = true
                    showData = Globals.MsgRule.blacklist_friends.map { "${UserInfoList.getUserName(it)} ($it)" }
                }
                else {
                    radioButtonWhiteMode.isChecked = true
                    showData = Globals.MsgRule.whitelist_friends.map { "${UserInfoList.getUserName(it)} ($it)" }
                }
            }
            else -> {
                showData = listOf()
            }
        }
    }

    fun refreshBWList() {
        checkBoxGroupAlso.isChecked = Globals.MsgRule.group_also
        updateShowDataAndRadioButton()
        renderListView()
    }

    private fun onBWRadioButtonCheckedChange() {
        val currentPageIndex = arguments?.getInt(ARG_SECTION_NUMBER) ?: return  // 0-群, 1-私
        val bwType = if (radioButtonBlackMode.isChecked) 0 else 1
        when (currentPageIndex) {
            0 -> Globals.MsgRule.bw_type_groups = bwType
            1 -> Globals.MsgRule.bw_type_friends = bwType
        }
        ConfigManager.saveConfig()
        refreshBWList()
    }

    private fun onBWGroupAlsoCheckedChange() {
        Globals.MsgRule.group_also = checkBoxGroupAlso.isChecked
        ConfigManager.saveConfig()
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
        fun newInstance(sectionNumber: Int): BWSetPlaceholderFragment {
            return BWSetPlaceholderFragment().apply {
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