package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Interrupt", appName)
  }

  @Test
  fun `usage state progress calculates properly for 1min and 2min intervals`() {
    val state1Min = com.example.model.UsageState(
      continuousSeconds = 30L,
      intervalMinutes = 1
    )
    // 30 seconds into a 1 minute (60s) interval = 50%
    assertEquals(0.5f, state1Min.progress, 0.01f)
    assertEquals("01:00", state1Min.formattedTarget)

    val state2Min = com.example.model.UsageState(
      continuousSeconds = 60L,
      intervalMinutes = 2
    )
    // 60 seconds into a 2 minute (120s) interval = 50%
    assertEquals(0.5f, state2Min.progress, 0.01f)
    assertEquals("02:00", state2Min.formattedTarget)
  }
}
