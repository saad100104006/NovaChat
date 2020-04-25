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
 */

package im.vector.novaChat.features.roomprofile.settings

import com.airbnb.epoxy.TypedEpoxyController
import im.vector.novaChat.R
import im.vector.novaChat.core.epoxy.profiles.buildProfileAction
import im.vector.novaChat.core.epoxy.profiles.buildProfileSection
import im.vector.novaChat.core.resources.ColorProvider
import im.vector.novaChat.core.resources.StringProvider
import javax.inject.Inject

// TODO Add other feature here (waiting for design)
class RoomSettingsController @Inject constructor(
        private val stringProvider: StringProvider,
        colorProvider: ColorProvider
) : TypedEpoxyController<RoomSettingsViewState>() {

    interface Callback {
        fun onEnableEncryptionClicked()
    }

    private val dividerColor = colorProvider.getColorFromAttribute(R.attr.vctr_list_divider_color)

    var callback: Callback? = null

    init {
        setData(null)
    }

    override fun buildModels(data: RoomSettingsViewState?) {
        val roomSummary = data?.roomSummary?.invoke() ?: return

        buildProfileSection(
                stringProvider.getString(R.string.settings)
        )

        if (roomSummary.isEncrypted) {
            buildProfileAction(
                    id = "encryption",
                    title = stringProvider.getString(R.string.room_settings_addresses_e2e_enabled),
                    dividerColor = dividerColor,
                    divider = false,
                    editable = false
            )
        } else {
            buildProfileAction(
                    id = "encryption",
                    title = stringProvider.getString(R.string.room_settings_enable_encryption),
                    subtitle = stringProvider.getString(R.string.room_settings_enable_encryption_warning),
                    dividerColor = dividerColor,
                    divider = false,
                    editable = true,
                    action = { callback?.onEnableEncryptionClicked() }
            )
        }
    }
}
