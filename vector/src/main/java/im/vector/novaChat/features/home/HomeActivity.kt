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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import im.vector.matrix.android.api.MatrixCallback
import im.vector.matrix.android.api.session.Session
import im.vector.matrix.android.api.util.toMatrixItem
import im.vector.matrix.android.internal.crypto.model.CryptoDeviceInfo
import im.vector.matrix.android.internal.crypto.model.MXUsersDevicesMap
import im.vector.novaChat.R
import im.vector.novaChat.core.di.ActiveSessionHolder
import im.vector.novaChat.core.di.ScreenComponent
import im.vector.novaChat.core.extensions.exhaustive
import im.vector.novaChat.core.extensions.hideKeyboard
import im.vector.novaChat.core.extensions.replaceFragment
import im.vector.novaChat.core.platform.ToolbarConfigurable
import im.vector.novaChat.core.platform.VectorBaseActivity
import im.vector.novaChat.core.pushers.PushersManager
import im.vector.novaChat.features.crypto.recover.BootstrapBottomSheet
import im.vector.novaChat.features.disclaimer.showDisclaimerDialog
import im.vector.novaChat.features.notifications.NotificationDrawerManager
import im.vector.novaChat.features.popup.PopupAlertManager
import im.vector.novaChat.features.popup.VerificationVectorAlert
import im.vector.novaChat.features.rageshake.VectorUncaughtExceptionHandler
import im.vector.novaChat.features.settings.VectorPreferences
import im.vector.novaChat.features.workers.signout.SignOutViewModel
import im.vector.novaChat.push.fcm.FcmHelper
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.merge_overlay_waiting_view.*
import timber.log.Timber
import javax.inject.Inject

class HomeActivity : VectorBaseActivity(), ToolbarConfigurable {

    private lateinit var sharedActionViewModel: HomeSharedActionViewModel

    @Inject lateinit var activeSessionHolder: ActiveSessionHolder
    @Inject lateinit var vectorUncaughtExceptionHandler: VectorUncaughtExceptionHandler
    @Inject lateinit var pushManager: PushersManager
    @Inject lateinit var notificationDrawerManager: NotificationDrawerManager
    @Inject lateinit var vectorPreferences: VectorPreferences
    @Inject lateinit var popupAlertManager: PopupAlertManager

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
        FcmHelper.ensureFcmTokenIsRetrieved(this, pushManager, vectorPreferences.areNotificationEnabledForDevice())
        sharedActionViewModel = viewModelProvider.get(HomeSharedActionViewModel::class.java)
        drawerLayout.addDrawerListener(drawerListener)
        if (isFirstCreation()) {
            replaceFragment(R.id.homeDetailFragmentContainer, LoadingFragment::class.java)
            replaceFragment(R.id.homeDrawerFragmentContainer, HomeDrawerFragment::class.java)
        }

        sharedActionViewModel
                .observe()
                .subscribe { sharedAction ->
                    when (sharedAction) {
                        is HomeActivitySharedAction.OpenDrawer                 -> drawerLayout.openDrawer(GravityCompat.START)
                        is HomeActivitySharedAction.CloseDrawer                -> drawerLayout.closeDrawer(GravityCompat.START)
                        is HomeActivitySharedAction.OpenGroup                  -> {
                            drawerLayout.closeDrawer(GravityCompat.START)
                            replaceFragment(R.id.homeDetailFragmentContainer, HomeDetailFragment::class.java)
                        }
                        is HomeActivitySharedAction.PromptForSecurityBootstrap -> {
                            BootstrapBottomSheet.show(supportFragmentManager, true)
                        }
                    }.exhaustive
                }
                .disposeOnDestroy()

        if (intent.getBooleanExtra(EXTRA_CLEAR_EXISTING_NOTIFICATION, false)) {
            notificationDrawerManager.clearAllEvents()
            intent.removeExtra(EXTRA_CLEAR_EXISTING_NOTIFICATION)
        }
        if (intent.getBooleanExtra(EXTRA_ACCOUNT_CREATION, false)) {
            sharedActionViewModel.post(HomeActivitySharedAction.PromptForSecurityBootstrap)
            sharedActionViewModel.isAccountCreation = true
            intent.removeExtra(EXTRA_ACCOUNT_CREATION)
        }

        activeSessionHolder.getSafeActiveSession()?.getInitialSyncProgressStatus()?.observe(this, Observer { status ->
            if (status == null) {
                waiting_view.isVisible = false
                promptCompleteSecurityIfNeeded()
            } else {
                sharedActionViewModel.hasDisplayedCompleteSecurityPrompt = false
                Timber.v("${getString(status.statusText)} ${status.percentProgress}")
                waiting_view.setOnClickListener {
                    // block interactions
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

        // Ask again if the app is relaunched
        if (!sharedActionViewModel.hasDisplayedCompleteSecurityPrompt
                && activeSessionHolder.getSafeActiveSession()?.hasAlreadySynced() == true) {
            promptCompleteSecurityIfNeeded()
        }
    }

    private fun promptCompleteSecurityIfNeeded() {
        val session = activeSessionHolder.getSafeActiveSession() ?: return
        if (!session.hasAlreadySynced()) return
        if (sharedActionViewModel.hasDisplayedCompleteSecurityPrompt) return

        // ensure keys are downloaded
        session.cryptoService().downloadKeys(listOf(session.myUserId), true, object : MatrixCallback<MXUsersDevicesMap<CryptoDeviceInfo>> {
            override fun onSuccess(data: MXUsersDevicesMap<CryptoDeviceInfo>) {
                runOnUiThread {
                    alertCompleteSecurity(session)
                }
            }
        })
    }

    private fun alertCompleteSecurity(session: Session) {
        val myCrossSigningKeys = session.cryptoService().crossSigningService()
                .getMyCrossSigningKeys()
        val crossSigningEnabledOnAccount = myCrossSigningKeys != null

        if (!crossSigningEnabledOnAccount && !sharedActionViewModel.isAccountCreation) {
            // We need to ask
            promptSecurityEvent(
                    session,
                    R.string.upgrade_security,
                    R.string.security_prompt_text
            ) {
                it.navigator.upgradeSessionSecurity(it)
            }
        } else if (myCrossSigningKeys?.isTrusted() == false) {
            // We need to ask
            promptSecurityEvent(
                    session,
                    R.string.complete_security,
                    R.string.crosssigning_verify_this_session
            ) {
                it.navigator.waitSessionVerification(it)
            }
        }
    }

    private fun promptSecurityEvent(session: Session, titleRes: Int, descRes: Int, action: ((VectorBaseActivity) -> Unit)) {
        sharedActionViewModel.hasDisplayedCompleteSecurityPrompt = true
        popupAlertManager.postVectorAlert(
                VerificationVectorAlert(
                        uid = "upgradeSecurity",
                        title = getString(titleRes),
                        description = getString(descRes),
                        iconId = R.drawable.ic_shield_warning
                ).apply {
                    matrixItem = session.getUser(session.myUserId)?.toMatrixItem()
                    colorInt = ContextCompat.getColor(this@HomeActivity, R.color.riotx_positive_accent)
                    contentAction = Runnable {
                        (weakCurrentActivity?.get() as? VectorBaseActivity)?.let {
                            action(it)
                        }
                    }
                    dismissedAction = Runnable {}
                }
        )
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

        // Force remote backup state update to update the banner if needed
        viewModelProvider.get(SignOutViewModel::class.java).refreshRemoteStateIfNeeded()
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

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val EXTRA_CLEAR_EXISTING_NOTIFICATION = "EXTRA_CLEAR_EXISTING_NOTIFICATION"
        private const val EXTRA_ACCOUNT_CREATION = "EXTRA_ACCOUNT_CREATION"

        fun newIntent(context: Context, clearNotification: Boolean = false, accountCreation: Boolean = false): Intent {
            return Intent(context, HomeActivity::class.java)
                    .apply {
                        putExtra(EXTRA_CLEAR_EXISTING_NOTIFICATION, clearNotification)
                        putExtra(EXTRA_ACCOUNT_CREATION, accountCreation)
                    }
        }
    }
}
