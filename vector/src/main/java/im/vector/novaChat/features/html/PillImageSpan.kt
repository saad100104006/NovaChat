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

package im.vector.novaChat.features.html

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ReplacementSpan
import android.widget.TextView
import androidx.annotation.UiThread
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.chip.ChipDrawable
import im.vector.matrix.android.api.session.user.model.User
import im.vector.novaChat.R
import im.vector.novaChat.core.glide.GlideRequests
import im.vector.novaChat.features.home.AvatarRenderer
import java.lang.ref.WeakReference

/**
 * This span is able to replace a text by a [ChipDrawable]
 * It's needed to call [bind] method to start requesting avatar, otherwise only the placeholder icon will be displayed if not already cached.
 */
class PillImageSpan(private val glideRequests: GlideRequests,
                    private val avatarRenderer: AvatarRenderer,
                    private val context: Context,
                    private val userId: String,
                    private val user: User?) : ReplacementSpan() {

    private val displayName by lazy {
        if (user?.displayName.isNullOrEmpty()) userId else user?.displayName!!
    }

    private val pillDrawable = createChipDrawable()
    private val target = PillImageSpanTarget(this)
    private var tv: WeakReference<TextView>? = null

    @UiThread
    fun bind(textView: TextView) {
        tv = WeakReference(textView)
        avatarRenderer.render(context, glideRequests, user?.avatarUrl, userId, displayName, target)
    }

    // ReplacementSpan *****************************************************************************

    override fun getSize(paint: Paint, text: CharSequence,
                         start: Int,
                         end: Int,
                         fm: Paint.FontMetricsInt?): Int {
        val rect = pillDrawable.bounds
        if (fm != null) {
            fm.ascent = -rect.bottom
            fm.descent = 0
            fm.top = fm.ascent
            fm.bottom = 0
        }
        return rect.right
    }

    override fun draw(canvas: Canvas, text: CharSequence,
                      start: Int,
                      end: Int,
                      x: Float,
                      top: Int,
                      y: Int,
                      bottom: Int,
                      paint: Paint) {
        canvas.save()
        val transY = bottom - pillDrawable.bounds.bottom
        canvas.translate(x, transY.toFloat())
        pillDrawable.draw(canvas)
        canvas.restore()
    }

    internal fun updateAvatarDrawable(drawable: Drawable?) {
        pillDrawable.apply {
            chipIcon = drawable
        }
        tv?.get()?.apply {
            invalidate()
        }
    }

    // Private methods *****************************************************************************

    private fun createChipDrawable(): ChipDrawable {
        val textPadding = context.resources.getDimension(R.dimen.pill_text_padding)
        return ChipDrawable.createFromResource(context, R.xml.pill_view).apply {
            setText(displayName)
            textEndPadding = textPadding
            textStartPadding = textPadding
            setChipMinHeightResource(R.dimen.pill_min_height)
            setChipIconSizeResource(R.dimen.pill_avatar_size)
            chipIcon = avatarRenderer.getPlaceholderDrawable(context, userId, displayName)
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        }
    }

}

/**
 * Glide target to handle avatar retrieval into [PillImageSpan].
 */
private class PillImageSpanTarget(pillImageSpan: PillImageSpan) : SimpleTarget<Drawable>() {

    private val pillImageSpan = WeakReference(pillImageSpan)

    override fun onResourceReady(drawable: Drawable, transition: Transition<in Drawable>?) {
        updateWith(drawable)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        updateWith(placeholder)
    }

    private fun updateWith(drawable: Drawable?) {
        pillImageSpan.get()?.apply {
            updateAvatarDrawable(drawable)
        }
    }
}