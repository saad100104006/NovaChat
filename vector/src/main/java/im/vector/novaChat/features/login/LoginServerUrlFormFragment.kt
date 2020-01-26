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

package im.vector.novaChat.features.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import butterknife.OnClick
import com.jakewharton.rxbinding3.widget.textChanges
import im.vector.novaChat.R
import im.vector.novaChat.core.extensions.hideKeyboard
import im.vector.novaChat.core.utils.openUrlInExternalBrowser
import kotlinx.android.synthetic.main.fragment_login_server_url_form.*
import javax.inject.Inject

/**
 * In this screen, the user is prompted to enter a homeserver url
 */
class LoginServerUrlFormFragment @Inject constructor() : AbstractLoginFragment() {

    override fun getLayoutResId() = R.layout.fragment_login_server_url_form

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHomeServerField()
    }

    private fun setupHomeServerField() {
        loginServerUrlFormHomeServerUrl.textChanges()
                .subscribe {
                    loginServerUrlFormHomeServerUrlTil.error = null
                    loginServerUrlFormSubmit.isEnabled = it.isNotBlank()
                }
                .disposeOnDestroyView()

        loginServerUrlFormHomeServerUrl.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun setupUi(state: LoginViewState) {
        when (state.serverType) {
            ServerType.Modular -> {
                loginServerUrlFormIcon.isVisible = true
                loginServerUrlFormTitle.text = getString(R.string.login_connect_to_modular)
                loginServerUrlFormText.text = getString(R.string.login_server_url_form_modular_text)
                loginServerUrlFormLearnMore.isVisible = true
                loginServerUrlFormHomeServerUrlTil.hint = getText(R.string.login_server_url_form_modular_hint)
                loginServerUrlFormNotice.text = getString(R.string.login_server_url_form_modular_notice)
            }
            ServerType.Other   -> {
                loginServerUrlFormIcon.isVisible = false
                loginServerUrlFormTitle.text = getString(R.string.login_server_other_title)
                loginServerUrlFormText.text = getString(R.string.login_connect_to_a_custom_server)
                loginServerUrlFormLearnMore.isVisible = false
                loginServerUrlFormHomeServerUrlTil.hint = getText(R.string.login_server_url_form_other_hint)
                loginServerUrlFormNotice.text = getString(R.string.login_server_url_form_other_notice)
            }
            else               -> error("This fragment should not be displayed in matrix.org mode")
        }
    }

    @OnClick(R.id.loginServerUrlFormLearnMore)
    fun learnMore() {
        openUrlInExternalBrowser(requireActivity(), MODULAR_LINK)
    }

    override fun resetViewModel() {
        loginViewModel.handle(LoginAction.ResetHomeServerUrl)
    }

    @SuppressLint("SetTextI18n")
    @OnClick(R.id.loginServerUrlFormSubmit)
    fun submit() {
        cleanupUi()

        // Static check of homeserver url, empty, malformed, etc.
        var serverUrl = loginServerUrlFormHomeServerUrl.text.toString().trim()

        when {
            serverUrl.isBlank() -> {
                loginServerUrlFormHomeServerUrlTil.error = getString(R.string.login_error_invalid_home_server)
            }
            else                -> {
                if (serverUrl.startsWith("http").not()) {
                    serverUrl = "https://$serverUrl"
                }
                loginServerUrlFormHomeServerUrl.setText(serverUrl)
                loginViewModel.handle(LoginAction.UpdateHomeServer(serverUrl))
            }
        }
    }

    private fun cleanupUi() {
        loginServerUrlFormSubmit.hideKeyboard()
        loginServerUrlFormHomeServerUrlTil.error = null
    }

    override fun onError(throwable: Throwable) {
        loginServerUrlFormHomeServerUrlTil.error = errorFormatter.toHumanReadable(throwable)
    }

    override fun updateWithState(state: LoginViewState) {
        setupUi(state)

        if (state.loginMode != LoginMode.Unknown) {
            // The home server url is valid
            loginSharedActionViewModel.post(LoginNavigation.OnLoginFlowRetrieved)
        }
    }
}
