package info.hannes.liveedgedetection.demo

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import info.hannes.logcat.BothLogActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogSmokeTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(BothLogActivity::class.java)

    @Test
    fun smokeTestSimplyStart() {
    }
}
