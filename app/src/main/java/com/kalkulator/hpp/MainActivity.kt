package com.kalkulator.hpp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.datastore.preferences.core.Preferences
import com.kalkulator.hpp.ads.AdMobManager
import com.kalkulator.hpp.di.AppModule
import com.kalkulator.hpp.di.SettingsKeys
import com.kalkulator.hpp.di.settingsDataStore
import com.kalkulator.hpp.ui.theme.KalkulatorHPPTheme

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
            // Read theme preferences
            val prefs by settingsDataStore.data.collectAsState(initial = androidx.datastore.preferences.core.emptyPreferences())
            val followSystem = prefs[SettingsKeys.FOLLOW_SYSTEM] ?: true
            val darkModePref = prefs[SettingsKeys.DARK_MODE] ?: false
            val systemDark = isSystemInDarkTheme()
            val useDark = if (followSystem) systemDark else darkModePref

            KalkulatorHPPTheme(useDarkTheme = useDark) {
                CompositionLocalProvider(LocalAdMobManager provides adMobManager) {
                    NavGraph(appModule = appModule, useDarkTheme = useDark)
                }
            }
        }
    }
}
