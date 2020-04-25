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

package im.vector.novaChat.core.epoxy.profiles

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import im.vector.novaChat.R
import im.vector.novaChat.core.epoxy.VectorEpoxyHolder
import im.vector.novaChat.core.epoxy.VectorEpoxyModel
import im.vector.novaChat.core.extensions.setTextOrHide
import im.vector.novaChat.features.themes.ThemeUtils

@EpoxyModelClass(layout = R.layout.item_profile_action)
abstract class ProfileActionItem : VectorEpoxyModel<ProfileActionItem.Holder>() {

    @EpoxyAttribute
    lateinit var title: String
    @EpoxyAttribute
    var subtitle: String? = null

    @EpoxyAttribute
    var iconRes: Int = 0

    @EpoxyAttribute
    var tintIcon: Boolean = true

    @EpoxyAttribute
    var editableRes: Int = R.drawable.ic_arrow_right

    @EpoxyAttribute
    var accessoryRes: Int = 0

    @EpoxyAttribute
    var editable: Boolean = true

    @EpoxyAttribute
    var destructive: Boolean = false

    @EpoxyAttribute
    var listener: View.OnClickListener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.view.setOnClickListener(listener)
        if (listener == null) {
            holder.view.isClickable = false
        }
        holder.editable.isVisible = editable
        holder.title.text = title
        val tintColor = if (destructive) {
            ContextCompat.getColor(holder.view.context, R.color.riotx_notice)
        } else {
            ThemeUtils.getColor(holder.view.context, R.attr.riotx_text_primary)
        }
        holder.title.setTextColor(tintColor)
        holder.subtitle.setTextOrHide(subtitle)
        if (iconRes != 0) {
            holder.icon.setImageResource(iconRes)
            if (tintIcon) {
                ImageViewCompat.setImageTintList(holder.icon, ColorStateList.valueOf(tintColor))
            } else {
                ImageViewCompat.setImageTintList(holder.icon, null)
            }
            holder.icon.isVisible = true
        } else {
            holder.icon.isVisible = false
        }

        if (accessoryRes != 0) {
            holder.secondaryAccessory.setImageResource(accessoryRes)
            holder.secondaryAccessory.isVisible = true
        } else {
            holder.secondaryAccessory.isVisible = false
        }

        if (editableRes != 0) {
            val tintColorSecondary = if (destructive) {
                tintColor
            } else {
                ThemeUtils.getColor(holder.view.context, R.attr.riotx_text_secondary)
            }
            holder.editable.setImageResource(editableRes)
            ImageViewCompat.setImageTintList(holder.editable, ColorStateList.valueOf(tintColorSecondary))
            holder.editable.isVisible = true
        } else {
            holder.editable.isVisible = false
        }
    }

    class Holder : VectorEpoxyHolder() {
        val icon by bind<ImageView>(R.id.actionIcon)
        val title by bind<TextView>(R.id.actionTitle)
        val subtitle by bind<TextView>(R.id.actionSubtitle)
        val editable by bind<ImageView>(R.id.actionEditable)
        val secondaryAccessory by bind<ImageView>(R.id.actionSecondaryAccessory)
    }
}
