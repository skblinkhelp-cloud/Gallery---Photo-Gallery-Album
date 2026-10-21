package com.aistudio.gallery.pxkq.util

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
  private const val TAG = "AdManager"

  // Google AdMob Official Test Unit IDs
  const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
  const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

  private var interstitialAd: InterstitialAd? = null
  private var isMobileAdsInitialized = false
  private var lastInterstitialTime = 0L

  fun initialize(context: Context) {
    if (!isMobileAdsInitialized) {
      MobileAds.initialize(context) {
        isMobileAdsInitialized = true
        Log.d(TAG, "Google Mobile Ads SDK Initialized")
        preloadInterstitial(context)
      }
    }
  }

  fun preloadInterstitial(context: Context) {
    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(
      context,
      TEST_INTERSTITIAL_AD_UNIT_ID,
      adRequest,
      object : InterstitialAdLoadCallback() {
        override fun onAdLoaded(ad: InterstitialAd) {
          interstitialAd = ad
          Log.d(TAG, "Test Interstitial Ad loaded successfully")
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
          interstitialAd = null
          Log.e(TAG, "Test Interstitial Ad failed to load: ${error.message}")
        }
      }
    )
  }

  fun showInterstitialIfReady(activity: Activity, onAdDismissed: () -> Unit = {}) {
    val now = System.currentTimeMillis()
    if ((now - lastInterstitialTime) < 30_000L) {
      onAdDismissed()
      return
    }

    val currentAd = interstitialAd
    if (currentAd != null) {
      lastInterstitialTime = now
      currentAd.show(activity)
      interstitialAd = null
      preloadInterstitial(activity)
      onAdDismissed()
    } else {
      preloadInterstitial(activity)
      onAdDismissed()
    }
  }
}

@Composable
fun TestBannerAd(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(50.dp)
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .testTag("test_banner_ad"),
    contentAlignment = Alignment.Center
  ) {
    AndroidView(
      factory = { ctx ->
        AdView(ctx).apply {
          setAdSize(AdSize.BANNER)
          adUnitId = AdManager.TEST_BANNER_AD_UNIT_ID
          loadAd(AdRequest.Builder().build())
        }
      },
      modifier = Modifier.fillMaxWidth()
    )
  }
}
