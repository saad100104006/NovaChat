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

package im.vector.novaChat.features.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.airbnb.mvrx.viewModel
import im.vector.novaChat.R
import im.vector.novaChat.core.di.ActiveSessionHolder
import im.vector.novaChat.core.di.ScreenComponent
import im.vector.novaChat.core.extensions.hideKeyboard
import im.vector.novaChat.core.extensions.observeEvent
import im.vector.novaChat.core.extensions.replaceFragment
import im.vector.novaChat.core.platform.ToolbarConfigurable
import im.vector.novaChat.core.platform.VectorBaseActivity
import im.vector.novaChat.core.pushers.PushersManager
import im.vector.novaChat.features.disclaimer.showDisclaimerDialog
import im.vector.novaChat.features.notifications.NotificationDrawerManager
import im.vector.novaChat.features.rageshake.VectorUncaughtExceptionHandler
import im.vector.novaChat.features.workers.signout.SignOutViewModel
import im.vector.novaChat.push.fcm.FcmHelper
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home_detail.*
import kotlinx.android.synthetic.main.merge_overlay_waiting_view.*
import timber.log.Timber
import javax.inject.Inject


class HomeActivity : VectorBaseActivity(), ToolbarConfigurable {

    // Supported navigation actions for this Activity
    sealed class Navigation {
        object OpenDrawer : Navigation()
        object OpenGroup : Navigation()
    }

    private val homeActivityViewModel: HomeActivityViewModel by viewModel()
    private lateinit var navigationViewModel: HomeNavigationViewModel

    @Inject lateinit var activeSessionHolder: ActiveSessionHolder
    @Inject lateinit var homeActivityViewModelFactory: HomeActivityViewModel.Factory
    @Inject lateinit var vectorUncaughtExceptionHandler: VectorUncaughtExceptionHandler
    @Inject lateinit var pushManager: PushersManager
    @Inject lateinit var notificationDrawerManager: NotificationDrawerManager

    private val drawerListener = object : DrawerLayout.SimpleDrawerListener() {
        override fun onDrawerStateChanged(newState: Int) {
            hideKeyboard()
        }
    }

    override fun getLayoutRes() = R.layout.activity_home

    override fun injectWith(injector: ScreenComponent) {
        injector.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FcmHelper.ensureFcmTokenIsRetrieved(this, pushManager)
        navigationViewModel = ViewModelProviders.of(this).get(HomeNavigationViewModel::class.java)
        drawerLayout.addDrawerListener(drawerListener)
        if (isFirstCreation()) {
            val homeDrawerFragment = HomeDrawerFragment.newInstance()
            val loadingDetail = LoadingFragment.newInstance()
            replaceFragment(loadingDetail, R.id.homeDetailFragmentContainer)
            replaceFragment(homeDrawerFragment, R.id.homeDrawerFragmentContainer)
        }

        navigationViewModel.navigateTo.observeEvent(this) { navigation ->
            when (navigation) {
                is Navigation.OpenDrawer -> drawerLayout.openDrawer(GravityCompat.START)
                is Navigation.OpenGroup  -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val homeDetailFragment = HomeDetailFragment.newInstance()
                    replaceFragment(homeDetailFragment, R.id.homeDetailFragmentContainer)
                }
            }
        }

        if (intent.getBooleanExtra(EXTRA_CLEAR_EXISTING_NOTIFICATION, false)) {
            notificationDrawerManager.clearAllEvents()
            intent.removeExtra(EXTRA_CLEAR_EXISTING_NOTIFICATION)
        }

        activeSessionHolder.getSafeActiveSession()?.getInitialSyncProgressStatus()?.observe(this, Observer { status ->
            if (status == null) {
                waiting_view.isVisible = false
            } else {
                Timber.e("${getString(status.statusText)} ${status.percentProgress}")
                waiting_view.setOnClickListener {
                    //block interactions
                }
                waiting_view_status_horizontal_progress.apply {
                    isIndeterminate = false
                    max = 100
                    progress = status.percentProgress
                    isVisible = true
                }
                waiting_view_status_text.apply {
                    text = getString(status.statusText)
                    isVisible = true
                }
                waiting_view.isVisible = true
            }
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.hasExtra(EXTRA_CLEAR_EXISTING_NOTIFICATION) == true) {
            notificationDrawerManager.clearAllEvents()
            intent.removeExtra(EXTRA_CLEAR_EXISTING_NOTIFICATION)
        }
    }

    override fun onDestroy() {
        drawerLayout.removeDrawerListener(drawerListener)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        if (vectorUncaughtExceptionHandler.didAppCrash(this)) {
            vectorUncaughtExceptionHandler.clearAppCrashStatus(this)

            AlertDialog.Builder(this)
                    .setMessage(R.string.send_bug_report_app_crashed)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes) { _, _ -> bugReporter.openBugReportScreen(this) }
                    .setNegativeButton(R.string.no) { _, _ -> bugReporter.deleteCrashFile(this) }
                    .show()
        } else {
            showDisclaimerDialog(this)
        }

        //Force remote backup state update to update the banner if needed
        ViewModelProviders.of(this).get(SignOutViewModel::class.java).refreshRemoteStateIfNeeded()
    }

    override fun configure(toolbar: Toolbar) {
        configureToolbar(toolbar, false)
    }

    override fun getMenuRes() = R.menu.home

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_home_suggestion -> {
                bugReporter.openBugReportScreen(this, true)
                return true
            }
            R.id.menu_home_report_bug -> {
                bugReporter.openBugReportScreen(this, false)
                return true
            }
            R.id.menu_home_filter     -> {
                navigator.openRoomsFiltering(this)
                return true
            }
        }

        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if(drawer.visibility ==View.VISIBLE){
            drawer.visibility =View.GONE
        } else {
            super.onBackPressed()
        }
    }


    companion object {
        private const val EXTRA_CLEAR_EXISTING_NOTIFICATION = "EXTRA_CLEAR_EXISTING_NOTIFICATION"

        fun newIntent(context: Context, clearNotification: Boolean = false): Intent {
            return Intent(context, HomeActivity::class.java)
                    .apply {
                        putExtra(EXTRA_CLEAR_EXISTING_NOTIFICATION, clearNotification)
                    }
        }
    }

}