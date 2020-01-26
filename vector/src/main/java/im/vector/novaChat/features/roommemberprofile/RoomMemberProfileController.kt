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

package im.vector.novaChat.features.roommemberprofile

import com.airbnb.epoxy.TypedEpoxyController
import im.vector.novaChat.R
import im.vector.novaChat.core.epoxy.profiles.buildProfileAction
import im.vector.novaChat.core.epoxy.profiles.buildProfileSection
import im.vector.novaChat.core.resources.ColorProvider
import im.vector.novaChat.core.resources.StringProvider
import javax.inject.Inject

class RoomMemberProfileController @Inject constructor(
        private val stringProvider: StringProvider,
        colorProvider: ColorProvider
) : TypedEpoxyController<RoomMemberProfileViewState>() {

    private val dividerColor = colorProvider.getColorFromAttribute(R.attr.vctr_list_divider_color)

    var callback: Callback? = null

    interface Callback {
        fun onIgnoreClicked()
        fun onLearnMoreClicked()
        fun onJumpToReadReceiptClicked()
        fun onMentionClicked()
    }

    override fun buildModels(data: RoomMemberProfileViewState?) {
        if (data?.userMatrixItem?.invoke() == null) {
            return
        }
        if (data.showAsMember) {
            buildRoomMemberActions(data)
        } else {
            buildUserActions(data)
        }
    }

    private fun buildUserActions(state: RoomMemberProfileViewState) {
        val ignoreActionTitle = state.buildIgnoreActionTitle() ?: return
        // More
        buildProfileSection(stringProvider.getString(R.string.room_profile_section_more))
        buildProfileAction(
                id = "ignore",
                title = ignoreActionTitle,
                dividerColor = dividerColor,
                destructive = true,
                editable = false,
                divider = false,
                action = { callback?.onIgnoreClicked() }
        )
    }

    private fun buildRoomMemberActions(state: RoomMemberProfileViewState) {
        // Security
        buildProfileSection(stringProvider.getString(R.string.room_profile_section_security))
        val learnMoreSubtitle = if (state.isRoomEncrypted) {
            R.string.room_profile_encrypted_subtitle
        } else {
            R.string.room_profile_not_encrypted_subtitle
        }
        buildProfileAction(
                id = "learn_more",
                title = stringProvider.getString(R.string.room_profile_section_security_learn_more),
                dividerColor = dividerColor,
                editable = false,
                divider = false,
                subtitle = stringProvider.getString(learnMoreSubtitle),
                action = { callback?.onLearnMoreClicked() }
        )

        // More
        if (!state.isMine) {
            buildProfileSection(stringProvider.getString(R.string.room_profile_section_more))
            buildProfileAction(
                    id = "read_receipt",
                    editable = false,
                    title = stringProvider.getString(R.string.room_member_jump_to_read_receipt),
                    dividerColor = dividerColor,
                    action = { callback?.onJumpToReadReceiptClicked() }
            )

            val ignoreActionTitle = state.buildIgnoreActionTitle()

            buildProfileAction(
                    id = "mention",
                    title = stringProvider.getString(R.string.room_participants_action_mention),
                    dividerColor = dividerColor,
                    editable = false,
                    divider = ignoreActionTitle != null,
                    action = { callback?.onMentionClicked() }
            )
            if (ignoreActionTitle != null) {
                buildProfileAction(
                        id = "ignore",
                        title = ignoreActionTitle,
                        dividerColor = dividerColor,
                        destructive = true,
                        editable = false,
                        divider = false,
                        action = { callback?.onIgnoreClicked() }
                )
            }
        }
    }

    private fun RoomMemberProfileViewState.buildIgnoreActionTitle(): String? {
        val isIgnored = isIgnored() ?: return null
        return if (isIgnored) {
            stringProvider.getString(R.string.unignore)
        } else {
            stringProvider.getString(R.string.ignore)
        }
    }
}
