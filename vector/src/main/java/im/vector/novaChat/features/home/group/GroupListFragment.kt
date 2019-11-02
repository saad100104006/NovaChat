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

package im.vector.novaChat.features.home.group

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.airbnb.mvrx.Incomplete
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.fragmentViewModel
import im.vector.matrix.android.api.session.group.model.GroupSummary
import im.vector.novaChat.R
import im.vector.novaChat.core.di.ScreenComponent
import im.vector.novaChat.core.extensions.observeEvent
import im.vector.novaChat.core.platform.StateView
import im.vector.novaChat.core.platform.VectorBaseFragment
import im.vector.novaChat.features.home.HomeActivity
import im.vector.novaChat.features.home.HomeNavigationViewModel
import kotlinx.android.synthetic.main.fragment_group_list.*
import javax.inject.Inject

class GroupListFragment : VectorBaseFragment(), GroupSummaryController.Callback {

    companion object {
        fun newInstance(): GroupListFragment {
            return GroupListFragment()
        }
    }

    private lateinit var navigationViewModel: HomeNavigationViewModel
    private val viewModel: GroupListViewModel by fragmentViewModel()

    @Inject lateinit var groupListViewModelFactory: GroupListViewModel.Factory
    @Inject lateinit var groupController: GroupSummaryController

    override fun getLayoutResId() = R.layout.fragment_group_list

    override fun injectWith(injector: ScreenComponent) {
        injector.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navigationViewModel = ViewModelProviders.of(requireActivity()).get(HomeNavigationViewModel::class.java)
        groupController.callback = this
        stateView.contentView = groupListEpoxyRecyclerView
        groupListEpoxyRecyclerView.setController(groupController)
        viewModel.subscribe { renderState(it) }
        viewModel.openGroupLiveData.observeEvent(this) {
            navigationViewModel.goTo(HomeActivity.Navigation.OpenGroup)
        }
    }

    private fun renderState(state: GroupListViewState) {
        when (state.asyncGroups) {
            is Incomplete -> stateView.state = StateView.State.Loading
            is Success    -> stateView.state = StateView.State.Content
        }
        groupController.setData(state)
    }

    override fun onGroupSelected(groupSummary: GroupSummary) {
        viewModel.accept(GroupListActions.SelectGroup(groupSummary))
    }

}