package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.NothingChronographClock
import com.example.ui.theme.NothingBlack
import com.example.ui.theme.NothingOutline
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingSurface
import com.example.ui.theme.NothingTextDisabled
import com.example.ui.theme.NothingTextPrimary
import com.example.ui.theme.NothingTextSecondary
import com.example.ui.theme.NothingWhite
import kotlinx.coroutines.delay

/**
 * Confirmation dialog shown before turning off continuous usage interval monitoring.
 * Features the Nothing OS Chronograph Clock dial with 60 precision ticks and luminous red sweep.
 * Enforces a mandatory 30-second confirmation waiting time.
 */
@Composable
fun TurnOffIntervalConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    initialSeconds: Int = 30
) {
    var secondsRemaining by remember { mutableIntStateOf(initialSeconds) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        }
    }

    val isReady = secondsRemaining == 0
    // Progress for the dial: starts at remaining fraction (or 1f down to 0f)
    val progress = if (initialSeconds > 0) {
        secondsRemaining.toFloat() / initialSeconds.toFloat()
    } else {
        0f
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NothingSurface),
            border = BorderStroke(1.dp, NothingOutline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("turn_off_interval_confirm_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Technical Monospace Header Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "// SAFETY INTERLOCK",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        ),
                        color = NothingRed
                    )
                    Text(
                        text = if (isReady) "[UNLOCKED]" else "[LOCKED]",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        ),
                        color = if (isReady) NothingWhite else NothingTextSecondary
                    )
                }

                // Title & Subtitle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "PAUSE INTERVAL",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = NothingWhite,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "A mandatory 30s delay prevents impulsive disruption of your focus habit.",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = NothingTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                // Chronograph Clock Dial (Matching the user's reference image)
                Box(
                    modifier = Modifier.padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    NothingChronographClock(
                        progress = if (isReady) 0f else progress,
                        timeText = "00:%02d".format(secondsRemaining),
                        unitText = "sec",
                        dialSize = 220.dp,
                        isActive = !isReady
                    )
                }

                // Status text tag for automated test compatibility
                Text(
                    text = "${secondsRemaining}s",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isReady) NothingWhite else NothingTextSecondary,
                    modifier = Modifier.testTag("countdown_seconds_text")
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Cancel / Keep Active Button
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(999.dp), // Pill button (Nothing spec)
                        border = BorderStroke(1.dp, NothingOutline),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cancel_turn_off_interval_button")
                    ) {
                        Text(
                            text = "KEEP ACTIVE",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            ),
                            color = NothingWhite
                        )
                    }

                    // Confirm Turn Off Button (Disabled during 30s wait)
                    Button(
                        onClick = onConfirm,
                        enabled = isReady,
                        shape = RoundedCornerShape(999.dp), // Pill button (Nothing spec)
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NothingRed,
                            contentColor = NothingWhite,
                            disabledContainerColor = NothingOutline,
                            disabledContentColor = NothingTextDisabled
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("confirm_turn_off_interval_button")
                    ) {
                        Text(
                            text = if (isReady) "TURN OFF" else "WAIT ${secondsRemaining}S",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
