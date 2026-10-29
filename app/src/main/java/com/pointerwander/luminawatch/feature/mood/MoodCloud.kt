package com.pointerwander.luminawatch.feature.mood

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import kotlin.math.min
import kotlin.math.sin

/** 情绪视觉的统一颜色映射；列表、月历和记录页共用。 */
internal fun moodPaletteColor(value: Float, palette: List<Color>): Color {
    val position = value.coerceIn(0f, 1f) * 7f
    val first = position.toInt().coerceAtMost(6)
    return lerp(palette[first], palette[first + 1], position - first)
}

internal fun moodColor(value: Float): Color {
    return moodPaletteColor(value, listOf(
        Color(0xFF8664C6), Color(0xFF7289DA), Color(0xFF65A7DB), Color(0xFF71C1D4),
        Color(0xFFA6D1AB), Color(0xFFF4D168), Color(0xFFFFB563), Color(0xFFFF956A),
    ))
}

internal fun moodBackground(value: Float): Color {
    val v = value.coerceIn(0f, 1f)
    return when {
        v < .35f -> lerp(Color(0xFFDBE8FF), Color(0xFFDBF4F3), v / .35f)
        v < .7f -> lerp(Color(0xFFDBF4F3), Color(0xFFEEF6BE), (v - .35f) / .35f)
        else -> lerp(Color(0xFFEEF6BE), Color(0xFFFFC995), (v - .7f) / .3f)
    }
}

private fun cloudPath(center: Offset, radius: Float, value: Float, phase: Float = 0f): Path {
    val v = value.coerceIn(0f, 1f)
    val spread = .94f + v * .10f + sin(phase) * .025f
    val lift = (v - .5f) * .14f + sin(phase + .8f) * .035f
    val droop = (1f - v) * .10f
    val float = sin(phase + 1.7f) * .025f
    fun x(factor: Float) = center.x + radius * factor * spread
    fun y(factor: Float) = center.y + radius * (factor + float)
    return Path().apply {
        moveTo(x(-.72f), y(.48f + droop))
        cubicTo(x(-1.05f), y(.43f), x(-1.16f), y(.03f), x(-.88f), y(-.18f))
        cubicTo(x(-.90f), y(-.43f), x(-.69f), y(-.63f - lift), x(-.43f), y(-.54f - lift))
        cubicTo(x(-.26f), y(-.85f - lift), x(.04f), y(-.90f - lift), x(.22f), y(-.68f - lift))
        cubicTo(x(.48f), y(-.82f - lift), x(.75f), y(-.58f - lift), x(.73f), y(-.31f))
        cubicTo(x(1.10f), y(-.26f), x(1.18f), y(.18f), x(.89f), y(.40f))
        cubicTo(x(.70f), y(.59f + droop), x(.39f), y(.53f + droop), x(.16f), y(.59f + droop))
        cubicTo(x(-.14f), y(.67f + droop), x(-.48f), y(.59f + droop), x(-.72f), y(.48f + droop))
        close()
    }
}

/** 多层半透明云。形状、颜色和中心亮度都跟随同一个连续情绪值。 */
@Composable
fun MoodCloud(value: Float, modifier: Modifier = Modifier, breathing: Boolean = true) {
    val animated by animateFloatAsState(value.coerceIn(0f, 1f), tween(220), label = "情绪云变化")
    val phase = if (breathing) {
        val motion = rememberInfiniteTransition(label = "云朵呼吸")
        val p by motion.animateFloat(
            initialValue = 0f,
            targetValue = 6.283185f,
            animationSpec = infiniteRepeatable(tween(4400, easing = LinearEasing), RepeatMode.Restart),
            label = "云朵光泽流动",
        )
        p
    } else 0f
    val base = moodColor(animated)
    Canvas(modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width * .44f, size.height * .48f)
        val glowCenter = Offset(center.x + sin(phase) * radius * .16f, center.y - radius * .12f)
        drawPath(
            cloudPath(Offset(center.x, center.y + radius * .055f), radius * 1.025f, animated, phase),
            base.copy(alpha = .10f),
        )
        for (layer in 0..3) {
            val scale = 1f - layer * .14f
            val path = cloudPath(center, radius * scale, animated, phase + layer * .17f)
            val light = lerp(base, Color.White, .52f + layer * .06f)
            drawPath(
                path,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = .76f - layer * .07f),
                        light.copy(alpha = .67f),
                        base.copy(alpha = .62f - layer * .06f),
                        Color.White.copy(alpha = .35f),
                    ),
                    start = Offset(center.x - radius, center.y - radius),
                    end = Offset(center.x + radius, center.y + radius),
                ),
            )
            drawPath(
                path,
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = .25f), Color.Transparent),
                    center = glowCenter,
                    radius = radius * scale * 1.25f,
                ),
            )
            drawPath(path, Color.White.copy(alpha = .82f - layer * .09f), style = Stroke(width = 2.2f - layer * .2f))
        }
        drawCircle(
            Brush.radialGradient(
                listOf(Color.White.copy(alpha = .82f), Color.White.copy(alpha = 0f)),
                center = glowCenter,
                radius = radius * .45f,
            ),
            radius = radius * .45f,
            center = glowCenter,
        )
        drawCircle(Color.White.copy(alpha = .92f), radius = radius * .022f, center = glowCenter)
    }
}
