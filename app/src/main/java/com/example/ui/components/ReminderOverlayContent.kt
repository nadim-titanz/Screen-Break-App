package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NothingBlack
import com.example.ui.theme.NothingOutline
import com.example.ui.theme.NothingOutlineVariant
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingSurface
import com.example.ui.theme.NothingSurfaceElevated
import com.example.ui.theme.NothingTextMuted
import com.example.ui.theme.NothingTextPrimary
import com.example.ui.theme.NothingTextSecondary
import com.example.ui.theme.NothingWhite

@Composable
fun ReminderOverlayContent(
    continuousMinutes: Int,
    activeAppName: String,
    activePackage: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = NothingSurface,
        border = BorderStroke(1.dp, NothingRed),
        tonalElevation = 0.dp,
        shadowElevation = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon Badge with Nothing Red
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(NothingRed.copy(alpha = 0.15f))
                    .border(BorderStroke(1.dp, NothingRed), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = "Interrupt Icon",
                    tint = NothingRed,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "// CONTINUOUS USAGE INTERRUPT",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                ),
                color = NothingRed
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "TAKE A BREAK",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = NothingWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Continuous screen time threshold of $continuousMinutes minutes reached.",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = NothingTextSecondary,
                textAlign = TextAlign.Center
            )

            if (activeAppName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NothingBlack)
                        .border(BorderStroke(1.dp, NothingOutline), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE // ",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        color = NothingTextSecondary
                    )
                    Text(
                        text = activeAppName,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = NothingWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mindful tip card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = NothingSurfaceElevated
                ),
                border = BorderStroke(1.dp, NothingOutline),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = NothingWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Look at an object 20 feet away for 20 seconds. Relax your shoulders and breathe.",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        ),
                        color = NothingTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Primary strict action
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("dismiss_break_button"),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NothingRed,
                    contentColor = NothingWhite
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "TAKE BREAK // EXIT TO HOME",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "DETOX PROTOCOL: Exits active application immediately",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = NothingTextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
