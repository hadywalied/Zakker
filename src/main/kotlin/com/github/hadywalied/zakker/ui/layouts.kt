package com.github.hadywalied.zakker.ui

import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager

// Custom grid layout for better responsiveness
class ModernGridLayout(
    private val columns: Int,
    private val gap: Int
) : LayoutManager {
    override fun addLayoutComponent(name: String?, comp: Component?) {}
    override fun removeLayoutComponent(comp: Component?) {}

    override fun preferredLayoutSize(parent: Container): Dimension {
        val componentCount = parent.componentCount
        if (componentCount == 0) return Dimension(0, 0)

        val component = parent.getComponent(0)
        val prefSize = component.preferredSize
        val rows = (componentCount + columns - 1) / columns

        return Dimension(
            columns * prefSize.width + (columns - 1) * gap,
            rows * prefSize.height + (rows - 1) * gap
        )
    }

    override fun minimumLayoutSize(parent: Container): Dimension =
        preferredLayoutSize(parent)

    override fun layoutContainer(parent: Container) {
        val componentCount = parent.componentCount
        if (componentCount == 0) return

        val component = parent.getComponent(0)
        val prefSize = component.preferredSize
        val containerWidth = parent.width

        val effectiveColumns = minOf(columns, componentCount)
        val totalGap = (effectiveColumns - 1) * gap
        val componentWidth = (containerWidth - totalGap) / effectiveColumns

        var x = 0
        var y = 0
        var column = 0

        parent.components.forEach { comp ->
            comp.setBounds(x, y, componentWidth, prefSize.height)

            column++
            if (column < effectiveColumns) {
                x += componentWidth + gap
            } else {
                x = 0
                y += prefSize.height + gap
                column = 0
            }
        }
    }
}
