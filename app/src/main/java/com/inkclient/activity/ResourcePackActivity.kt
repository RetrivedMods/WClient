package com.inkclient.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.inkclient.logging.InkClientLogger
import com.inkclient.modules.ModuleRegistry
import com.inkclient.modules.resource.GlobalResourcePackChanger
import com.inkclient.ui.components.ResourcePackPanel
import com.inkclient.ui.theme.InkColors
import kotlinx.coroutines.launch
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

class ResourcePackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register module for discoverability
        ModuleRegistry.register("globalResourcePackChanger", GlobalResourcePackChanger)

        setContent {
            Surface(
                modifier = Modifier,
                color = InkColors.VelvetBlack
            ) {
                ResourcePackPanel(
                    onApply = { pack ->
                        lifecycleScope.launch {
                            val result = GlobalResourcePackChanger.applyPack(pack)
                            InkClientLogger.i("InkClient: applyPack result=$result for ${pack.id}")
                        }
                    },
                    onHotSwap = { pack ->
                        lifecycleScope.launch {
                            val result = GlobalResourcePackChanger.hotSwapPack(pack)
                            InkClientLogger.i("InkClient: hotSwapPack result=$result for ${pack.id}")
                        }
                    }
                )
            }
        }
    }
}
