package com.Mayuri.osmosaddemo

import android.app.Application
import com.Mayuri.osmosaddemo.utils.AdLogger

class OsmosApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initOsmosSDK()
    }

    private fun initOsmosSDK() {
        try {
            // TODO: Replace with actual Osmos SDK init from docs
            // OsmosSDK.initialize(
            //     context = this,
            //     clientId = 10088010,
            //     productAdsHost = "demo.o-s.io",
            //     displayAdsHost = "demo-ba.o-s.io"
            // )
            AdLogger.log("SDK Initialized ✅")
        } catch (e: Exception) {
            AdLogger.logError("SDK Init Failed: ${e.message}")
        }
    }
}