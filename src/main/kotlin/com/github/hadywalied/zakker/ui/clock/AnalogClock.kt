package com.github.hadywalied.zakker.ui.clock

import com.intellij.ui.JBColor
import java.awt.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.swing.JPanel
import kotlin.math.cos
import kotlin.math.sin

class AnalogClock(val backgroundColor: Color) : JPanel() {
    init {
        preferredSize = Dimension(200, 200)
        background = backgroundColor
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val width = width
        val height = height
        val centerX = width / 2
        val centerY = height / 2
        val radius = kotlin.math.min(width, height) / 2 - 10

        // Draw clock face
        g2d.color = JBColor.WHITE
        g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2)
        g2d.color = JBColor.BLACK
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2)

        // Draw hour marks
        for (i in 1..12) {
            val angle = Math.toRadians(30.0 * i - 90)
            val x1 = centerX + (radius - 10) * cos(angle)
            val y1 = centerY + (radius - 10) * sin(angle)
            val x2 = centerX + radius * cos(angle)
            val y2 = centerY + radius * sin(angle)
            g2d.drawLine(x1.toInt(), y1.toInt(), x2.toInt(), y2.toInt())
        }

        val now = LocalTime.now()

        // Draw hour hand
        drawHand(g2d, centerX, centerY, radius * 0.5, (now.hour % 12 + now.minute / 60.0) * 30 - 90)

        // Draw minute hand
        drawHand(g2d, centerX, centerY, radius * 0.7, (now.minute * 6 - 90).toDouble())

        // Draw second hand
        g2d.color = JBColor.RED
        drawHand(g2d, centerX, centerY, radius * 0.9, (now.second * 6 - 90).toDouble())

        // Draw text
        g2d.color = JBColor.RED
        g2d.font = Font("Arial", Font.BOLD, 10)

        val label = now.format(DateTimeFormatter.ofPattern("hh:mm")).toString()
        val fontMetrics = g2d.fontMetrics
        val textBounds = fontMetrics.getStringBounds(label, g2d)
        val textX = ((width - textBounds.width) / 2).toFloat()
        val textY = ((height - textBounds.height) / 5 + fontMetrics.ascent).toFloat()

        g2d.drawString(label, textX, textY)
    }

    private fun drawHand(g2d: Graphics2D, centerX: Int, centerY: Int, length: Double, angle: Double) {
        val radians = Math.toRadians(angle)
        val x = centerX + length * cos(radians)
        val y = centerY + length * sin(radians)
        g2d.stroke = BasicStroke(2f)
        g2d.drawLine(centerX, centerY, x.toInt(), y.toInt())
    }
}
