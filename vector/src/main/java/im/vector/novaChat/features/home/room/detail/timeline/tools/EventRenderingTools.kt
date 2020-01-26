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

package im.vector.novaChat.features.home.room.detail.timeline.tools

import android.text.SpannableStringBuilder
import android.view.MotionEvent
import androidx.core.text.toSpannable
import im.vector.matrix.android.api.permalinks.MatrixLinkify
import im.vector.matrix.android.api.permalinks.MatrixPermalinkSpan
import im.vector.novaChat.core.linkify.VectorLinkify
import im.vector.novaChat.core.utils.isValidUrl
import im.vector.novaChat.features.home.room.detail.timeline.TimelineEventController
import im.vector.novaChat.features.html.PillImageSpan
import kotlinx.coroutines.*
import me.saket.bettermovementmethod.BetterLinkMovementMethod

fun CharSequence.findPillsAndProcess(scope: CoroutineScope, processBlock: (PillImageSpan) -> Unit) {
    scope.launch(Dispatchers.Main) {
        withContext(Dispatchers.IO) {
            toSpannable().let { spannable ->
                spannable.getSpans(0, spannable.length, PillImageSpan::class.java)
            }
        }.forEach { processBlock(it) }
    }
}

fun CharSequence.linkify(callback: TimelineEventController.UrlClickCallback?): CharSequence {
    val spannable = SpannableStringBuilder(this)
    MatrixLinkify.addLinks(spannable, object : MatrixPermalinkSpan.Callback {
        override fun onUrlClicked(url: String) {
            callback?.onUrlClicked(url)
        }
    })
    VectorLinkify.addLinks(spannable, true)
    return spannable
}

// Better link movement methods fixes the issue when
// long pressing to open the context menu on a TextView also triggers an autoLink click.
fun createLinkMovementMethod(urlClickCallback: TimelineEventController.UrlClickCallback?): BetterLinkMovementMethod {
    return BetterLinkMovementMethod.newInstance()
            .apply {
                setOnLinkClickListener { _, url ->
                    // Return false to let android manage the click on the link, or true if the link is handled by the application
                    url.isValidUrl() && urlClickCallback?.onUrlClicked(url) == true
                }

                // We need also to fix the case when long click on link will trigger long click on cell
                setOnLinkLongClickListener { tv, url ->
                    // Long clicks are handled by parent, return true to block android to do something with url
                    if (url.isValidUrl() && urlClickCallback?.onUrlLongClicked(url) == true) {
                        tv.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0f, 0f, 0))
                        true
                    } else {
                        false
                    }
                }
            }
}
