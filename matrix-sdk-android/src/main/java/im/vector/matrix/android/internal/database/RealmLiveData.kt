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

package im.vector.matrix.android.internal.database

import androidx.lifecycle.LiveData
import io.realm.*

class RealmLiveData<T : RealmModel>(private val realmConfiguration: RealmConfiguration,
                                    private val query: (Realm) -> RealmQuery<T>) : LiveData<RealmResults<T>>() {

    private val listener = RealmChangeListener<RealmResults<T>> { results ->
        value = results
    }

    private var realm: Realm? = null
    private var results: RealmResults<T>? = null

    override fun onActive() {
        val realm = Realm.getInstance(realmConfiguration)
        val results = query.invoke(realm).findAllAsync()
        results.addChangeListener(listener)
        this.realm = realm
        this.results = results
    }

    override fun onInactive() {
        results?.removeChangeListener(listener)
        results = null
        realm?.close()
        realm = null
    }
}