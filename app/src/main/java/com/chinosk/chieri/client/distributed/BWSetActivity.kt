package com.chinosk.chieri.client.distributed

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.chinosk.chieri.client.distributed.ui.bwSet.BWSetSectionsPagerAdapter
import com.chinosk.chieri.client.distributed.databinding.ActivityBwsetBinding
import com.chinosk.chieri.client.distributed.ui.bwSet.BWSetPlaceholderFragment

class BWSetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBwsetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBwsetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = BWSetSectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = binding.fab

        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val isNormalEnd = result.data?.getBooleanExtra("isNormalEnd", false) ?: false
                if (isNormalEnd) {
                    for (i in supportFragmentManager.fragments) {
                        (i as BWSetPlaceholderFragment).refreshBWList()
                    }
                }
            }
        }

        fab.setOnClickListener { view ->
            // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            val intent = Intent(this, MemberSelectActivity::class.java)
            intent.putExtra("currentTab", viewPager.currentItem)
            // startActivity(intent)
            launcher.launch(intent)
        }
    }

}