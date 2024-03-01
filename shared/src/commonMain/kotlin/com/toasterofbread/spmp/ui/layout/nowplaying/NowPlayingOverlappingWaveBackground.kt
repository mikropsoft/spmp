package com.toasterofbread.spmp.ui.layout.nowplaying

import LocalNowPlayingExpansion
import LocalPlayerState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.toasterofbread.spmp.service.playercontroller.PlayerState
import com.toasterofbread.composekit.utils.composable.wave.OverlappingWaves
import com.toasterofbread.composekit.utils.composable.wave.WaveLayer
import com.toasterofbread.composekit.utils.composable.wave.getDefaultOverlappingWavesLayers
import com.toasterofbread.spmp.ui.layout.nowplaying.maintab.NOW_PLAYING_LARGE_BOTTOM_BAR_HEIGHT

@Composable
fun NowPlayingOverlappingWaveBackground(modifier: Modifier = Modifier) {
    val player: PlayerState = LocalPlayerState.current
    val expansion: NowPlayingExpansionState = LocalNowPlayingExpansion.current
    
    val current_song: Song? by player.status.song_state
    
    val wave_layers: List<WaveLayer> = remember {
        getDefaultOverlappingWavesLayers(7, 0.35f)
    }
    
    val default_background_wave_speed: Float by ThemeSettings.Key.NOWPLAYING_DEFAULT_WAVE_SPEED.rememberMutableState()
    val default_background_wave_opacity: Float by ThemeSettings.Key.NOWPLAYING_DEFAULT_WAVE_OPACITY.rememberMutableState()

    val background_wave_speed: Float = song.BackgroundWaveSpeed.observe(player.database).value ?: default_background_wave_speed
    val background_wave_opacity: Float = song.BackgroundWaveOpacity.observe(player.database).value ?: default_background_wave_opacity

    val wave_height: Dp
    val wave_alpha: Float
    val speed: Float
    val bottom_spacing: Dp

    BoxWithConstraints(modifier) {
        val form_factor: NowPlayingPage.FormFactor = NowPlayingPage.getFormFactor(player, maxSize)
        
        when (form_factor) {
            NowPlayingPage.FormFactor.PORTRAIT, 
            NowPlayingPage.FormFactor.NARROW_VERTICAL,
            NowPlayingPage.FormFactor.NARROW_HORIZONTAL -> {
                wave_height = player.screen_size.height * 0.5f
                wave_alpha = 0.5f * background_wave_opacity
                speed = 0.15f * background_wave_speed
                bottom_spacing = 0.dp
            }
            NowPlayingPage.FormFactor.LANDSCAPE -> {
                wave_height = player.screen_size.height * 0.5f
                wave_alpha = 1f * background_wave_opacity
                speed = 0.5f * background_wave_speed
                bottom_spacing = NOW_PLAYING_LARGE_BOTTOM_BAR_HEIGHT
            }
        }

        OverlappingWaves(
            { player.theme.accent.copy(alpha = wave_alpha) },
            BlendMode.Screen,
            Modifier
                .fillMaxWidth(1f)
                .requiredHeight(wave_height)
                .offset {
                    val queue_expansion: Float = expansion.get().coerceAtLeast(1f)
                    IntOffset(0, ((queue_expansion * player.screen_size.height) - bottom_spacing - wave_height).roundToPx())
                },
            layers = wave_layers,
            speed = speed
        )
    }
x
}
