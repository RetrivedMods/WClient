package com.retrivedmods.wclient.game

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.retrivedmods.wclient.R

enum class ModuleCategory(
    @DrawableRes val iconResId: Int,
    @StringRes val labelResId: Int
) {

    Combat(
        iconResId = R.drawable.swords_24px,
        labelResId = R.string.combat
    ),
    Motion(
        iconResId = R.drawable.sprint_24px,
        labelResId = R.string.motion
    ),
    Visual(
        iconResId = R.drawable.view_in_ar_24px,
        labelResId = R.string.visual
    ),
    Misc(
        iconResId = R.drawable.toc_24px,
        labelResId = R.string.misc
    ),
    Config(
        iconResId = R.drawable.manufacturing_24px,
        labelResId = R.string.config
    )

}