package com.inkclient.modules

import com.inkclient.logging.InkClientLogger

/**
 * ModuleRegistry — lightweight registry for modular hot-swappable components.
 */
object ModuleRegistry {
    private val modules = mutableMapOf<String, Any>()

    fun register(key: String, module: Any) {
        modules[key] = module
        InkClientLogger.i("InkClient: module registered: $key")
    }

    fun unregister(key: String) {
        modules.remove(key)
        InkClientLogger.i("InkClient: module unregistered: $key")
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? = modules[key] as? T

    fun listKeys(): List<String> = modules.keys.toList()
}
