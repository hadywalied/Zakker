package com.github.hadywalied.zakker.ui.card

import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.RoundRectangle2D
import javax.swing.JPanel
import javax.swing.Timer
import kotlin.math.min

class GradientCard(
    private val label: String,
    private val baseColor: Color,
    private val font: Font,
    private val onClick: () -> Unit
) : JPanel() {

    private var isHovered = false
    private val ripples = mutableListOf<Ripple>()
    private lateinit var rippleTimer: Timer

    private data class Ripple(
        val x: Float,
        val y: Float,
        val startTime: Long = System.currentTimeMillis(),
        var alpha: Float = 0.8f
    ) {
        private val RIPPLE_DURATION = 50f
        fun getProgress(): Float =
            min(1.0f, (System.currentTimeMillis() - startTime) / RIPPLE_DURATION)
    }

    init {
        preferredSize = Dimension(200, 100)
        isOpaque = false

        initializeRippleTimer()
        setupMouseListeners()
    }

    private fun initializeRippleTimer() {
        rippleTimer = Timer(16) {
            var needsRepaint = false
            ripples.removeAll { ripple ->
                val progress = ripple.getProgress()
                if (progress >= 1.0f) {
                    true
                } else {
                    ripple.alpha = 0.8f * (1 - progress)
                    needsRepaint = true
                    false
                }
            }

            if (needsRepaint) {
                repaint()
            } else if (ripples.isEmpty()) {
                rippleTimer.stop()
            }
        }
    }

    private fun setupMouseListeners() {
        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                isHovered = true
                repaint()
            }

            override fun mouseExited(e: MouseEvent) {
                isHovered = false
                repaint()
            }

            override fun mousePressed(e: MouseEvent) {
                ripples.add(Ripple(e.x.toFloat(), e.y.toFloat()))
                if (!rippleTimer.isRunning) {
                    rippleTimer.start()
                }
                repaint()
            }

            override fun mouseReleased(e: MouseEvent) {
                if (contains(e.point)) {
                    onClick()
                }
            }
        })
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val (brighterColor, darkerColor) = if (isHovered) {
            baseColor.brighter().brighter() to baseColor
        } else {
            baseColor.brighter() to baseColor.darker()
        }

        // Create gradient and rounded rectangle
        val gradient = GradientPaint(0f, 0f, brighterColor, 0f, height.toFloat(), darkerColor)
        val roundedRectangle = RoundRectangle2D.Float(2f, 2f, width - 4f, height - 4f, 15f, 15f)

        // Draw background
        g2d.paint = gradient
        g2d.fill(roundedRectangle)

        // Draw ripples
        g2d.clip = roundedRectangle
        ripples.forEach { ripple ->
            val progress = ripple.getProgress()
            val rippleSize = (min(width, height) * 2 * progress).toInt()

            g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ripple.alpha)
            g2d.color = Color.WHITE
            g2d.fillOval(
                (ripple.x - rippleSize / 2).toInt(),
                (ripple.y - rippleSize / 2).toInt(),
                rippleSize,
                rippleSize
            )
        }

        // Reset clip and composite
        g2d.clip = null
        g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)

        // Draw border
        g2d.color = darkerColor
        g2d.stroke = BasicStroke(2f)
        g2d.draw(roundedRectangle)

        // Draw text
        g2d.color = getContrastColor(baseColor)
        g2d.font = font

        val fontMetrics = g2d.fontMetrics
        val textBounds = fontMetrics.getStringBounds(label, g2d)
        val textX = ((width - textBounds.width) / 2).toFloat()
        val textY = ((height - textBounds.height) / 2 + fontMetrics.ascent).toFloat()

        g2d.drawString(label, textX, textY)
        g2d.dispose()
    }

    private fun getContrastColor(background: Color): Color {
        val luminance = (0.299 * background.red +
                0.587 * background.green +
                0.114 * background.blue) / 255
        return if (luminance > 0.5) Color.BLACK else Color.WHITE
    }

}