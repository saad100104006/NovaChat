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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.forEachIndexed
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import im.vector.matrix.android.api.session.Session
import im.vector.matrix.android.api.session.crypto.keysbackup.KeysBackupState
import im.vector.matrix.android.api.session.group.model.GroupSummary
import im.vector.novaChat.R
import im.vector.novaChat.core.di.ActiveSessionHolder
import im.vector.novaChat.core.di.ScreenComponent
import im.vector.novaChat.core.extensions.observeK
import im.vector.novaChat.core.extensions.vectorComponent
import im.vector.novaChat.core.platform.ToolbarConfigurable
import im.vector.novaChat.core.platform.VectorBaseFragment
import im.vector.novaChat.core.ui.views.KeysBackupBanner
import im.vector.novaChat.core.utils.toast
import im.vector.novaChat.features.MainActivity
import im.vector.novaChat.features.crypto.keysbackup.settings.KeysBackupManageActivity
import im.vector.novaChat.features.crypto.keysbackup.setup.KeysBackupSetupActivity
import im.vector.novaChat.features.home.room.list.RoomListFragment
import im.vector.novaChat.features.home.room.list.RoomListParams
import im.vector.novaChat.features.home.room.list.UnreadCounterBadgeView
import im.vector.novaChat.features.notifications.NotificationDrawerManager
import im.vector.novaChat.features.workers.signout.SignOutBottomSheetDialogFragment
import im.vector.novaChat.features.workers.signout.SignOutViewModel
import kotlinx.android.synthetic.main.fragment_home_detail.*
import kotlinx.android.synthetic.main.fragment_home_detail.homeDrawerHeaderAvatarView
import kotlinx.android.synthetic.main.fragment_home_detail.homeDrawerUserIdView
import kotlinx.android.synthetic.main.fragment_home_detail.homeDrawerUsernameView
import timber.log.Timber
import javax.inject.Inject

private const val INDEX_PEOPLE = 0
private const val INDEX_ROOMS = 1

class HomeDetailFragment : VectorBaseFragment(), KeysBackupBanner.Delegate {

    private val unreadCounterBadgeViews = arrayListOf<UnreadCounterBadgeView>()

    private val viewModel: HomeDetailViewModel by fragmentViewModel()
    private lateinit var navigationViewModel: HomeNavigationViewModel

    @Inject lateinit var session: Session
    @Inject lateinit var homeDetailViewModelFactory: HomeDetailViewModel.Factory
    @Inject lateinit var avatarRenderer: AvatarRenderer
    private lateinit var viewModel2: SignOutViewModel
    var onSignOut: Runnable? = null

    lateinit var notificationDrawerManager: NotificationDrawerManager
    lateinit var activeSessionHolder: ActiveSessionHolder


    override fun getLayoutResId(): Int {
        return R.layout.fragment_home_detail
    }

    override fun injectWith(injector: ScreenComponent) {
        injector.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel2 = ViewModelProviders.of(this, viewModelFactory).get(SignOutViewModel::class.java)

        viewModel2.init(session)

        navigationViewModel = ViewModelProviders.of(requireActivity()).get(HomeNavigationViewModel::class.java)

        setupBottomNavigationView()
        setupToolbar()
        setupKeysBackupBanner()

        viewModel.selectSubscribe(this, HomeDetailViewState::groupSummary) { groupSummary ->
            onGroupChange(groupSummary.orNull())
        }
        viewModel.selectSubscribe(this, HomeDetailViewState::displayMode) { displayMode ->
            switchDisplayMode(displayMode)
        }

        notificationDrawerManager = context!!.vectorComponent().notificationDrawerManager()
        activeSessionHolder = context!!.vectorComponent().activeSessionHolder()

        session.liveUser(session.myUserId).observeK(this) { optionalUser ->
            val user = optionalUser?.getOrNull()
            if (user != null) {
                avatarRenderer.render(user.avatarUrl, user.userId, user.displayName, homeDrawerHeaderAvatarView)
                homeDrawerUsernameView.text = user.displayName
                homeDrawerUserIdView.text = user.userId
            }
        }

        menu.setOnClickListener({ v ->
            drawer.visibility = View.VISIBLE
        })

        top_layout.setOnClickListener({ v ->
            drawer.visibility = View.GONE
        })

        settings.setOnClickListener {
            navigator.openSettings(requireActivity())
            drawer.visibility = View.GONE
        }

        sign_out.setOnClickListener { _ ->
            context?.let {
                AlertDialog.Builder(it)
                        .setTitle(R.string.are_you_sure)
                        .setMessage(R.string.sign_out_bottom_sheet_will_lose_secure_messages)
                        .setPositiveButton(R.string.backup) { _, _ ->
                            when (viewModel2.keysBackupState.value) {
                                KeysBackupState.NotTrusted -> {
                                    context?.let { context ->
                                        startActivity(KeysBackupManageActivity.intent(context))
                                    }
                                }
                                KeysBackupState.Disabled   -> {
                                    context?.let { context ->
                                        startActivityForResult(KeysBackupSetupActivity.intent(context, true), Companion.EXPORT_REQ)
                                    }
                                }
                                KeysBackupState.BackingUp,
                                KeysBackupState.WillBackUp -> {
                                    //keys are already backing up please wait
                                    context?.toast(R.string.keys_backup_is_not_finished_please_wait)
                                }
                                else                       -> {
                                    //nop
                                }
                            }
                        }
                        .setNegativeButton(R.string.action_sign_out) { _, _ ->
                            doSignOut()
                        }
                        .show()
            }

        }


    }

    private fun doSignOut() {
        // Dismiss all notifications
        notificationDrawerManager.clearAllEvents()
        notificationDrawerManager.persistInfo()

        activity?.let { MainActivity.restartApp(it, clearCache = true, clearCredentials = true) }
    }

    private fun onGroupChange(groupSummary: GroupSummary?) {
        groupSummary?.let {
            avatarRenderer.render(
                    it.avatarUrl,
                    it.groupId,
                    it.displayName,
                    groupToolbarAvatarImageView
            )
        }
    }

    private fun setupKeysBackupBanner() {
        // Keys backup banner
        // Use the SignOutViewModel, it observe the keys backup state and this is what we need here
        val model = ViewModelProviders.of(this, viewModelFactory).get(SignOutViewModel::class.java)

        model.init(session)

        model.keysBackupState.observe(this, Observer { keysBackupState ->
            when (keysBackupState) {
                null                               ->
                    homeKeysBackupBanner.render(KeysBackupBanner.State.Hidden, false)
                KeysBackupState.Disabled           ->
                    homeKeysBackupBanner.render(KeysBackupBanner.State.Setup(model.getNumberOfKeysToBackup()), false)
                KeysBackupState.NotTrusted,
                KeysBackupState.WrongBackUpVersion ->
                    // In this case, getCurrentBackupVersion() should not return ""
                    homeKeysBackupBanner.render(KeysBackupBanner.State.Recover(model.getCurrentBackupVersion()), false)
                KeysBackupState.WillBackUp,
                KeysBackupState.BackingUp          ->
                    homeKeysBackupBanner.render(KeysBackupBanner.State.BackingUp, false)
                KeysBackupState.ReadyToBackUp      ->
                    if (model.canRestoreKeys()) {
                        homeKeysBackupBanner.render(KeysBackupBanner.State.Update(model.getCurrentBackupVersion()), false)
                    } else {
                        homeKeysBackupBanner.render(KeysBackupBanner.State.Hidden, false)
                    }
                else                               ->
                    homeKeysBackupBanner.render(KeysBackupBanner.State.Hidden, false)
            }
        })

        homeKeysBackupBanner.delegate = this
    }


    private fun setupToolbar() {
        val parentActivity = vectorBaseActivity
        if (parentActivity is ToolbarConfigurable) {
            parentActivity.configure(groupToolbar)
        }
        groupToolbar.title = ""
        groupToolbarAvatarImageView.visibility = View.GONE
        groupToolbarAvatarImageView.setOnClickListener {
            navigationViewModel.goTo(HomeActivity.Navigation.OpenDrawer)
        }
    }

    private fun setupBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener {
            val displayMode = if (it.itemId == R.id.bottom_action_people) {
                RoomListFragment.DisplayMode.PEOPLE
            } else {
                RoomListFragment.DisplayMode.ROOMS
            }
            // if (it.itemId == R.id.bottom_action_people) RoomListFragment.DisplayMode.PEOPLE
             if (it.itemId == R.id.bottom_action_rooms) RoomListFragment.DisplayMode.ROOMS
             else  RoomListFragment.DisplayMode.PEOPLE
           /* else RoomListFragment.DisplayMode.HOME*/
            viewModel.switchDisplayMode(displayMode)
            true
        }

        val menuView = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
        menuView.forEachIndexed { index, view ->
            val itemView = view as BottomNavigationItemView
            val badgeLayout = LayoutInflater.from(requireContext()).inflate(R.layout.vector_home_badge_unread_layout, menuView, false)
            val unreadCounterBadgeView: UnreadCounterBadgeView = badgeLayout.findViewById(R.id.actionUnreadCounterBadgeView)
            itemView.addView(badgeLayout)
            unreadCounterBadgeViews.add(index, unreadCounterBadgeView)
        }
    }

    private fun switchDisplayMode(displayMode: RoomListFragment.DisplayMode) {
        groupToolbarTitleView.setText(displayMode.titleRes)

        if (displayMode == RoomListFragment.DisplayMode.PEOPLE) {
            R.id.bottom_action_people
            one.visibility=View.VISIBLE
           // three.visibility=View.INVISIBLE
            two.visibility = View.INVISIBLE
        }
        else if (displayMode == RoomListFragment.DisplayMode.ROOMS) {
            R.id.bottom_action_rooms
            one.visibility=View.INVISIBLE
            two.visibility=View.VISIBLE
           // three.visibility = View.VISIBLE

        }
        updateSelectedFragment(displayMode)
        // Update the navigation view (for when we restore the tabs)
        bottomNavigationView.selectedItemId =

          /*      if (displayMode == RoomListFragment.DisplayMode.PEOPLE) {
            R.id.bottom_action_people
        }*/
         if (displayMode == RoomListFragment.DisplayMode.ROOMS) {
            R.id.bottom_action_rooms
        }
        else {
            R.id.bottom_action_people
        }
    }

    private fun updateSelectedFragment(displayMode: RoomListFragment.DisplayMode) {
        val fragmentTag = "FRAGMENT_TAG_${displayMode.name}"
        var fragment = childFragmentManager.findFragmentByTag(fragmentTag)
        if (fragment == null) {
            fragment = RoomListFragment.newInstance(RoomListParams(displayMode))
        }

        childFragmentManager.beginTransaction()
                .replace(R.id.roomListContainer, fragment, fragmentTag)
                .addToBackStack(fragmentTag)
                .commit()
    }

    override fun setupKeysBackup() {
        navigator.openKeysBackupSetup(requireActivity(), false)
    }

    override fun recoverKeysBackup() {
        navigator.openKeysBackupManager(requireActivity())
    }

    override fun invalidate() = withState(viewModel) {
        Timber.v(it.toString())
       // unreadCounterBadgeViews[INDEX_CATCHUP].render(UnreadCounterBadgeView.State(it.notificationCountCatchup, it.notificationHighlightCatchup))
        unreadCounterBadgeViews[INDEX_PEOPLE].render(UnreadCounterBadgeView.State(it.notificationCountPeople, it.notificationHighlightPeople))
        unreadCounterBadgeViews[INDEX_ROOMS].render(UnreadCounterBadgeView.State(it.notificationCountRooms, it.notificationHighlightRooms))
        syncStateView.render(it.syncState)
    }

    companion object {

        fun newInstance(): HomeDetailFragment {
            return HomeDetailFragment()
        }

        private const val EXPORT_REQ = 0

    }
}