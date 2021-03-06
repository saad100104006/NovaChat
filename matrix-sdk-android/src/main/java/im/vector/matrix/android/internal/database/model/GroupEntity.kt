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

package im.vector.matrix.android.internal.database.model

import im.vector.matrix.android.api.session.room.model.Membership
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * This class is used to store group info (groupId and membership) from the sync response.
 * Then [im.vector.matrix.android.internal.session.group.GroupSummaryUpdater] observes change and
 * makes requests to fetch group information from the homeserver
 */
internal open class GroupEntity(@PrimaryKey var groupId: String = "")
    : RealmObject() {

    private var membershipStr: String = Membership.NONE.name
    var membership: Membership
        get() {
            return Membership.valueOf(membershipStr)
        }
        set(value) {
            membershipStr = value.name
        }

    companion object

}