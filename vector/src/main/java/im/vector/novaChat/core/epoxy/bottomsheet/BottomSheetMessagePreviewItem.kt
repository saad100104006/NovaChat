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
 *
 */
package im.vector.novaChat.core.epoxy.bottomsheet

import android.text.method.MovementMethod
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import im.vector.matrix.android.api.util.MatrixItem
import im.vector.novaChat.R
import im.vector.novaChat.core.epoxy.VectorEpoxyHolder
import im.vector.novaChat.core.epoxy.VectorEpoxyModel
import im.vector.novaChat.core.extensions.setTextOrHide
import im.vector.novaChat.features.home.AvatarRenderer
import im.vector.novaChat.features.home.room.detail.timeline.tools.findPillsAndProcess

/**
 * A message preview for bottom sheet.
 */
@EpoxyModelClass(layout = R.layout.item_bottom_sheet_message_preview)
abstract class BottomSheetMessagePreviewItem : VectorEpoxyModel<BottomSheetMessagePreviewItem.Holder>() {

    @EpoxyAttribute
    lateinit var avatarRenderer: AvatarRenderer
    @EpoxyAttribute
    lateinit var matrixItem: MatrixItem
    @EpoxyAttribute
    lateinit var body: CharSequence
    @EpoxyAttribute
    var time: CharSequence? = null
    @EpoxyAttribute
    var movementMethod: MovementMethod? = null
    @EpoxyAttribute
    var userClicked: (() -> Unit)? = null

    override fun bind(holder: Holder) {
        avatarRenderer.render(matrixItem, holder.avatar)
        holder.avatar.setOnClickListener { userClicked?.invoke() }
        holder.sender.setTextOrHide(matrixItem.displayName)
        holder.body.movementMethod = movementMethod
        holder.body.text = body
        body.findPillsAndProcess(coroutineScope) { it.bind(holder.body) }
        holder.timestamp.setTextOrHide(time)
    }

    class Holder : VectorEpoxyHolder() {
        val avatar by bind<ImageView>(R.id.bottom_sheet_message_preview_avatar)
        val sender by bind<TextView>(R.id.bottom_sheet_message_preview_sender)
        val body by bind<TextView>(R.id.bottom_sheet_message_preview_body)
        val timestamp by bind<TextView>(R.id.bottom_sheet_message_preview_timestamp)
    }
}
