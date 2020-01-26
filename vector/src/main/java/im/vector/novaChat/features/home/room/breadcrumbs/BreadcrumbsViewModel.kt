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

package im.vector.novaChat.features.home.room.breadcrumbs

import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import im.vector.matrix.android.api.session.Session
import im.vector.matrix.rx.rx
import im.vector.novaChat.core.platform.EmptyAction
import im.vector.novaChat.core.platform.VectorViewModel
import io.reactivex.schedulers.Schedulers

class BreadcrumbsViewModel @AssistedInject constructor(@Assisted initialState: BreadcrumbsViewState,
                                                       private val session: Session)
    : VectorViewModel<BreadcrumbsViewState, EmptyAction>(initialState) {

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: BreadcrumbsViewState): BreadcrumbsViewModel
    }

    companion object : MvRxViewModelFactory<BreadcrumbsViewModel, BreadcrumbsViewState> {

        @JvmStatic
        override fun create(viewModelContext: ViewModelContext, state: BreadcrumbsViewState): BreadcrumbsViewModel? {
            val fragment: BreadcrumbsFragment = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.breadcrumbsViewModelFactory.create(state)
        }
    }

    init {
        observeBreadcrumbs()
    }

    override fun handle(action: EmptyAction) {
        // No op
    }

    // PRIVATE METHODS *****************************************************************************

    private fun observeBreadcrumbs() {
        session.rx()
                .liveBreadcrumbs()
                .observeOn(Schedulers.computation())
                .execute { asyncBreadcrumbs ->
                    copy(asyncBreadcrumbs = asyncBreadcrumbs)
                }
    }
}
