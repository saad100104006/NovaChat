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

package im.vector.novaChat.features.roomdirectory.picker

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import im.vector.novaChat.R
import im.vector.novaChat.core.epoxy.VectorEpoxyHolder
import im.vector.novaChat.core.epoxy.VectorEpoxyModel
import im.vector.novaChat.core.extensions.setTextOrHide
import im.vector.novaChat.core.glide.GlideApp

@EpoxyModelClass(layout = R.layout.item_room_directory)
abstract class RoomDirectoryItem : VectorEpoxyModel<RoomDirectoryItem.Holder>() {

    @EpoxyAttribute
    var directoryAvatarUrl: String? = null

    @EpoxyAttribute
    var directoryName: String? = null

    @EpoxyAttribute
    var directoryDescription: String? = null

    @EpoxyAttribute
    var includeAllNetworks: Boolean = false

    @EpoxyAttribute
    var globalListener: (() -> Unit)? = null

    override fun bind(holder: Holder) {
        holder.rootView.setOnClickListener { globalListener?.invoke() }

        // Avatar
        GlideApp.with(holder.avatarView)
                .load(directoryAvatarUrl)
                .apply {
                    if (!includeAllNetworks) {
                        placeholder(R.drawable.network_matrix)
                    }
                }
                .into(holder.avatarView)

        holder.nameView.text = directoryName
        holder.descritionView.setTextOrHide(directoryDescription)
    }


    class Holder : VectorEpoxyHolder() {
        val rootView by bind<ViewGroup>(R.id.itemRoomDirectoryLayout)

        val avatarView by bind<ImageView>(R.id.itemRoomDirectoryAvatar)
        val nameView by bind<TextView>(R.id.itemRoomDirectoryName)
        val descritionView by bind<TextView>(R.id.itemRoomDirectoryDescription)
    }

}

