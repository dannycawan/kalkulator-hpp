package com.kalkulator.hpp.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import androidx.compose.runtime.mutableStateOf

/**
 * Simple wrapper for loading a banner and an interstitial ad.
 */
class AdMobManager(private val context: Context) {

    val bannerAdView = mutableStateOf<AdView?>(null)
    private var interstitialAd: InterstitialAd? = null

    init {
        MobileAds.initialize(context) {}
        loadBanner()
        loadInterstitial()
    }

    private fun loadBanner() {
        val adView = AdView(context)
        adView.adUnitId = context.getString(com.kalkulator.hpp.R.string.admob_banner_id)
        adView.setAdSize(AdSize.BANNER)
        adView.loadAd(AdRequest.Builder().build())
        bannerAdView.value = adView
    }

    private fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            context.getString(com.kalkulator.hpp.R.string.admob_interstitial_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            })
    }

    fun showInterstitial(activity: Activity, onClosed: () -> Unit) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadInterstitial()
                onClosed()
            }
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                onClosed()
            }
        }
        interstitialAd?.show(activity) ?: onClosed()
    }
}
