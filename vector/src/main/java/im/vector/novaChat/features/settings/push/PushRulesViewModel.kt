/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package im.vector.novaChat.features.settings.push

import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import im.vector.matrix.android.api.pushrules.rest.PushRule
import im.vector.novaChat.core.di.HasScreenInjector
import im.vector.novaChat.core.platform.EmptyAction
import im.vector.novaChat.core.platform.VectorViewModel

data class PushRulesViewState(
        val rules: List<PushRule> = emptyList()
) : MvRxState

class PushRulesViewModel(initialState: PushRulesViewState)
    : VectorViewModel<PushRulesViewState, EmptyAction>(initialState) {

    companion object : MvRxViewModelFactory<PushRulesViewModel, PushRulesViewState> {

        override fun initialState(viewModelContext: ViewModelContext): PushRulesViewState? {
            val session = (viewModelContext.activity as HasScreenInjector).injector().activeSessionHolder().getActiveSession()
            val rules = session.getPushRules()
            return PushRulesViewState(rules)
        }
    }

    override fun handle(action: EmptyAction) {
        // No op
    }
}
