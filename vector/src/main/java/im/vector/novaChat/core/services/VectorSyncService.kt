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
package im.vector.novaChat.core.services

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import im.vector.matrix.android.internal.session.sync.job.SyncService
import im.vector.novaChat.R
import im.vector.novaChat.core.extensions.vectorComponent
import im.vector.novaChat.features.notifications.NotificationUtils

class VectorSyncService : SyncService() {

    companion object {

        fun newIntent(context: Context, sessionId: String): Intent {
            return Intent(context, VectorSyncService::class.java).also {
                it.putExtra(EXTRA_SESSION_ID, sessionId)
            }
        }
    }

    private lateinit var notificationUtils: NotificationUtils

    override fun onCreate() {
        super.onCreate()
        notificationUtils = vectorComponent().notificationUtils()
    }

    override fun onStart(isInitialSync: Boolean) {
        val notificationSubtitleRes = if (isInitialSync) {
            R.string.notification_initial_sync
        } else {
            R.string.notification_listening_for_events
        }
        val notification = notificationUtils.buildForegroundServiceNotification(notificationSubtitleRes, false)
        startForeground(NotificationUtils.NOTIFICATION_ID_FOREGROUND_SERVICE, notification)
    }

    override fun onRescheduleAsked(sessionId: String, isInitialSync: Boolean, delay: Long) {
        reschedule(sessionId, delay)
    }

    override fun onDestroy() {
        removeForegroundNotification()
        super.onDestroy()
    }

    private fun removeForegroundNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NotificationUtils.NOTIFICATION_ID_FOREGROUND_SERVICE)
    }

    private fun reschedule(sessionId: String, delay: Long) {
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 0, newIntent(this, sessionId), 0)
        } else {
            PendingIntent.getService(this, 0, newIntent(this, sessionId), 0)
        }
        val firstMillis = System.currentTimeMillis() + delay
        val alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, firstMillis, pendingIntent)
        } else {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, firstMillis, pendingIntent)
        }
    }
}
