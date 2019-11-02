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

package im.vector.novaChat.features.settings

import im.vector.novaChat.R
import im.vector.novaChat.core.extensions.withArgs

class VectorSettingsRootFragment : VectorSettingsBaseFragment() {

    override var titleRes: Int = R.string.title_activity_settings
    override val preferenceXmlRes = R.xml.vector_settings_root

    override fun bindPref() {
        // Nothing to do
    }

    companion object {
        fun newInstance() = VectorSettingsRootFragment()
                .withArgs {
                    //putString(ARG_MATRIX_ID, matrixId)
                }
    }

}