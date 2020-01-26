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

package im.vector.novaChat.features.home.room.detail

import androidx.recyclerview.widget.LinearLayoutManager
import im.vector.novaChat.core.platform.DefaultListUpdateCallback
import im.vector.novaChat.features.home.room.detail.timeline.TimelineEventController
import timber.log.Timber

class ScrollOnNewMessageCallback(private val layoutManager: LinearLayoutManager,
                                 private val timelineEventController: TimelineEventController) : DefaultListUpdateCallback {

    override fun onInserted(position: Int, count: Int) {
        Timber.v("On inserted $count count at position: $position")
        if (position == 0 && layoutManager.findFirstVisibleItemPosition() == 0 && !timelineEventController.isLoadingForward()) {
            layoutManager.scrollToPosition(0)
        }
    }
}
