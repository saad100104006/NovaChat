/*
 * Copyright 2018 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package im.vector.novaChat.fdroid.features.settings.troubleshoot

import androidx.fragment.app.Fragment
import im.vector.novaChat.R
import im.vector.novaChat.core.utils.isIgnoringBatteryOptimizations
import im.vector.novaChat.core.utils.requestDisablingBatteryOptimization
import im.vector.novaChat.features.settings.troubleshoot.NotificationTroubleshootTestManager
import im.vector.novaChat.features.settings.troubleshoot.TroubleshootTest

// Not used anymore
class TestBatteryOptimization(val fragment: Fragment) : TroubleshootTest(R.string.settings_troubleshoot_test_battery_title) {

    override fun perform() {
        if (fragment.context != null && isIgnoringBatteryOptimizations(fragment.context!!)) {
            description = fragment.getString(R.string.settings_troubleshoot_test_battery_success)
            status = TestStatus.SUCCESS
            quickFix = null
        } else {
            description = fragment.getString(R.string.settings_troubleshoot_test_battery_failed)
            quickFix = object : TroubleshootQuickFix(R.string.settings_troubleshoot_test_battery_quickfix) {
                override fun doFix() {
                    fragment.activity?.let {
                        requestDisablingBatteryOptimization(it, fragment, NotificationTroubleshootTestManager.REQ_CODE_FIX)
                    }
                }
            }
            status = TestStatus.FAILED
        }
    }
}
