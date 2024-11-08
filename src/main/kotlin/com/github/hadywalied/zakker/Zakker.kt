package com.github.hadywalied.zakker

import com.github.hadywalied.zakker.application.AzkarService
import com.github.hadywalied.zakker.ui.ModernZakkerPanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import javax.swing.SwingUtilities
import kotlin.concurrent.fixedRateTimer

class Zakker : ToolWindowFactory, KoinComponent, Disposable {
    private val azkarService: AzkarService by inject()
    private var clockTimer: Timer? = null
    lateinit var zakkerPanel: ModernZakkerPanel

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        zakkerPanel = ModernZakkerPanel(project, azkarService)
        val content = contentFactory.createContent(zakkerPanel, "", false)
        toolWindow.contentManager.addContent(content)
        initializeClock()
    }

    private fun initializeClock() {
        clockTimer = fixedRateTimer("clock", false, 0L, 1000) {
            SwingUtilities.invokeLater {
                // Find and update all AnalogClock instances
                zakkerPanel.clock.repaint()
            }
        }
    }

    override fun dispose() {
        clockTimer?.cancel()
        clockTimer = null
    }
}