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
package im.vector.novaChat.features.home.room.detail.timeline.action

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import butterknife.BindView
import butterknife.ButterKnife
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import im.vector.novaChat.R
import im.vector.novaChat.core.di.ScreenComponent
import im.vector.novaChat.features.home.room.detail.timeline.item.MessageInformationData
import im.vector.novaChat.features.html.EventHtmlRenderer
import kotlinx.android.synthetic.main.bottom_sheet_epoxylist_with_title.*
import javax.inject.Inject


/**
 * Bottom sheet displaying list of edits for a given event ordered by timestamp
 */
class ViewEditHistoryBottomSheet : VectorBaseBottomSheetDialogFragment() {

    private val viewModel: ViewEditHistoryViewModel by fragmentViewModel(ViewEditHistoryViewModel::class)

    @Inject lateinit var viewEditHistoryViewModelFactory: ViewEditHistoryViewModel.Factory
    @Inject lateinit var eventHtmlRenderer: EventHtmlRenderer

    @BindView(R.id.bottom_sheet_display_reactions_list)
    lateinit var epoxyRecyclerView: EpoxyRecyclerView

    private val epoxyController by lazy {
        ViewEditHistoryEpoxyController(requireContext(), viewModel.dateFormatter, eventHtmlRenderer)
    }

    override fun injectWith(screenComponent: ScreenComponent) {
        screenComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_epoxylist_with_title, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        epoxyRecyclerView.setController(epoxyController)
        val dividerItemDecoration = DividerItemDecoration(epoxyRecyclerView.context,
                LinearLayout.VERTICAL)
        epoxyRecyclerView.addItemDecoration(dividerItemDecoration)
        bottomSheetTitle.text = context?.getString(R.string.message_edits)
    }


    override fun invalidate() = withState(viewModel) {
        epoxyController.setData(it)
    }

    companion object {
        fun newInstance(roomId: String, informationData: MessageInformationData): ViewEditHistoryBottomSheet {
            val args = Bundle()
            val parcelableArgs = TimelineEventFragmentArgs(
                    informationData.eventId,
                    roomId,
                    informationData
            )
            args.putParcelable(MvRx.KEY_ARG, parcelableArgs)
            return ViewEditHistoryBottomSheet().apply { arguments = args }

        }
    }
}

