/*
 * Copyright 2018 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.novaChat.features.settings

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.content.edit
import im.vector.novaChat.BuildConfig
import im.vector.novaChat.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * Object to manage the Locale choice of the user
 */
object VectorLocale {
    private const val APPLICATION_LOCALE_COUNTRY_KEY = "APPLICATION_LOCALE_COUNTRY_KEY"
    private const val APPLICATION_LOCALE_VARIANT_KEY = "APPLICATION_LOCALE_VARIANT_KEY"
    private const val APPLICATION_LOCALE_LANGUAGE_KEY = "APPLICATION_LOCALE_LANGUAGE_KEY"
    private const val APPLICATION_LOCALE_SCRIPT_KEY = "APPLICATION_LOCALE_SCRIPT_KEY"

    private val defaultLocale = Locale("en", "US")

    /**
     * The supported application languages
     */
    var supportedLocales = ArrayList<Locale>()
        private set

    /**
     * Provides the current application locale
     */
    var applicationLocale = defaultLocale
        private set

    /**
     * Init this object
     */
    fun init(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        if (preferences.contains(APPLICATION_LOCALE_LANGUAGE_KEY)) {
            applicationLocale = Locale(preferences.getString(APPLICATION_LOCALE_LANGUAGE_KEY, ""),
                    preferences.getString(APPLICATION_LOCALE_COUNTRY_KEY, ""),
                    preferences.getString(APPLICATION_LOCALE_VARIANT_KEY, "")
            )
        } else {
            applicationLocale = Locale.getDefault()

            // detect if the default language is used
            val defaultStringValue = getString(context, defaultLocale, R.string.resources_country_code)
            if (defaultStringValue == getString(context, applicationLocale, R.string.resources_country_code)) {
                applicationLocale = defaultLocale
            }

            saveApplicationLocale(context, applicationLocale)
        }

        // init the known locales in background, using kotlin coroutines
        GlobalScope.launch(Dispatchers.IO) {
            initApplicationLocales(context)
        }
    }

    /**
     * Save the new application locale.
     */
    fun saveApplicationLocale(context: Context, locale: Locale) {
        applicationLocale = locale

        PreferenceManager.getDefaultSharedPreferences(context).edit {
            val language = locale.language
            if (language.isEmpty()) {
                remove(APPLICATION_LOCALE_LANGUAGE_KEY)
            } else {
                putString(APPLICATION_LOCALE_LANGUAGE_KEY, language)
            }

            val country = locale.country
            if (country.isEmpty()) {
                remove(APPLICATION_LOCALE_COUNTRY_KEY)
            } else {
                putString(APPLICATION_LOCALE_COUNTRY_KEY, country)
            }

            val variant = locale.variant
            if (variant.isEmpty()) {
                remove(APPLICATION_LOCALE_VARIANT_KEY)
            } else {
                putString(APPLICATION_LOCALE_VARIANT_KEY, variant)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val script = locale.script
                if (script.isEmpty()) {
                    remove(APPLICATION_LOCALE_SCRIPT_KEY)
                } else {
                    putString(APPLICATION_LOCALE_SCRIPT_KEY, script)
                }
            }
        }
    }

    /**
     * Get String from a locale
     *
     * @param context    the context
     * @param locale     the locale
     * @param resourceId the string resource id
     * @return the localized string
     */
    private fun getString(context: Context, locale: Locale, resourceId: Int): String {
        val result: String

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            result = try {
                context.createConfigurationContext(config).getText(resourceId).toString()
            } catch (e: Exception) {
                Timber.e(e, "## getString() failed")
                // use the default one
                context.getString(resourceId)
            }
        } else {
            val resources = context.resources
            val conf = resources.configuration
            @Suppress("DEPRECATION")
            val savedLocale = conf.locale
            @Suppress("DEPRECATION")
            conf.locale = locale
            @Suppress("DEPRECATION")
            resources.updateConfiguration(conf, null)

            // retrieve resources from desired locale
            result = resources.getString(resourceId)

            // restore original locale
            @Suppress("DEPRECATION")
            conf.locale = savedLocale
            @Suppress("DEPRECATION")
            resources.updateConfiguration(conf, null)
        }

        return result
    }

    /**
     * Provides the supported application locales list
     *
     * @param context the context
     */
    private fun initApplicationLocales(context: Context) {
        val knownLocalesSet = HashSet<Triple<String, String, String>>()

        try {
            val availableLocales = Locale.getAvailableLocales()

            for (locale in availableLocales) {
                knownLocalesSet.add(
                        Triple(
                                getString(context, locale, R.string.resources_language),
                                getString(context, locale, R.string.resources_country_code),
                                getString(context, locale, R.string.resources_script)
                        )
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "## getApplicationLocales() : failed")
            knownLocalesSet.add(
                    Triple(
                            context.getString(R.string.resources_language),
                            context.getString(R.string.resources_country_code),
                            context.getString(R.string.resources_script)
                    )
            )
        }

        supportedLocales.clear()

        knownLocalesSet.mapTo(supportedLocales) { (language, country, script) ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Locale.Builder()
                        .setLanguage(language)
                        .setRegion(country)
                        .setScript(script)
                        .build()
            } else {
                Locale(language, country)
            }
        }

        // sort by human display names
        supportedLocales.sortWith(Comparator { lhs, rhs -> localeToLocalisedString(lhs).compareTo(localeToLocalisedString(rhs)) })
    }

    /**
     * Convert a locale to a string
     *
     * @param locale the locale to convert
     * @return the string
     */
    fun localeToLocalisedString(locale: Locale): String {
        return buildString {
            append(locale.getDisplayLanguage(locale))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && locale.script != "Latn"
                    && locale.getDisplayScript(locale).isNotEmpty()) {
                append(" - ")
                append(locale.getDisplayScript(locale))
            }

            if (locale.getDisplayCountry(locale).isNotEmpty()) {
                append(" (")
                append(locale.getDisplayCountry(locale))
                append(")")
            }

            // In debug mode, also display information about the locale in the current locale.
            if (BuildConfig.DEBUG) {
                append("\n[")
                append(locale.displayLanguage)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && locale.script != "Latn") {
                    append(" - ")
                    append(locale.displayScript)
                }
                if (locale.displayCountry.isNotEmpty()) {
                    append(" (")
                    append(locale.displayCountry)
                    append(")")
                }
                append("]")
            }
        }
    }
}
