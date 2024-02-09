package com.toasterofbread.spmp.platform.playerservice

import android.media.audiofx.LoudnessEnhancer
import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import com.toasterofbread.db.Database
import com.toasterofbread.spmp.model.mediaitem.song.Song
import com.toasterofbread.spmp.model.mediaitem.song.SongRef
import com.toasterofbread.spmp.model.settings.category.StreamingSettings
import com.toasterofbread.spmp.platform.AppContext
import spms.socketapi.shared.SpMsPlayerState
import androidx.media3.common.MediaItem as ExoMediaItem

internal fun Song.buildExoMediaItem(context: AppContext): ExoMediaItem =
    ExoMediaItem.Builder()
        .setRequestMetadata(ExoMediaItem.RequestMetadata.Builder().setMediaUri(id.toUri()).build())
        .setUri(id)
        .setCustomCacheKey(id)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .apply {
                    val db: Database = context.database

                    setArtworkUri(id.toUri())
                    setTitle(getActiveTitle(db))
                    setArtist(Artist.get(db)?.getActiveTitle(db))

                    val album = Album.get(db)
                    setAlbumTitle(album?.getActiveTitle(db))
                    setAlbumArtist(album?.Artist?.get(db)?.getActiveTitle(db))
                }
                .build()
        )
        .build()

fun convertState(exo_state: Int): SpMsPlayerState =
    SpMsPlayerState.entries[exo_state - 1]

fun ExoMediaItem.getSong(): Song =
    SongRef(mediaMetadata.artworkUri.toString())

internal fun LoudnessEnhancer.update(song: Song?, context: AppContext) {
    if (song == null || !StreamingSettings.Key.ENABLE_AUDIO_NORMALISATION.get<Boolean>(context)) {
        enabled = false
        return
    }

    val loudness_db: Float? = song.LoudnessDb.get(context.database)
    if (loudness_db == null) {
        setTargetGain(0)
    }
    else {
        setTargetGain((loudness_db * 100).toInt())
    }

    enabled = true
}
