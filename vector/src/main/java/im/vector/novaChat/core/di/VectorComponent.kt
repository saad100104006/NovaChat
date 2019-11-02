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

import android.content.Context
import android.content.res.Resources
import dagger.BindsInstance
import dagger.Component
import im.vector.matrix.android.api.Matrix
import im.vector.matrix.android.api.auth.Authenticator
import im.vector.matrix.android.api.session.Session
import im.vector.novaChat.EmojiCompatFontProvider
import im.vector.novaChat.EmojiCompatWrapper
import im.vector.novaChat.VectorApplication
import im.vector.novaChat.core.pushers.PushersManager
import im.vector.novaChat.core.utils.DimensionConverter
import im.vector.novaChat.features.configuration.VectorConfiguration
import im.vector.novaChat.features.crypto.keysrequest.KeyRequestHandler
import im.vector.novaChat.features.crypto.verification.IncomingVerificationRequestHandler
import im.vector.novaChat.features.home.AvatarRenderer
import im.vector.novaChat.features.home.HomeRoomListObservableStore
import im.vector.novaChat.features.home.group.SelectedGroupStore
import im.vector.novaChat.features.html.EventHtmlRenderer
import im.vector.novaChat.features.navigation.Navigator
import im.vector.novaChat.features.notifications.*
import im.vector.novaChat.features.rageshake.BugReporter
import im.vector.novaChat.features.rageshake.VectorFileLogger
import im.vector.novaChat.features.rageshake.VectorUncaughtExceptionHandler
import im.vector.novaChat.features.settings.VectorPreferences
import im.vector.novaChat.features.ui.UiStateRepository
import javax.inject.Singleton

@Component(modules = [VectorModule::class])
@Singleton
interface VectorComponent {

    fun inject(notificationBroadcastReceiver: NotificationBroadcastReceiver)

    fun inject(vectorApplication: VectorApplication)

    fun matrix(): Matrix

    fun currentSession(): Session

    fun notificationUtils(): NotificationUtils

    fun notificationDrawerManager(): NotificationDrawerManager

    fun appContext(): Context

    fun resources(): Resources

    fun dimensionConverter(): DimensionConverter

    fun vectorConfiguration(): VectorConfiguration

    fun avatarRenderer(): AvatarRenderer

    fun activeSessionHolder(): ActiveSessionHolder

    fun emojiCompatFontProvider(): EmojiCompatFontProvider

    fun emojiCompatWrapper(): EmojiCompatWrapper

    fun eventHtmlRenderer(): EventHtmlRenderer

    fun navigator(): Navigator

    fun homeRoomListObservableStore(): HomeRoomListObservableStore

    fun selectedGroupStore(): SelectedGroupStore

    fun incomingVerificationRequestHandler(): IncomingVerificationRequestHandler

    fun incomingKeyRequestHandler(): KeyRequestHandler

    fun authenticator(): Authenticator

    fun bugReporter(): BugReporter

    fun vectorUncaughtExceptionHandler(): VectorUncaughtExceptionHandler

    fun pushRuleTriggerListener(): PushRuleTriggerListener

    fun pusherManager(): PushersManager

    fun notifiableEventResolver(): NotifiableEventResolver

    fun vectorPreferences(): VectorPreferences

    fun vectorFileLogger(): VectorFileLogger

    fun uiStateRepository(): UiStateRepository

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): VectorComponent
    }

}