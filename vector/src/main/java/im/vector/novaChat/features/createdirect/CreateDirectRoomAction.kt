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

package im.vector.novaChat.features.createdirect

import im.vector.matrix.android.api.session.user.model.User
import im.vector.novaChat.core.platform.VectorViewModelAction

sealed class CreateDirectRoomAction : VectorViewModelAction {
    object CreateRoomAndInviteSelectedUsers : CreateDirectRoomAction()
    data class FilterKnownUsers(val value: String) : CreateDirectRoomAction()
    data class SearchDirectoryUsers(val value: String) : CreateDirectRoomAction()
    object ClearFilterKnownUsers : CreateDirectRoomAction()
    data class SelectUser(val user: User) : CreateDirectRoomAction()
    data class RemoveSelectedUser(val user: User) : CreateDirectRoomAction()
}
