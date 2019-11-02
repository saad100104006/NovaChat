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

package im.vector.novaChat.features.home.room.detail.timeline.item

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import im.vector.novaChat.R
import im.vector.novaChat.core.ui.views.ReadMarkerView
import im.vector.novaChat.core.utils.DebouncedClickListener
import im.vector.novaChat.features.home.AvatarRenderer
import im.vector.novaChat.features.home.room.detail.timeline.TimelineEventController

@EpoxyModelClass(layout = R.layout.item_timeline_event_base_noinfo)
abstract class NoticeItem : BaseEventItem<NoticeItem.Holder>() {

    @EpoxyAttribute
    lateinit var attributes: Attributes

    private val _readReceiptsClickListener = DebouncedClickListener(View.OnClickListener {
        attributes.readReceiptsCallback?.onReadReceiptsClicked(attributes.informationData.readReceipts)
    })

    private val _readMarkerCallback = object : ReadMarkerView.Callback {

        override fun onReadMarkerLongBound(isDisplayed: Boolean) {
            attributes.readReceiptsCallback?.onReadMarkerLongBound(attributes.informationData.eventId, isDisplayed)
        }
    }

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.noticeTextView.text = attributes.noticeText
        attributes.avatarRenderer.render(
                attributes.informationData.avatarUrl,
                attributes.informationData.senderId,
                attributes.informationData.memberName?.toString()
                        ?: attributes.informationData.senderId,
                holder.avatarImageView
        )
        holder.view.setOnLongClickListener(attributes.itemLongClickListener)
        holder.readReceiptsView.render(attributes.informationData.readReceipts, attributes.avatarRenderer, _readReceiptsClickListener)
        holder.readMarkerView.bindView(
                attributes.informationData.eventId,
                attributes.informationData.hasReadMarker,
                attributes.informationData.displayReadMarker,
                _readMarkerCallback
        )
    }

    override fun unbind(holder: Holder) {
        holder.readMarkerView.unbind()
        super.unbind(holder)
    }


    override fun getEventIds(): List<String> {
        return listOf(attributes.informationData.eventId)
    }

    override fun getViewType() = STUB_ID

    class Holder : BaseHolder(STUB_ID) {
        val avatarImageView by bind<ImageView>(R.id.itemNoticeAvatarView)
        val noticeTextView by bind<TextView>(R.id.itemNoticeTextView)
    }

    data class Attributes(
            val avatarRenderer: AvatarRenderer,
            val informationData: MessageInformationData,
            val noticeText: CharSequence,
            val itemLongClickListener: View.OnLongClickListener? = null,
            val readReceiptsCallback: TimelineEventController.ReadReceiptsCallback? = null
    )

    companion object {
        private const val STUB_ID = R.id.messageContentNoticeStub
    }
}