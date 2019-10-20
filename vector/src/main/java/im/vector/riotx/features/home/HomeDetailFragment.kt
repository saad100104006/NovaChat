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

package im.vector.riotx.features.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
import im.vector.riotx.R
import im.vector.riotx.core.di.ScreenComponent
import im.vector.riotx.core.extensions.observeK
import im.vector.riotx.core.platform.ToolbarConfigurable
import im.vector.riotx.core.platform.VectorBaseFragment
import im.vector.riotx.core.ui.views.KeysBackupBanner
import im.vector.riotx.features.home.room.list.RoomListFragment
import im.vector.riotx.features.home.room.list.RoomListParams
import im.vector.riotx.features.home.room.list.UnreadCounterBadgeView
import im.vector.riotx.features.workers.signout.SignOutViewModel
import kotlinx.android.synthetic.main.fragment_home_detail.*
import kotlinx.android.synthetic.main.fragment_home_detail.homeDrawerHeaderAvatarView
import kotlinx.android.synthetic.main.fragment_home_detail.homeDrawerUserIdView
import kotlinx.android.synthetic.main.fragment_home_detail.homeDrawerUsernameView
import kotlinx.android.synthetic.main.fragment_home_drawer.*
import timber.log.Timber
import javax.inject.Inject


private const val INDEX_CATCHUP = 0
private const val INDEX_PEOPLE = 1
private const val INDEX_ROOMS = 2

class HomeDetailFragment : VectorBaseFragment(), KeysBackupBanner.Delegate {

    private val unreadCounterBadgeViews = arrayListOf<UnreadCounterBadgeView>()

    private val viewModel: HomeDetailViewModel by fragmentViewModel()
    private lateinit var navigationViewModel: HomeNavigationViewModel

    @Inject lateinit var session: Session
    @Inject lateinit var homeDetailViewModelFactory: HomeDetailViewModel.Factory
    @Inject lateinit var avatarRenderer: AvatarRenderer


    override fun getLayoutResId(): Int {
        return R.layout.fragment_home_detail
    }

    override fun injectWith(injector: ScreenComponent) {
        injector.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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
            val displayMode = if (it.itemId == R.id.bottom_action_home) {
                RoomListFragment.DisplayMode.HOME
            }
            else if (it.itemId == R.id.bottom_action_people) RoomListFragment.DisplayMode.PEOPLE
            else if (it.itemId == R.id.bottom_action_rooms) RoomListFragment.DisplayMode.ROOMS
            else RoomListFragment.DisplayMode.HOME
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
            one.visibility=View.INVISIBLE
            three.visibility=View.INVISIBLE
            two.visibility = View.VISIBLE
        }
        else if (displayMode == RoomListFragment.DisplayMode.ROOMS) {
            R.id.bottom_action_rooms
            one.visibility=View.INVISIBLE
            two.visibility=View.INVISIBLE
            three.visibility = View.VISIBLE

        }
        else if (displayMode == RoomListFragment.DisplayMode.HOME){
            R.id.bottom_action_home
            one.visibility = View.VISIBLE
            two.visibility=View.INVISIBLE
            three.visibility=View.INVISIBLE
        }
        updateSelectedFragment(displayMode)
        // Update the navigation view (for when we restore the tabs)
        bottomNavigationView.selectedItemId = if (displayMode == RoomListFragment.DisplayMode.PEOPLE) {
            R.id.bottom_action_people
        }
        else if (displayMode == RoomListFragment.DisplayMode.ROOMS) {
            R.id.bottom_action_rooms
        }
        else {
            R.id.bottom_action_home
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
        unreadCounterBadgeViews[INDEX_CATCHUP].render(UnreadCounterBadgeView.State(it.notificationCountCatchup, it.notificationHighlightCatchup))
        unreadCounterBadgeViews[INDEX_PEOPLE].render(UnreadCounterBadgeView.State(it.notificationCountPeople, it.notificationHighlightPeople))
        unreadCounterBadgeViews[INDEX_ROOMS].render(UnreadCounterBadgeView.State(it.notificationCountRooms, it.notificationHighlightRooms))
        syncStateView.render(it.syncState)
    }

    companion object {

        fun newInstance(): HomeDetailFragment {
            return HomeDetailFragment()
        }

    }
}