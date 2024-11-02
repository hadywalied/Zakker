package com.github.hadywalied.zakker.ui

import com.github.hadywalied.zakker.domain.Category
import com.github.hadywalied.zakker.domain.Zekr
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.jediterm.core.input.KeyEvent
import java.awt.*
import javax.swing.*


class ZekrDialogWrapper(project: Project, private val zekrs: List<Zekr>, private val font: Font) : DialogWrapper(project, true) {
    init {
        title = "قائمة الاذكار"
        isModal = true
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BorderLayout()

        // Header
        val headerLabel = JLabel("قائمة الاذكار")
        headerLabel.font = font
        headerLabel.alignmentX = JComponent.RIGHT_ALIGNMENT
        panel.add(headerLabel, BorderLayout.NORTH)

        // List
        val listModel = DefaultListModel<Zekr>()
        zekrs.forEach { listModel.addElement(it) }

        val zekrList = JList(listModel)
        zekrList.cellRenderer = ZekrListCellRenderer(font)
        zekrList.alignmentX = JComponent.RIGHT_ALIGNMENT

        val scrollPane = JScrollPane(zekrList)
        scrollPane.alignmentX = JComponent.RIGHT_ALIGNMENT
        panel.add(scrollPane, BorderLayout.CENTER)

        // Footer
        val footerPanel = JPanel()
        footerPanel.layout = FlowLayout(FlowLayout.RIGHT)

        panel.add(footerPanel, BorderLayout.SOUTH)

        return panel
    }
}

class ZekrListCellRenderer(font: Font) : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): JComponent {
        val zekr = value as Zekr
        val descriptionLabel = zekr.description?.let { JBLabel(it) }
        if (descriptionLabel != null) {
            descriptionLabel.font = font
        }
        if (descriptionLabel != null) {
            descriptionLabel.alignmentX = JComponent.RIGHT_ALIGNMENT
        }

        val zekrLabel = zekr.zekr?.let { JBLabel(it) }
        if (zekrLabel != null) {
            zekrLabel.font = font
        }
        if (zekrLabel != null) {
            zekrLabel.alignmentX = JComponent.RIGHT_ALIGNMENT
        }

        val panel = JPanel()
        panel.layout = BorderLayout()
        if (zekrLabel != null) {
            panel.add(zekrLabel, BorderLayout.NORTH)
        }
        if (descriptionLabel != null) {
            panel.add(descriptionLabel, BorderLayout.SOUTH)
        }

        return panel
    }
}


class ZekrCarouselDialog(project: Project, private val category: Category, private val zekrs: List<Zekr>, private val customFont: Font) : DialogWrapper(project, true) {
    private var currentIndex = 0
    private lateinit var previousButton: JButton
    private lateinit var nextButton: JButton
    private lateinit var pageLabel: JLabel

    init {
        title = category.name
        isModal = true
        init()
    }

    private fun createZekrPanel(zekr: Zekr): JPanel {
        return JPanel(BorderLayout(10, 10)).apply {
            border = BorderFactory.createEmptyBorder(15, 15, 15, 15)

            // Create a panel for the main content with vertical BoxLayout
            val contentPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
            }

            // Helper function to create styled text areas
            fun createStyledTextArea(text: String): JBTextArea {
                return JBTextArea(text).apply {
                    font = customFont
                    lineWrap = true
                    wrapStyleWord = true
                    isEditable = false
                    componentOrientation = ComponentOrientation.RIGHT_TO_LEFT
                    background = UIUtil.getPanelBackground()
                    border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
                }
            }

            // Add components with proper spacing
            contentPanel.add(zekr.zekr?.let { createStyledTextArea(it) })
            contentPanel.add(Box.createVerticalStrut(10))
            contentPanel.add(JSeparator())
            contentPanel.add(Box.createVerticalStrut(10))
            contentPanel.add(zekr.description?.let { createStyledTextArea(it) })
            contentPanel.add(Box.createVerticalStrut(10))
            contentPanel.add(JSeparator())
            contentPanel.add(Box.createVerticalStrut(10))

            // References in a scrollable panel
            val refsArea = zekr.reference?.let { createStyledTextArea(it) }
            val scrollPane = JBScrollPane(refsArea).apply {
                preferredSize = Dimension(400, 100)
                border = BorderFactory.createEmptyBorder()
            }
            contentPanel.add(scrollPane)

            // Wrap the content panel in a scroll pane
            add(JBScrollPane(contentPanel).apply {
                border = BorderFactory.createEmptyBorder()
            }, BorderLayout.CENTER)
        }
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())

        // Carousel
        val carouselPanel = JPanel(CardLayout())
        zekrs.forEachIndexed { index, zekr ->
            carouselPanel.add(createZekrPanel(zekr), index.toString())
        }

        panel.add(carouselPanel, BorderLayout.CENTER)

        // Navigation panel
        val navigationPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        }

        // Button panel
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 0))

        previousButton = JButton("Previous").apply {
            addActionListener {
                if (currentIndex > 0) {
                    currentIndex--
                    (carouselPanel.layout as CardLayout).show(carouselPanel, currentIndex.toString())
                    updateNavigationState()
                }
            }
        }

        nextButton = JButton("Next").apply {
            addActionListener {
                if (currentIndex < zekrs.size - 1) {
                    currentIndex++
                    (carouselPanel.layout as CardLayout).show(carouselPanel, currentIndex.toString())
                    updateNavigationState()
                }
            }
        }

        // Page indicator
        pageLabel = JLabel().apply {
            horizontalAlignment = SwingConstants.CENTER
            border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
        }

        buttonPanel.add(previousButton)
        buttonPanel.add(pageLabel)
        buttonPanel.add(nextButton)
        navigationPanel.add(buttonPanel, BorderLayout.CENTER)

        // Keyboard navigation
        panel.registerKeyboardAction(
            { previousButton.doClick() },
            KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        )
        panel.registerKeyboardAction(
            { nextButton.doClick() },
            KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        )

        panel.add(navigationPanel, BorderLayout.SOUTH)

        // Initialize navigation state
        updateNavigationState()

        return panel
    }

    private fun updateNavigationState() {
        previousButton.isEnabled = currentIndex > 0
        nextButton.isEnabled = currentIndex < zekrs.size - 1
        pageLabel.text = "${currentIndex + 1} / ${zekrs.size}"
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(600, 500)
    }
}