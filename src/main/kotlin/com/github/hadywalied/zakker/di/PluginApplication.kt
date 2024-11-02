package com.github.hadywalied.zakker.di

import com.intellij.openapi.components.BaseComponent
import org.koin.core.context.startKoin

class PluginApplication : BaseComponent {
    override fun initComponent() {
        startKoin {
            modules(pluginModule)
        }
    }
}