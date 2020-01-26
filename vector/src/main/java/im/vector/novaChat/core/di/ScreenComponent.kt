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

package im.vector.novaChat.core.di

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import dagger.BindsInstance
import dagger.Component
import im.vector.novaChat.core.error.ErrorFormatter
import im.vector.novaChat.core.preference.UserAvatarPreference
import im.vector.novaChat.features.MainActivity
import im.vector.novaChat.features.crypto.keysbackup.settings.KeysBackupManageActivity
import im.vector.novaChat.features.home.HomeActivity
import im.vector.novaChat.features.home.HomeModule
import im.vector.novaChat.features.createdirect.CreateDirectRoomActivity
import im.vector.novaChat.features.home.room.detail.readreceipts.DisplayReadReceiptsBottomSheet
import im.vector.novaChat.features.home.room.detail.timeline.action.MessageActionsBottomSheet
import im.vector.novaChat.features.home.room.detail.timeline.edithistory.ViewEditHistoryBottomSheet
import im.vector.novaChat.features.home.room.detail.timeline.reactions.ViewReactionsBottomSheet
import im.vector.novaChat.features.home.room.filtered.FilteredRoomsActivity
import im.vector.novaChat.features.home.room.list.RoomListModule
import im.vector.novaChat.features.home.room.list.actions.RoomListQuickActionsBottomSheet
import im.vector.novaChat.features.invite.VectorInviteView
import im.vector.novaChat.features.link.LinkHandlerActivity
import im.vector.novaChat.features.login.LoginActivity
import im.vector.novaChat.features.media.ImageMediaViewerActivity
import im.vector.novaChat.features.media.VideoMediaViewerActivity
import im.vector.novaChat.features.navigation.Navigator
import im.vector.novaChat.features.permalink.PermalinkHandlerActivity
import im.vector.novaChat.features.rageshake.BugReportActivity
import im.vector.novaChat.features.rageshake.BugReporter
import im.vector.novaChat.features.rageshake.RageShake
import im.vector.novaChat.features.reactions.EmojiReactionPickerActivity
import im.vector.novaChat.features.reactions.widget.ReactionButton
import im.vector.novaChat.features.roomdirectory.RoomDirectoryActivity
import im.vector.novaChat.features.roomdirectory.createroom.CreateRoomActivity
import im.vector.novaChat.features.settings.VectorSettingsActivity
import im.vector.novaChat.features.share.IncomingShareActivity
import im.vector.novaChat.features.signout.soft.SoftLogoutActivity
import im.vector.novaChat.features.ui.UiStateRepository

@Component(
        dependencies = [
            VectorComponent::class
        ],
        modules = [
            AssistedInjectModule::class,
            ViewModelModule::class,
            FragmentModule::class,
            HomeModule::class,
            RoomListModule::class,
            ScreenModule::class
        ]
)
@ScreenScope
interface ScreenComponent {

    fun activeSessionHolder(): ActiveSessionHolder

    fun fragmentFactory(): FragmentFactory

    fun viewModelFactory(): ViewModelProvider.Factory

    fun bugReporter(): BugReporter

    fun rageShake(): RageShake

    fun navigator(): Navigator

    fun errorFormatter(): ErrorFormatter

    fun uiStateRepository(): UiStateRepository

    fun inject(activity: HomeActivity)

    fun inject(messageActionsBottomSheet: MessageActionsBottomSheet)

    fun inject(viewReactionsBottomSheet: ViewReactionsBottomSheet)

    fun inject(viewEditHistoryBottomSheet: ViewEditHistoryBottomSheet)

    fun inject(vectorSettingsActivity: VectorSettingsActivity)

    fun inject(keysBackupManageActivity: KeysBackupManageActivity)

    fun inject(emojiReactionPickerActivity: EmojiReactionPickerActivity)

    fun inject(loginActivity: LoginActivity)

    fun inject(linkHandlerActivity: LinkHandlerActivity)

    fun inject(mainActivity: MainActivity)

    fun inject(roomDirectoryActivity: RoomDirectoryActivity)

    fun inject(bugReportActivity: BugReportActivity)

    fun inject(imageMediaViewerActivity: ImageMediaViewerActivity)

    fun inject(filteredRoomsActivity: FilteredRoomsActivity)

    fun inject(createRoomActivity: CreateRoomActivity)

    fun inject(vectorInviteView: VectorInviteView)

    fun inject(videoMediaViewerActivity: VideoMediaViewerActivity)

    fun inject(userAvatarPreference: UserAvatarPreference)

    fun inject(createDirectRoomActivity: CreateDirectRoomActivity)

    fun inject(displayReadReceiptsBottomSheet: DisplayReadReceiptsBottomSheet)

    fun inject(reactionButton: ReactionButton)

    fun inject(incomingShareActivity: IncomingShareActivity)

    fun inject(roomListActionsBottomSheet: RoomListQuickActionsBottomSheet)

    fun inject(activity: SoftLogoutActivity)

    fun inject(permalinkHandlerActivity: PermalinkHandlerActivity)

    @Component.Factory
    interface Factory {
        fun create(vectorComponent: VectorComponent,
                   @BindsInstance context: AppCompatActivity
        ): ScreenComponent
    }
}
