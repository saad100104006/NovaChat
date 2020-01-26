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

package im.vector.novaChat.core.di

import arrow.core.Option
import im.vector.matrix.android.api.auth.AuthenticationService
import im.vector.matrix.android.api.session.Session
import im.vector.novaChat.ActiveSessionDataSource
import im.vector.novaChat.features.crypto.keysrequest.KeyRequestHandler
import im.vector.novaChat.features.crypto.verification.IncomingVerificationRequestHandler
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveSessionHolder @Inject constructor(private val authenticationService: AuthenticationService,
                                              private val sessionObservableStore: ActiveSessionDataSource,
                                              private val keyRequestHandler: KeyRequestHandler,
                                              private val incomingVerificationRequestHandler: IncomingVerificationRequestHandler
) {

    private var activeSession: AtomicReference<Session?> = AtomicReference()

    fun setActiveSession(session: Session) {
        activeSession.set(session)
        sessionObservableStore.post(Option.just(session))
        keyRequestHandler.start(session)
        incomingVerificationRequestHandler.start(session)
    }

    fun clearActiveSession() {
        activeSession.set(null)
        sessionObservableStore.post(Option.empty())
        keyRequestHandler.stop()
        incomingVerificationRequestHandler.stop()
    }

    fun hasActiveSession(): Boolean {
        return activeSession.get() != null
    }

    fun getSafeActiveSession(): Session? {
        return activeSession.get()
    }

    fun getActiveSession(): Session {
        return activeSession.get()
                ?: throw IllegalStateException("You should authenticate before using this")
    }

    // TODO: Stop sync ?
//    fun switchToSession(sessionParams: SessionParams) {
//        val newActiveSession = authenticationService.getSession(sessionParams)
//        activeSession.set(newActiveSession)
//    }
}
