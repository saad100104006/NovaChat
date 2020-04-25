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

package im.vector.novaChat.features.settings.devices

import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import im.vector.matrix.android.api.extensions.sortByLastSeen
import im.vector.matrix.android.internal.crypto.model.CryptoDeviceInfo
import im.vector.matrix.android.internal.crypto.model.rest.DeviceInfo
import im.vector.novaChat.R
import im.vector.novaChat.core.epoxy.errorWithRetryItem
import im.vector.novaChat.core.epoxy.loadingItem
import im.vector.novaChat.core.error.ErrorFormatter
import im.vector.novaChat.core.resources.StringProvider
import im.vector.novaChat.core.ui.list.genericItemHeader
import im.vector.novaChat.features.settings.VectorPreferences
import javax.inject.Inject

class DevicesController @Inject constructor(private val errorFormatter: ErrorFormatter,
                                            private val stringProvider: StringProvider,
                                            private val vectorPreferences: VectorPreferences) : EpoxyController() {

    var callback: Callback? = null
    private var viewState: DevicesViewState? = null

    init {
        requestModelBuild()
    }

    fun update(viewState: DevicesViewState) {
        this.viewState = viewState
        requestModelBuild()
    }

    override fun buildModels() {
        val nonNullViewState = viewState ?: return
        buildDevicesModels(nonNullViewState)
    }

    private fun buildDevicesModels(state: DevicesViewState) {
        when (val devices = state.devices) {
            is Loading,
            is Uninitialized ->
                loadingItem {
                    id("loading")
                }
            is Fail          ->
                errorWithRetryItem {
                    id("error")
                    text(errorFormatter.toHumanReadable(devices.error))
                    listener { callback?.retry() }
                }
            is Success       ->
                buildDevicesList(devices(), state.cryptoDevices(), state.myDeviceId)
        }
    }

    private fun buildDevicesList(devices: List<DeviceInfo>, cryptoDevices: List<CryptoDeviceInfo>?, myDeviceId: String) {
        // Current device
        genericItemHeader {
            id("current")
            text(stringProvider.getString(R.string.devices_current_device))
        }

        devices
                .filter {
                    it.deviceId == myDeviceId
                }
                .forEachIndexed { idx, deviceInfo ->
                    deviceItem {
                        id("myDevice$idx")
                        detailedMode(vectorPreferences.developerMode())
                        deviceInfo(deviceInfo)
                        currentDevice(true)
                        itemClickAction { callback?.onDeviceClicked(deviceInfo) }
                        trusted(true)
                    }
                }

        // Other devices
        if (devices.size > 1) {
            genericItemHeader {
                id("others")
                text(stringProvider.getString(R.string.devices_other_devices))
            }

            devices
                    .filter {
                        it.deviceId != myDeviceId
                    }
                    // sort before display: most recent first
                    .sortByLastSeen()
                    .forEachIndexed { idx, deviceInfo ->
                        val isCurrentDevice = deviceInfo.deviceId == myDeviceId
                        deviceItem {
                            id("device$idx")
                            detailedMode(vectorPreferences.developerMode())
                            deviceInfo(deviceInfo)
                            currentDevice(isCurrentDevice)
                            itemClickAction { callback?.onDeviceClicked(deviceInfo) }
                            trusted(cryptoDevices?.firstOrNull { it.deviceId == deviceInfo.deviceId }?.isVerified)
                        }
                    }
        }
    }

    interface Callback {
        fun retry()
        fun onDeviceClicked(deviceInfo: DeviceInfo)
    }
}
