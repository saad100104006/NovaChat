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

package im.vector.novaChat.features.home.room.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import im.vector.novaChat.R
import im.vector.novaChat.core.extensions.replaceFragment
import im.vector.novaChat.core.platform.ToolbarConfigurable
import im.vector.novaChat.core.platform.VectorBaseActivity
import kotlinx.android.synthetic.main.merge_overlay_waiting_view.*

class RoomDetailActivity : VectorBaseActivity(), ToolbarConfigurable {

    override fun getLayoutRes(): Int {
        return R.layout.activity_room_detail
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        waitingView = waiting_view
        if (isFirstCreation()) {
            val roomDetailArgs: RoomDetailArgs = intent?.extras?.getParcelable(EXTRA_ROOM_DETAIL_ARGS)
                    ?: return
            val roomDetailFragment = RoomDetailFragment.newInstance(roomDetailArgs)
            replaceFragment(roomDetailFragment, R.id.roomDetailContainer)
        }

        setStatusBarTransparent()
    }

    override fun configure(toolbar: Toolbar) {
        configureToolbar(toolbar)
    }

    companion object {

        private const val EXTRA_ROOM_DETAIL_ARGS = "EXTRA_ROOM_DETAIL_ARGS"

        fun newIntent(context: Context, roomDetailArgs: RoomDetailArgs): Intent {
            return Intent(context, RoomDetailActivity::class.java).apply {
                putExtra(EXTRA_ROOM_DETAIL_ARGS, roomDetailArgs)
            }
        }


    }

    fun setStatusBarTransparent() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions
    }


}