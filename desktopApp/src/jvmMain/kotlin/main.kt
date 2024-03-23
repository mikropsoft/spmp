import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.toasterofbread.composekit.platform.composable.onWindowBackPressed
import com.toasterofbread.composekit.utils.common.addUnique
import com.toasterofbread.spmp.model.settings.category.DesktopSettings
import com.toasterofbread.spmp.model.settings.category.ThemeSettings
import com.toasterofbread.spmp.platform.AppContext
import com.toasterofbread.spmp.ui.layout.apppage.mainpage.getTextFieldFocusState
import com.toasterofbread.spmp.ui.shortcut.PressedShortcutModifiers
import com.toasterofbread.spmp.ui.shortcut.ShortcutModifier
import kotlinx.coroutines.*
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.awt.Toolkit
import java.awt.Frame
import java.lang.reflect.Field

@OptIn(ExperimentalComposeUiApi::class)
fun main(args: Array<String>) {
    Thread.setDefaultUncaughtExceptionHandler { _: Thread, error: Throwable ->
        val dialog = ExceptionDialog(Frame(), error)
        dialog.isVisible = true
    }

    val coroutine_scope: CoroutineScope = CoroutineScope(Job())
    val context: AppContext = AppContext(SpMp.app_name, coroutine_scope)

    SpMp.init(context)

    val arguments: ProgramArguments = ProgramArguments.parse(args) ?: return

    SpMp.onStart()

    if (hostOs == OS.Linux) {
        try {
            // Set AWT class name of window
            val toolkit: Toolkit = Toolkit.getDefaultToolkit()
            val class_name_field: Field = toolkit.javaClass.getDeclaredField("awtAppClassName")
            class_name_field.isAccessible = true
            class_name_field.set(toolkit, SpMp.app_name.lowercase())
        }
        catch (_: Throwable) {}
    }

    lateinit var window: ComposeWindow
    val enable_window_transparency: Boolean = ThemeSettings.Key.ENABLE_WINDOW_TRANSPARENCY.get(context.getPrefs())

    application {
        val text_field_focus_state: Any = getTextFieldFocusState()
        val pressed_shortcut_modifiers: MutableList<ShortcutModifier> = remember { mutableStateListOf() }

        Window(
            title = SpMp.app_name,
            onCloseRequest = ::exitApplication,
            onKeyEvent = { event ->
                if (event.key == Key.CtrlLeft || event.key == Key.CtrlRight) {
                    if (event.type == KeyEventType.KeyDown) {
                        pressed_shortcut_modifiers.addUnique(ShortcutModifier.CTRL)
                    }
                    else {
                        pressed_shortcut_modifiers.remove(ShortcutModifier.CTRL)
                    }
                    return@Window false
                }

                return@Window SpMp.player_state.processKeyEventShortcuts(event, window, text_field_focus_state)
            },
            state = rememberWindowState(
                size = DpSize(1280.dp, 720.dp)
            ),
            undecorated = enable_window_transparency,
            transparent = enable_window_transparency
        ) {
            LaunchedEffect(Unit) {
                window = this@Window.window

                if (enable_window_transparency) {
                    window.background = java.awt.Color(0, 0, 0, 0)
                }

                val startup_command: String = DesktopSettings.Key.STARTUP_COMMAND.get()
                if (startup_command.isBlank()) {
                    return@LaunchedEffect
                }

                withContext(Dispatchers.IO) {
                    try {
                        val process_builder: ProcessBuilder =
                            when (hostOs) {
                                OS.Linux -> ProcessBuilder("bash", "-c", startup_command)
                                OS.Windows -> TODO()
                                else -> return@withContext
                            }

                        process_builder.inheritIO().start()
                    }
                    catch (e: Throwable) {
                        RuntimeException("Execution of startup command failed", e).printStackTrace()
                    }
                }
            }

            SpMp.App(
                arguments,
                Modifier.onPointerEvent(PointerEventType.Press) { event ->
                    // Mouse back click
                    if (event.button?.index == 5) {
                        onWindowBackPressed()
                    }
                },
                pressed_shortcut_modifiers = remember { PressedShortcutModifiers(pressed_shortcut_modifiers) }
            )
        }
    }

    coroutine_scope.cancel()

    SpMp.onStop()
    SpMp.release()
}
