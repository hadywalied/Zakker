package com.github.hadywalied.zakker

import com.github.hadywalied.zakker.application.AzkarService
import com.github.hadywalied.zakker.domain.Azkar
import com.github.hadywalied.zakker.ui.ZekrCarouselDialog
import com.github.hadywalied.zakker.ui.card.GradientCard
import com.github.hadywalied.zakker.ui.clock.AnalogClock
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.awt.BorderLayout
import java.awt.ComponentOrientation
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.util.Timer
import javax.swing.*
import kotlin.concurrent.fixedRateTimer

class Zakker : ToolWindowFactory, KoinComponent, Disposable {
    private val azkarService: AzkarService by inject()
    private var clockTimer: Timer? = null
    private var project: Project? = null

    // Font management
    private data class FontSet(
        val h1: Font,
        val h2: Font,
        val h3: Font,
        val regular: Font
    )

    private val fonts: FontSet by lazy { loadFonts() }

    companion object {
        private const val FONT_PATH = "/fonts/UthmanicHafs_V22.ttf"
        private const val TITLE_TEXT = "ذكِّر"
        private const val SUBTITLE_TEXT = "وذكّرهم"
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        this.project = project
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(createMainPanel(), "", false)
        toolWindow.contentManager.addContent(content)
        initializeClock()
    }

    private fun createMainPanel(): JPanel = JPanel(BorderLayout()).apply {
        background = JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR
        border = BorderFactory.createEmptyBorder(20, 20, 20, 20)

        add(createHeader(), BorderLayout.NORTH)
        add(createContentPanel(), BorderLayout.CENTER)
    }

    private fun createHeader(): JPanel = JPanel(BorderLayout()).apply {
        background = JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR
        add(createTitleLabel(), BorderLayout.CENTER)
        add(createAnalogClock(), BorderLayout.WEST)
    }

    private fun createTitleLabel(): JBLabel = JBLabel(TITLE_TEXT).apply {
        font = fonts.h1
        componentOrientation = ComponentOrientation.RIGHT_TO_LEFT
        horizontalAlignment = SwingConstants.CENTER
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    }

    private fun createAnalogClock(): AnalogClock = AnalogClock(JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR).apply {
        preferredSize = JBUI.size(100)
    }

    private fun createContentPanel(): JBScrollPane {
        val cardsPanel = JPanel().apply {
            background = JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            azkarService.getAzkar().forEach { azkar: Azkar ->
                add(createCategoryCard(azkar))
                add(Box.createVerticalStrut(20))
            }
        }

        return JBScrollPane(cardsPanel).apply {
            verticalScrollBarPolicy = JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            border = BorderFactory.createEmptyBorder()
            viewport.background = JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR
        }
    }

    private fun createCategoryCard(azkar: Azkar) =
        GradientCard(
            azkar.category.name,
            JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR,
            font = fonts.h3
        ) {
            project?.let { currentProject ->
                ZekrCarouselDialog(
                    project = currentProject,
                    category = azkar.category,
                    zekrs = azkar.zikr,
                    customFont = fonts.h3
                ).show()
            }
        }

    private fun loadFonts(): FontSet {
        return try {
            javaClass.getResourceAsStream(FONT_PATH)?.use { fontStream ->
                val baseFont = Font.createFont(Font.TRUETYPE_FONT, fontStream)
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(baseFont)

                FontSet(
                    h1 = baseFont.deriveFont(48f),
                    h2 = baseFont.deriveFont(32f),
                    h3 = baseFont.deriveFont(24f),
                    regular = baseFont.deriveFont(16f)
                )
            } ?: throw IllegalStateException("Could not load font from $FONT_PATH")
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to system fonts
            FontSet(
                h1 = Font.getFont(Font.SANS_SERIF).deriveFont(48f),
                h2 = Font.getFont(Font.SANS_SERIF).deriveFont(32f),
                h3 = Font.getFont(Font.SANS_SERIF).deriveFont(24f),
                regular = Font.getFont(Font.SANS_SERIF).deriveFont(16f)
            )
        }
    }

    private fun initializeClock() {
        clockTimer = fixedRateTimer("clock", false, 0L, 1000) {
            SwingUtilities.invokeLater {
                // Find and update all AnalogClock instances
                project?.let { currentProject ->
                    currentProject.getComponent(AnalogClock::class.java)?.repaint()
                }
            }
        }
    }

    override fun dispose() {
        clockTimer?.cancel()
        clockTimer = null
        project = null
    }
}