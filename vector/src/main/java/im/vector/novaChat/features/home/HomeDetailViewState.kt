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

package im.vector.novaChat.features.home

import arrow.core.Option
import com.airbnb.mvrx.MvRxState
import im.vector.matrix.android.api.session.group.model.GroupSummary
import im.vector.matrix.android.api.session.sync.SyncState
import im.vector.novaChat.features.home.room.list.RoomListFragment

data class HomeDetailViewState(
        val groupSummary: Option<GroupSummary> = Option.empty(),
        val displayMode: RoomListFragment.DisplayMode = RoomListFragment.DisplayMode.HOME,
        val notificationCountCatchup: Int = 0,
        val notificationHighlightCatchup: Boolean = false,
        val notificationCountPeople: Int = 0,
        val notificationHighlightPeople: Boolean = false,
        val notificationCountRooms: Int = 0,
        val notificationHighlightRooms: Boolean = false,
        val syncState: SyncState = SyncState.IDLE
) : MvRxState