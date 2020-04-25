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

import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.amulyakhare.textdrawable.TextDrawable
import im.vector.matrix.android.api.crypto.RoomEncryptionTrustLevel
import im.vector.matrix.android.api.util.MatrixItem
import im.vector.novaChat.R
import im.vector.novaChat.core.epoxy.VectorEpoxyHolder
import im.vector.novaChat.core.epoxy.VectorEpoxyModel
import im.vector.novaChat.core.extensions.setTextOrHide
import im.vector.novaChat.features.crypto.util.toImageRes
import im.vector.novaChat.features.home.AvatarRenderer

@EpoxyModelClass(layout = R.layout.item_room)
abstract class RoomSummaryItem : VectorEpoxyModel<RoomSummaryItem.Holder>() {

    @EpoxyAttribute lateinit var avatarRenderer: AvatarRenderer
    @EpoxyAttribute lateinit var matrixItem: MatrixItem
    @EpoxyAttribute lateinit var lastFormattedEvent: CharSequence
    @EpoxyAttribute lateinit var lastEventTime: CharSequence
    @EpoxyAttribute var typingString: CharSequence? = null
    @EpoxyAttribute var encryptionTrustLevel: RoomEncryptionTrustLevel? = null
    @EpoxyAttribute var unreadNotificationCount: Int = 0
    @EpoxyAttribute var hasUnreadMessage: Boolean = false
    @EpoxyAttribute var hasDraft: Boolean = false
    @EpoxyAttribute var showHighlighted: Boolean = false
    @EpoxyAttribute var itemLongClickListener: View.OnLongClickListener? = null
    @EpoxyAttribute var itemClickListener: View.OnClickListener? = null
    @EpoxyAttribute var showSelected: Boolean = false

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.rootView.setOnClickListener(itemClickListener)
        holder.rootView.setOnLongClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            itemLongClickListener?.onLongClick(it) ?: false
        }
        holder.titleView.text = matrixItem.getBestName()
        holder.lastEventTimeView.text = lastEventTime
        holder.lastEventView.text = lastFormattedEvent
        holder.typingView.setTextOrHide(typingString)
        holder.lastEventView.isInvisible = holder.typingView.isVisible
        holder.unreadCounterBadgeView.render(UnreadCounterBadgeView.State(unreadNotificationCount, showHighlighted))
        holder.unreadIndentIndicator.isVisible = hasUnreadMessage
        holder.draftView.isVisible = hasDraft
        avatarRenderer.render(matrixItem, holder.avatarImageView)
        holder.roomAvatarDecorationImageView.isVisible = encryptionTrustLevel != null
        holder.roomAvatarDecorationImageView.setImageResource(encryptionTrustLevel.toImageRes())
        renderSelection(holder, showSelected)
    }

    private fun renderSelection(holder: Holder, isSelected: Boolean) {
        if (isSelected) {
            holder.avatarCheckedImageView.visibility = View.VISIBLE
            val backgroundColor = ContextCompat.getColor(holder.view.context, R.color.riotx_accent)
            val backgroundDrawable = TextDrawable.builder().buildRound("", backgroundColor)
            holder.avatarImageView.setImageDrawable(backgroundDrawable)
        } else {
            holder.avatarCheckedImageView.visibility = View.GONE
            avatarRenderer.render(matrixItem, holder.avatarImageView)
        }
    }

    class Holder : VectorEpoxyHolder() {
        val titleView by bind<TextView>(R.id.roomNameView)
        val unreadCounterBadgeView by bind<UnreadCounterBadgeView>(R.id.roomUnreadCounterBadgeView)
        val unreadIndentIndicator by bind<View>(R.id.roomUnreadIndicator)
        val lastEventView by bind<TextView>(R.id.roomLastEventView)
        val typingView by bind<TextView>(R.id.roomTypingView)
        val draftView by bind<ImageView>(R.id.roomDraftBadge)
        val lastEventTimeView by bind<TextView>(R.id.roomLastEventTimeView)
        val avatarCheckedImageView by bind<ImageView>(R.id.roomAvatarCheckedImageView)
        val avatarImageView by bind<ImageView>(R.id.roomAvatarImageView)
        val roomAvatarDecorationImageView by bind<ImageView>(R.id.roomAvatarDecorationImageView)
        val rootView by bind<ViewGroup>(R.id.itemRoomLayout)
    }
}
