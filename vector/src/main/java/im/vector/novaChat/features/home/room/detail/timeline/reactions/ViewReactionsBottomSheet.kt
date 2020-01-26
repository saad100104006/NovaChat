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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import im.vector.novaChat.R
import im.vector.novaChat.core.di.ScreenComponent
import im.vector.novaChat.core.extensions.cleanup
import im.vector.novaChat.core.extensions.configureWith
import im.vector.novaChat.core.platform.VectorBaseBottomSheetDialogFragment
import im.vector.novaChat.features.home.room.detail.timeline.action.TimelineEventFragmentArgs
import im.vector.novaChat.features.home.room.detail.timeline.item.MessageInformationData
import kotlinx.android.synthetic.main.bottom_sheet_generic_list_with_title.*
import javax.inject.Inject

/**
 * Bottom sheet displaying list of reactions for a given event ordered by timestamp
 */
class ViewReactionsBottomSheet : VectorBaseBottomSheetDialogFragment() {

    private val viewModel: ViewReactionsViewModel by fragmentViewModel(ViewReactionsViewModel::class)

    @Inject lateinit var viewReactionsViewModelFactory: ViewReactionsViewModel.Factory

    @BindView(R.id.bottomSheetRecyclerView)
    lateinit var recyclerView: RecyclerView

    @Inject lateinit var epoxyController: ViewReactionsEpoxyController

    override fun injectWith(injector: ScreenComponent) {
        injector.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_generic_list_with_title, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerView.configureWith(epoxyController, hasFixedSize = false)
        bottomSheetTitle.text = context?.getString(R.string.reactions)
    }

    override fun onDestroyView() {
        recyclerView.cleanup()
        super.onDestroyView()
    }

    override fun invalidate() = withState(viewModel) {
        epoxyController.setData(it)
        super.invalidate()
    }

    companion object {
        fun newInstance(roomId: String, informationData: MessageInformationData): ViewReactionsBottomSheet {
            val args = Bundle()
            val parcelableArgs = TimelineEventFragmentArgs(
                    informationData.eventId,
                    roomId,
                    informationData
            )
            args.putParcelable(MvRx.KEY_ARG, parcelableArgs)
            return ViewReactionsBottomSheet().apply { arguments = args }
        }
    }
}
