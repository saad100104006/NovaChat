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
import im.vector.novaChat.features.createdirect.CreateDirectRoomActivity
import im.vector.novaChat.features.crypto.keysbackup.settings.KeysBackupManageActivity
import im.vector.novaChat.features.crypto.quads.SharedSecureStorageActivity
import im.vector.novaChat.features.crypto.recover.BootstrapBottomSheet
import im.vector.novaChat.features.crypto.verification.VerificationBottomSheet
import im.vector.novaChat.features.debug.DebugMenuActivity
import im.vector.novaChat.features.home.HomeActivity
import im.vector.novaChat.features.home.HomeModule
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
import im.vector.novaChat.features.media.BigImageViewerActivity
import im.vector.novaChat.features.media.ImageMediaViewerActivity
import im.vector.novaChat.features.media.VideoMediaViewerActivity
import im.vector.novaChat.features.navigation.Navigator
import im.vector.novaChat.features.permalink.PermalinkHandlerActivity
import im.vector.novaChat.features.qrcode.QrCodeScannerActivity
import im.vector.novaChat.features.rageshake.BugReportActivity
import im.vector.novaChat.features.rageshake.BugReporter
import im.vector.novaChat.features.rageshake.RageShake
import im.vector.novaChat.features.reactions.EmojiReactionPickerActivity
import im.vector.novaChat.features.reactions.widget.ReactionButton
import im.vector.novaChat.features.roomdirectory.RoomDirectoryActivity
import im.vector.novaChat.features.roomdirectory.createroom.CreateRoomActivity
import im.vector.novaChat.features.roommemberprofile.devices.DeviceListBottomSheet
import im.vector.novaChat.features.settings.VectorSettingsActivity
import im.vector.novaChat.features.settings.devices.DeviceVerificationInfoBottomSheet
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

    /* ==========================================================================================
     * Shortcut to VectorComponent elements
     * ========================================================================================== */

    fun activeSessionHolder(): ActiveSessionHolder
    fun fragmentFactory(): FragmentFactory
    fun viewModelFactory(): ViewModelProvider.Factory
    fun bugReporter(): BugReporter
    fun rageShake(): RageShake
    fun navigator(): Navigator
    fun errorFormatter(): ErrorFormatter
    fun uiStateRepository(): UiStateRepository

    /* ==========================================================================================
     * Activities
     * ========================================================================================== */

    fun inject(activity: HomeActivity)
    fun inject(activity: VectorSettingsActivity)
    fun inject(activity: KeysBackupManageActivity)
    fun inject(activity: EmojiReactionPickerActivity)
    fun inject(activity: LoginActivity)
    fun inject(activity: LinkHandlerActivity)
    fun inject(activity: MainActivity)
    fun inject(activity: RoomDirectoryActivity)
    fun inject(activity: BugReportActivity)
    fun inject(activity: ImageMediaViewerActivity)
    fun inject(activity: FilteredRoomsActivity)
    fun inject(activity: CreateRoomActivity)
    fun inject(activity: VideoMediaViewerActivity)
    fun inject(activity: CreateDirectRoomActivity)
    fun inject(activity: IncomingShareActivity)
    fun inject(activity: SoftLogoutActivity)
    fun inject(activity: PermalinkHandlerActivity)
    fun inject(activity: QrCodeScannerActivity)
    fun inject(activity: DebugMenuActivity)
    fun inject(activity: SharedSecureStorageActivity)
    fun inject(activity: BigImageViewerActivity)

    /* ==========================================================================================
     * BottomSheets
     * ========================================================================================== */

    fun inject(bottomSheet: MessageActionsBottomSheet)
    fun inject(bottomSheet: ViewReactionsBottomSheet)
    fun inject(bottomSheet: ViewEditHistoryBottomSheet)
    fun inject(bottomSheet: DisplayReadReceiptsBottomSheet)
    fun inject(bottomSheet: RoomListQuickActionsBottomSheet)
    fun inject(bottomSheet: VerificationBottomSheet)
    fun inject(bottomSheet: DeviceVerificationInfoBottomSheet)
    fun inject(bottomSheet: DeviceListBottomSheet)
    fun inject(bottomSheet: BootstrapBottomSheet)

    /* ==========================================================================================
     * Others
     * ========================================================================================== */

    fun inject(view: VectorInviteView)
    fun inject(preference: UserAvatarPreference)
    fun inject(button: ReactionButton)

    /* ==========================================================================================
     * Factory
     * ========================================================================================== */

    @Component.Factory
    interface Factory {
        fun create(vectorComponent: VectorComponent,
                   @BindsInstance context: AppCompatActivity
        ): ScreenComponent
    }
}
