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
 *
 */

package im.vector.novaChat.core.di

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import im.vector.novaChat.features.attachments.preview.AttachmentsPreviewFragment
import im.vector.novaChat.features.createdirect.CreateDirectRoomDirectoryUsersFragment
import im.vector.novaChat.features.createdirect.CreateDirectRoomKnownUsersFragment
import im.vector.novaChat.features.crypto.keysbackup.settings.KeysBackupSettingsFragment
import im.vector.novaChat.features.crypto.quads.SharedSecuredStorageKeyFragment
import im.vector.novaChat.features.crypto.quads.SharedSecuredStoragePassphraseFragment
import im.vector.novaChat.features.crypto.recover.BootstrapAccountPasswordFragment
import im.vector.novaChat.features.crypto.recover.BootstrapConclusionFragment
import im.vector.novaChat.features.crypto.recover.BootstrapConfirmPassphraseFragment
import im.vector.novaChat.features.crypto.recover.BootstrapEnterPassphraseFragment
import im.vector.novaChat.features.crypto.recover.BootstrapMigrateBackupFragment
import im.vector.novaChat.features.crypto.recover.BootstrapSaveRecoveryKeyFragment
import im.vector.novaChat.features.crypto.recover.BootstrapWaitingFragment
import im.vector.novaChat.features.crypto.verification.cancel.VerificationCancelFragment
import im.vector.novaChat.features.crypto.verification.cancel.VerificationNotMeFragment
import im.vector.novaChat.features.crypto.verification.choose.VerificationChooseMethodFragment
import im.vector.novaChat.features.crypto.verification.conclusion.VerificationConclusionFragment
import im.vector.novaChat.features.crypto.verification.emoji.VerificationEmojiCodeFragment
import im.vector.novaChat.features.crypto.verification.qrconfirmation.VerificationQRWaitingFragment
import im.vector.novaChat.features.crypto.verification.qrconfirmation.VerificationQrScannedByOtherFragment
import im.vector.novaChat.features.crypto.verification.request.VerificationRequestFragment
import im.vector.novaChat.features.grouplist.GroupListFragment
import im.vector.novaChat.features.home.HomeDetailFragment
import im.vector.novaChat.features.home.HomeDrawerFragment
import im.vector.novaChat.features.home.LoadingFragment
import im.vector.novaChat.features.home.room.breadcrumbs.BreadcrumbsFragment
import im.vector.novaChat.features.home.room.detail.RoomDetailFragment
import im.vector.novaChat.features.home.room.list.RoomListFragment
import im.vector.novaChat.features.login.LoginCaptchaFragment
import im.vector.novaChat.features.login.LoginFragment
import im.vector.novaChat.features.login.LoginGenericTextInputFormFragment
import im.vector.novaChat.features.login.LoginResetPasswordFragment
import im.vector.novaChat.features.login.LoginResetPasswordMailConfirmationFragment
import im.vector.novaChat.features.login.LoginResetPasswordSuccessFragment
import im.vector.novaChat.features.login.LoginServerSelectionFragment
import im.vector.novaChat.features.login.LoginServerUrlFormFragment
import im.vector.novaChat.features.login.LoginSignUpSignInSelectionFragment
import im.vector.novaChat.features.login.LoginSplashFragment
import im.vector.novaChat.features.login.LoginWaitForEmailFragment
import im.vector.novaChat.features.login.LoginWebFragment
import im.vector.novaChat.features.login.terms.LoginTermsFragment
import im.vector.novaChat.features.qrcode.QrCodeScannerFragment
import im.vector.novaChat.features.reactions.EmojiChooserFragment
import im.vector.novaChat.features.reactions.EmojiSearchResultFragment
import im.vector.novaChat.features.roomdirectory.PublicRoomsFragment
import im.vector.novaChat.features.roomdirectory.createroom.CreateRoomFragment
import im.vector.novaChat.features.roomdirectory.picker.RoomDirectoryPickerFragment
import im.vector.novaChat.features.roomdirectory.roompreview.RoomPreviewNoPreviewFragment
import im.vector.novaChat.features.roommemberprofile.RoomMemberProfileFragment
import im.vector.novaChat.features.roommemberprofile.devices.DeviceListFragment
import im.vector.novaChat.features.roommemberprofile.devices.DeviceTrustInfoActionFragment
import im.vector.novaChat.features.roomprofile.RoomProfileFragment
import im.vector.novaChat.features.roomprofile.members.RoomMemberListFragment
import im.vector.novaChat.features.roomprofile.settings.RoomSettingsFragment
import im.vector.novaChat.features.settings.VectorSettingsAdvancedNotificationPreferenceFragment
import im.vector.novaChat.features.settings.VectorSettingsHelpAboutFragment
import im.vector.novaChat.features.settings.VectorSettingsLabsFragment
import im.vector.novaChat.features.settings.VectorSettingsNotificationPreferenceFragment
import im.vector.novaChat.features.settings.VectorSettingsNotificationsTroubleshootFragment
import im.vector.novaChat.features.settings.VectorSettingsPreferencesFragment
import im.vector.novaChat.features.settings.VectorSettingsSecurityPrivacyFragment
import im.vector.novaChat.features.settings.account.deactivation.DeactivateAccountFragment
import im.vector.novaChat.features.settings.crosssigning.CrossSigningSettingsFragment
import im.vector.novaChat.features.settings.devices.VectorSettingsDevicesFragment
import im.vector.novaChat.features.settings.devtools.AccountDataFragment
import im.vector.novaChat.features.settings.devtools.GossipingEventsPaperTrailFragment
import im.vector.novaChat.features.settings.devtools.IncomingKeyRequestListFragment
import im.vector.novaChat.features.settings.devtools.KeyRequestsFragment
import im.vector.novaChat.features.settings.devtools.OutgoingKeyRequestListFragment
import im.vector.novaChat.features.settings.ignored.VectorSettingsIgnoredUsersFragment
import im.vector.novaChat.features.settings.push.PushGatewaysFragment
import im.vector.novaChat.features.share.IncomingShareFragment
import im.vector.novaChat.features.signout.soft.SoftLogoutFragment

@Module
interface FragmentModule {
    /**
     * Fragments with @IntoMap will be injected by this factory
     */
    @Binds
    fun bindFragmentFactory(factory: VectorFragmentFactory): FragmentFactory

    @Binds
    @IntoMap
    @FragmentKey(RoomListFragment::class)
    fun bindRoomListFragment(fragment: RoomListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(GroupListFragment::class)
    fun bindGroupListFragment(fragment: GroupListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomDetailFragment::class)
    fun bindRoomDetailFragment(fragment: RoomDetailFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomDirectoryPickerFragment::class)
    fun bindRoomDirectoryPickerFragment(fragment: RoomDirectoryPickerFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(CreateRoomFragment::class)
    fun bindCreateRoomFragment(fragment: CreateRoomFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomPreviewNoPreviewFragment::class)
    fun bindRoomPreviewNoPreviewFragment(fragment: RoomPreviewNoPreviewFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(KeysBackupSettingsFragment::class)
    fun bindKeysBackupSettingsFragment(fragment: KeysBackupSettingsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoadingFragment::class)
    fun bindLoadingFragment(fragment: LoadingFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(HomeDrawerFragment::class)
    fun bindHomeDrawerFragment(fragment: HomeDrawerFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(HomeDetailFragment::class)
    fun bindHomeDetailFragment(fragment: HomeDetailFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(EmojiSearchResultFragment::class)
    fun bindEmojiSearchResultFragment(fragment: EmojiSearchResultFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginFragment::class)
    fun bindLoginFragment(fragment: LoginFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginCaptchaFragment::class)
    fun bindLoginCaptchaFragment(fragment: LoginCaptchaFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginTermsFragment::class)
    fun bindLoginTermsFragment(fragment: LoginTermsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginServerUrlFormFragment::class)
    fun bindLoginServerUrlFormFragment(fragment: LoginServerUrlFormFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginResetPasswordMailConfirmationFragment::class)
    fun bindLoginResetPasswordMailConfirmationFragment(fragment: LoginResetPasswordMailConfirmationFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginResetPasswordFragment::class)
    fun bindLoginResetPasswordFragment(fragment: LoginResetPasswordFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginResetPasswordSuccessFragment::class)
    fun bindLoginResetPasswordSuccessFragment(fragment: LoginResetPasswordSuccessFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginServerSelectionFragment::class)
    fun bindLoginServerSelectionFragment(fragment: LoginServerSelectionFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginSignUpSignInSelectionFragment::class)
    fun bindLoginSignUpSignInSelectionFragment(fragment: LoginSignUpSignInSelectionFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginSplashFragment::class)
    fun bindLoginSplashFragment(fragment: LoginSplashFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginWebFragment::class)
    fun bindLoginWebFragment(fragment: LoginWebFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginGenericTextInputFormFragment::class)
    fun bindLoginGenericTextInputFormFragment(fragment: LoginGenericTextInputFormFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginWaitForEmailFragment::class)
    fun bindLoginWaitForEmailFragment(fragment: LoginWaitForEmailFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(CreateDirectRoomDirectoryUsersFragment::class)
    fun bindCreateDirectRoomDirectoryUsersFragment(fragment: CreateDirectRoomDirectoryUsersFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(CreateDirectRoomKnownUsersFragment::class)
    fun bindCreateDirectRoomKnownUsersFragment(fragment: CreateDirectRoomKnownUsersFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(PushGatewaysFragment::class)
    fun bindPushGatewaysFragment(fragment: PushGatewaysFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsNotificationsTroubleshootFragment::class)
    fun bindVectorSettingsNotificationsTroubleshootFragment(fragment: VectorSettingsNotificationsTroubleshootFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsAdvancedNotificationPreferenceFragment::class)
    fun bindVectorSettingsAdvancedNotificationPreferenceFragment(fragment: VectorSettingsAdvancedNotificationPreferenceFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsNotificationPreferenceFragment::class)
    fun bindVectorSettingsNotificationPreferenceFragment(fragment: VectorSettingsNotificationPreferenceFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsLabsFragment::class)
    fun bindVectorSettingsLabsFragment(fragment: VectorSettingsLabsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsPreferencesFragment::class)
    fun bindVectorSettingsPreferencesFragment(fragment: VectorSettingsPreferencesFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsSecurityPrivacyFragment::class)
    fun bindVectorSettingsSecurityPrivacyFragment(fragment: VectorSettingsSecurityPrivacyFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsHelpAboutFragment::class)
    fun bindVectorSettingsHelpAboutFragment(fragment: VectorSettingsHelpAboutFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsIgnoredUsersFragment::class)
    fun bindVectorSettingsIgnoredUsersFragment(fragment: VectorSettingsIgnoredUsersFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsDevicesFragment::class)
    fun bindVectorSettingsDevicesFragment(fragment: VectorSettingsDevicesFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(PublicRoomsFragment::class)
    fun bindPublicRoomsFragment(fragment: PublicRoomsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomProfileFragment::class)
    fun bindRoomProfileFragment(fragment: RoomProfileFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomMemberListFragment::class)
    fun bindRoomMemberListFragment(fragment: RoomMemberListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomSettingsFragment::class)
    fun bindRoomSettingsFragment(fragment: RoomSettingsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomMemberProfileFragment::class)
    fun bindRoomMemberProfileFragment(fragment: RoomMemberProfileFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BreadcrumbsFragment::class)
    fun bindBreadcrumbsFragment(fragment: BreadcrumbsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(EmojiChooserFragment::class)
    fun bindEmojiChooserFragment(fragment: EmojiChooserFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SoftLogoutFragment::class)
    fun bindSoftLogoutFragment(fragment: SoftLogoutFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationRequestFragment::class)
    fun bindVerificationRequestFragment(fragment: VerificationRequestFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationChooseMethodFragment::class)
    fun bindVerificationChooseMethodFragment(fragment: VerificationChooseMethodFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationEmojiCodeFragment::class)
    fun bindVerificationEmojiCodeFragment(fragment: VerificationEmojiCodeFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationQrScannedByOtherFragment::class)
    fun bindVerificationQrScannedByOtherFragment(fragment: VerificationQrScannedByOtherFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationQRWaitingFragment::class)
    fun bindVerificationQRWaitingFragment(fragment: VerificationQRWaitingFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationConclusionFragment::class)
    fun bindVerificationConclusionFragment(fragment: VerificationConclusionFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationCancelFragment::class)
    fun bindVerificationCancelFragment(fragment: VerificationCancelFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationNotMeFragment::class)
    fun bindVerificationNotMeFragment(fragment: VerificationNotMeFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(QrCodeScannerFragment::class)
    fun bindQrCodeScannerFragment(fragment: QrCodeScannerFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(DeviceListFragment::class)
    fun bindDeviceListFragment(fragment: DeviceListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(DeviceTrustInfoActionFragment::class)
    fun bindDeviceTrustInfoActionFragment(fragment: DeviceTrustInfoActionFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(CrossSigningSettingsFragment::class)
    fun bindCrossSigningSettingsFragment(fragment: CrossSigningSettingsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(AttachmentsPreviewFragment::class)
    fun bindAttachmentsPreviewFragment(fragment: AttachmentsPreviewFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(IncomingShareFragment::class)
    fun bindIncomingShareFragment(fragment: IncomingShareFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(AccountDataFragment::class)
    fun bindAccountDataFragment(fragment: AccountDataFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(OutgoingKeyRequestListFragment::class)
    fun bindOutgoingKeyRequestListFragment(fragment: OutgoingKeyRequestListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(IncomingKeyRequestListFragment::class)
    fun bindIncomingKeyRequestListFragment(fragment: IncomingKeyRequestListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(KeyRequestsFragment::class)
    fun bindKeyRequestsFragment(fragment: KeyRequestsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(GossipingEventsPaperTrailFragment::class)
    fun bindGossipingEventsPaperTrailFragment(fragment: GossipingEventsPaperTrailFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BootstrapEnterPassphraseFragment::class)
    fun bindBootstrapEnterPassphraseFragment(fragment: BootstrapEnterPassphraseFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BootstrapConfirmPassphraseFragment::class)
    fun bindBootstrapConfirmPassphraseFragment(fragment: BootstrapConfirmPassphraseFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BootstrapWaitingFragment::class)
    fun bindBootstrapWaitingFragment(fragment: BootstrapWaitingFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BootstrapSaveRecoveryKeyFragment::class)
    fun bindBootstrapSaveRecoveryKeyFragment(fragment: BootstrapSaveRecoveryKeyFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BootstrapConclusionFragment::class)
    fun bindBootstrapConclusionFragment(fragment: BootstrapConclusionFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BootstrapAccountPasswordFragment::class)
    fun bindBootstrapAccountPasswordFragment(fragment: BootstrapAccountPasswordFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BootstrapMigrateBackupFragment::class)
    fun bindBootstrapMigrateBackupFragment(fragment: BootstrapMigrateBackupFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(DeactivateAccountFragment::class)
    fun bindDeactivateAccountFragment(fragment: DeactivateAccountFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SharedSecuredStoragePassphraseFragment::class)
    fun bindSharedSecuredStoragePassphraseFragment(fragment: SharedSecuredStoragePassphraseFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SharedSecuredStorageKeyFragment::class)
    fun bindSharedSecuredStorageKeyFragment(fragment: SharedSecuredStorageKeyFragment): Fragment
}
