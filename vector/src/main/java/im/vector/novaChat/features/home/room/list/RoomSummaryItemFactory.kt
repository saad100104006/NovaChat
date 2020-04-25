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

package im.vector.novaChat.features.home.room.list

import android.view.View
import im.vector.matrix.android.api.session.Session
import im.vector.matrix.android.api.session.room.model.Membership
import im.vector.matrix.android.api.session.room.model.RoomSummary
import im.vector.matrix.android.api.util.toMatrixItem
import im.vector.novaChat.R
import im.vector.novaChat.core.date.VectorDateFormatter
import im.vector.novaChat.core.epoxy.VectorEpoxyModel
import im.vector.novaChat.core.extensions.localDateTime
import im.vector.novaChat.core.resources.DateProvider
import im.vector.novaChat.core.resources.StringProvider
import im.vector.novaChat.core.utils.DebouncedClickListener
import im.vector.novaChat.features.home.AvatarRenderer
import im.vector.novaChat.features.home.room.detail.timeline.format.DisplayableEventFormatter
import im.vector.novaChat.features.home.room.typing.TypingHelper
import javax.inject.Inject

class RoomSummaryItemFactory @Inject constructor(private val displayableEventFormatter: DisplayableEventFormatter,
                                                 private val dateFormatter: VectorDateFormatter,
                                                 private val stringProvider: StringProvider,
                                                 private val typingHelper: TypingHelper,
                                                 private val session: Session,
                                                 private val avatarRenderer: AvatarRenderer) {

    fun create(roomSummary: RoomSummary,
               joiningRoomsIds: Set<String>,
               joiningErrorRoomsIds: Set<String>,
               rejectingRoomsIds: Set<String>,
               rejectingErrorRoomsIds: Set<String>,
               selectedRoomIds: Set<String>,
               listener: RoomSummaryController.Listener?): VectorEpoxyModel<*> {
        return when (roomSummary.membership) {
            Membership.INVITE -> createInvitationItem(roomSummary, joiningRoomsIds, joiningErrorRoomsIds, rejectingRoomsIds, rejectingErrorRoomsIds, listener)
            else              -> createRoomItem(roomSummary, selectedRoomIds, listener?.let { it::onRoomClicked }, listener?.let { it::onRoomLongClicked })
        }
    }

    fun createInvitationItem(roomSummary: RoomSummary,
                             joiningRoomsIds: Set<String>,
                             joiningErrorRoomsIds: Set<String>,
                             rejectingRoomsIds: Set<String>,
                             rejectingErrorRoomsIds: Set<String>,
                             listener: RoomSummaryController.Listener?): VectorEpoxyModel<*> {
        val secondLine = if (roomSummary.isDirect) {
            roomSummary.inviterId
        } else {
            roomSummary.inviterId?.let {
                stringProvider.getString(R.string.invited_by, it)
            }
        }

        return RoomInvitationItem_()
                .id(roomSummary.roomId)
                .avatarRenderer(avatarRenderer)
                .matrixItem(roomSummary.toMatrixItem())
                .secondLine(secondLine)
                .invitationAcceptInProgress(joiningRoomsIds.contains(roomSummary.roomId))
                .invitationAcceptInError(joiningErrorRoomsIds.contains(roomSummary.roomId))
                .invitationRejectInProgress(rejectingRoomsIds.contains(roomSummary.roomId))
                .invitationRejectInError(rejectingErrorRoomsIds.contains(roomSummary.roomId))
                .acceptListener { listener?.onAcceptRoomInvitation(roomSummary) }
                .rejectListener { listener?.onRejectRoomInvitation(roomSummary) }
                .listener { listener?.onRoomClicked(roomSummary) }
    }

    fun createRoomItem(
            roomSummary: RoomSummary,
            selectedRoomIds: Set<String>,
            onClick: ((RoomSummary) -> Unit)?,
            onLongClick: ((RoomSummary) -> Boolean)?
    ): VectorEpoxyModel<*> {
        val unreadCount = roomSummary.notificationCount
        val showHighlighted = roomSummary.highlightCount > 0
        val showSelected = selectedRoomIds.contains(roomSummary.roomId)
        var latestFormattedEvent: CharSequence = ""
        var latestEventTime: CharSequence = ""
        val latestEvent = roomSummary.latestPreviewableEvent
        if (latestEvent != null) {
            val date = latestEvent.root.localDateTime()
            val currentDate = DateProvider.currentLocalDateTime()
            val isSameDay = date.toLocalDate() == currentDate.toLocalDate()
            latestFormattedEvent = displayableEventFormatter.format(latestEvent, roomSummary.isDirect.not())
            latestEventTime = if (isSameDay) {
                dateFormatter.formatMessageHour(date)
            } else {
                dateFormatter.formatMessageDay(date)
            }
        }

        val typingString = typingHelper.excludeCurrentUser(roomSummary.typingRoomMemberIds)
                .takeIf { it.isNotEmpty() }
                ?.let { typingMembers ->
                    // It's not ideal to get a Room and to fetch data from DB here, but let's keep it like this for the moment
                    val room = session.getRoom(roomSummary.roomId)
                    val typingRoomMembers = typingHelper.toTypingRoomMembers(typingMembers, room)
                    typingHelper.toTypingMessage(typingRoomMembers)
                }

        return RoomSummaryItem_()
                .id(roomSummary.roomId)
                .avatarRenderer(avatarRenderer)
                .encryptionTrustLevel(roomSummary.roomEncryptionTrustLevel)
                .matrixItem(roomSummary.toMatrixItem())
                .lastEventTime(latestEventTime)
                .typingString(typingString)
                .lastFormattedEvent(latestFormattedEvent)
                .showHighlighted(showHighlighted)
                .showSelected(showSelected)
                .unreadNotificationCount(unreadCount)
                .hasUnreadMessage(roomSummary.hasUnreadMessages)
                .hasDraft(roomSummary.userDrafts.isNotEmpty())
                .itemLongClickListener { _ ->
                    onLongClick?.invoke(roomSummary) ?: false
                }
                .itemClickListener(
                        DebouncedClickListener(View.OnClickListener { _ ->
                            onClick?.invoke(roomSummary)
                        })
                )
    }
}
