/*
 * Copyright (c) 2020 New Vector Ltd
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

package im.vector.novaChat.features.home.room.detail.timeline.factory

import im.vector.matrix.android.api.session.events.model.toModel
import im.vector.matrix.android.api.session.room.timeline.TimelineEvent
import im.vector.matrix.android.internal.crypto.MXCRYPTO_ALGORITHM_MEGOLM
import im.vector.matrix.android.internal.crypto.model.event.EncryptionEventContent
import im.vector.novaChat.R
import im.vector.novaChat.core.resources.StringProvider
import im.vector.novaChat.features.home.room.detail.timeline.MessageColorProvider
import im.vector.novaChat.features.home.room.detail.timeline.TimelineEventController
import im.vector.novaChat.features.home.room.detail.timeline.helper.AvatarSizeProvider
import im.vector.novaChat.features.home.room.detail.timeline.helper.MessageInformationDataFactory
import im.vector.novaChat.features.home.room.detail.timeline.helper.MessageItemAttributesFactory
import im.vector.novaChat.features.home.room.detail.timeline.item.StatusTileTimelineItem
import im.vector.novaChat.features.home.room.detail.timeline.item.StatusTileTimelineItem_
import javax.inject.Inject

class EncryptionItemFactory @Inject constructor(
        private val messageItemAttributesFactory: MessageItemAttributesFactory,
        private val messageColorProvider: MessageColorProvider,
        private val stringProvider: StringProvider,
        private val informationDataFactory: MessageInformationDataFactory,
        private val avatarSizeProvider: AvatarSizeProvider) {

    fun create(event: TimelineEvent,
               highlight: Boolean,
               callback: TimelineEventController.Callback?): StatusTileTimelineItem? {
        val algorithm = event.root.getClearContent().toModel<EncryptionEventContent>()?.algorithm
        val informationData = informationDataFactory.create(event, null)
        val attributes = messageItemAttributesFactory.create(null, informationData, callback)

        val isSafeAlgorithm = algorithm == MXCRYPTO_ALGORITHM_MEGOLM
        val title: String
        val description: String
        val shield: StatusTileTimelineItem.ShieldUIState
        if (isSafeAlgorithm) {
            title = stringProvider.getString(R.string.encryption_enabled)
            description = stringProvider.getString(R.string.encryption_enabled_tile_description)
            shield = StatusTileTimelineItem.ShieldUIState.BLACK
        } else {
            title = stringProvider.getString(R.string.encryption_not_enabled)
            description = stringProvider.getString(R.string.encryption_unknown_algorithm_tile_description)
            shield = StatusTileTimelineItem.ShieldUIState.RED
        }
        return StatusTileTimelineItem_()
                .attributes(
                        StatusTileTimelineItem.Attributes(
                                title = title,
                                description = description,
                                shieldUIState = shield,
                                informationData = informationData,
                                avatarRenderer = attributes.avatarRenderer,
                                messageColorProvider = messageColorProvider,
                                emojiTypeFace = attributes.emojiTypeFace,
                                itemClickListener = attributes.itemClickListener,
                                itemLongClickListener = attributes.itemLongClickListener,
                                reactionPillCallback = attributes.reactionPillCallback,
                                readReceiptsCallback = attributes.readReceiptsCallback
                        )
                )
                .highlighted(highlight)
                .leftGuideline(avatarSizeProvider.leftGuideline)
    }
}
