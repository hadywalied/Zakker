package com.github.hadywalied.zakker.ui.clock

import com.intellij.util.ui.JBUI
import java.awt.*
import java.util.*
import javax.swing.JPanel
import kotlin.math.cos
import kotlin.math.sin


// Modern analog clock implementation
class AnalogClock : JPanel() {
    init {
        preferredSize = Dimension(80, 80)
        background = JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val center = Point(width / 2, height / 2)
        val radius = minOf(width, height) / 2 - 5

        // Draw clock face
        g2.color = JBUI.CurrentTheme.Label.foreground()
        g2.drawOval(center.x - radius, center.y - radius, radius * 2, radius * 2)

        // Get current time
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        // Draw hands
        drawHand(g2, center, radius * 0.5, (hour + minute / 60.0) * 30, 3f) // Hour
        drawHand(g2, center, radius * 0.7, minute * 6.0, 2f) // Minute
        drawHand(g2, center, radius * 0.8, second * 6.0, 1f) // Second
    }

    private fun drawHand(g2: Graphics2D, center: Point, length: Double, angle: Double, thickness: Float) {
        val radian = Math.toRadians(angle - 90)
        val endX = center.x + (length * cos(radian)).toInt()
        val endY = center.y + (length * sin(radian)).toInt()

        g2.stroke = BasicStroke(thickness)
        g2.drawLine(center.x, center.y, endX, endY)
    }
}
