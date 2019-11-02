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

package im.vector.novaChat.features.autocomplete.user

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import im.vector.novaChat.R
import im.vector.novaChat.core.epoxy.VectorEpoxyHolder
import im.vector.novaChat.core.epoxy.VectorEpoxyModel
import im.vector.novaChat.features.home.AvatarRenderer

@EpoxyModelClass(layout = R.layout.item_autocomplete_user)
abstract class AutocompleteUserItem : VectorEpoxyModel<AutocompleteUserItem.Holder>() {

    @EpoxyAttribute lateinit var avatarRenderer: AvatarRenderer
    @EpoxyAttribute var name: String? = null
    @EpoxyAttribute var userId: String = ""
    @EpoxyAttribute var avatarUrl: String? = null
    @EpoxyAttribute var clickListener: View.OnClickListener? = null

    override fun bind(holder: Holder) {
        holder.view.setOnClickListener(clickListener)
        holder.nameView.text = name
        avatarRenderer.render(avatarUrl, userId, name, holder.avatarImageView)
    }

    class Holder : VectorEpoxyHolder() {
        val nameView by bind<TextView>(R.id.userAutocompleteName)
        val avatarImageView by bind<ImageView>(R.id.userAutocompleteAvatar)
    }

}