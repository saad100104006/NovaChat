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
import androidx.lifecycle.ViewModelProvider
import dagger.BindsInstance
import dagger.Component
import im.vector.fragments.keysbackup.restore.KeysBackupRestoreFromPassphraseFragment
import im.vector.novaChat.core.preference.UserAvatarPreference
import im.vector.novaChat.features.MainActivity
import im.vector.novaChat.features.crypto.keysbackup.restore.KeysBackupRestoreFromKeyFragment
import im.vector.novaChat.features.crypto.keysbackup.restore.KeysBackupRestoreSuccessFragment
import im.vector.novaChat.features.crypto.keysbackup.settings.KeysBackupManageActivity
import im.vector.novaChat.features.crypto.keysbackup.settings.KeysBackupSettingsFragment
import im.vector.novaChat.features.crypto.keysbackup.setup.KeysBackupSetupStep1Fragment
import im.vector.novaChat.features.crypto.keysbackup.setup.KeysBackupSetupStep2Fragment
import im.vector.novaChat.features.crypto.keysbackup.setup.KeysBackupSetupStep3Fragment
import im.vector.novaChat.features.crypto.verification.SASVerificationIncomingFragment
import im.vector.novaChat.features.home.HomeActivity
import im.vector.novaChat.features.home.HomeDetailFragment
import im.vector.novaChat.features.home.HomeDrawerFragment
import im.vector.novaChat.features.home.HomeModule
import im.vector.novaChat.features.home.createdirect.CreateDirectRoomActivity
import im.vector.novaChat.features.home.createdirect.CreateDirectRoomDirectoryUsersFragment
import im.vector.novaChat.features.home.createdirect.CreateDirectRoomKnownUsersFragment
import im.vector.novaChat.features.home.group.GroupListFragment
import im.vector.novaChat.features.home.room.detail.RoomDetailFragment
import im.vector.novaChat.features.home.room.detail.readreceipts.DisplayReadReceiptsBottomSheet
import im.vector.novaChat.features.home.room.detail.timeline.action.*
import im.vector.novaChat.features.home.room.filtered.FilteredRoomsActivity
import im.vector.novaChat.features.home.room.list.RoomListFragment
import im.vector.novaChat.features.invite.VectorInviteView
import im.vector.novaChat.features.link.LinkHandlerActivity
import im.vector.novaChat.features.login.LoginActivity
import im.vector.novaChat.features.login.LoginFragment
import im.vector.novaChat.features.login.LoginSsoFallbackFragment
import im.vector.novaChat.features.media.ImageMediaViewerActivity
import im.vector.novaChat.features.media.VideoMediaViewerActivity
import im.vector.novaChat.features.navigation.Navigator
import im.vector.novaChat.features.rageshake.BugReportActivity
import im.vector.novaChat.features.rageshake.BugReporter
import im.vector.novaChat.features.rageshake.RageShake
import im.vector.novaChat.features.reactions.EmojiReactionPickerActivity
import im.vector.novaChat.features.reactions.widget.ReactionButton
import im.vector.novaChat.features.roomdirectory.PublicRoomsFragment
import im.vector.novaChat.features.roomdirectory.RoomDirectoryActivity
import im.vector.novaChat.features.roomdirectory.createroom.CreateRoomActivity
import im.vector.novaChat.features.roomdirectory.createroom.CreateRoomFragment
import im.vector.novaChat.features.roomdirectory.picker.RoomDirectoryPickerFragment
import im.vector.novaChat.features.roomdirectory.roompreview.RoomPreviewNoPreviewFragment
import im.vector.novaChat.features.settings.*
import im.vector.novaChat.features.settings.push.PushGatewaysFragment
import im.vector.novaChat.features.ui.UiStateRepository

@Component(dependencies = [VectorComponent::class], modules = [AssistedInjectModule::class, ViewModelModule::class, HomeModule::class])
@ScreenScope
interface ScreenComponent {

    fun activeSessionHolder(): ActiveSessionHolder

    fun viewModelFactory(): ViewModelProvider.Factory

    fun bugReporter(): BugReporter

    fun rageShake(): RageShake

    fun navigator(): Navigator

    fun uiStateRepository(): UiStateRepository

    fun inject(activity: HomeActivity)

    fun inject(roomDetailFragment: RoomDetailFragment)

    fun inject(roomListFragment: RoomListFragment)

    fun inject(groupListFragment: GroupListFragment)

    fun inject(roomDirectoryPickerFragment: RoomDirectoryPickerFragment)

    fun inject(roomPreviewNoPreviewFragment: RoomPreviewNoPreviewFragment)

    fun inject(keysBackupSettingsFragment: KeysBackupSettingsFragment)

    fun inject(homeDrawerFragment: HomeDrawerFragment)

    fun inject(homeDetailFragment: HomeDetailFragment)

    fun inject(messageActionsBottomSheet: MessageActionsBottomSheet)

    fun inject(viewReactionBottomSheet: ViewReactionBottomSheet)

    fun inject(viewEditHistoryBottomSheet: ViewEditHistoryBottomSheet)

    fun inject(messageMenuFragment: MessageMenuFragment)

    fun inject(vectorSettingsActivity: VectorSettingsActivity)

    fun inject(createRoomFragment: CreateRoomFragment)

    fun inject(keysBackupManageActivity: KeysBackupManageActivity)

    fun inject(keysBackupRestoreFromKeyFragment: KeysBackupRestoreFromKeyFragment)

    fun inject(keysBackupRestoreFromPassphraseFragment: KeysBackupRestoreFromPassphraseFragment)

    fun inject(keysBackupRestoreSuccessFragment: KeysBackupRestoreSuccessFragment)

    fun inject(keysBackupSetupStep1Fragment: KeysBackupSetupStep1Fragment)

    fun inject(keysBackupSetupStep2Fragment: KeysBackupSetupStep2Fragment)

    fun inject(keysBackupSetupStep3Fragment: KeysBackupSetupStep3Fragment)

    fun inject(publicRoomsFragment: PublicRoomsFragment)

    fun inject(loginFragment: LoginFragment)

    fun inject(loginSsoFallbackFragment: LoginSsoFallbackFragment)

    fun inject(sasVerificationIncomingFragment: SASVerificationIncomingFragment)

    fun inject(quickReactionFragment: QuickReactionFragment)

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

    fun inject(vectorSettingsNotificationPreferenceFragment: VectorSettingsNotificationPreferenceFragment)

    fun inject(vectorSettingsPreferencesFragment: VectorSettingsPreferencesFragment)

    fun inject(vectorSettingsAdvancedNotificationPreferenceFragment: VectorSettingsAdvancedNotificationPreferenceFragment)

    fun inject(vectorSettingsSecurityPrivacyFragment: VectorSettingsSecurityPrivacyFragment)

    fun inject(vectorSettingsHelpAboutFragment: VectorSettingsHelpAboutFragment)

    fun inject(userAvatarPreference: UserAvatarPreference)

    fun inject(vectorSettingsNotificationsTroubleshootFragment: VectorSettingsNotificationsTroubleshootFragment)

    fun inject(pushGatewaysFragment: PushGatewaysFragment)

    fun inject(createDirectRoomKnownUsersFragment: CreateDirectRoomKnownUsersFragment)

    fun inject(createDirectRoomDirectoryUsersFragment: CreateDirectRoomDirectoryUsersFragment)

    fun inject(createDirectRoomActivity: CreateDirectRoomActivity)

    fun inject(displayReadReceiptsBottomSheet: DisplayReadReceiptsBottomSheet)

    fun inject(reactionButton: ReactionButton)

    @Component.Factory
    interface Factory {
        fun create(vectorComponent: VectorComponent,
                   @BindsInstance context: AppCompatActivity
        ): ScreenComponent
    }
}
