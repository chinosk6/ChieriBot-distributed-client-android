package com.chinosk.chieri.client.distributed

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.ViewPager
import com.chinosk.chieri.client.distributed.databinding.ActivityPokeSetBinding
import com.chinosk.chieri.client.distributed.ui.pokeSet.PokeSetPlaceholderFragment
import com.chinosk.chieri.client.distributed.ui.pokeSet.PokeSetSectionsPagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout


class PokeSetActivity : AppCompatActivity() {
    companion object {
        val cancelVisibilityLiveData = MutableLiveData<Boolean>()
    }

    private lateinit var binding: ActivityPokeSetBinding
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPokeSetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = PokeSetSectionsPagerAdapter(this, supportFragmentManager)
        viewPager = binding.viewPager
        val textViewCancel: TextView = binding.textViewCancel
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        val fabAdd: FloatingActionButton = binding.fabAdd
        val fabDelete: FloatingActionButton = binding.fabDelete

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { cancelTarget() }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        fabAdd.setOnClickListener {
            for ((index, item) in supportFragmentManager.fragments.withIndex()) {
                if (index == viewPager.currentItem) {
                    (item as PokeSetPlaceholderFragment).showAddDataFloatingDialog()
                    break
                }
            }
        }
        fabDelete.setOnClickListener {
            for ((index, item) in supportFragmentManager.fragments.withIndex()) {
                if (index == viewPager.currentItem) {
                    (item as PokeSetPlaceholderFragment).deleteSelect()
                    break
                }
            }
        }
        textViewCancel.setOnClickListener { cancelTarget() }
        cancelVisibilityLiveData.observe(this) {
            if (it) {
                textViewCancel.visibility = View.VISIBLE
                fabAdd.visibility = View.GONE
                fabDelete.visibility = View.VISIBLE
            }
            else {
                textViewCancel.visibility = View.GONE
                fabAdd.visibility = View.VISIBLE
                fabDelete.visibility = View.GONE
            }
        }

    }

    private fun cancelTarget() {
        for ((index, item) in supportFragmentManager.fragments.withIndex()) {
            if (index == viewPager.currentItem) {
                (item as PokeSetPlaceholderFragment).onCancelClicked()
                break
            }
        }
    }

}