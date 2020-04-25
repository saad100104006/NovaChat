/*
 * Copyright 2020 New Vector Ltd
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

package im.vector.novaChat.core.epoxy.profiles

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import im.vector.matrix.android.api.crypto.RoomEncryptionTrustLevel
import im.vector.matrix.android.api.util.MatrixItem
import im.vector.novaChat.R
import im.vector.novaChat.core.epoxy.VectorEpoxyHolder
import im.vector.novaChat.core.epoxy.VectorEpoxyModel
import im.vector.novaChat.core.extensions.setTextOrHide
import im.vector.novaChat.features.crypto.util.toImageRes
import im.vector.novaChat.features.home.AvatarRenderer

@EpoxyModelClass(layout = R.layout.item_profile_matrix_item)
abstract class ProfileMatrixItem : VectorEpoxyModel<ProfileMatrixItem.Holder>() {

    @EpoxyAttribute lateinit var avatarRenderer: AvatarRenderer
    @EpoxyAttribute lateinit var matrixItem: MatrixItem
    @EpoxyAttribute var userEncryptionTrustLevel: RoomEncryptionTrustLevel? = null
    @EpoxyAttribute var clickListener: View.OnClickListener? = null

    override fun bind(holder: Holder) {
        val bestName = matrixItem.getBestName()
        val matrixId = matrixItem.id.takeIf { it != bestName }
        holder.view.setOnClickListener(clickListener)
        holder.titleView.text = bestName
        holder.subtitleView.setTextOrHide(matrixId)
        avatarRenderer.render(matrixItem, holder.avatarImageView)
        holder.avatarDecorationImageView.setImageResource(userEncryptionTrustLevel.toImageRes())
    }

    class Holder : VectorEpoxyHolder() {
        val titleView by bind<TextView>(R.id.matrixItemTitle)
        val subtitleView by bind<TextView>(R.id.matrixItemSubtitle)
        val avatarImageView by bind<ImageView>(R.id.matrixItemAvatar)
        val avatarDecorationImageView by bind<ImageView>(R.id.matrixItemAvatarDecoration)
    }
}
