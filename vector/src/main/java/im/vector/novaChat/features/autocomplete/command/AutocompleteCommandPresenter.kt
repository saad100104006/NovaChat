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

package im.vector.novaChat.features.autocomplete.command

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import im.vector.novaChat.features.autocomplete.EpoxyAutocompletePresenter
import im.vector.novaChat.features.command.Command
import javax.inject.Inject

class AutocompleteCommandPresenter @Inject constructor(context: Context,
                                                       private val controller: AutocompleteCommandController) :
        EpoxyAutocompletePresenter<Command>(context) {

    init {
        controller.listener = this
    }

    override fun providesController(): EpoxyController {
        return controller
    }

    override fun onQuery(query: CharSequence?) {
        val data = Command.values().filter {
            if (query.isNullOrEmpty()) {
                true
            } else {
                it.command.startsWith(query, 1, true)
            }
        }
        controller.setData(data)
    }
}