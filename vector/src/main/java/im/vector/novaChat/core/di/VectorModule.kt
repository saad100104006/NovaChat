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
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.res.Resources
import dagger.Binds
import dagger.Module
import dagger.Provides
import im.vector.matrix.android.api.Matrix
import im.vector.matrix.android.api.auth.AuthenticationService
import im.vector.matrix.android.api.session.Session
import im.vector.novaChat.core.error.DefaultErrorFormatter
import im.vector.novaChat.core.error.ErrorFormatter
import im.vector.novaChat.features.navigation.DefaultNavigator
import im.vector.novaChat.features.navigation.Navigator
import im.vector.novaChat.features.ui.SharedPreferencesUiStateRepository
import im.vector.novaChat.features.ui.UiStateRepository

@Module
abstract class VectorModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun providesResources(context: Context): Resources {
            return context.resources
        }

        @Provides
        @JvmStatic
        fun providesSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences("im.vector.riot", MODE_PRIVATE)
        }

        @Provides
        @JvmStatic
        fun providesMatrix(context: Context): Matrix {
            return Matrix.getInstance(context)
        }

        @Provides
        @JvmStatic
        fun providesCurrentSession(activeSessionHolder: ActiveSessionHolder): Session {
            // TODO: handle session injection better
            return activeSessionHolder.getActiveSession()
        }

        @Provides
        @JvmStatic
        fun providesAuthenticationService(matrix: Matrix): AuthenticationService {
            return matrix.authenticationService()
        }
    }

    @Binds
    abstract fun bindNavigator(navigator: DefaultNavigator): Navigator

    @Binds
    abstract fun bindErrorFormatter(errorFormatter: DefaultErrorFormatter): ErrorFormatter

    @Binds
    abstract fun bindUiStateRepository(uiStateRepository: SharedPreferencesUiStateRepository): UiStateRepository
}
