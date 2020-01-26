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

package im.vector.novaChat.core.pushers

import im.vector.matrix.android.api.MatrixCallback
import im.vector.novaChat.R
import im.vector.novaChat.core.di.ActiveSessionHolder
import im.vector.novaChat.core.resources.AppNameProvider
import im.vector.novaChat.core.resources.LocaleProvider
import im.vector.novaChat.core.resources.StringProvider
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

private const val DEFAULT_PUSHER_FILE_TAG = "mobile"

class PushersManager @Inject constructor(
        private val activeSessionHolder: ActiveSessionHolder,
        private val localeProvider: LocaleProvider,
        private val stringProvider: StringProvider,
        private val appNameProvider: AppNameProvider
) {

    fun registerPusherWithFcmKey(pushKey: String): UUID {
        val currentSession = activeSessionHolder.getActiveSession()
        val profileTag = DEFAULT_PUSHER_FILE_TAG + "_" + abs(currentSession.myUserId.hashCode())

        return currentSession.addHttpPusher(
                pushKey,
                stringProvider.getString(R.string.pusher_app_id),
                profileTag,
                localeProvider.current().language,
                appNameProvider.getAppName(),
                currentSession.sessionParams.credentials.deviceId ?: "MOBILE",
                stringProvider.getString(R.string.pusher_http_url),
                false,
                true
        )
    }

    fun unregisterPusher(pushKey: String, callback: MatrixCallback<Unit>) {
        val currentSession = activeSessionHolder.getSafeActiveSession() ?: return
        currentSession.removeHttpPusher(pushKey, stringProvider.getString(R.string.pusher_app_id), callback)
    }
}
