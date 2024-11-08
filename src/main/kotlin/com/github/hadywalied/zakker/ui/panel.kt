package com.github.hadywalied.zakker.ui

import com.github.hadywalied.zakker.application.AzkarService
import com.github.hadywalied.zakker.domain.Azkar
import com.github.hadywalied.zakker.ui.clock.AnalogClock
import com.intellij.openapi.project.Project
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants
import javax.swing.SwingConstants

data class FontSet(
    val h1: Font,
    val h2: Font,
    val h3: Font,
    val regular: Font
)

class ModernZakkerPanel(
    var project: Project?,
    private val azkarService: AzkarService
) : BorderLayoutPanel() {
    private val fonts: FontSet
    private var searchField: SearchTextField
    private var categoriesPanel: JPanel
    val clock = AnalogClock()

    init {
        fonts = loadFonts()
        background = JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR
        border = JBUI.Borders.empty(20)

        // Initialize components
        searchField = createSearchField()
        categoriesPanel = createCategoriesPanel()

        // Build layout
        add(createHeader(), BorderLayout.NORTH)
        add(createContentPanel(), BorderLayout.CENTER)
    }

    companion object {
        private const val FONT_PATH = "/fonts/UthmanicHafs_V22.ttf"
        private const val TITLE_TEXT = "ذكِّر"
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

    private fun createHeader(): JPanel = JPanel(BorderLayout()).apply {
        background = JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR
        border = JBUI.Borders.emptyBottom(20)

        // Title panel with icon
        val titlePanel = JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
            background = JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR
            add(JBLabel(TITLE_TEXT).apply {
                font = fonts.h1
                foreground = JBUI.CurrentTheme.Label.foreground()
            })
        }

        // Clock panel
        val clockPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            background = JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR
            add(clock)
        }

        add(titlePanel, BorderLayout.CENTER)
        add(clockPanel, BorderLayout.WEST)
    }

    private fun createSearchField(): SearchTextField = SearchTextField().apply {
        textEditor.apply {
            border = JBUI.Borders.empty(8)
            font = fonts.regular
            componentOrientation = ComponentOrientation.RIGHT_TO_LEFT
        }
        addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: javax.swing.event.DocumentEvent) {
                filterCategories(text)
            }
        })
    }

    private fun createContentPanel(): JPanel = JPanel(BorderLayout()).apply {
        background = JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR

        // Add search panel
        val searchPanel = JPanel(BorderLayout()).apply {
            background = JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR
            border = JBUI.Borders.emptyBottom(20)
            add(searchField, BorderLayout.CENTER)
        }

        // Add categories panel with scroll
        val scrollPane = JBScrollPane(categoriesPanel).apply {
            border = JBUI.Borders.empty()
            verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            viewport.background = JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR
        }

        add(searchPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
    }

    private fun createCategoriesPanel(): JPanel = JPanel().apply {
        background = JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR
        layout = ModernGridLayout(1, 10) // 1 columns, 10px gap
        border = JBUI.Borders.empty(10)

        // Add category cards
        azkarService.getAzkar().forEach { azkar ->
            add(createModernCategoryCard(azkar))
        }
    }

    private fun createModernCategoryCard(azkar: Azkar): JPanel = JPanel(BorderLayout()).apply {
        background = JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR
        border = JBUI.Borders.customLine(
            JBUI.CurrentTheme.Focus.focusColor(),
            1, 1, 1, 1
        )

        // Card content
        val content = JPanel(BorderLayout()).apply {
            background = UIUtil.getPanelBackground()
            border = JBUI.Borders.empty(15)

            // Category name
            add(JBLabel(azkar.category.name).apply {
                font = fonts.h3
                horizontalAlignment = SwingConstants.RIGHT
            }, BorderLayout.NORTH)

            // Count label
            add(JBLabel("${azkar.zikr.size} ذكر").apply {
                font = fonts.regular
                foreground = JBUI.CurrentTheme.Label.disabledForeground()
                horizontalAlignment = SwingConstants.RIGHT
            }, BorderLayout.CENTER)
        }

        add(content, BorderLayout.CENTER)

        // Hover effect and click handler
        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                background = JBUI.CurrentTheme.Focus.focusColor().brighter()
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            }

            override fun mouseExited(e: MouseEvent) {
                background = JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR
                cursor = Cursor.getDefaultCursor()
            }

            override fun mouseClicked(e: MouseEvent) {
                project?.let {
                    ZekrCarouselDialog(
                        project = it,
                        category = azkar.category,
                        zekrs = azkar.zikr,
                        customFont = fonts.h3
                    ).show()
                }
            }
        })
    }

    private fun filterCategories(searchText: String) {
        categoriesPanel.removeAll()

        val filteredAzkar = if (searchText.isEmpty()) {
            azkarService.getAzkar()
        } else {
            azkarService.getAzkar().filter { azkar ->
                azkar.category.name.contains(searchText, ignoreCase = true) ||
                        azkar.zikr.any { it.search?.contains(searchText, ignoreCase = true) ?: false }
            }
        }

        filteredAzkar.forEach { azkar ->
            categoriesPanel.add(createModernCategoryCard(azkar))
        }

        categoriesPanel.revalidate()
        categoriesPanel.repaint()
    }

}


