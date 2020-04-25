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

package im.vector.novaChat.features.media

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import im.vector.matrix.android.api.MatrixCallback
import im.vector.matrix.android.api.session.Session
import im.vector.matrix.android.api.session.file.FileService
import im.vector.novaChat.R
import im.vector.novaChat.core.di.ScreenComponent
import im.vector.novaChat.core.intent.getMimeTypeFromUri
import im.vector.novaChat.core.platform.VectorBaseActivity
import im.vector.novaChat.core.utils.shareMedia
import kotlinx.android.synthetic.main.activity_video_media_viewer.*
import java.io.File
import javax.inject.Inject

class VideoMediaViewerActivity : VectorBaseActivity() {

    @Inject lateinit var session: Session
    @Inject lateinit var imageContentRenderer: ImageContentRenderer
    @Inject lateinit var videoContentRenderer: VideoContentRenderer

    private lateinit var mediaData: VideoContentRenderer.Data

    override fun injectWith(injector: ScreenComponent) {
        injector.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(im.vector.novaChat.R.layout.activity_video_media_viewer)

        if (intent.hasExtra(EXTRA_MEDIA_DATA)) {
            mediaData = intent.getParcelableExtra<VideoContentRenderer.Data>(EXTRA_MEDIA_DATA)!!

            configureToolbar(videoMediaViewerToolbar, mediaData)
            imageContentRenderer.render(mediaData.thumbnailMediaData, ImageContentRenderer.Mode.FULL_SIZE, videoMediaViewerThumbnailView)
            videoContentRenderer.render(mediaData,
                    videoMediaViewerThumbnailView,
                    videoMediaViewerLoading,
                    videoMediaViewerVideoView,
                    videoMediaViewerErrorView)
        } else {
            finish()
        }
    }

    override fun getMenuRes() = R.menu.vector_media_viewer

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mediaViewerShareAction -> {
                onShareActionClicked()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onShareActionClicked() {
        session.downloadFile(
                FileService.DownloadMode.FOR_EXTERNAL_SHARE,
                mediaData.eventId,
                mediaData.filename,
                mediaData.url,
                mediaData.elementToDecrypt,
                object : MatrixCallback<File> {
                    override fun onSuccess(data: File) {
                        shareMedia(this@VideoMediaViewerActivity, data, getMimeTypeFromUri(this@VideoMediaViewerActivity, data.toUri()))
                    }
                }
        )
    }

    private fun configureToolbar(toolbar: Toolbar, mediaData: VideoContentRenderer.Data) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = mediaData.filename
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    companion object {

        private const val EXTRA_MEDIA_DATA = "EXTRA_MEDIA_DATA"

        fun newIntent(context: Context, mediaData: VideoContentRenderer.Data): Intent {
            return Intent(context, VideoMediaViewerActivity::class.java).apply {
                putExtra(EXTRA_MEDIA_DATA, mediaData)
            }
        }
    }
}
