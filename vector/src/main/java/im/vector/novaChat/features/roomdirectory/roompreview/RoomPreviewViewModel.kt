/*
 * Copyright 2019 New Vector Ltd
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

package im.vector.novaChat.features.roomdirectory.roompreview

import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import im.vector.matrix.android.api.MatrixCallback
import im.vector.matrix.android.api.session.Session
import im.vector.matrix.android.api.session.room.model.Membership
import im.vector.matrix.android.api.session.room.roomSummaryQueryParams
import im.vector.matrix.rx.rx
import im.vector.novaChat.core.platform.VectorViewModel
import im.vector.novaChat.features.roomdirectory.JoinState
import timber.log.Timber

class RoomPreviewViewModel @AssistedInject constructor(@Assisted initialState: RoomPreviewViewState,
                                                       private val session: Session)
    : VectorViewModel<RoomPreviewViewState, RoomPreviewAction>(initialState) {

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: RoomPreviewViewState): RoomPreviewViewModel
    }

    companion object : MvRxViewModelFactory<RoomPreviewViewModel, RoomPreviewViewState> {

        @JvmStatic
        override fun create(viewModelContext: ViewModelContext, state: RoomPreviewViewState): RoomPreviewViewModel? {
            val fragment: RoomPreviewNoPreviewFragment = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.roomPreviewViewModelFactory.create(state)
        }
    }

    init {
        // Observe joined room (from the sync)
        observeJoinedRooms()
    }

    private fun observeJoinedRooms() {
        val queryParams = roomSummaryQueryParams {
            memberships = listOf(Membership.JOIN)
        }
        session
                .rx()
                .liveRoomSummaries(queryParams)
                .subscribe { list ->
                    withState { state ->
                        val isRoomJoined = list
                                ?.map { it.roomId }
                                ?.toList()
                                ?.contains(state.roomId) == true

                        if (isRoomJoined) {
                            setState {
                                copy(
                                        roomJoinState = JoinState.JOINED
                                )
                            }
                        }
                    }
                }
                .disposeOnClear()
    }

    override fun handle(action: RoomPreviewAction) {
        when (action) {
            RoomPreviewAction.Join -> joinRoom()
        }
    }

    private fun joinRoom() = withState { state ->
        if (state.roomJoinState == JoinState.JOINING) {
            // Request already sent, should not happen
            Timber.w("Try to join an already joining room. Should not happen")
            return@withState
        }

        setState {
            copy(
                    roomJoinState = JoinState.JOINING,
                    lastError = null
            )
        }

        session.joinRoom(state.roomId, callback = object : MatrixCallback<Unit> {
            override fun onSuccess(data: Unit) {
                // We do not update the joiningRoomsIds here, because, the room is not joined yet regarding the sync data.
                // Instead, we wait for the room to be joined
            }

            override fun onFailure(failure: Throwable) {
                setState {
                    copy(
                            roomJoinState = JoinState.JOINING_ERROR,
                            lastError = failure
                    )
                }
            }
        })
    }
}
