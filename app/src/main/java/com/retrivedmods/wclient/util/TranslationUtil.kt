package com.retrivedmods.wclient.util

import com.retrivedmods.wclient.game.TranslationManager
import java.util.Locale

inline val String.translatedSelf: String
    get() {
        return TranslationManager.getTranslationMap(Locale.getDefault().language)[this]
            ?: this
    }