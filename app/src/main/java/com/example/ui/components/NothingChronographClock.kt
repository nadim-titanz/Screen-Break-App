package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NothingBlack
import com.example.ui.theme.NothingOutline
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingRedGlow
import com.example.ui.theme.NothingTextSecondary
import com.example.ui.theme.NothingWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Nothing OS inspired Chronograph Clock dial.
 * Renders an OLED dark gauge disc with 60 precision radial tick marks,
 * a luminous crimson red active sweep sector, and clean display typography.
 */
@Composable
fun NothingChronographClock(
    progress: Float, // 0f to 1f
    timeText: String, // e.g. "00:30"
    unitText: String = "min", // e.g. "min", "sec"
    modifier: Modifier = Modifier,
    dialSize: Dp = 260.dp,
    isActive: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350),
        label = "chronograph_sweep"
    )

    Box(
        modifier = modifier.size(dialSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(dialSize)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) - 6.dp.toPx()
            val outerStrokeWidth = 1.5.dp.toPx()

            // 1. Dark Gauge Disc Background with subtle depth
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF14161B),
                        Color(0xFF0D0F13),
                        NothingBlack
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // 2. Subtle Outer Bezel Rim
            drawCircle(
                color = Color(0xFF232730),
                radius = radius,
                center = center,
                style = Stroke(width = outerStrokeWidth)
            )

            // 3. Active Luminous Red Sector / Wedge (From 12 o'clock / -90 deg clockwise)
            val sweepDegrees = animatedProgress * 360f
            if (isActive && sweepDegrees > 0.5f) {
                // Red glowing wedge
                val sectorPath = Path().apply {
                    moveTo(center.x, center.y)
                    arcTo(
                        rect = Rect(
                            left = center.x - radius,
                            top = center.y - radius,
                            right = center.x + radius,
                            bottom = center.y + radius
                        ),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = sweepDegrees,
                        forceMoveTo = false
                    )
                    close()
                }

                // Fill glowing red wedge with radial gradient fading towards center
                drawPath(
                    path = sectorPath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NothingRed.copy(alpha = 0.04f),
                            NothingRed.copy(alpha = 0.22f),
                            NothingRedGlow.copy(alpha = 0.45f)
                        ),
                        center = center,
                        radius = radius
                    )
                )

                // Intense perimeter arc accent line on the rim
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to NothingRed,
                        (animatedProgress.coerceIn(0.01f, 1f)) to NothingRedGlow,
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepDegrees,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 4. 60 Radial Chronograph Tick Marks
            val tickCount = 60
            val outerTickRadius = radius - 4.dp.toPx()
            val majorTickLength = 12.dp.toPx()
            val minorTickLength = 7.dp.toPx()

            for (i in 0 until tickCount) {
                val tickAngleDeg = (i * 6f) - 90f
                val tickAngleRad = tickAngleDeg * (PI / 180f)
                val isMajor = (i % 5 == 0)

                val tickLength = if (isMajor) majorTickLength else minorTickLength
                val innerTickRadius = outerTickRadius - tickLength

                val startX = center.x + (innerTickRadius * cos(tickAngleRad)).toFloat()
                val startY = center.y + (innerTickRadius * sin(tickAngleRad)).toFloat()
                val endX = center.x + (outerTickRadius * cos(tickAngleRad)).toFloat()
                val endY = center.y + (outerTickRadius * sin(tickAngleRad)).toFloat()

                // Check if this tick falls within the active red sweep
                // Normalized angle relative to -90° (0 to 360°)
                val normalizedTickAngle = (i * 6f)
                val isTickInActiveSweep = isActive && (normalizedTickAngle <= sweepDegrees + 0.1f)

                val tickColor = when {
                    isTickInActiveSweep -> if (isMajor) NothingRedGlow else NothingRed.copy(alpha = 0.9f)
                    isMajor -> Color(0xFF383E4C)
                    else -> Color(0xFF222630)
                }

                val strokeWidth = if (isTickInActiveSweep && isMajor) {
                    2.dp.toPx()
                } else if (isMajor) {
                    1.6.dp.toPx()
                } else {
                    1.1.dp.toPx()
                }

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        // 5. Center Time Readout & Unit
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeText,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light,
                    fontSize = if (dialSize >= 240.dp) 44.sp else 34.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = NothingWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = unitText,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Normal,
                    fontSize = if (dialSize >= 240.dp) 14.sp else 12.sp,
                    letterSpacing = 1.sp
                ),
                color = NothingTextSecondary
            )
        }
    }
}
