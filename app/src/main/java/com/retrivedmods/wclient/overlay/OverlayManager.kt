package com.retrivedmods.wclient.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.view.WindowManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.platform.compositionContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner

import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.retrivedmods.wclient.game.ModuleManager
import com.retrivedmods.wclient.ui.theme.WClientTheme
import com.retrivedmods.wclient.overlay.gui.classic.OverlayButton
import com.retrivedmods.wclient.overlay.gui.classic.OverlayShortcutButton
import com.retrivedmods.wclient.overlay.gui.clickgui.ClickGUIButton
import com.retrivedmods.wclient.overlay.gui.clickgui.ClickGUIOverlay
import com.retrivedmods.wclient.overlay.gui.w.WOverlayButton
import com.retrivedmods.wclient.overlay.gui.w.WOverlayManager
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
@Suppress("MemberVisibilityCanBePrivate")
object OverlayManager {

    private val overlayWindows = ArrayList<OverlayWindow>()

    var currentContext: Context? = null
        private set

    var isShowing = false
        private set

    private var currentOverlayButton: OverlayWindow? = null
    private var currentClickGUI: OverlayWindow? = null
    private var currentClickGUIOverlay: OverlayWindow? = null

    private fun getGUITheme(context: Context): GUITheme {
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return try {
            GUITheme.valueOf(
                sharedPreferences.getString("gui_theme", "CLASSIC") ?: "CLASSIC"
            )
        } catch (e: Exception) {
            GUITheme.CLASSIC
        }
    }

    private fun initializeOverlays(context: Context) {
        val theme = getGUITheme(context)


        overlayWindows.clear()
        currentOverlayButton = null
        currentClickGUI = null
        currentClickGUIOverlay = null


        when (theme) {
            GUITheme.W -> {

                currentOverlayButton = WOverlayButton()

            }
            GUITheme.CLICKGUI -> {
                currentOverlayButton = ClickGUIButton()
            }
            GUITheme.CLASSIC -> {
                currentOverlayButton = OverlayButton()
                overlayWindows.addAll(
                    ModuleManager
                        .modules
                        .filter { it.isShortcutDisplayed }
                        .map { it.overlayShortcutButton }
                )
            }
        }


        currentOverlayButton?.let { overlayWindows.add(it) }
    }

    fun showOverlayWindow(overlayWindow: OverlayWindow) {
        overlayWindows.add(overlayWindow)

        val context = currentContext
        if (isShowing && context != null) {
            showOverlayWindow(context, overlayWindow)
        }
    }

    fun dismissOverlayWindow(overlayWindow: OverlayWindow) {
        overlayWindows.remove(overlayWindow)

        val context = currentContext
        if (isShowing && context != null) {
            dismissOverlayWindow(context, overlayWindow)
        }
    }

    fun show(context: Context) {
        currentContext = context


        initializeOverlays(context)

        val theme = getGUITheme(context)
        when (theme) {
            GUITheme.W -> {

                WOverlayManager.showOverlayButton()
            }
            GUITheme.CLICKGUI -> {
                overlayWindows.forEach {
                    showOverlayWindow(context, it)
                }
            }
            GUITheme.CLASSIC -> {

                overlayWindows.forEach {
                    showOverlayWindow(context, it)
                }
            }
        }

        isShowing = true
    }

    fun dismiss() {
        val context = currentContext
        if (context != null) {
            val theme = getGUITheme(context)
            when (theme) {
                GUITheme.W -> {

                    WOverlayManager.hideAll()
                }
                GUITheme.CLICKGUI -> {
                    overlayWindows.forEach {
                        dismissOverlayWindow(context, it)
                    }
                    currentClickGUIOverlay?.let { dismissOverlayWindow(context, it) }
                }
                GUITheme.CLASSIC -> {

                    overlayWindows.forEach {
                        dismissOverlayWindow(context, it)
                    }
                }
            }
            isShowing = false
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun showOverlayWindow(context: Context, overlayWindow: OverlayWindow) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = overlayWindow.layoutParams
        val composeView = overlayWindow.composeView
        composeView.setContent {
            WClientTheme {
                overlayWindow.Content()
            }
        }
        val lifecycleOwner = overlayWindow.lifecycleOwner
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        composeView.setViewTreeLifecycleOwner(lifecycleOwner)
        composeView.setViewTreeViewModelStoreOwner(object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore
                get() = overlayWindow.viewModelStore
        })
        composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        composeView.compositionContext = overlayWindow.recomposer
        if (overlayWindow.firstRun) {
            overlayWindow.composeScope.launch {
                overlayWindow.recomposer.runRecomposeAndApplyChanges()
            }
            overlayWindow.firstRun = false
        }

        try {
            windowManager.addView(composeView, layoutParams)
        } catch (_: Exception) {

        }
    }

    private fun dismissOverlayWindow(context: Context, overlayWindow: OverlayWindow) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val composeView = overlayWindow.composeView

        try {
            windowManager.removeView(composeView)
        } catch (_: Exception) {

        }
    }

    fun updateOverlayOpacity(opacity: Float) {
        overlayWindows.find { it is OverlayButton }?.let { button ->
            button.layoutParams.alpha = opacity
            currentContext?.let { context ->
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                    .updateViewLayout(button.composeView, button.layoutParams)
            }
        }
    }

    fun updateShortcutOpacity(opacity: Float) {
        overlayWindows.filter { it is OverlayShortcutButton }.forEach { button ->
            button.layoutParams.alpha = opacity
            currentContext?.let { context ->
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                    .updateViewLayout(button.composeView, button.layoutParams)
            }
        }
    }

    fun updateOverlayIcon() {
        overlayWindows.find { it is OverlayButton }?.let { button ->
            currentContext?.let { context ->
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                    .updateViewLayout(button.composeView, button.layoutParams)
            }
        }
    }

    fun updateOverlayBorder() {
        overlayWindows.find { it is OverlayButton }?.let { button ->
            currentContext?.let { context ->
                try {
                    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

                    if (button.composeView.isAttachedToWindow) {
                        windowManager.updateViewLayout(button.composeView, button.layoutParams)
                    }
                } catch (e: Exception) {

                }
            }
        }
    }

    fun refreshTheme() {
        val context = currentContext
        if (context != null && isShowing) {

            dismissAllOverlays()

            show(context)
        }
    }

    private fun dismissAllOverlays() {
        val context = currentContext ?: return


        overlayWindows.forEach {
            try {
                dismissOverlayWindow(context, it)
            } catch (_: Exception) {

            }
        }


        try {
            WOverlayManager.hideAll()
        } catch (_: Exception) {

        }


        overlayWindows.clear()
        currentOverlayButton = null
        currentClickGUI = null
        currentClickGUIOverlay = null
    }

    fun showClickGUI() {
        val context = currentContext ?: return
        if (currentClickGUIOverlay == null) {
            currentClickGUIOverlay = ClickGUIOverlay()
        }
        currentClickGUIOverlay?.let { showOverlayWindow(context, it) }
    }

    fun dismissClickGUI() {
        val context = currentContext ?: return
        currentClickGUIOverlay?.let {
            dismissOverlayWindow(context, it)
            currentClickGUIOverlay = null
        }
    }

}