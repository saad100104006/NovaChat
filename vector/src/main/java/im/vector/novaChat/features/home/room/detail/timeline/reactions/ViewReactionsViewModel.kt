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

package im.vector.novaChat.features.home.room.detail.timeline.reactions

import com.airbnb.mvrx.*
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import im.vector.matrix.android.api.session.Session
import im.vector.matrix.android.api.session.room.model.ReactionAggregatedSummary
import im.vector.matrix.rx.RxRoom
import im.vector.matrix.rx.unwrap
import im.vector.novaChat.core.date.VectorDateFormatter
import im.vector.novaChat.core.platform.EmptyAction
import im.vector.novaChat.core.platform.VectorViewModel
import im.vector.novaChat.features.home.room.detail.timeline.action.TimelineEventFragmentArgs
import io.reactivex.Observable
import io.reactivex.Single

data class DisplayReactionsViewState(
        val eventId: String,
        val roomId: String,
        val mapReactionKeyToMemberList: Async<List<ReactionInfo>> = Uninitialized)
    : MvRxState {

    constructor(args: TimelineEventFragmentArgs) : this(roomId = args.roomId, eventId = args.eventId)
}

data class ReactionInfo(
        val eventId: String,
        val reactionKey: String,
        val authorId: String,
        val authorName: String? = null,
        val timestamp: String? = null
)

/**
 * Used to display the list of members that reacted to a given event
 */
class ViewReactionsViewModel @AssistedInject constructor(@Assisted
                                                         initialState: DisplayReactionsViewState,
                                                         private val session: Session,
                                                         private val dateFormatter: VectorDateFormatter
) : VectorViewModel<DisplayReactionsViewState, EmptyAction>(initialState) {

    private val roomId = initialState.roomId
    private val eventId = initialState.eventId
    private val room = session.getRoom(roomId)
            ?: throw IllegalStateException("Shouldn't use this ViewModel without a room")

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: DisplayReactionsViewState): ViewReactionsViewModel
    }

    companion object : MvRxViewModelFactory<ViewReactionsViewModel, DisplayReactionsViewState> {

        @JvmStatic
        override fun create(viewModelContext: ViewModelContext, state: DisplayReactionsViewState): ViewReactionsViewModel? {
            val fragment: ViewReactionsBottomSheet = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.viewReactionsViewModelFactory.create(state)
        }
    }

    init {
        observeEventAnnotationSummaries()
    }

    private fun observeEventAnnotationSummaries() {
        RxRoom(room)
                .liveAnnotationSummary(eventId)
                .unwrap()
                .flatMapSingle { summaries ->
                    Observable
                            .fromIterable(summaries.reactionsSummary)
                            // .filter { reactionAggregatedSummary -> isSingleEmoji(reactionAggregatedSummary.key) }
                            .toReactionInfoList()
                }
                .execute {
                    copy(mapReactionKeyToMemberList = it)
                }
    }

    private fun Observable<ReactionAggregatedSummary>.toReactionInfoList(): Single<List<ReactionInfo>> {
        return flatMap { summary ->
            Observable
                    .fromIterable(summary.sourceEvents)
                    .map {
                        val event = room.getTimeLineEvent(it)
                                ?: throw RuntimeException("Your eventId is not valid")
                        ReactionInfo(
                                event.root.eventId!!,
                                summary.key,
                                event.root.senderId ?: "",
                                event.getDisambiguatedDisplayName(),
                                dateFormatter.formatRelativeDateTime(event.root.originServerTs)

                        )
                    }
        }.toList()
    }

    override fun handle(action: EmptyAction) {
        // No op
    }
}
