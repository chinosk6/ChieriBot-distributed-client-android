package com.chinosk.chieri.client.distributed.ui

import androidx.lifecycle.*

class PageViewModel : ViewModel() {

    private val _index = MutableLiveData<Int>()

    val text: LiveData<String> = _index.map { "$it" }

    fun setIndex(index: Int) {
        _index.value = index
    }
}