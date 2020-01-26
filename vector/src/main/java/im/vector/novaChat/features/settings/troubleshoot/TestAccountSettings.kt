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
package im.vector.novaChat.features.settings.troubleshoot

import im.vector.matrix.android.api.MatrixCallback
import im.vector.matrix.android.api.pushrules.RuleIds
import im.vector.matrix.android.api.pushrules.RuleKind
import im.vector.novaChat.R
import im.vector.novaChat.core.di.ActiveSessionHolder
import im.vector.novaChat.core.resources.StringProvider
import javax.inject.Inject

/**
 * Check that the main pushRule (RULE_ID_DISABLE_ALL) is correctly setup
 */
class TestAccountSettings @Inject constructor(private val stringProvider: StringProvider,
                                              private val activeSessionHolder: ActiveSessionHolder)
    : TroubleshootTest(R.string.settings_troubleshoot_test_account_settings_title) {

    override fun perform() {
        val session = activeSessionHolder.getSafeActiveSession() ?: return
        val defaultRule = session.getPushRules()
                .find { it.ruleId == RuleIds.RULE_ID_DISABLE_ALL }

        if (defaultRule != null) {
            if (!defaultRule.enabled) {
                description = stringProvider.getString(R.string.settings_troubleshoot_test_account_settings_success)
                quickFix = null
                status = TestStatus.SUCCESS
            } else {
                description = stringProvider.getString(R.string.settings_troubleshoot_test_account_settings_failed)
                quickFix = object : TroubleshootQuickFix(R.string.settings_troubleshoot_test_account_settings_quickfix) {
                    override fun doFix() {
                        if (manager?.diagStatus == TestStatus.RUNNING) return // wait before all is finished

                        session.updatePushRuleEnableStatus(RuleKind.OVERRIDE, defaultRule, !defaultRule.enabled,
                                                           object : MatrixCallback<Unit> {
                                                               override fun onSuccess(data: Unit) {
                                                                   manager?.retry()
                                                               }

                                                               override fun onFailure(failure: Throwable) {
                                                                   manager?.retry()
                                                               }
                                                           })
                    }
                }
                status = TestStatus.FAILED
            }
        } else {
            // should not happen?
            status = TestStatus.FAILED
        }
    }
}
