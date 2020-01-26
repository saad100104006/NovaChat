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

package im.vector.novaChat.features.crypto.keys

import android.content.Context
import android.net.Uri
import im.vector.matrix.android.api.MatrixCallback
import im.vector.matrix.android.api.session.Session
import im.vector.matrix.android.internal.crypto.model.ImportRoomKeysResult
import im.vector.matrix.android.internal.extensions.foldToCallback
import im.vector.matrix.android.internal.util.awaitCallback
import im.vector.novaChat.core.intent.getMimeTypeFromUri
import im.vector.novaChat.core.resources.openResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class KeysImporter(private val session: Session) {

    /**
     * Import keys from provided Uri
     */
    fun import(context: Context,
               uri: Uri,
               mimetype: String?,
               password: String,
               callback: MatrixCallback<ImportRoomKeysResult>) {
        GlobalScope.launch(Dispatchers.Main) {
            runCatching {
                withContext(Dispatchers.IO) {
                    val resource = openResource(context, uri, mimetype ?: getMimeTypeFromUri(context, uri))

                    if (resource?.mContentStream == null) {
                        throw Exception("Error")
                    }

                    val data: ByteArray
                    try {
                        data = resource.mContentStream!!.use { it.readBytes() }
                    } catch (e: Exception) {
                        Timber.e(e, "## importKeys()")
                        throw e
                    }

                    awaitCallback<ImportRoomKeysResult> {
                        session.importRoomKeys(data, password, null, it)
                    }
                }
            }.foldToCallback(callback)
        }
    }
}
