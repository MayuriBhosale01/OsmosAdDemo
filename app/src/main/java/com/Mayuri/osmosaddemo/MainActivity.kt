package com.Mayuri.osmosaddemo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.Mayuri.osmosaddemo.databinding.ActivityMainBinding
import com.Mayuri.osmosaddemo.domain.model.BannerAd
import com.Mayuri.osmosaddemo.presentation.viewmodel.AdViewModel
import com.Mayuri.osmosaddemo.utils.AdLogger
import com.Mayuri.osmosaddemo.utils.VisibilityTracker
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AdViewModel by viewModels()
    private var visibilityTracker: VisibilityTracker? = null
    private var currentAd: BannerAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupVisibilityTracker()
        setupObservers()
    }

    private fun setupClickListeners() {
        binding.btnLoadAd.setOnClickListener {
            viewModel.loadAd()
        }

        binding.ivBannerAd.setOnClickListener {
            currentAd?.let { ad ->
                viewModel.fireClick(ad)
                openUrl(ad.destinationUrl)
            }
        }
    }

    private fun setupVisibilityTracker() {
        visibilityTracker = VisibilityTracker {
            currentAd?.let { ad ->
                viewModel.fireImpression(ad)
            }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AdViewModel.UiState.Idle -> showIdle()
                        is AdViewModel.UiState.Loading -> showLoading()
                        is AdViewModel.UiState.AdLoaded -> {
                            currentAd = state.ad
                            renderAd(state.ad)
                        }
                        is AdViewModel.UiState.Empty -> showFallback(state.message)
                        is AdViewModel.UiState.Error -> showError(state.message)
                    }
                }
            }
        }

        AdLogger.events.observe(this) { events ->
            binding.tvEventLog.text = events.takeLast(10).joinToString("\n")
        }
    }

    private fun renderAd(ad: BannerAd) {
        binding.progressBar.visibility = View.GONE
        binding.tvStatus.text = "✅ Ad Loaded"
        binding.tvFallback.visibility = View.GONE
        binding.ivBannerAd.visibility = View.VISIBLE

        if (ad.width > 0 && ad.height > 0) {
            val ratio = ad.height.toFloat() / ad.width.toFloat()
            binding.ivBannerAd.post {
                val targetHeight = (binding.ivBannerAd.width * ratio).toInt()
                binding.ivBannerAd.layoutParams.height = targetHeight
                binding.ivBannerAd.requestLayout()
            }
        }

        Glide.with(this)
            .load(ad.imageUrl)
            .into(binding.ivBannerAd)

        binding.ivBannerAd.post {
            visibilityTracker?.reset()
            visibilityTracker?.attach(binding.ivBannerAd)
        }
    }

    private fun showIdle() {
        binding.tvStatus.text = "Tap 'Load Ad' to begin"
        binding.progressBar.visibility = View.GONE
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvStatus.text = "Fetching ad..."
        binding.ivBannerAd.visibility = View.GONE
        binding.tvFallback.visibility = View.GONE
    }

    private fun showFallback(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.tvStatus.text = "ℹ️ $message"
        binding.ivBannerAd.visibility = View.GONE
        binding.tvFallback.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.tvStatus.text = "❌ $message"
        binding.ivBannerAd.visibility = View.GONE
        binding.tvFallback.visibility = View.VISIBLE
    }

    private fun openUrl(url: String) {
        if (url.isEmpty()) return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            AdLogger.logError("No browser to open URL")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        visibilityTracker?.detach(binding.ivBannerAd)
    }
}