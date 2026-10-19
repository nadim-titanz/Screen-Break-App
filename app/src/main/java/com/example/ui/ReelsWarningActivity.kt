package com.example.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NothingBlack
import com.example.ui.theme.NothingOutline
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingSurface
import com.example.ui.theme.NothingTextSecondary
import com.example.ui.theme.NothingWhite

class ReelsWarningActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SOURCE_APP = "extra_source_app"

        fun showWarning(context: Context, sourceApp: String) {
            val intent = Intent(context, ReelsWarningActivity::class.java).apply {
                putExtra(EXTRA_SOURCE_APP, sourceApp)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val sourceApp = intent.getStringExtra(EXTRA_SOURCE_APP) ?: "Short-form video"

        setContent {
            MyApplicationTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            // Non-dismissible by clicking outside
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = NothingSurface),
                        border = BorderStroke(1.5.dp, NothingRed),
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Pulsing / Red Detox Header
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(NothingRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = NothingRed,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "⚡ REELS BLOCKED // DETOX ACTIVE",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = NothingRed
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "STREAM INTERCEPTED",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = NothingWhite
                                )
                            }

                            Text(
                                text = "Dopamine-loop short-form content was detected in $sourceApp and blocked to protect your continuous focus.",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                ),
                                color = NothingTextSecondary,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { finish() },
                                    shape = RoundedCornerShape(999.dp),
                                    border = BorderStroke(1.dp, NothingOutline),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = NothingWhite
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Text(
                                        text = "DISMISS",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }

                                Button(
                                    onClick = {
                                        // Go to Android home screen
                                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                            addCategory(Intent.CATEGORY_HOME)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        startActivity(homeIntent)
                                        finish()
                                    },
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NothingRed,
                                        contentColor = NothingWhite
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "HOME",
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
        }
    }
}
