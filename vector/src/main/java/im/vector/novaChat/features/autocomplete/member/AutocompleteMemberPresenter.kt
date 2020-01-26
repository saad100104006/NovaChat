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

package im.vector.novaChat.features.autocomplete.member

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.otaliastudios.autocomplete.RecyclerViewPresenter
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import im.vector.matrix.android.api.query.QueryStringValue
import im.vector.matrix.android.api.session.Session
import im.vector.matrix.android.api.session.room.members.roomMemberQueryParams
import im.vector.matrix.android.api.session.room.model.Membership
import im.vector.matrix.android.api.session.room.model.RoomMemberSummary
import im.vector.novaChat.features.autocomplete.AutocompleteClickListener

class AutocompleteMemberPresenter @AssistedInject constructor(context: Context,
                                                              @Assisted val roomId: String,
                                                              private val session: Session,
                                                              private val controller: AutocompleteMemberController
) : RecyclerViewPresenter<RoomMemberSummary>(context), AutocompleteClickListener<RoomMemberSummary> {

    private val room = session.getRoom(roomId)!!

    init {
        controller.listener = this
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(roomId: String): AutocompleteMemberPresenter
    }

    override fun instantiateAdapter(): RecyclerView.Adapter<*> {
        // Also remove animation
        recyclerView?.itemAnimator = null
        return controller.adapter
    }

    override fun onItemClick(t: RoomMemberSummary) {
        dispatchClick(t)
    }

    override fun onQuery(query: CharSequence?) {
        val queryParams = roomMemberQueryParams {
            displayName = if (query.isNullOrBlank()) {
                QueryStringValue.IsNotEmpty
            } else {
                QueryStringValue.Contains(query.toString(), QueryStringValue.Case.INSENSITIVE)
            }
            memberships = listOf(Membership.JOIN)
            excludeSelf = true
        }
        val members = room.getRoomMembers(queryParams)
                .asSequence()
                .sortedBy { it.displayName }
        controller.setData(members.toList())
    }
}
