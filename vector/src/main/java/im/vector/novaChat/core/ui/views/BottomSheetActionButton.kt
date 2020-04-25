/*
 * Copyright (c) 2020 New Vector Ltd
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

package im.vector.novaChat.core.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import butterknife.BindView
import butterknife.ButterKnife
import im.vector.novaChat.R
import im.vector.novaChat.core.extensions.setTextOrHide
import im.vector.novaChat.features.themes.ThemeUtils

class BottomSheetActionButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    @BindView(R.id.itemVerificationActionTitle)
    lateinit var actionTextView: TextView

    @BindView(R.id.itemVerificationActionSubTitle)
    lateinit var descriptionTextView: TextView

    @BindView(R.id.itemVerificationLeftIcon)
    lateinit var leftIconImageView: ImageView

    @BindView(R.id.itemVerificationActionIcon)
    lateinit var rightIconImageView: ImageView

    @BindView(R.id.itemVerificationClickableZone)
    lateinit var clickableView: View

    var title: String? = null
        set(value) {
            field = value
            actionTextView.setTextOrHide(value)
        }

    var subTitle: String? = null
        set(value) {
            field = value
            descriptionTextView.setTextOrHide(value)
        }

    var forceStartPadding: Boolean? = null
        set(value) {
            field = value
            if (leftIcon == null) {
                if (forceStartPadding == true) {
                    leftIconImageView.isInvisible = true
                } else {
                    leftIconImageView.isGone = true
                }
            }
        }

    var leftIcon: Drawable? = null
        set(value) {
            field = value
            if (value == null) {
                if (forceStartPadding == true) {
                    leftIconImageView.isInvisible = true
                } else {
                    leftIconImageView.isGone = true
                }
                leftIconImageView.setImageDrawable(null)
            } else {
                leftIconImageView.isVisible = true
                leftIconImageView.setImageDrawable(value)
            }
        }

    var rightIcon: Drawable? = null
        set(value) {
            field = value
            rightIconImageView.setImageDrawable(value)
        }

    var tint: Int? = null
        set(value) {
            field = value
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                leftIconImageView.imageTintList = value?.let { ColorStateList.valueOf(value) }
            } else {
                leftIcon?.let {
                    leftIcon = ThemeUtils.tintDrawable(context, it, value ?: ThemeUtils.getColor(context, android.R.attr.textColor))
                }
            }
        }

    init {
        inflate(context, R.layout.item_verification_action, this)
        ButterKnife.bind(this)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetActionButton, 0, 0)
        title = typedArray.getString(R.styleable.BottomSheetActionButton_actionTitle) ?: ""
        subTitle = typedArray.getString(R.styleable.BottomSheetActionButton_actionDescription) ?: ""
        forceStartPadding = typedArray.getBoolean(R.styleable.BottomSheetActionButton_forceStartPadding, false)
        leftIcon = typedArray.getDrawable(R.styleable.BottomSheetActionButton_leftIcon)

        rightIcon = typedArray.getDrawable(R.styleable.BottomSheetActionButton_rightIcon)

        tint = typedArray.getColor(R.styleable.BottomSheetActionButton_tint, ThemeUtils.getColor(context, android.R.attr.textColor))

        typedArray.recycle()
    }
}
