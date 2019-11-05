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

import android.graphics.Typeface
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.constraintlayout.helper.widget.Flow
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyAttribute
import im.vector.matrix.android.api.session.room.send.SendState
import im.vector.novaChat.R
import im.vector.novaChat.core.resources.ColorProvider
import im.vector.novaChat.core.ui.views.ReadMarkerView
import im.vector.novaChat.core.utils.DebouncedClickListener
import im.vector.novaChat.features.home.AvatarRenderer
import im.vector.novaChat.features.home.room.detail.timeline.TimelineEventController
import im.vector.novaChat.features.reactions.widget.ReactionButton
import im.vector.novaChat.features.ui.getMessageTextColor

abstract class AbsMessageItem<H : AbsMessageItem.Holder> : BaseEventItem<H>() {

    @EpoxyAttribute
    lateinit var attributes: Attributes

    private val _avatarClickListener = DebouncedClickListener(View.OnClickListener {
        attributes.avatarCallback?.onAvatarClicked(attributes.informationData)
    })
    private val _memberNameClickListener = DebouncedClickListener(View.OnClickListener {
        attributes.avatarCallback?.onMemberNameClicked(attributes.informationData)
    })

    private val _readReceiptsClickListener = DebouncedClickListener(View.OnClickListener {
        attributes.readReceiptsCallback?.onReadReceiptsClicked(attributes.informationData.readReceipts)
    })

    private val _readMarkerCallback = object : ReadMarkerView.Callback {

        override fun onReadMarkerLongBound(isDisplayed: Boolean) {
            attributes.readReceiptsCallback?.onReadMarkerLongBound(attributes.informationData.eventId, isDisplayed)
        }
    }

    var reactionClickListener: ReactionButton.ReactedListener = object : ReactionButton.ReactedListener {
        override fun onReacted(reactionButton: ReactionButton) {
            attributes.reactionPillCallback?.onClickOnReactionPill(attributes.informationData, reactionButton.reactionString, true)
        }

        override fun onUnReacted(reactionButton: ReactionButton) {
            attributes.reactionPillCallback?.onClickOnReactionPill(attributes.informationData, reactionButton.reactionString, false)
        }

        override fun onLongClick(reactionButton: ReactionButton) {
            attributes.reactionPillCallback?.onLongClickOnReactionPill(attributes.informationData, reactionButton.reactionString)
        }
    }

    override fun bind(holder: H) {
        super.bind(holder)
        if (attributes.informationData.showInformation) {
            holder.avatarImageView.layoutParams = holder.avatarImageView.layoutParams?.apply {
                height = attributes.avatarSize
                width = attributes.avatarSize
            }
            holder.avatarImageView.visibility = View.GONE
            holder.avatarImageView.setOnClickListener(_avatarClickListener)

            holder.memberNameView.setOnClickListener(_memberNameClickListener)
            holder.timeView.visibility = View.VISIBLE
            holder.timeView.text = attributes.informationData.time
            holder.memberNameView.text = attributes.informationData.memberName
            attributes.avatarRenderer.render(
                    attributes.informationData.avatarUrl,
                    attributes.informationData.senderId,
                    attributes.informationData.memberName?.toString(),
                    holder.avatarImageView
            )

            holder.avatarImageView.setOnLongClickListener(attributes.itemLongClickListener)
            holder.memberNameView.setOnLongClickListener(attributes.itemLongClickListener)
        } else {
            holder.avatarImageView.setOnClickListener(null)
            holder.memberNameView.setOnClickListener(null)
            holder.avatarImageView.visibility = View.GONE
            holder.memberNameView.visibility = View.GONE
            holder.timeView.text = attributes.informationData.time
            holder.timeView.visibility = View.VISIBLE
            holder.avatarImageView.setOnLongClickListener(null)
            holder.memberNameView.setOnLongClickListener(null)
        }

       /* if(attributes.informationData.readReceipts.size<2){
            holder.memberNameView.visibility = View.GONE
        } else {
            holder.memberNameView.visibility = View.VISIBLE
        }
*/

        holder.view.setOnClickListener(attributes.itemClickListener)
        holder.view.setOnLongClickListener(attributes.itemLongClickListener)

        holder.readReceiptsView.render(
                attributes.informationData.readReceipts,
                attributes.avatarRenderer,
                _readReceiptsClickListener
        )
        holder.readMarkerView.bindView(
                attributes.informationData.eventId,
                attributes.informationData.hasReadMarker,
                attributes.informationData.displayReadMarker,
                _readMarkerCallback
        )

        if (!shouldShowReactionAtBottom() || attributes.informationData.orderedReactionList.isNullOrEmpty()) {
            holder.reactionWrapper?.isVisible = false

        } else {
            //inflate if needed
            if (holder.reactionFlowHelper == null) {
                holder.reactionWrapper = holder.view.findViewById<ViewStub>(R.id.messageBottomInfo).inflate() as? ViewGroup
                holder.reactionFlowHelper = holder.view.findViewById(R.id.reactionsFlowHelper)
            }
            holder.reactionWrapper?.isVisible = true
            //clear all reaction buttons (but not the Flow helper!)
            holder.reactionWrapper?.children?.forEach { (it as? ReactionButton)?.isGone = true }
            val idToRefInFlow = ArrayList<Int>()
            attributes.informationData.orderedReactionList?.chunked(8)?.firstOrNull()?.forEachIndexed { index, reaction ->
                (holder.reactionWrapper?.children?.elementAtOrNull(index) as? ReactionButton)?.let { reactionButton ->
                    reactionButton.isVisible = true
                    reactionButton.reactedListener = reactionClickListener
                    reactionButton.setTag(R.id.messageBottomInfo, reaction.key)
                    idToRefInFlow.add(reactionButton.id)
                    reactionButton.reactionString = reaction.key
                    reactionButton.reactionCount = reaction.count
                    reactionButton.setChecked(reaction.addedByMe)
                    reactionButton.isEnabled = reaction.synced
                }
            }
            // Just setting the view as gone will break the FlowHelper (and invisible will take too much space),
            // so have to update ref ids
            holder.reactionFlowHelper?.referencedIds = idToRefInFlow.toIntArray()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && !holder.view.isInLayout) {
                holder.reactionFlowHelper?.requestLayout()
            }
            holder.reactionWrapper?.setOnLongClickListener(attributes.itemLongClickListener)
        }
    }

    override fun unbind(holder: H) {
        holder.readMarkerView.unbind()
        super.unbind(holder)
    }

    open fun shouldShowReactionAtBottom(): Boolean {
        return true
    }

    override fun getEventIds(): List<String> {
        return listOf(attributes.informationData.eventId)
    }

    protected open fun renderSendState(root: View, textView: TextView?, failureIndicator: ImageView? = null) {
        root.isClickable = attributes.informationData.sendState.isSent()
        val state = if (attributes.informationData.hasPendingEdits) SendState.UNSENT else attributes.informationData.sendState
        textView?.setTextColor(attributes.colorProvider.getMessageTextColor(state))
        failureIndicator?.isVisible = attributes.informationData.sendState.hasFailed()
    }

    abstract class Holder(@IdRes stubId: Int) : BaseHolder(stubId) {
        val avatarImageView by bind<ImageView>(R.id.messageAvatarImageView)
        val memberNameView by bind<TextView>(R.id.messageMemberNameView)
        val timeView by bind<TextView>(R.id.messageTimeView)
        var reactionWrapper: ViewGroup? = null
        var reactionFlowHelper: Flow? = null
    }

    /**
     * This class holds all the common attributes for timeline items.
     */
    data class Attributes(
            val avatarSize: Int,
            val informationData: MessageInformationData,
            val avatarRenderer: AvatarRenderer,
            val colorProvider: ColorProvider,
            val itemLongClickListener: View.OnLongClickListener? = null,
            val itemClickListener: View.OnClickListener? = null,
            val memberClickListener: View.OnClickListener? = null,
            val reactionPillCallback: TimelineEventController.ReactionPillCallback? = null,
            val avatarCallback: TimelineEventController.AvatarCallback? = null,
            val readReceiptsCallback: TimelineEventController.ReadReceiptsCallback? = null,
            val emojiTypeFace: Typeface? = null
    )

}