package com.kalkulator.hpp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kalkulator.hpp.ads.AdMobManager
import com.kalkulator.hpp.di.AppModule
import com.kalkulator.hpp.ui.theme.KalkulatorHPPTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAdMobManager = staticCompositionLocalOf<AdMobManager> {
    error("AdMobManager not provided")
}

class MainActivity : ComponentActivity() {

    private lateinit var adMobManager: AdMobManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appModule = application as AppModule
        adMobManager = AdMobManager(this)
        setContent {
            KalkulatorHPPTheme {
                CompositionLocalProvider(LocalAdMobManager provides adMobManager) {
                    NavGraph(appModule = appModule)
                }
            }
        }
    }
}
