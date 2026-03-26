package com.alan.homeautomationapp.core;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Objects;

// Class responsible for managing the APP language
public class LanguageManager {

    // Method to set the language
    public static void setLanguage(String languageCode) {

        LocaleListCompat appLocale =
                LocaleListCompat.forLanguageTags(languageCode);

        AppCompatDelegate.setApplicationLocales(appLocale);
    }

    // Method to get the current language
    public static String getLanguage() {

        LocaleListCompat locales =
                AppCompatDelegate.getApplicationLocales();

        if (!locales.isEmpty()) {
            return Objects.requireNonNull(locales.get(0)).getLanguage();
        }

        return "en";
    }
}
