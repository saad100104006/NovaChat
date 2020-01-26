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

package im.vector.novaChat.features.home.room.breadcrumbs

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import im.vector.matrix.android.api.util.MatrixItem
import im.vector.novaChat.R
import im.vector.novaChat.core.epoxy.VectorEpoxyHolder
import im.vector.novaChat.core.epoxy.VectorEpoxyModel
import im.vector.novaChat.features.home.AvatarRenderer
import im.vector.novaChat.features.home.room.list.UnreadCounterBadgeView

@EpoxyModelClass(layout = R.layout.item_breadcrumbs)
abstract class BreadcrumbsItem : VectorEpoxyModel<BreadcrumbsItem.Holder>() {

    @EpoxyAttribute lateinit var avatarRenderer: AvatarRenderer
    @EpoxyAttribute lateinit var matrixItem: MatrixItem
    @EpoxyAttribute var unreadNotificationCount: Int = 0
    @EpoxyAttribute var showHighlighted: Boolean = false
    @EpoxyAttribute var hasUnreadMessage: Boolean = false
    @EpoxyAttribute var hasTypingUsers: Boolean = false
    @EpoxyAttribute var hasDraft: Boolean = false
    @EpoxyAttribute var itemClickListener: View.OnClickListener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.rootView.setOnClickListener(itemClickListener)
        holder.unreadIndentIndicator.isVisible = hasUnreadMessage
        holder.typingIndicator.isVisible = hasTypingUsers
        avatarRenderer.render(matrixItem, holder.avatarImageView)
        holder.unreadCounterBadgeView.render(UnreadCounterBadgeView.State(unreadNotificationCount, showHighlighted))
        holder.draftIndentIndicator.isVisible = hasDraft
    }

    class Holder : VectorEpoxyHolder() {
        val unreadCounterBadgeView by bind<UnreadCounterBadgeView>(R.id.breadcrumbsUnreadCounterBadgeView)
        val unreadIndentIndicator by bind<View>(R.id.breadcrumbsUnreadIndicator)
        val draftIndentIndicator by bind<View>(R.id.breadcrumbsDraftBadge)
        val typingIndicator by bind<View>(R.id.breadcrumbsTypingView)
        val avatarImageView by bind<ImageView>(R.id.breadcrumbsImageView)
        val rootView by bind<ViewGroup>(R.id.breadcrumbsRoot)
    }
}
