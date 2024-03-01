package com.toasterofbread.spmp.ui.layout.nowplaying

import LocalPlayerState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.toasterofbread.composekit.utils.composable.getTop
import com.toasterofbread.spmp.platform.form_factor
import com.toasterofbread.spmp.platform.getFormFactor
import com.toasterofbread.spmp.service.playercontroller.PlayerState
import com.toasterofbread.spmp.ui.layout.nowplaying.maintab.NowPlayingMainTabPage
import com.toasterofbread.spmp.ui.layout.nowplaying.queue.NowPlayingQueuePage
import androidx.compose.ui.unit.DpSize

const val NOW_PLAYING_MAIN_PADDING_DP = 10f
const val NOW_PLAYING_MAIN_PADDING_LARGE_DP = 30f

abstract class NowPlayingPage {
    enum class FormFactor {
        PORTRAIT,
        LANDSCAPE,
        NARROW_VERTICAL,
        NARROW_HORIZONTAL
    }
    
    @Composable
    abstract fun Page(page_size: DpSize, top_bar: NowPlayingTopBar, content_padding: PaddingValues, swipe_modifier: Modifier, modifier: Modifier)
    abstract fun shouldShow(player: PlayerState, form_factor: FormFactor): Boolean

    open fun getPlayerBackgroundColourOverride(player: PlayerState): Color? = null

    companion object {
        internal fun getFormFactor(player: PlayerState, size: DpSize): FormFactor {
            if (size.width <= NARROW_PLAYER_MAX_SIZE_DP.dp) {
                return FormFactor.NARROW_VERTICAL
            }
            else if (size.height <= NARROW_PLAYER_MAX_SIZE_DP.dp) {
                return FormFactor.NARROW_HORIZONTAL
            }
            return player.getFormFactor(0.75f)
        }

        @Composable
        private fun getMainPadding(): Dp =
            if (LocalPlayerState.current.form_factor.is_large) NOW_PLAYING_MAIN_PADDING_LARGE_DP.dp
            else NOW_PLAYING_MAIN_PADDING_DP.dp

        val top_padding: Dp
            @Composable get() {
                if (WindowInsets.getTop() > 0.dp) {
                    return getMainPadding() / 2
                }
                return getMainPadding()
            }
        val bottom_padding: Dp @Composable get() = getMainPadding()

        val horizontal_padding: Dp @Composable get() = getMainPadding()
        val horizontal_padding_minimised: Dp @Composable get() = NOW_PLAYING_MAIN_PADDING_DP.dp

        val ALL: List<NowPlayingPage> =
            listOf(
                NowPlayingMainTabPage(),
                NowPlayingQueuePage()
            )
    }
}
