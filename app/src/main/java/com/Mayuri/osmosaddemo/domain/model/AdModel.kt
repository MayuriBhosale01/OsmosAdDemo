package com.Mayuri.osmosaddemo.domain.model

data class BannerAd(
    val imageUrl: String,
    val destinationUrl: String,
    val impressionTrackingUrl: String,
    val clickTrackingUrl: String,
    val width: Int,
    val height: Int
)

sealed class AdResult {
    data class Success(val ad: BannerAd) : AdResult()
    data class Empty(val message: String = "No ads available") : AdResult()
    data class Failure(val error: String) : AdResult()
}