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
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import im.vector.novaChat.R
import im.vector.novaChat.core.utils.DebouncedClickListener
import im.vector.novaChat.features.home.AvatarRenderer
import im.vector.novaChat.features.home.room.detail.timeline.TimelineEventController

@EpoxyModelClass(layout = R.layout.item_timeline_event_base_noinfo)
abstract class DefaultItem : BaseEventItem<DefaultItem.Holder>() {

    @EpoxyAttribute
    lateinit var informationData: MessageInformationData
    @EpoxyAttribute
    lateinit var avatarRenderer: AvatarRenderer
    @EpoxyAttribute
    var baseCallback: TimelineEventController.BaseCallback? = null

    private var longClickListener = View.OnLongClickListener {
        return@OnLongClickListener baseCallback?.onEventLongClicked(informationData, null, it) == true
    }

    @EpoxyAttribute
    var readReceiptsCallback: TimelineEventController.ReadReceiptsCallback? = null

    private val _readReceiptsClickListener = DebouncedClickListener(View.OnClickListener {
        readReceiptsCallback?.onReadReceiptsClicked(informationData.readReceipts)
    })

    @EpoxyAttribute
    var text: CharSequence? = null

    override fun bind(holder: Holder) {
        holder.messageView.text = text
        holder.view.setOnLongClickListener(longClickListener)
        holder.readReceiptsView.render(informationData.readReceipts, avatarRenderer, _readReceiptsClickListener)
    }

    override fun getEventIds(): List<String> {
        return listOf(informationData.eventId)
    }

    override fun getViewType() = STUB_ID

    class Holder : BaseHolder(STUB_ID) {
        val messageView by bind<TextView>(R.id.stateMessageView)
    }

    companion object {
        private const val STUB_ID = R.id.messageContentDefaultStub
    }
}