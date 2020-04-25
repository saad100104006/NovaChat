/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.novaChat.features.settings.devices

import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import im.vector.matrix.android.internal.crypto.model.rest.DeviceInfo
import im.vector.novaChat.R
import im.vector.novaChat.core.epoxy.VectorEpoxyHolder
import im.vector.novaChat.core.epoxy.VectorEpoxyModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A list item for Device.
 */
@EpoxyModelClass(layout = R.layout.item_device)
abstract class DeviceItem : VectorEpoxyModel<DeviceItem.Holder>() {

    @EpoxyAttribute
    lateinit var deviceInfo: DeviceInfo

    @EpoxyAttribute
    var currentDevice = false

    @EpoxyAttribute
    var itemClickAction: (() -> Unit)? = null

    @EpoxyAttribute
    var detailedMode = false

    @EpoxyAttribute
    var trusted : Boolean? = null

    override fun bind(holder: Holder) {
        holder.root.setOnClickListener { itemClickAction?.invoke() }

        if (trusted != null) {
            holder.trustIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                            holder.view.context,
                            if (trusted!!) R.drawable.ic_shield_trusted else R.drawable.ic_shield_warning
                    )
            )
            holder.trustIcon.isInvisible = false
        } else {
            holder.trustIcon.isInvisible = true
        }

        val detailedModeLabels = listOf(
                holder.displayNameLabelText,
                holder.displayNameText,
                holder.deviceIdLabelText,
                holder.deviceIdText,
                holder.deviceLastSeenLabelText,
                holder.deviceLastSeenText
        )
        if (detailedMode) {
            holder.summaryLabelText.isVisible = false

            holder.displayNameText.text = deviceInfo.displayName ?: ""
            holder.deviceIdText.text = deviceInfo.deviceId ?: ""

            val lastSeenIp = deviceInfo.lastSeenIp?.takeIf { ip -> ip.isNotBlank() } ?: "-"

            val lastSeenTime = deviceInfo.lastSeenTs?.let { ts ->
                val dateFormatTime = SimpleDateFormat("HH:mm:ss", Locale.ROOT)
                val date = Date(ts)

                val time = dateFormatTime.format(date)
                val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())

                dateFormat.format(date) + ", " + time
            } ?: "-"

            holder.deviceLastSeenText.text = holder.root.context.getString(R.string.devices_details_last_seen_format, lastSeenIp, lastSeenTime)

            detailedModeLabels.map {
                it.isVisible = true
                it.setTypeface(null, if (currentDevice) Typeface.BOLD else Typeface.NORMAL)
            }
        } else {
            holder.summaryLabelText.text = deviceInfo.displayName ?: deviceInfo.deviceId ?: ""
            holder.summaryLabelText.isVisible = true
            detailedModeLabels.map {
                it.isVisible = false
            }
        }
    }

    class Holder : VectorEpoxyHolder() {
        val root by bind<ViewGroup>(R.id.itemDeviceRoot)
        val summaryLabelText by bind<TextView>(R.id.itemDeviceSimpleSummary)
        val displayNameLabelText by bind<TextView>(R.id.itemDeviceDisplayNameLabel)
        val displayNameText by bind<TextView>(R.id.itemDeviceDisplayName)
        val deviceIdLabelText by bind<TextView>(R.id.itemDeviceIdLabel)
        val deviceIdText by bind<TextView>(R.id.itemDeviceId)
        val deviceLastSeenLabelText by bind<TextView>(R.id.itemDeviceLastSeenLabel)
        val deviceLastSeenText by bind<TextView>(R.id.itemDeviceLastSeen)

        val trustIcon by bind<ImageView>(R.id.itemDeviceTrustLevelIcon)
    }
}
