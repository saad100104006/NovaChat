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

package im.vector.novaChat.features.roomprofile.members

import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import im.vector.matrix.android.api.crypto.RoomEncryptionTrustLevel
import im.vector.matrix.android.api.extensions.orFalse
import im.vector.matrix.android.api.query.QueryStringValue
import im.vector.matrix.android.api.session.Session
import im.vector.matrix.android.api.session.events.model.EventType
import im.vector.matrix.android.api.session.events.model.toModel
import im.vector.matrix.android.api.session.room.members.roomMemberQueryParams
import im.vector.matrix.android.api.session.room.model.Membership
import im.vector.matrix.android.api.session.room.model.PowerLevelsContent
import im.vector.matrix.android.api.session.room.model.RoomMemberSummary
import im.vector.matrix.android.api.session.room.powerlevels.PowerLevelsConstants
import im.vector.matrix.android.api.session.room.powerlevels.PowerLevelsHelper
import im.vector.matrix.rx.asObservable
import im.vector.matrix.rx.mapOptional
import im.vector.matrix.rx.rx
import im.vector.matrix.rx.unwrap
import im.vector.novaChat.core.platform.EmptyViewEvents
import im.vector.novaChat.core.platform.VectorViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import timber.log.Timber

class RoomMemberListViewModel @AssistedInject constructor(@Assisted initialState: RoomMemberListViewState,
                                                          private val roomMemberSummaryComparator: RoomMemberSummaryComparator,
                                                          private val session: Session)
    : VectorViewModel<RoomMemberListViewState, RoomMemberListAction, EmptyViewEvents>(initialState) {

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: RoomMemberListViewState): RoomMemberListViewModel
    }

    companion object : MvRxViewModelFactory<RoomMemberListViewModel, RoomMemberListViewState> {

        @JvmStatic
        override fun create(viewModelContext: ViewModelContext, state: RoomMemberListViewState): RoomMemberListViewModel? {
            val fragment: RoomMemberListFragment = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.viewModelFactory.create(state)
        }
    }

    private val room = session.getRoom(initialState.roomId)!!

    init {
        observeRoomMemberSummaries()
        observeRoomSummary()
    }

    private fun observeRoomMemberSummaries() {
        val roomMemberQueryParams = roomMemberQueryParams {
            displayName = QueryStringValue.IsNotEmpty
            memberships = Membership.activeMemberships()
        }

        Observable
                .combineLatest<List<RoomMemberSummary>, PowerLevelsContent, RoomMemberSummaries>(
                        room.rx().liveRoomMembers(roomMemberQueryParams),
                        room.rx()
                                .liveStateEvent(EventType.STATE_ROOM_POWER_LEVELS, "")
                                .mapOptional { it.content.toModel<PowerLevelsContent>() }
                                .unwrap(),
                        BiFunction { roomMembers, powerLevelsContent ->
                            buildRoomMemberSummaries(powerLevelsContent, roomMembers)
                        }
                )
                .execute { async ->
                    copy(roomMemberSummaries = async)
                }

        if (room.isEncrypted()) {
            room.rx().liveRoomMembers(roomMemberQueryParams)
                    .observeOn(AndroidSchedulers.mainThread())
                    .switchMap { membersSummary ->
                        session.cryptoService().getLiveCryptoDeviceInfo(membersSummary.map { it.userId })
                                .asObservable()
                                .doOnError { Timber.e(it) }
                                .map { deviceList ->
                                    // If any key change, emit the userIds list
                                    deviceList.groupBy { it.userId }.mapValues {
                                        val allDeviceTrusted = it.value.fold(it.value.isNotEmpty()) { prev, next ->
                                            prev && next.trustLevel?.isCrossSigningVerified().orFalse()
                                        }
                                        if (session.cryptoService().crossSigningService().getUserCrossSigningKeys(it.key)?.isTrusted().orFalse()) {
                                            if (allDeviceTrusted) RoomEncryptionTrustLevel.Trusted else RoomEncryptionTrustLevel.Warning
                                        } else {
                                            RoomEncryptionTrustLevel.Default
                                        }
                                    }
                                }
                    }
                    .execute { async ->
                        copy(trustLevelMap = async)
                    }
        }
    }

    private fun observeRoomSummary() {
        room.rx().liveRoomSummary()
                .unwrap()
                .execute { async ->
                    copy(roomSummary = async)
                }
    }

    private fun buildRoomMemberSummaries(powerLevelsContent: PowerLevelsContent, roomMembers: List<RoomMemberSummary>): RoomMemberSummaries {
        val admins = ArrayList<RoomMemberSummary>()
        val moderators = ArrayList<RoomMemberSummary>()
        val users = ArrayList<RoomMemberSummary>(roomMembers.size)
        val customs = ArrayList<RoomMemberSummary>()
        val invites = ArrayList<RoomMemberSummary>()
        val powerLevelsHelper = PowerLevelsHelper(powerLevelsContent)
        roomMembers
                .forEach { roomMember ->
                    val memberPowerLevel = powerLevelsHelper.getUserPowerLevel(roomMember.userId)
                    when {
                        roomMember.membership == Membership.INVITE                            -> invites.add(roomMember)
                        memberPowerLevel == PowerLevelsConstants.DEFAULT_ROOM_ADMIN_LEVEL     -> admins.add(roomMember)
                        memberPowerLevel == PowerLevelsConstants.DEFAULT_ROOM_MODERATOR_LEVEL -> moderators.add(roomMember)
                        memberPowerLevel == PowerLevelsConstants.DEFAULT_ROOM_USER_LEVEL      -> users.add(roomMember)
                        else                                                                  -> customs.add(roomMember)
                    }
                }

        return listOf(
                PowerLevelCategory.ADMIN to admins.sortedWith(roomMemberSummaryComparator),
                PowerLevelCategory.MODERATOR to moderators.sortedWith(roomMemberSummaryComparator),
                PowerLevelCategory.CUSTOM to customs.sortedWith(roomMemberSummaryComparator),
                PowerLevelCategory.INVITE to invites.sortedWith(roomMemberSummaryComparator),
                PowerLevelCategory.USER to users.sortedWith(roomMemberSummaryComparator)
        )
    }

    override fun handle(action: RoomMemberListAction) {
    }
}
