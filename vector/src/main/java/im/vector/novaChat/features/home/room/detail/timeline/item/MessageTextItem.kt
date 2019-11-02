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

import android.view.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.PrecomputedTextCompat
import androidx.core.text.toSpannable
import androidx.core.widget.TextViewCompat
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import im.vector.matrix.android.api.session.Session
import im.vector.novaChat.R
import im.vector.novaChat.features.home.room.detail.RoomDetailFragment.Companion.titless
import im.vector.novaChat.features.home.room.detail.timeline.TimelineEventController
import im.vector.novaChat.features.html.PillImageSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import javax.inject.Inject

@EpoxyModelClass(layout = R.layout.item_timeline_event_base)
abstract class MessageTextItem : AbsMessageItem<MessageTextItem.Holder>() {

    @EpoxyAttribute
    var message: CharSequence? = null
    @EpoxyAttribute
    var useBigFont: Boolean = false
    @EpoxyAttribute
    var urlClickCallback: TimelineEventController.UrlClickCallback? = null

    @Inject
    lateinit var session: Session

    // Better link movement methods fixes the issue when
    // long pressing to open the context menu on a TextView also triggers an autoLink click.
    private val mvmtMethod = BetterLinkMovementMethod.newInstance().also {
        it.setOnLinkClickListener { _, url ->
            //Return false to let android manage the click on the link, or true if the link is handled by the application
            urlClickCallback?.onUrlClicked(url) == true
        }
        //We need also to fix the case when long click on link will trigger long click on cell
        it.setOnLinkLongClickListener { tv, url ->
            //Long clicks are handled by parent, return true to block android to do something with url
            if (urlClickCallback?.onUrlLongClicked(url) == true) {
                tv.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0f, 0f, 0))
                true
            } else {
                false
            }

        }
    }

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.messageView.movementMethod = mvmtMethod

        if (useBigFont) {
            holder.messageView.textSize = 44F
        } else {
            holder.messageView.textSize = 14F
        }
        val textFuture = PrecomputedTextCompat.getTextFuture(message ?: "",
                TextViewCompat.getTextMetricsParams(holder.messageView),
                null)

        holder.messageView.setTextFuture(textFuture)

        if (attributes.informationData.memberName.toString().equals(titless)) {
            holder.messageView.setBackgroundResource(R.drawable.incoming)
            holder.messageViews.setMarginLeft(200)
            holder.messageViews.setMarginRight(0)
        } else {
            holder.messageView.setBackgroundResource(R.drawable.out)
            holder.messageViews.setMarginLeft(0)
            holder.messageViews.setMarginRight(200)
        }
        renderSendState(holder.messageView, holder.messageView)
        holder.messageView.setOnClickListener(attributes.itemClickListener)
        holder.messageView.setOnLongClickListener(attributes.itemLongClickListener)
        findPillsAndProcess { it.bind(holder.messageView) }
    }

    private fun findPillsAndProcess(processBlock: (span: PillImageSpan) -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            val pillImageSpans: Array<PillImageSpan>? = withContext(Dispatchers.IO) {
                message?.toSpannable()?.let { spannable ->
                    spannable.getSpans(0, spannable.length, PillImageSpan::class.java)
                }
            }
            pillImageSpans?.forEach { processBlock(it) }
        }
    }

    fun View.setMarginLeft(leftMargin: Int) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
       params.setMargins(leftMargin, params.topMargin, params.rightMargin, params.bottomMargin)
        layoutParams = params
    }


    fun View.setMarginRight(rightMargin: Int) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(params.leftMargin, params.topMargin, rightMargin, params.bottomMargin)
        layoutParams = params
    }

    fun ConstraintLayout.setMargin(leftMargin: Int? = null, topMargin: Int? = null,
                       rightMargin: Int? = null, bottomMargin: Int? = null) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(
                leftMargin ?: params.leftMargin,
                topMargin ?: params.topMargin,
                rightMargin ?: params.rightMargin,
                bottomMargin ?: params.bottomMargin)
        layoutParams = params
    }

    override fun getViewType() = STUB_ID

    class Holder : AbsMessageItem.Holder(STUB_ID) {
        val messageView by bind<AppCompatTextView>(R.id.messageTextView)

        val messageViews by bind<ConstraintLayout>(R.id.messages)

    }

    companion object {
        private const val STUB_ID = R.id.messageContentTextStub
    }
}