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

package im.vector.riotx.features.home.room.detail.timeline.item

import android.graphics.Color
import android.text.method.MovementMethod
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.PrecomputedTextCompat
import androidx.core.widget.TextViewCompat
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import im.vector.riotx.R
import im.vector.riotx.features.home.HomeDrawerFragment
import im.vector.riotx.features.home.room.detail.timeline.tools.findPillsAndProcess

@EpoxyModelClass(layout = R.layout.item_timeline_event_base)
abstract class MessageTextItem : AbsMessageItem<MessageTextItem.Holder>() {

    @EpoxyAttribute
    var searchForPills: Boolean = false
    @EpoxyAttribute
    var message: CharSequence? = null
    @EpoxyAttribute
    var useBigFont: Boolean = false
    @EpoxyAttribute
    var movementMethod: MovementMethod? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.messageView.movementMethod = movementMethod
        if (useBigFont) {
            holder.messageView.textSize = 44F
        } else {
            holder.messageView.textSize = 14F
        }

        if (attributes.informationData.memberName.toString().equals(HomeDrawerFragment.titless)) {
            holder.messageViews.setBackgroundResource(R.drawable.ic_outgoing_new)
            if(holder.messageView.text.length<25){
                holder.messageViews.setMarginLeft(250)
            } else{
                holder.messageViews.setMarginLeft(100)
            }

            holder.messageViews.setMarginRight(30)
        } else {
            holder.messageViews.setBackgroundResource(R.drawable.ic_incoming_new)
            holder.messageViews.setMarginLeft(30)
            if(holder.messageView.text.length<25){
                holder.messageViews.setMarginRight(250)
            } else{
                holder.messageViews.setMarginRight(100)
            }
        }




        renderSendState(holder.messageView, holder.messageView)
        holder.messageView.setOnClickListener(attributes.itemClickListener)
        holder.messageView.setOnLongClickListener(attributes.itemLongClickListener)
        if (searchForPills) {
            message?.findPillsAndProcess(coroutineScope) { it.bind(holder.messageView) }
        }
        val textFuture = PrecomputedTextCompat.getTextFuture(
                message ?: "",
                TextViewCompat.getTextMetricsParams(holder.messageView),
                null)
        holder.messageView.setTextFuture(textFuture)
        holder.messageView.gravity= Gravity.START
        holder.messageView.setTextColor(Color.WHITE)
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

    fun RelativeLayout.setMargin(leftMargin: Int? = null, topMargin: Int? = null,
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
        val messageViews by bind<RelativeLayout>(R.id.messages)
    }

    companion object {
        private const val STUB_ID = R.id.messageContentTextStub
    }
}
