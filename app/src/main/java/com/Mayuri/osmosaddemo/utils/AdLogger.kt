package com.Mayuri.osmosaddemo.utils

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.*

object AdLogger {

    private val _events = MutableLiveData<MutableList<String>>(mutableListOf())
    val events: LiveData<MutableList<String>> = _events

    fun log(message: String) {
        val entry = "[${timestamp()}] $message"
        Log.d("OsmosAd", entry)
        val current = _events.value ?: mutableListOf()
        current.add(entry)
        _events.postValue(current)
    }

    fun logError(message: String) {
        val entry = "[${timestamp()}] ❌ ERROR: $message"
        Log.e("OsmosAd", entry)
        val current = _events.value ?: mutableListOf()
        current.add(entry)
        _events.postValue(current)
    }

    private fun timestamp(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}