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

package im.vector.novaChat.features.attachments

import im.vector.matrix.android.api.session.content.ContentAttachmentData
import im.vector.riotx.multipicker.entity.MultiPickerAudioType
import im.vector.riotx.multipicker.entity.MultiPickerBaseType
import im.vector.riotx.multipicker.entity.MultiPickerContactType
import im.vector.riotx.multipicker.entity.MultiPickerFileType
import im.vector.riotx.multipicker.entity.MultiPickerImageType
import im.vector.riotx.multipicker.entity.MultiPickerVideoType
import timber.log.Timber

fun MultiPickerContactType.toContactAttachment(): ContactAttachment {
    return ContactAttachment(
            displayName = displayName,
            photoUri = photoUri,
            emails = emailList.toList(),
            phones = phoneNumberList.toList()
    )
}

fun MultiPickerFileType.toContentAttachmentData(): ContentAttachmentData {
    if (mimeType == null) Timber.w("No mimeType")
    return ContentAttachmentData(
            mimeType = mimeType,
            type = mapType(),
            size = size,
            name = displayName,
            queryUri = contentUri
    )
}

fun MultiPickerAudioType.toContentAttachmentData(): ContentAttachmentData {
    if (mimeType == null) Timber.w("No mimeType")
    return ContentAttachmentData(
            mimeType = mimeType,
            type = mapType(),
            size = size,
            name = displayName,
            duration = duration,
            queryUri = contentUri
    )
}

private fun MultiPickerBaseType.mapType(): ContentAttachmentData.Type {
    return when {
        mimeType?.startsWith("image/") == true -> ContentAttachmentData.Type.IMAGE
        mimeType?.startsWith("video/") == true -> ContentAttachmentData.Type.VIDEO
        mimeType?.startsWith("audio/") == true -> ContentAttachmentData.Type.AUDIO
        else                                   -> ContentAttachmentData.Type.FILE
    }
}

fun MultiPickerImageType.toContentAttachmentData(): ContentAttachmentData {
    if (mimeType == null) Timber.w("No mimeType")
    return ContentAttachmentData(
            mimeType = mimeType,
            type = mapType(),
            name = displayName,
            size = size,
            height = height.toLong(),
            width = width.toLong(),
            exifOrientation = orientation,
            queryUri = contentUri
    )
}

fun MultiPickerVideoType.toContentAttachmentData(): ContentAttachmentData {
    if (mimeType == null) Timber.w("No mimeType")
    return ContentAttachmentData(
            mimeType = mimeType,
            type = ContentAttachmentData.Type.VIDEO,
            size = size,
            height = height.toLong(),
            width = width.toLong(),
            duration = duration,
            name = displayName,
            queryUri = contentUri
    )
}
