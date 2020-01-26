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

package im.vector.novaChat.features.autocomplete.group

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.otaliastudios.autocomplete.RecyclerViewPresenter
import im.vector.matrix.android.api.query.QueryStringValue
import im.vector.matrix.android.api.session.Session
import im.vector.matrix.android.api.session.group.groupSummaryQueryParams
import im.vector.matrix.android.api.session.group.model.GroupSummary
import im.vector.novaChat.features.autocomplete.AutocompleteClickListener
import javax.inject.Inject

class AutocompleteGroupPresenter @Inject constructor(context: Context,
                                                     private val controller: AutocompleteGroupController,
                                                     private val session: Session
) : RecyclerViewPresenter<GroupSummary>(context), AutocompleteClickListener<GroupSummary> {

    init {
        controller.listener = this
    }

    override fun instantiateAdapter(): RecyclerView.Adapter<*> {
        // Also remove animation
        recyclerView?.itemAnimator = null
        return controller.adapter
    }

    override fun onItemClick(t: GroupSummary) {
        dispatchClick(t)
    }

    override fun onQuery(query: CharSequence?) {
        val queryParams = groupSummaryQueryParams {
            displayName = if (query.isNullOrBlank()) {
                QueryStringValue.IsNotEmpty
            } else {
                QueryStringValue.Contains(query.toString(), QueryStringValue.Case.INSENSITIVE)
            }
        }
        val groups = session.getGroupSummaries(queryParams)
                .asSequence()
                .sortedBy { it.displayName }
        controller.setData(groups.toList())
    }
}
