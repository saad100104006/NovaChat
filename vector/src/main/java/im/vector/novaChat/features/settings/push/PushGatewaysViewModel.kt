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

import com.airbnb.mvrx.*
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import im.vector.matrix.android.api.session.Session
import im.vector.matrix.android.api.session.pushers.Pusher
import im.vector.matrix.rx.RxSession
import im.vector.novaChat.core.platform.EmptyAction
import im.vector.novaChat.core.platform.VectorViewModel

data class PushGatewayViewState(
        val pushGateways: Async<List<Pusher>> = Uninitialized
) : MvRxState

class PushGatewaysViewModel @AssistedInject constructor(@Assisted initialState: PushGatewayViewState,
                                                        private val session: Session)
    : VectorViewModel<PushGatewayViewState, EmptyAction>(initialState) {

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: PushGatewayViewState): PushGatewaysViewModel
    }

    companion object : MvRxViewModelFactory<PushGatewaysViewModel, PushGatewayViewState> {

        @JvmStatic
        override fun create(viewModelContext: ViewModelContext, state: PushGatewayViewState): PushGatewaysViewModel? {
            val fragment: PushGatewaysFragment = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.pushGatewaysViewModelFactory.create(state)
        }
    }

    init {
        observePushers()
    }

    private fun observePushers() {
        RxSession(session)
                .livePushers()
                .execute {
                    copy(pushGateways = it)
                }
    }

    override fun handle(action: EmptyAction) {
        // No op
    }
}
