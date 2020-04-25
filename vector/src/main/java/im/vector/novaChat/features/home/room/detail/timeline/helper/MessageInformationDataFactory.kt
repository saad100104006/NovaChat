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

package im.vector.novaChat.features.home.room.detail.timeline.helper

import im.vector.matrix.android.api.session.Session
import im.vector.matrix.android.api.session.events.model.EventType
import im.vector.matrix.android.api.session.events.model.toModel
import im.vector.matrix.android.api.session.room.model.ReferencesAggregatedContent
import im.vector.matrix.android.api.session.room.model.message.MessageVerificationRequestContent
import im.vector.matrix.android.api.session.room.timeline.TimelineEvent
import im.vector.matrix.android.api.session.room.timeline.getLastMessageContent
import im.vector.matrix.android.api.session.room.timeline.hasBeenEdited
import im.vector.matrix.android.internal.session.room.VerificationState
import im.vector.novaChat.core.date.VectorDateFormatter
import im.vector.novaChat.core.extensions.localDateTime
import im.vector.novaChat.core.resources.ColorProvider
import im.vector.novaChat.core.utils.getColorFromUserId
import im.vector.novaChat.features.home.room.detail.timeline.item.MessageInformationData
import im.vector.novaChat.features.home.room.detail.timeline.item.PollResponseData
import im.vector.novaChat.features.home.room.detail.timeline.item.ReactionInfoData
import im.vector.novaChat.features.home.room.detail.timeline.item.ReadReceiptData
import im.vector.novaChat.features.home.room.detail.timeline.item.ReferencesInfoData
import me.gujun.android.span.span
import javax.inject.Inject

/**
 * TODO Update this comment
 * This class compute if data of an event (such has avatar, display name, ...) should be displayed, depending on the previous event in the timeline
 */
class MessageInformationDataFactory @Inject constructor(private val session: Session,
                                                        private val dateFormatter: VectorDateFormatter,
                                                        private val colorProvider: ColorProvider) {

    fun create(event: TimelineEvent, nextEvent: TimelineEvent?): MessageInformationData {
        // Non nullability has been tested before
        val eventId = event.root.eventId!!

        val date = event.root.localDateTime()
        val nextDate = nextEvent?.root?.localDateTime()
        val addDaySeparator = date.toLocalDate() != nextDate?.toLocalDate()
        val isNextMessageReceivedMoreThanOneHourAgo = nextDate?.isBefore(date.minusMinutes(60))
                ?: false

        val showInformation =
                addDaySeparator
                        || event.senderAvatar != nextEvent?.senderAvatar
                        || event.getDisambiguatedDisplayName() != nextEvent?.getDisambiguatedDisplayName()
                        || (nextEvent.root.getClearType() != EventType.MESSAGE && nextEvent.root.getClearType() != EventType.ENCRYPTED)
                        || isNextMessageReceivedMoreThanOneHourAgo
                        || isTileTypeMessage(nextEvent)

        val time = dateFormatter.formatMessageHour(date)
        val avatarUrl = event.senderAvatar
        val memberName = event.getDisambiguatedDisplayName()
        val formattedMemberName = span(memberName) {
            textColor = colorProvider.getColor(getColorFromUserId(event.root.senderId))
        }

        return MessageInformationData(
                eventId = eventId,
                senderId = event.root.senderId ?: "",
                sendState = event.root.sendState,
                time = time,
                ageLocalTS = event.root.ageLocalTs,
                avatarUrl = avatarUrl,
                memberName = formattedMemberName,
                showInformation = showInformation,
                orderedReactionList = event.annotations?.reactionsSummary
                        // ?.filter { isSingleEmoji(it.key) }
                        ?.map {
                            ReactionInfoData(it.key, it.count, it.addedByMe, it.localEchoEvents.isEmpty())
                        },
                pollResponseAggregatedSummary = event.annotations?.pollResponseSummary?.let {
                    PollResponseData(
                            myVote = it.aggregatedContent?.myVote,
                            isClosed = it.closedTime ?: Long.MAX_VALUE > System.currentTimeMillis(),
                            votes = it.aggregatedContent?.votes
                                    ?.groupBy({ it.optionIndex }, { it.userId })
                                    ?.mapValues { it.value.size }
                    )
                },
                hasBeenEdited = event.hasBeenEdited(),
                hasPendingEdits = event.annotations?.editSummary?.localEchos?.any() ?: false,
                readReceipts = event.readReceipts
                        .asSequence()
                        .filter {
                            it.user.userId != session.myUserId
                        }
                        .map {
                            ReadReceiptData(it.user.userId, it.user.avatarUrl, it.user.displayName, it.originServerTs)
                        }
                        .toList(),
                referencesInfoData = event.annotations?.referencesAggregatedSummary?.let { referencesAggregatedSummary ->
                    val verificationState = referencesAggregatedSummary.content.toModel<ReferencesAggregatedContent>()?.verificationState
                            ?: VerificationState.REQUEST
                    ReferencesInfoData(verificationState)
                },
                sentByMe = event.root.senderId == session.myUserId
        )
    }

    /**
     * Tiles type message never show the sender information (like verification request), so we should repeat it for next message
     * even if same sender
     */
    private fun isTileTypeMessage(event: TimelineEvent?): Boolean {
        return when (event?.root?.getClearType()) {
            EventType.KEY_VERIFICATION_DONE,
            EventType.KEY_VERIFICATION_CANCEL -> true
            EventType.MESSAGE                 -> {
                event.getLastMessageContent() is MessageVerificationRequestContent
            }
            else                              -> false
        }
    }
}
